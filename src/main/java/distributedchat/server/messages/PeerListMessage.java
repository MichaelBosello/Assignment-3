package distributedchat.server.messages;

import akka.actor.ActorRef;

import java.util.Set;

public class PeerListMessage {

    private final Set<ActorRef> peer;

    public PeerListMessage(Set<ActorRef> peer) {
        this.peer = peer;
    }

    public Set<ActorRef> getPeer() {
        return peer;
    }
}
