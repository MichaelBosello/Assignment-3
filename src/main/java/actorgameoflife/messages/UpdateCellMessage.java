package actorgameoflife.messages;

import java.io.Serializable;

public class UpdateCellMessage implements Serializable {
    private final boolean alive;

    public UpdateCellMessage(boolean alive){
        this.alive = alive;
    }

    public boolean isAlive(){
        return alive;
    }

}
