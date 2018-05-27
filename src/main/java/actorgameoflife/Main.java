package actorgameoflife;

import actorgameoflife.actors.ControllerActor;
import actorgameoflife.board.Board;
import akka.actor.ActorSystem;

public class Main {

    private final static int ROW = 1000;
    private final static int COLUMN = 1000;
    private final static int VIEWED_ROW = 100;
    private final static int VIEWED_COLUMN = 1000;
    private final static Board.BoardType BOARD_TYPE = Board.BoardType.RANDOM;

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("GameOfLife");
        system.actorOf(ControllerActor.props(ROW, COLUMN, BOARD_TYPE, VIEWED_ROW, VIEWED_COLUMN), "Controller");
    }
}
