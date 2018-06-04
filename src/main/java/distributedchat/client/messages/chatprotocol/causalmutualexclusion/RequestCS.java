package distributedchat.client.messages.chatprotocol.causalmutualexclusion;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

public class RequestCS implements Serializable {
    private List<SimpleEntry<ActorRef, Integer>> waitingQueue;

    public RequestCS(List<SimpleEntry<ActorRef, Integer>> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

    public List<SimpleEntry<ActorRef, Integer>> getWaitingQueue() {
        return waitingQueue;
    }
}
