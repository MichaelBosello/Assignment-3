package actorgameoflife.messages;

import java.io.Serializable;

public class BoardMessage implements Serializable {

    protected boolean[][] board;

    public BoardMessage(boolean[][] board){
        this.board = board;
    }

    public boolean isCellAlive(int x, int y){
        return board[x][y];
    }

}
