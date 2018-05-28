package akkaswingreactivitytest;

import akka.actor.*;

public class PongActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PingMsg.class, msg -> {
            //System.out.println(Thread.currentThread().getName() + " Priority: " + Thread.currentThread().getPriority());
            //System.out.println("PING received: "+  msg.getValue());

            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
            getSender().tell(new PongMsg(msg.getValue() + 1), getSelf());
        }).build();
    }
}
