package actorgameoflife.messages;

import java.io.Serializable;

public class CellMessage implements Serializable {
    private final boolean alive;

    public CellMessage(boolean alive){
        this.alive = alive;
    }

    public boolean isAlive(){
        return alive;
    }
}
