package actorgameoflife.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CellActor extends AbstractActor {

    boolean currentStare;

    public CellActor(boolean currentStare) {
        this.currentStare = currentStare;
    }

    static Props props(Boolean state) {
        return Props.create(CellActor.class, () -> new CellActor(state));
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
