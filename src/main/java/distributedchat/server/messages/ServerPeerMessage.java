package distributedchat.server.messages;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.Set;

public class ServerPeerMessage implements Serializable {

    private final Set<ActorRef> peer;

    public ServerPeerMessage(Set<ActorRef> peer) {
        this.peer = peer;
    }

    public Set<ActorRef> getPeer() {
        return peer;
    }
}
