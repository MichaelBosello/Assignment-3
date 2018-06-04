package distributedchat.client;

import akka.actor.*;
import distributedchat.client.chatgui.ChatGUIActor;
import distributedchat.client.messages.chatprotocol.causalmutualexclusion.RequestCS;
import distributedchat.client.messages.chatprotocol.causalmutualexclusion.Token;
import distributedchat.client.messages.fromtogui.JoinRequestMessage;
import distributedchat.client.messages.fromtogui.JoinResultMessage;
import distributedchat.server.messages.PeerListMessage;
import distributedchat.server.messages.PeerRequestMessage;
import javafx.util.Pair;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatActor extends AbstractActor {

    private final ActorRef gui;
    private ActorSelection lastContactedRegistry;

    private Map<ActorRef, Integer> lastCSRequest = new HashMap<>();//Ri
    private Map<ActorRef, Integer> lastCSExecution = new HashMap<>();//T
    private List<Pair<ActorRef, Integer>> waitingQueue = new ArrayList<>();//Qi
    private boolean holdToken = false;

    public ChatActor() {
        gui = getContext().actorOf(Props.create(ChatGUIActor.class));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JoinRequestMessage.class, this::join)
                .match(ReceiveTimeout.class, this::connectionFailed)
                .match(PeerListMessage.class, this::connectionSuccess)
                .build();
    }

    private void join(JoinRequestMessage msg){
        lastContactedRegistry =
                getContext().actorSelection("akka.tcp://chat@" + msg.getHost() + "/registry");
        lastContactedRegistry.tell(new PeerRequestMessage(), getSelf());
        getContext().setReceiveTimeout(Duration.create(3, TimeUnit.SECONDS));
    }

    private void connectionFailed(ReceiveTimeout msg){
        getContext().setReceiveTimeout(Duration.Undefined());
        lastContactedRegistry = null;
        gui.tell(new JoinResultMessage(false, "Timeout"), getSelf());
    }

    private void connectionSuccess(PeerListMessage msg){
        if(getSender().equals(lastContactedRegistry)){
            getContext().setReceiveTimeout(Duration.Undefined());

            gui.tell(new JoinResultMessage(true, ""), getSelf());
        }
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
            for(ActorRef peer : lastCSRequest.keySet()){
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
