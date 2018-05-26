package actorgameoflife.messages;

import actorgameoflife.board.Board;

import java.io.Serializable;

public class BoardMessage implements Serializable {

    protected Board board;

    public BoardMessage(Board board){
        this.board = board;
    }

    public Board getBoard(){
        return board;
    }

}
