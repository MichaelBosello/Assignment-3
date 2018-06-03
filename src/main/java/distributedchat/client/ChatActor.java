package distributedchat.client;

import akka.actor.*;
import distributedchat.client.chatgui.ChatGUIActor;
import distributedchat.client.messages.fromtogui.JoinRequestMessage;
import distributedchat.client.messages.fromtogui.JoinResultMessage;
import distributedchat.server.messages.PeerListMessage;
import distributedchat.server.messages.PeerRequestMessage;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class ChatActor extends AbstractActor {

    private final ActorRef gui;
    private ActorSelection lastContactedRegistry;

    public ChatActor() {
        gui = getContext().actorOf(Props.create(ChatGUIActor.class));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JoinRequestMessage.class, this::join)
                .match(ReceiveTimeout.class, this::connectionFailed)
                .match(PeerListMessage.class, this::connectionSuccess)


                .build();
    }

    private void join(JoinRequestMessage msg){
        lastContactedRegistry =
                getContext().actorSelection("akka.tcp://chat@" + msg.getHost() + "/registry");
        lastContactedRegistry.tell(new PeerRequestMessage(), getSelf());
        getContext().setReceiveTimeout(Duration.create(3, TimeUnit.SECONDS));
    }

    private void connectionFailed(ReceiveTimeout msg){
        lastContactedRegistry = null;
        gui.tell(new JoinResultMessage(false, "Timeout"), getSelf());
    }

    private void connectionSuccess(PeerListMessage msg){
        if(getSender().equals(lastContactedRegistry)){



            gui.tell(new JoinResultMessage(true, ""), getSelf());
        }
    }



}
