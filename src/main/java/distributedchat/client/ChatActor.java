package distributedchat.client;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import distributedchat.client.chatgui.ChatGUIActor;
import distributedchat.client.messages.fromtogui.JoinRequestMessage;
import distributedchat.server.messages.PeerRequestMessage;

public class ChatActor extends AbstractActor {

    private final ActorRef gui;

    public ChatActor() {
        gui = getContext().actorOf(Props.create(ChatGUIActor.class));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JoinRequestMessage.class, msg -> {
            ActorSelection registry =
                    getContext().actorSelection("akka.tcp://chat@" + msg.getHost() + "/registry");
            registry.tell(new PeerRequestMessage(), getSelf());
        }).build();
    }
}
