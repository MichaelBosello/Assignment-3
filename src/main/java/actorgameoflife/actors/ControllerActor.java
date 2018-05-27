package actorgameoflife.actors;

import actorgameoflife.board.Board;
import actorgameoflife.messages.BoardMessage;
import actorgameoflife.messages.BoardRequestMessage;
import actorgameoflife.messages.UpdateProceedMessage;
import actorgameoflife.messages.gui.BoardUpdatedMessage;
import actorgameoflife.messages.gui.ScrollMessage;
import actorgameoflife.messages.gui.StartMessage;
import actorgameoflife.messages.gui.StopMessage;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class ControllerActor extends AbstractActor {

    ActorRef board;
    ActorRef gui;
    boolean run = false;

    public ControllerActor(int row, int column, Board.BoardType startBoard, int visualizedRow, int visualizedColumn) {
        board = getContext().actorOf(BoardActor.props(row, column, startBoard), "Board");
        gui = getContext().actorOf(GUIActor.props(row, column, visualizedRow, visualizedColumn), "GUI");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(BoardMessage.class, msg -> {
            gui.tell(msg, getSelf());
        }).match(StartMessage.class, msg -> {
            run = true;
        }).match(StopMessage.class, msg -> {
            run = false;
        }).match(ScrollMessage.class, msg -> {
            board.tell(new BoardRequestMessage(msg.getX(), msg.getY()), getSelf());
        }).match(BoardUpdatedMessage.class, msg -> {
            if(run){
                board.tell(new UpdateProceedMessage(), getSelf());
            }
        }).build();
    }
}
