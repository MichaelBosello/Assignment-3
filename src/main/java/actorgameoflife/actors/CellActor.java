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

    private Receive updating = receiveBuilder().match(UpdateCellMessage.class, this::processNeighborCell).build();

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(UpdateMessage.class, msg -> {
            sendState();
            getContext().become(updating);
        }).match(UpdateCellMessage.class, msg -> {
            sendState();
            processNeighborCell(msg);
            getContext().become(updating);
        }).build();
    }

    private void sendState(){
        neighborhood.forEach( neighbor -> {
            neighbor.tell(new UpdateCellMessage(currentState), getSelf());
        });
    }

    private void processNeighborCell(UpdateCellMessage msg){
        neighborSum += msg.isAlive() ? 1 : 0;
        received++;

        if(received == neighborhood.size()){
            received = 0;

            if(currentState){
                if(! (neighborSum == 2 || neighborSum == 3) ){
                    currentState = false;
                }
            } else {
                if (neighborSum == 3) {
                    currentState = true;
                }
            }

            neighborSum = 0;
            getContext().getParent().tell(new CellMessage(currentState, myRow, myColumn), getSelf());
            getContext().unbecome();
        }
    }
}
