package actorgameoflife.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class GUIActor extends AbstractActor {



    static Props props() {
        return Props.create(GUIActor.class, () -> new GUIActor());
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
