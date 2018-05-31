package actorgameoflife.actors;

import actorgameoflife.board.Board;
import actorgameoflife.messages.*;
import actorgameoflife.messages.gui.BoardVisualizedMessage;
import actorgameoflife.messages.gui.ScrollMessage;
import actorgameoflife.messages.gui.StartMessage;
import actorgameoflife.messages.gui.StopMessage;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ControllerActor extends AbstractActorWithStash {

    private ActorRef board;
    private ActorRef gui;
    private int visualizedRow;
    private int visualizedColumn;
    private int currentX = 0;
    private int currentY = 0;
    private boolean run = false;
    private boolean previousImageVisualized = true;

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
