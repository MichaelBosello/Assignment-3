package actorgameoflife.actors;

import actorgameoflife.board.Board;
import actorgameoflife.messages.BoardMessage;
import actorgameoflife.messages.BoardRequestMessage;
import actorgameoflife.messages.UpdateMessage;
import actorgameoflife.messages.UpdateReadyMessage;
import actorgameoflife.messages.gui.ScrollMessage;
import actorgameoflife.messages.gui.StartMessage;
import actorgameoflife.messages.gui.StopMessage;
import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ControllerActor extends AbstractActorWithStash {

    ActorRef board;
    ActorRef gui;
    boolean run = false;

    public ControllerActor(int row, int column, Board.BoardType startBoard, int visualizedRow, int visualizedColumn) {
        gui = getContext().actorOf(GUIActor.props(row, column, visualizedRow, visualizedColumn), "GUI");
        board = getContext().actorOf(BoardActor.props(row, column, startBoard), "Board");
    }

    public static Props props(int row, int column, Board.BoardType startBoard, int visualizedRow, int visualizedColumn) {
        return Props.create(ControllerActor.class, () -> new ControllerActor(row, column, startBoard, visualizedRow, visualizedColumn));
    }

    @Override
    public void preStart() {
        board.tell(new BoardRequestMessage(0, 0), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StartMessage.class, msg -> {
            run = true;
            unstash();
        }).match(StopMessage.class, msg -> {
            run = false;
        }).match(ScrollMessage.class, msg -> {
            board.tell(new BoardRequestMessage(msg.getX(), msg.getY()), getSelf());
        }).match(BoardMessage.class, msg -> {
            gui.tell(msg, getSelf());
        }).match(UpdateReadyMessage.class, msg -> {
            if(run){
                board.tell(new UpdateMessage(), getSelf());
            }else{
                stash();
            }
        }).build();
    }
}
