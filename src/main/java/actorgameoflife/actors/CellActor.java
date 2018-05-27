package actorgameoflife.actors;

import actorgameoflife.messages.CellMessage;
import actorgameoflife.messages.UpdateCellMessage;
import actorgameoflife.messages.UpdateProceedMessage;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.LinkedList;
import java.util.List;

public class CellActor extends AbstractActor {

    private boolean currentStare;
    private final int myX;
    private final int myY;
    private List<ActorRef> neighborhood = new LinkedList<>();
    private int received = 0;
    private int neighborSum = 0;
    private boolean starterCell = false;

    public CellActor(boolean currentStare, int myX, int myY, int boardRow, int boardColumn) {
        this.currentStare = currentStare;
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
                neighborhood.add(getContext().actorSelection("Cell[" + nearRow + "][" + nearColumn +"]").anchor());
            }
        }
    }

    static Props props(Boolean state, int x, int y, int boardRow, int boardColumn) {
        return Props.create(CellActor.class, () -> new CellActor(state, x, y, boardRow, boardColumn));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(UpdateProceedMessage.class, msg -> {
            starterCell = true;
            neighborhood.forEach( neighbor -> {
                neighbor.tell(new UpdateCellMessage(currentStare), getSelf());
            });
        }).match(UpdateCellMessage.class, msg -> {
            if(received == 0 && ! starterCell){
                neighborhood.forEach( neighbor -> {
                    neighbor.tell(new UpdateCellMessage(currentStare), getSelf());
                });
            }
            neighborSum += msg.isAlive() ? 1 : 0;

            received++;

            if(received == neighborhood.size()){
                received = 0;

                if(currentStare){
                    neighborSum--;
                    if(neighborSum == 2){
                        currentStare = true;
                    } else {
                        currentStare = false;
                    }
                } else {
                    if (neighborSum == 3) {
                        currentStare = true;
                    } else {
                        currentStare = false;
                    }
                }

                neighborSum = 0;

                getContext().parent().tell(new CellMessage(currentStare, myX, myY), getSelf());
            }

        }).build();
    }
}
