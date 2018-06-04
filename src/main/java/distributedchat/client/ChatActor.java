package distributedchat.client;

import akka.actor.*;
import distributedchat.client.chatgui.ChatGUIActor;
import distributedchat.client.messages.chatprotocol.AskToJoin;
import distributedchat.client.messages.chatprotocol.MessageReceived;
import distributedchat.client.messages.chatprotocol.NextMessage;
import distributedchat.client.messages.chatprotocol.PeerList;
import distributedchat.client.messages.chatprotocol.causalmutualexclusion.RequestCS;
import distributedchat.client.messages.chatprotocol.causalmutualexclusion.Token;
import distributedchat.client.messages.fromtogui.ConnectRequestMessage;
import distributedchat.client.messages.fromtogui.ConnectionResultMessage;
import distributedchat.client.messages.fromtogui.LeaveRequestMessage;
import distributedchat.client.messages.fromtogui.SendMessage;
import distributedchat.server.messages.ServerPeerMessage;
import distributedchat.server.messages.PeerRequestMessage;
import javafx.util.Pair;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatActor extends AbstractActorWithStash {

    private final ActorRef gui;
    private ActorSelection lastContactedRegistry;
    private Set<ActorRef> allPeer = new HashSet<>();
    private Set<ActorRef> findPeer = new HashSet<>();

    private Map<ActorRef, Integer> lastCSRequest = new HashMap<>();//Ri
    private Map<ActorRef, Integer> lastCSExecution = new HashMap<>();//T
    private List<Pair<ActorRef, Integer>> waitingQueue = new ArrayList<>();//Qi
    private boolean holdToken = false;

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
                getContext().actorSelection("akka.tcp://chat@" + msg.getHost() + "/registry");
        lastContactedRegistry.tell(new PeerRequestMessage(), getSelf());
        getContext().setReceiveTimeout(Duration.create(3, TimeUnit.SECONDS));
    }

    private void connectionFailed(ReceiveTimeout msg){
        getContext().setReceiveTimeout(Duration.Undefined());
        lastContactedRegistry = null;
        gui.tell(new ConnectionResultMessage(false, "Timeout"), getSelf());
    }

    private void connectionSuccess(ServerPeerMessage msg){
        if(getSender().equals(lastContactedRegistry)){
            getContext().setReceiveTimeout(Duration.Undefined());
            for(ActorRef peer : msg.getPeer()){
                peer.tell(new AskToJoin(), getSelf());
            }
        }
    }

    private void joinResponse(PeerList msg){
        allPeer.addAll(msg.getPeer());
        findPeer.add(getSender());
        if(allPeer.equals(findPeer)) {
            getContext().become(
                    receiveBuilder()
                            .match(AskToJoin.class, this::addPeer)
                            .match(SendMessage.class, this::)
                            .match(LeaveRequestMessage.class, this::)
                            .match(NextMessage.class, this::)
                            .match(MessageReceived.class, this::)
                            .match(RequestCS.class, this::receiveRequest)
                            .match(Token.class, this::receiveToken)
                            .build()
            );
            gui.tell(new ConnectionResultMessage(true, ""), getSelf());
        }
    }

    private void addPeer(AskToJoin msg){
        lastCSRequest.put(getSender(), 0);
        lastCSExecution.put(getSender(), 0);
        getSender().tell(new PeerList(allPeer), getSelf());
        allPeer.add(getSender());
        findPeer.add(getSender());
    }



    private List<Pair<ActorRef, Integer>> removeObsoleteRequest(List<Pair<ActorRef, Integer>> q){
        for(Pair<ActorRef, Integer> request : q){
            if(request.getValue() <= lastCSRequest.get(request.getKey()) ||
                    request.getValue() <= lastCSExecution.get(request.getKey())){
                q.remove(request);
            }
        }
        return q;
    }

    private void requestCS(){
        if(!holdToken){
            lastCSRequest.replace(getSelf(), lastCSRequest.get(getSelf()) + 1);
            waitingQueue.add(new Pair<>(getSelf(), lastCSRequest.get(getSelf())));
            for(ActorRef peer : allPeer){
                peer.tell(new RequestCS(waitingQueue), getSelf());
            }
            waitingQueue = new ArrayList<>();
        }
    }

    private void receiveRequest(RequestCS req){
        waitingQueue.addAll(req.getWaitingQueue());
        removeObsoleteRequest(waitingQueue);
        for(Pair<ActorRef, Integer> request : waitingQueue){
            if(lastCSRequest.get(request.getKey()) < request.getValue()){
                lastCSRequest.replace(request.getKey(), request.getValue());
            }
        }

        lastCSRequest.replace(getSelf(), Collections.max(lastCSRequest.values()));
    }

    private void receiveToken(Token token){
        holdToken = true;
        lastCSExecution = token.getLastCSExecution();
        for(ActorRef peer : lastCSRequest.keySet()){
            if(lastCSRequest.get(peer) < lastCSExecution.get(peer)){
                lastCSRequest.replace(peer, lastCSExecution.get(peer));
            }
        }
        removeObsoleteRequest(waitingQueue);
        for(Pair<ActorRef, Integer> request : token.getWaitingQueue()){
            if(waitingQueue.contains(request)){
                waitingQueue.remove(request);
            }
        }
        waitingQueue.addAll(token.getWaitingQueue());
    }

    private void releaseCS(){
        lastCSExecution.replace(getSelf(), lastCSRequest.get(getSelf()));
        if(waitingQueue.size() > 0) {
            ActorRef next = waitingQueue.get(0).getKey();
            holdToken = false;
            waitingQueue.remove(0);
            next.tell(new Token(waitingQueue, lastCSExecution), getSelf());
            waitingQueue = new ArrayList<>();
        }
    }
}
