package actorgameoflife.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class BoardActor extends AbstractActor {

    @Override
    public void preStart() {
        final ActorRef ponger = getContext().actorOf(Props.create(PongActor.class), "ponger");
        ponger.tell(new PingMsg(0), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PongMsg.class, msg -> {
            System.out.println("PONG received: "+  msg.getValue());
            getSender().tell(new PingMsg( msg.getValue() + 1), getSelf());
        }).build();
}
