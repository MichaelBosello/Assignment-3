package actorgameoflife.messages;

import java.io.Serializable;

public class CellMessage implements Serializable {
    private final boolean alive;
    private final int x;
    private final int y;

    public CellMessage(boolean alive, int x, int y){
        this.alive = alive;
        this.x = x;
        this.y = y;
    }

    public boolean isAlive(){
        return alive;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
