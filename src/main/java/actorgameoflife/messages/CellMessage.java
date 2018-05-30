package actorgameoflife.messages;

import java.io.Serializable;

public class CellMessage implements Serializable {
    private final boolean alive;
    private final int row;
    private final int column;

    public CellMessage(boolean alive, int row, int column){
        this.alive = alive;
        this.row = row;
        this.column = column;
    }

    public boolean isAlive(){
        return alive;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
