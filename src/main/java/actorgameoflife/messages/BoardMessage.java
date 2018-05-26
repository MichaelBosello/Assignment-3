package actorgameoflife.messages;

import actorgameoflife.board.Board;

import java.io.Serializable;

public class BoardMessage implements Serializable {

    private final Board board;
    private final int livingCell;

    public BoardMessage(Board board, int livingCell){
        this.board = board;
        this.livingCell = livingCell;
    }

    public Board getBoard(){
        return board;
    }

    public int getLivingCell() {
        return livingCell;
    }
}
