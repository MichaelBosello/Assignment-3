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
    private int currentLivingCell = 0;
    private int nextLivingCell = 0;
    private int subWidth = 0;
    private int subHeight = 0;
    private int currentX = 0;
    private int currentY = 0;
    private int updateCount = 0;
    private ActorRef updateApplicant;

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
                currentLivingCell++;
            }
            cell[cellRow][cellColumn] = getContext().actorOf(CellActor.props(currentBoard.isCellAlive(cellRow, cellColumn)), "Cell[" + cellRow + "][" + cellColumn +"]");
        });

    }

    @Override
    public Receive createReceive() {
        return updated;
    }

    private Receive base = receiveBuilder().match(BoardRequestMessage.class, msg -> {
        getSender().tell(new BoardMessage(
                BoardFactory.createSubBoard(currentBoard, msg.getX(), msg.getY(), subWidth, subHeight),
                currentLivingCell), getSelf());
        currentX = msg.getX();
        currentY = msg.getY();
    }).build();

    private Receive updating = receiveBuilder().match(BoardRequestMessage.class, msg -> {
        getSender().tell(new BoardMessage(
                BoardFactory.createSubBoard(currentBoard, msg.getX(), msg.getY(), subWidth, subHeight),
                currentLivingCell), getSelf());
        currentX = msg.getX();
        currentY = msg.getY();
    }).match(CellMessage.class, msg -> {
        updateCount++;
        if(msg.isAlive()) {
            nextBoard.setAlive(msg.getX(),msg.getY());
            nextLivingCell++;
        }else{
            nextBoard.setDead(msg.getX(),msg.getY());
        }
        if(updateCount == (cell.length * cell[0].length)){
            updateCount = 0;
            ManagedBoard tmp = currentBoard;
            currentBoard = nextBoard;
            nextBoard = tmp;
            currentLivingCell = nextLivingCell;
            nextLivingCell = 0;
            updateApplicant.tell(new BoardMessage(
                    BoardFactory.createSubBoard(currentBoard, currentX, currentY, subWidth, subHeight),
                    currentLivingCell), getSelf());
            getContext().unbecome();
        }
    }).build();

    private Receive updated = receiveBuilder().match(BoardRequestMessage.class, msg -> {
        getSender().tell(new BoardMessage(
                BoardFactory.createSubBoard(currentBoard, msg.getX(), msg.getY(), subWidth, subHeight),
                currentLivingCell), getSelf());
        currentX = msg.getX();
        currentY = msg.getY();
    }).match(UpdateMessage.class, msg -> {
        updateApplicant = getSender();
        cell[0][0].tell(new UpdateMessage(), getSelf());
        getContext().become(updating);
    }).build();
}
