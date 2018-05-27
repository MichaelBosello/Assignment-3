package actorgameoflife.actors;

import actorgameoflife.messages.CellMessage;
import actorgameoflife.messages.UpdateCellMessage;
import actorgameoflife.messages.UpdateMessage;
import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;

import java.util.LinkedList;
import java.util.List;

public class CellActor extends AbstractActor {

    private boolean currentState;
    private final int myX;
    private final int myY;
    private List<ActorSelection> neighborhood = new LinkedList<>();
    private int received = 0;
    private int neighborSum = 0;
    private boolean starterCell = false;

    public CellActor(boolean currentState, int myX, int myY, int boardRow, int boardColumn) {
        this.currentState = currentState;
        this.myX = myX;
        this.myY = myY;

        int rowStart = myX > 0 ? (myX - 1) : 0;
        int columnStart = myY > 0 ? (myY - 1) : 0;
        int rowEnd = myX < boardRow - 1 ? (myX + 2) :
                myX < boardRow ? (myX + 1) : boardRow;
        int columnEnd = myY < boardColumn -1 ? (myY + 2) :
                myY < boardColumn ? (myY + 1) : boardColumn;

        for(int nearRow = rowStart; nearRow < rowEnd; nearRow++){
            for(int nearColumn = columnStart; nearColumn < columnEnd; nearColumn++){
                if(nearRow != myX || nearColumn != myY)
                    neighborhood.add(getContext().actorSelection("../Cell" + nearRow + ":" + nearColumn));
            }
        }
    }

    static Props props(Boolean state, int x, int y, int boardRow, int boardColumn) {
        return Props.create(CellActor.class, () -> new CellActor(state, x, y, boardRow, boardColumn));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(UpdateMessage.class, msg -> {
            starterCell = true;
            neighborhood.forEach( neighbor -> {
                neighbor.tell(new UpdateCellMessage(currentState), getSelf());
            });
        }).match(UpdateCellMessage.class, msg -> {
            if(received == 0 && ! starterCell){
                neighborhood.forEach( neighbor -> {
                    neighbor.tell(new UpdateCellMessage(currentState), getSelf());
                });
            }

            neighborSum += msg.isAlive() ? 1 : 0;
            received++;

            if(received == neighborhood.size()){
                received = 0;

                if(currentState){
                    if(neighborSum == 2 || neighborSum == 3){
                        currentState = true;
                    } else {
                        currentState = false;
                    }
                } else {
                    if (neighborSum == 3) {
                        currentState = true;
                    } else {
                        currentState = false;
                    }
                }

                neighborSum = 0;

                getContext().getParent().tell(new CellMessage(currentState, myX, myY), getSelf());
            }

        }).build();
    }
}
