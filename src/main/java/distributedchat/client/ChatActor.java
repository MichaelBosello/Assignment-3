package distributedchat.client;

import akka.actor.*;
import distributedchat.client.chatgui.ChatGUIActor;
import distributedchat.client.messages.chatprotocol.*;
import distributedchat.client.messages.chatprotocol.causalmutualexclusion.RequestCS;
import distributedchat.client.messages.chatprotocol.causalmutualexclusion.Token;
import distributedchat.client.messages.fromtogui.ConnectRequestMessage;
import distributedchat.client.messages.fromtogui.ConnectionResultMessage;
import distributedchat.client.messages.fromtogui.LeaveRequestMessage;
import distributedchat.client.messages.fromtogui.SendMessage;
import distributedchat.server.messages.ServerPeerMessage;
import distributedchat.server.messages.PeerRequestMessage;
import scala.concurrent.duration.Duration;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatActor extends AbstractActorWithStash {

    private static final boolean DEBUG = false;

    private final ActorRef gui;
    private ActorSelection lastContactedRegistry;
    private Set<ActorRef> allPeer = new HashSet<>();
    private Set<ActorRef> findPeer = new HashSet<>();

    private Map<ActorRef, Integer> lastCSRequest = new HashMap<>();//Ri
    private Map<ActorRef, Integer> lastCSExecution = new HashMap<>();//T
    private List<SimpleEntry<ActorRef, Integer>> waitingQueue = new ArrayList<>();//Qi
    private boolean holdToken = false;
    private boolean inCS = false;
    private boolean csRequestSubmitted = false;
    private List<String> pendingMessage = new LinkedList<>();
    private int ack = 0;
    private int leaveAck = 0;

    public ChatActor() {
        gui = getContext().actorOf(Props.create(ChatGUIActor.class));
        lastCSRequest.put(getSelf(), 0);
        lastCSExecution.put(getSelf(), 0);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConnectRequestMessage.class, this::serverConnection)
                .match(ReceiveTimeout.class, this::connectionFailed)
                .match(ServerPeerMessage.class, this::connectionSuccess)
                .match(PeerList.class, this::joinResponse)
                .matchAny( o -> stash())
                .build();
    }

    private void serverConnection(ConnectRequestMessage msg){
        lastContactedRegistry =
                getContext().actorSelection("akka.tcp://chat@" + msg.getHost() + "/user/registry");
        if(DEBUG)
            System.out.println("akka.tcp://chat@" + msg.getHost() + "/registry");
        lastContactedRegistry.tell(new PeerRequestMessage(), getSelf());
        getContext().setReceiveTimeout(Duration.create(5, TimeUnit.SECONDS));
    }

    private void connectionFailed(ReceiveTimeout msg){
        getContext().setReceiveTimeout(Duration.Undefined());
        lastContactedRegistry = null;
        gui.tell(new ConnectionResultMessage(false, "Timeout"), getSelf());
    }

    private void connectionSuccess(ServerPeerMessage msg){
        if(lastContactedRegistry != null){
            getContext().setReceiveTimeout(Duration.Undefined());
            if(msg.getPeer().size() > 0) {
                if(DEBUG)
                    System.out.println("peer present on server");
                for (ActorRef peer : msg.getPeer()) {
                    if(DEBUG)
                        System.out.println("request to join at " + peer);
                    peer.tell(new AskToJoin(), getSelf());
                }
            }else{
                holdToken = true;
                joined();
            }
        }
    }

    private void joinResponse(PeerList msg){
        allPeer.addAll(msg.getPeer());
        findPeer.add(getSender());
        lastCSRequest.put(getSender(), 0);
        lastCSExecution.put(getSender(), 0);
        if(DEBUG)
            System.out.println("response from peer to join");
        if(allPeer.size() == findPeer.size()) {
            joined();
            unstashAll();
        }
    }

    private void joined(){
        getContext().become(
                receiveBuilder()
                        .match(AskToJoin.class, this::addPeer)
                        .match(SendMessage.class, this::sendChatMessage)
                        .match(LeaveRequestMessage.class, this::leaveGroup)
                        .match(Leave.class, this::removePeer)
                        .match(LeaveReceived.class, this::quit)
                        .match(NextMessages.class, this::visualizeMessage)
                        .match(MessageReceived.class, this::ackReceived)
                        .match(RequestCS.class, this::receiveRequest)
                        .match(Token.class, this::receiveToken)
                        .build()
        );
        gui.tell(new ConnectionResultMessage(true, ""), getSelf());
    }

    private void addPeer(AskToJoin msg){
        if(DEBUG)
            System.out.println("new peer ask to join");
        getSender().tell(new PeerList(new HashSet<>(lastCSRequest.keySet())), getSelf());
        lastCSRequest.put(getSender(), 0);
        lastCSExecution.put(getSender(), 0);
        allPeer.add(getSender());
        findPeer.add(getSender());
    }

    private void leaveGroup(LeaveRequestMessage msg){
        for(ActorRef peer : allPeer){
            peer.tell(new Leave(), getSelf());
        }
    }

    private void removePeer(Leave msg){
        allPeer.remove(getSender());
        findPeer.remove(getSender());
        lastCSRequest.remove(getSender());
        lastCSExecution.remove(getSender());
        for(SimpleEntry<ActorRef, Integer> request : waitingQueue){
            if(request.getKey().equals(getSender())){
                waitingQueue.remove(request);
            }
        }
        getSender().tell(new LeaveReceived(), getSelf());
        if(inCS && ack == allPeer.size()){
            releaseCS();
            if(pendingMessage.isEmpty()) {
                csRequestSubmitted = false;
            }else{
                requestCS();
            }
        }
    }

    private void quit(LeaveReceived msg){
        leaveAck++;
        if(leaveAck == allPeer.size()){
            if(holdToken){
                if(waitingQueue.size() > 0) {
                    ActorRef next = waitingQueue.get(0).getKey();
                    waitingQueue.remove(0);

                    lastCSExecution.remove(getSelf());
                    for(SimpleEntry<ActorRef, Integer> request : waitingQueue){
                        if(request.getKey().equals(getSelf())){
                            waitingQueue.remove(request);
                        }
                    }

                    next.tell(new Token(new LinkedList<>(waitingQueue), new HashMap<>(lastCSExecution)), getSelf());
                } else {
                    allPeer.iterator().next().tell(new Token(new LinkedList<>(waitingQueue), new HashMap<>(lastCSExecution)), getSelf());
                }
            }
            System.exit(0);
        }
    }

    private void sendChatMessage(SendMessage msg){
        if(DEBUG)
            System.out.println("want send message");
        pendingMessage.add(msg.getMessage());
        if(!csRequestSubmitted){
            if(DEBUG)
                System.out.println("send request");
            requestCS();
            csRequestSubmitted = true;
        }
    }

    private void visualizeMessage(NextMessages msg){
        gui.tell(msg, getSelf());
        getSender().tell(new MessageReceived(), getSelf());
    }

    private void ackReceived(MessageReceived msg){
        ack++;
        if(ack == lastCSRequest.size()){
            if(DEBUG)
                System.out.println("all ack received");
            ack = 0;
            inCS = false;
            releaseCS();
            if(pendingMessage.isEmpty()) {
                csRequestSubmitted = false;
            }else{
                requestCS();
            }
        }
    }

    private void requestCS() {
        if (!holdToken) {
            lastCSRequest.replace(getSelf(), lastCSRequest.get(getSelf()) + 1);
            waitingQueue.add(new SimpleEntry<>(getSelf(), lastCSRequest.get(getSelf())));
            for (ActorRef peer : lastCSRequest.keySet()) {
                if(DEBUG)
                    System.out.println("request cs to " + peer);
                peer.tell(new RequestCS(new LinkedList<>(waitingQueue)), getSelf());
            }
            waitingQueue = new ArrayList<>();
        } else {
            sendPendingMessageCS();
        }
    }

    private void receiveRequest(RequestCS req){
        if(DEBUG) {
            System.out.println("request cs from " + getSender());
            System.out.println("local queue size " + waitingQueue.size());
            System.out.println("request queue size " + req.getWaitingQueue().size());
        }

        List<SimpleEntry<ActorRef, Integer>> requestQueue = new LinkedList<>(req.getWaitingQueue());

        for(SimpleEntry<ActorRef, Integer> request : req.getWaitingQueue()){
            if(waitingQueue.contains(request)){
                waitingQueue.remove(request);
                requestQueue.remove(request);
            }
        }

        waitingQueue.addAll(requestQueue);
        if(DEBUG)
            System.out.println("local queue size after merge" + waitingQueue.size());
        for(SimpleEntry<ActorRef, Integer> request : waitingQueue){
            if(lastCSRequest.get(request.getKey()) < request.getValue()){
                lastCSRequest.replace(request.getKey(), request.getValue());
            }
        }

        lastCSRequest.replace(getSelf(), Collections.max(lastCSRequest.values()));

        if(holdToken && !inCS){
            releaseCS();
        }
    }

    private void receiveToken(Token token){
        holdToken = true;
        lastCSExecution = token.getLastCSExecution();
        for(ActorRef peer : lastCSRequest.keySet()){
            if(lastCSRequest.get(peer) < lastCSExecution.get(peer)){
                lastCSRequest.replace(peer, lastCSExecution.get(peer));
            }
        }

        for(SimpleEntry<ActorRef, Integer> request : token.getWaitingQueue()){
            if(waitingQueue.contains(request)){
                waitingQueue.remove(request);
            }
        }
        waitingQueue.addAll(token.getWaitingQueue());
        sendPendingMessageCS();
    }

    private void sendPendingMessageCS(){
        for(ActorRef peer : lastCSRequest.keySet()){
            peer.tell(new NextMessages(pendingMessage), getSelf());
        }
        pendingMessage = new LinkedList<>();
        inCS = true;
    }
    private void releaseCS(){
        lastCSExecution.replace(getSelf(), lastCSRequest.get(getSelf()));
        if(waitingQueue.size() > 0) {
            ActorRef next = waitingQueue.get(0).getKey();
            holdToken = false;
            waitingQueue.remove(0);
            next.tell(new Token(new ArrayList<>(waitingQueue), new HashMap<>(lastCSExecution)), getSelf());
            waitingQueue = new ArrayList<>();
        }
    }
}
