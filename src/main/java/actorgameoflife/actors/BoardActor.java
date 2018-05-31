package actorgameoflife.actors;

import actorgameoflife.board.Board;
import actorgameoflife.board.BoardFactory;
import actorgameoflife.board.ManagedBoard;
import actorgameoflife.messages.*;
import actorgameoflife.utility.MillisecondStopWatch;
import actorgameoflife.utility.StopWatch;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class BoardActor extends AbstractActor {

    private ActorRef[][] cell;
    private ManagedBoard currentBoard;
    private ManagedBoard nextBoard;
    private int currentLivingCell = 0;
    private int nextLivingCell = 0;
    private int updateCount = 0;
    private final StopWatch watch = new MillisecondStopWatch();

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
            cell[cellRow][cellColumn] = getContext().actorOf(
                    CellActor.props(currentBoard.isCellAlive(cellRow, cellColumn), cellRow, cellColumn, row, column),
                    "Cell" + cellRow + ":" + cellColumn);
        });
    }

    static Props props(int row, int column, Board.BoardType startBoard) {
        return Props.create(BoardActor.class, () -> new BoardActor(row, column, startBoard));
    }

    @Override
    public void preStart() {
        update();
    }

    @Override
    public Receive createReceive() {
        return updating;
    }

    private Receive boardReply = receiveBuilder().match(BoardRequestMessage.class, msg -> {
        getSender().tell(new BoardMessage(
                BoardFactory.createSubBoard(currentBoard, msg.getStartRow(), msg.getStartColumn(), msg.getEndRow(), msg.getEndColumn()),
                currentLivingCell), getSelf());
    }).build();

    private Receive updatePending = boardReply.orElse(receiveBuilder().match(UpdatePermitMessage.class, msg -> {
        updateCount = 0;
        swapBoard();
        update();
        getContext().unbecome();
    }).build());

    private Receive updating = boardReply.orElse(receiveBuilder().match(CellMessage.class, msg -> {
        updateCount++;
        if(msg.isAlive()) {
            nextBoard.setAlive(msg.getRow(),msg.getColumn());
            nextLivingCell++;
        }else{
            nextBoard.setDead(msg.getRow(),msg.getColumn());
        }
        if(updateCount == (cell.length * cell[0].length)){
            getContext().getParent().tell(new UpdateReadyMessage(), getSelf());
            getContext().become(updatePending);
            watch.stop();
            System.out.println("Board computation time (ms): " + watch.getTime());
        }
    }).build());

    private void update(){
        watch.start();
        cell[0][0].tell(new UpdateMessage(), getSelf());
    }

    private void swapBoard(){
        ManagedBoard tmp = currentBoard;
        currentBoard = nextBoard;
        nextBoard = tmp;
        currentLivingCell = nextLivingCell;
        nextLivingCell = 0;
    }
}
