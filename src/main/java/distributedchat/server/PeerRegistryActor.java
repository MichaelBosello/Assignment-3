package distributedchat.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import distributedchat.server.messages.ServerPeerMessage;
import distributedchat.server.messages.PeerRequestMessage;

import java.util.HashSet;
import java.util.Set;

public class PeerRegistryActor extends AbstractActor {

    private final Set<ActorRef> peer = new HashSet<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PeerRequestMessage.class, msg -> {
            getSender().tell(new ServerPeerMessage(new HashSet<>(peer)), getSelf());
            peer.add(getSender());
        }).build();
    }
}
