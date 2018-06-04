package distributedchat.client.messages.chatprotocol.causalmutualexclusion;

import akka.actor.ActorRef;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Token implements Serializable {
    private List<Pair<ActorRef, Integer>> waitingQueue;//Q
    private Map<ActorRef, Integer> lastCSExecution;//T

    public Token(List<Pair<ActorRef, Integer>> waitingQueue, Map<ActorRef, Integer> lastCSExecution) {
        this.waitingQueue = waitingQueue;
        this.lastCSExecution = lastCSExecution;
    }

    public List<Pair<ActorRef, Integer>> getWaitingQueue() {
        return waitingQueue;
    }

    public Map<ActorRef, Integer> getLastCSExecution() {
        return lastCSExecution;
    }
}
