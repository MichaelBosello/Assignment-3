package distributedchat.server;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import distributedchat.client.messages.chatprotocol.Leave;
import distributedchat.server.messages.ServerPeerMessage;
import distributedchat.server.messages.PeerRequestMessage;

import java.util.HashSet;
import java.util.Set;

public class PeerRegistryActor extends AbstractActor {

    private final Set<ActorRef> peer = new HashSet<>();

    public PeerRegistryActor() {
        System.out.println("Server akka id " + getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PeerRequestMessage.class, msg -> {
            System.out.println("Received request");
            getSender().tell(new ServerPeerMessage(new HashSet<>(peer)), getSelf());
            peer.add(getSender());
        }).match(Leave.class, msg -> {
            System.out.println("Removed peer");
            peer.remove(getSender());
        }).build();
    }
}
