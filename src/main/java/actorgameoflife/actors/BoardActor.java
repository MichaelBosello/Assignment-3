package actorgameoflife.actors;

import actorgameoflife.board.Board;
import actorgameoflife.board.BoardFactory;
import actorgameoflife.board.ManagedBoard;
import actorgameoflife.messages.*;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class BoardActor extends AbstractActor {

    private ActorRef[][] cell;
    private ManagedBoard currentBoard;
    private ManagedBoard nextBoard;
    private int currentLivingCell = 0;
    private int nextLivingCell = 0;
    private int subRow = 0;
    private int subColumn = 0;
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
                currentLivingCell++;
            }
            cell[cellRow][cellColumn] = getContext().actorOf(CellActor.props(currentBoard.isCellAlive(cellRow, cellColumn), cellRow, cellColumn, row, column), "Cell[" + cellRow + "][" + cellColumn +"]");
        });
    }

    static Props props(int row, int column, Board.BoardType startBoard) {
        return Props.create(BoardActor.class, () -> new BoardActor(row, column, startBoard));
    }

    @Override
    public void preStart() {
        cell[0][0].tell(new UpdateMessage(), getSelf());
    }

    @Override
    public Receive createReceive() {
        return updating;
    }

    private Receive base = receiveBuilder().match(BoardRequestMessage.class, msg -> {
        getSender().tell(new BoardMessage(
                BoardFactory.createSubBoard(currentBoard, msg.getX(), msg.getY(), subRow, subColumn),
                currentLivingCell), getSelf());
        currentX = msg.getX();
        currentY = msg.getY();
    }).match(DimensionMessage.class, msg -> {
        subRow = msg.getRow();
        subColumn = msg.getColumn();
    }).build();

    private Receive updated = base.orElse(receiveBuilder().match(UpdateMessage.class, msg -> {
        updateCount = 0;
        ManagedBoard tmp = currentBoard;
        currentBoard = nextBoard;
        nextBoard = tmp;
        currentLivingCell = nextLivingCell;
        nextLivingCell = 0;
        getSender().tell(new BoardMessage(
                BoardFactory.createSubBoard(currentBoard, currentX, currentY, subRow, subColumn),
                currentLivingCell), getSelf());
        cell[0][0].tell(new UpdateMessage(), getSelf());
        getContext().unbecome();
    }).build());

    private Receive updating = base.orElse(receiveBuilder().match(CellMessage.class, msg -> {
        updateCount++;
        if(msg.isAlive()) {
            nextBoard.setAlive(msg.getX(),msg.getY());
            nextLivingCell++;
        }else{
            nextBoard.setDead(msg.getX(),msg.getY());
        }
        if(updateCount == (cell.length * cell[0].length)){
            getContext().getParent().tell(new UpdateReadyMessage(), getSelf());
            getContext().become(updated);
        }
    }).build());
}
