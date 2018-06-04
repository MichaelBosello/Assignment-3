package distributedchat.client.messages.chatprotocol;

import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.Set;

public class PeerList implements Serializable {

    private final Set<ActorRef> peer;

    public PeerList(Set<ActorRef> peer) {
        this.peer = peer;
    }

    public Set<ActorRef> getPeer() {
        return peer;
    }
}
