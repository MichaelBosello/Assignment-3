package actorgameoflife.actors;

import actorgameoflife.board.Board;
import actorgameoflife.messages.*;
import actorgameoflife.messages.gui.BoardVisualizedMessage;
import actorgameoflife.messages.gui.ScrollMessage;
import actorgameoflife.messages.gui.StartMessage;
import actorgameoflife.messages.gui.StopMessage;
import actorgameoflife.utility.MillisecondStopWatch;
import actorgameoflife.utility.StopWatch;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ControllerActor extends AbstractActorWithStash {

    private final ActorRef board;
    private final ActorRef gui;
    private int visualizedRow;
    private int visualizedColumn;
    private int currentX = 0;
    private int currentY = 0;
    private boolean run = false;
    private boolean previousImageVisualized = true;
    private final StopWatch watch = new MillisecondStopWatch();

    public ControllerActor(int row, int column, Board.BoardType startBoard, int visualizedRow, int visualizedColumn) {
        this.visualizedRow = visualizedRow;
        this.visualizedColumn = visualizedColumn;
        board = getContext().actorOf(BoardActor.props(row, column, startBoard), "Board");
        gui = getContext().actorOf(GUIActor.props(row, column, visualizedRow, visualizedColumn), "GUI");
    }

    public static Props props(int row, int column, Board.BoardType startBoard, int visualizedRow, int visualizedColumn) {
        return Props.create(ControllerActor.class, () -> new ControllerActor(row, column, startBoard, visualizedRow, visualizedColumn));
    }

    @Override
    public void preStart() {
        watch.start();
        boardRequest();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StartMessage.class, msg -> {
            run = true;
            unstashAll();
        }).match(StopMessage.class, msg -> {
            run = false;
        }).match(ScrollMessage.class, msg -> {
            currentX = msg.getX();
            currentY = msg.getY();
            boardRequest();
        }).match(BoardMessage.class, msg -> {
            if(previousImageVisualized) {
                gui.tell(msg, getSelf());
                previousImageVisualized = false;
            } else {
                stash();
            }
        }).match(BoardVisualizedMessage.class, msg -> {
            previousImageVisualized = true;
            unstashAll();

            watch.stop();
            System.out.println("Time between frame (ms): " + watch.getTime());
            watch.start();
        }).match(UpdateReadyMessage.class, msg -> {
            if (run && previousImageVisualized) {
                board.tell(new UpdatePermitMessage(), getSelf());
                boardRequest();
            } else {
                stash();
            }
        }).build();
    }

    private void boardRequest(){
        board.tell(new BoardRequestMessage(currentX, currentY, visualizedRow, visualizedColumn), getSelf());
    }
}
