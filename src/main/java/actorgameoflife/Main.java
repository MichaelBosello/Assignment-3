package actorgameoflife;

import actorgameoflife.actors.ControllerActor;
import actorgameoflife.board.Board;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Main {

    private final static int ROW = 1000;
    private final static int COLUMN = 1000;
    private final static int VIEWED_ROW = 80;
    private final static int VIEWED_COLUMN = 80;
    private final static Board.BoardType BOARD_TYPE = Board.BoardType.LWSS;

    public static void main(String[] args) {
        Config config = ConfigFactory.parseFile(new File("src/main/resources/gameoflife.conf"));
        ActorSystem system = ActorSystem.create("GameOfLife", config);
        system.actorOf(ControllerActor.props(ROW, COLUMN, BOARD_TYPE, VIEWED_ROW, VIEWED_COLUMN), "Controller");
    }
}
