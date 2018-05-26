package actorgameoflife.messages;

import java.io.Serializable;

public class BoardRequestMessage implements Serializable {

    private int x;
    private int y;

    public BoardRequestMessage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
