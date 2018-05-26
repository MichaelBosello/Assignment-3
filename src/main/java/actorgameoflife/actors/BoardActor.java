package actorgameoflife.actors;

import actorgameoflife.board.Board;
import actorgameoflife.board.BoardFactory;
import actorgameoflife.board.ManagedBoard;
import actorgameoflife.messages.BoardMessage;
import actorgameoflife.messages.BoardRequestMessage;
import actorgameoflife.messages.CellMessage;
import actorgameoflife.messages.UpdateMessage;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class BoardActor extends AbstractActor {

    private ActorRef[][] cell;
    private ManagedBoard currentBoard;
    private ManagedBoard nextBoard;
    private int livingCell = 0;
    private int nextLivingCell = 0;
    private int subWidth = 0;
    private int subHeight = 0;
    private int currentX = 0;
    private int currentY = 0;
    private int updateCount = 0;

    public BoardActor(int row, int column, Board.BoardType startBoard) {
        switch (startBoard){
            case LWSS:
                currentBoard = (ManagedBoard) BoardFactory.createLotOfLWSS(row,column);
                break;
            case GLIDER:
                currentBoard = (ManagedBoard) BoardFactory.createLotOfGlider(row,column);
                break;
            case RANDOM:
                currentBoard = (ManagedBoard) BoardFactory.createSimpleBoard(row,column);
        }
        nextBoard = (ManagedBoard) BoardFactory.createCopyBoard(currentBoard);

        cell = new ActorRef[row][column];

        currentBoard.iterateCell((cellRow, cellColumn) -> {
            if(currentBoard.isCellAlive(cellRow, cellColumn)){
                livingCell++;
            }
            cell[cellRow][cellColumn] = getContext().actorOf(CellActor.props(currentBoard.isCellAlive(cellRow, cellColumn)), "Cell[" + cellRow + "][" + cellColumn +"]");
        });

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(BoardRequestMessage.class, msg -> {
            getSender().tell(new BoardMessage(
                    BoardFactory.createSubBoard(currentBoard, msg.getX(), msg.getY(), subWidth, subHeight)), getSelf());
            currentX = msg.getX();
            currentY = msg.getY();
        }).match(UpdateMessage.class, msg -> {
            cell[0][0].tell(new UpdateMessage(), getSelf());
        }).match(CellMessage.class, msg -> {
            updateCount++;
            if(msg.isAlive()) {
                nextBoard.setAlive(msg.getX(),msg.getY());
                nextLivingCell++;
            }else{
                nextBoard.setDead(msg.getX(),msg.getY());
            }

        }).build();
    }
}
