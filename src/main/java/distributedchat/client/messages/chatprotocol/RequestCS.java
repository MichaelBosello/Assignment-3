package distributedchat.client.messages.chatprotocol;

import akka.actor.ActorRef;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.List;

public class RequestCS implements Serializable {
    private List<Pair<ActorRef, Integer>> waitingQueue;

    public RequestCS(List<Pair<ActorRef, Integer>> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

    public List<Pair<ActorRef, Integer>> getWaitingQueue() {
        return waitingQueue;
    }
}
