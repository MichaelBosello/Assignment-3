package distributedchat.client.messages.chatprotocol.causalmutualexclusion;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

public class Token implements Serializable {
    private List<SimpleEntry<ActorRef, Integer>> waitingQueue;//Q
    private Map<ActorRef, Integer> lastCSExecution;//T

    public Token(List<SimpleEntry<ActorRef, Integer>> waitingQueue, Map<ActorRef, Integer> lastCSExecution) {
        this.waitingQueue = waitingQueue;
        this.lastCSExecution = lastCSExecution;
    }

    public List<SimpleEntry<ActorRef, Integer>> getWaitingQueue() {
        return waitingQueue;
    }

    public Map<ActorRef, Integer> getLastCSExecution() {
        return lastCSExecution;
    }
}
