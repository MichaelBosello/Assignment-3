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
    private final int myRow;
    private final int myColumn;
    private List<ActorSelection> neighborhood = new LinkedList<>();
    private int received = 0;
    private int neighborSum = 0;
    private boolean sent = false;

    public CellActor(boolean currentState, int myRow, int myColumn, int boardRow, int boardColumn) {
        this.currentState = currentState;
        this.myRow = myRow;
        this.myColumn = myColumn;

        int rowStart = myRow > 0 ? (myRow - 1) : 0;
        int columnStart = myColumn > 0 ? (myColumn - 1) : 0;
        int rowEnd = myRow < boardRow - 1 ? (myRow + 2) :
                myRow < boardRow ? (myRow + 1) : boardRow;
        int columnEnd = myColumn < boardColumn -1 ? (myColumn + 2) :
                myColumn < boardColumn ? (myColumn + 1) : boardColumn;

        for(int nearRow = rowStart; nearRow < rowEnd; nearRow++){
            for(int nearColumn = columnStart; nearColumn < columnEnd; nearColumn++){
                if(nearRow != myRow || nearColumn != myColumn)
                    neighborhood.add(getContext().actorSelection("../Cell" + nearRow + ":" + nearColumn));
            }
        }
    }

    static Props props(Boolean state, int cellRow, int cellColumn, int boardRow, int boardColumn) {
        return Props.create(CellActor.class, () -> new CellActor(state, cellRow, cellColumn, boardRow, boardColumn));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(UpdateMessage.class, msg -> {
            if(!sent) {
                sent = true;
                neighborhood.forEach(neighbor -> {
                    neighbor.tell(new UpdateCellMessage(currentState), getSelf());
                });
            }
        }).match(UpdateCellMessage.class, msg -> {
            if(received == 0 && !sent){
                sent = true;
                neighborhood.forEach( neighbor -> {
                    neighbor.tell(new UpdateCellMessage(currentState), getSelf());
                });
            }

            neighborSum += msg.isAlive() ? 1 : 0;
            received++;

            if(received == neighborhood.size()){
                received = 0;
                sent = false;

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

                getContext().getParent().tell(new CellMessage(currentState, myRow, myColumn), getSelf());
            }

        }).build();
    }
}
