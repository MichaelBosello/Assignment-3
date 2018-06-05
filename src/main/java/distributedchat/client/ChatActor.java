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
    private int messageAck = 0;
    private int leaveAck = 0;

    public ChatActor() {
        gui = getContext().actorOf(Props.create(ChatGUIActor.class));
        lastCSRequest.put(getSelf(), 0);
        lastCSExecution.put(getSelf(), 0);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //server connection and response
                .match(ConnectRequestMessage.class, this::serverConnection)
                .match(ServerPeerMessage.class, this::connectionSuccess)
                .match(ReceiveTimeout.class, this::connectionFailed)
                //prepare to became peer
                .match(PeerList.class, this::joinResponse)
                .matchAny( o -> stash())
                .build();
    }

    //server connection and response

    private void serverConnection(ConnectRequestMessage msg){
        lastContactedRegistry =
                getContext().actorSelection("akka.tcp://chat@" + msg.getHost() + "/user/registry");
        lastContactedRegistry.tell(new PeerRequestMessage(), getSelf());
        getContext().setReceiveTimeout(Duration.create(5, TimeUnit.SECONDS));
    }

    private void connectionSuccess(ServerPeerMessage msg){
        if(lastContactedRegistry != null){//desiderata: lastContactedRegistry.equals(getSender) ... todo ActorSelection -> ActorRef
            getContext().setReceiveTimeout(Duration.Undefined());
            if(msg.getPeer().size() > 0) {
                for (ActorRef peer : msg.getPeer()) {
                    peer.tell(new AskToJoin(), getSelf());
                }
            }else{
                holdToken = true;
                becamePeer();
            }
        }
    }

    private void connectionFailed(ReceiveTimeout msg){
        getContext().setReceiveTimeout(Duration.Undefined());
        lastContactedRegistry = null;
        gui.tell(new ConnectionResultMessage(false, "Timeout"), getSelf());
    }

    //prepare to became peer

    private void joinResponse(PeerList msg){
        allPeer.addAll(msg.getPeer());
        findPeer.add(getSender());
        lastCSRequest.put(getSender(), 0);
        lastCSExecution.put(getSender(), 0);
        if(allPeer.size() == findPeer.size()) {
            becamePeer();
            unstashAll();
        }
    }

    private void becamePeer(){
        getContext().become(
                receiveBuilder()
                        //accept peer
                        .match(AskToJoin.class, this::addPeer)
                        //leave protocol
                        .match(LeaveRequestMessage.class, this::leaveGroup)
                        .match(Leave.class, this::removePeer)
                        .match(LeaveReceived.class, this::leaveAck)
                        //chat mutual exclusion protocol
                        .match(SendMessage.class, this::sendChatMessage)
                        .match(NextMessages.class, this::visualizeMessage)
                        .match(MessageReceived.class, this::chatMessageAck)
                        .match(RequestCS.class, this::receiveRequest)
                        .match(Token.class, this::receiveToken)
                        .build()
        );
        gui.tell(new ConnectionResultMessage(true, ""), getSelf());
    }

    //accept peer

    private void addPeer(AskToJoin msg){
        getSender().tell(new PeerList(new HashSet<>(lastCSRequest.keySet())), getSelf());
        lastCSRequest.put(getSender(), 0);
        lastCSExecution.put(getSender(), 0);
        allPeer.add(getSender());
        findPeer.add(getSender());
    }

    //leave protocol

    private void leaveGroup(LeaveRequestMessage msg){
        for(ActorRef peer : findPeer){
            peer.tell(new Leave(), getSelf());
        }
        lastContactedRegistry.tell(new Leave(), getSelf());

        if(findPeer.size() == 0){
            System.exit(0);
        }
    }

    private void removePeer(Leave msg){
        allPeer.remove(getSender());
        findPeer.remove(getSender());
        lastCSRequest.remove(getSender());
        lastCSExecution.remove(getSender());
        List<SimpleEntry<ActorRef, Integer>> tmp = new ArrayList<>(waitingQueue);
        for(SimpleEntry<ActorRef, Integer> request : tmp){
            if(request.getKey().equals(getSender())){
                waitingQueue.remove(request);
            }
        }
        getSender().tell(new LeaveReceived(), getSelf());
    }

    private void leaveAck(LeaveReceived msg){
        leaveAck++;
        if(leaveAck == findPeer.size()){
            if(holdToken){
                if(waitingQueue.size() > 0) {
                    ActorRef next = waitingQueue.get(0).getKey();
                    waitingQueue.remove(0);
                    lastCSExecution.remove(getSelf());
                    List<SimpleEntry<ActorRef, Integer>> tmp = new ArrayList<>(waitingQueue);
                    for(SimpleEntry<ActorRef, Integer> request : tmp){
                        if(request.getKey().equals(getSelf())){
                            waitingQueue.remove(request);
                        }
                    }
                    next.tell(new Token(new ArrayList<>(waitingQueue), new HashMap<>(lastCSExecution)), getSelf());
                } else {
                    findPeer.iterator().next().tell(new Token(new ArrayList<>(waitingQueue), new HashMap<>(lastCSExecution)), getSelf());
                }
            }
            System.exit(0);
        }
    }

    //chat mutual exclusion protocol

    private void sendChatMessage(SendMessage msg){
        pendingMessage.add(msg.getMessage());
        if(!csRequestSubmitted){
            csRequestSubmitted = true;
            requestCS();
        }
    }

    private void visualizeMessage(NextMessages msg){
        gui.tell(msg, getSelf());
        getSender().tell(new MessageReceived(), getSelf());
    }

    private void chatMessageAck(MessageReceived msg){
        messageAck++;
        if(messageAck == lastCSRequest.size()){
            messageAck = 0;
            inCS = false;
            releaseCS();
            if(pendingMessage.isEmpty()) {
                csRequestSubmitted = false;
            }else{
                requestCS();
            }
        }
    }

    private void receiveRequest(RequestCS req){
        List<SimpleEntry<ActorRef, Integer>> requestQueue = new ArrayList<>(req.getWaitingQueue());
        for(SimpleEntry<ActorRef, Integer> request : req.getWaitingQueue()){
            if(waitingQueue.contains(request)){
                waitingQueue.remove(request);
                requestQueue.remove(request);
            }
        }
        waitingQueue.addAll(requestQueue);

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
        ExecuteCS();
    }

    private void requestCS() {
        if (!holdToken) {
            lastCSRequest.replace(getSelf(), lastCSRequest.get(getSelf()) + 1);
            waitingQueue.add(new SimpleEntry<>(getSelf(), lastCSRequest.get(getSelf())));
            for (ActorRef peer : lastCSRequest.keySet()) {
                peer.tell(new RequestCS(new ArrayList<>(waitingQueue)), getSelf());
            }
            waitingQueue = new ArrayList<>();
        } else {
            ExecuteCS();
        }
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

    private void ExecuteCS(){
        inCS = true;
        for(ActorRef peer : lastCSRequest.keySet()){
            peer.tell(new NextMessages(pendingMessage), getSelf());
        }
        pendingMessage = new LinkedList<>();
    }
}
