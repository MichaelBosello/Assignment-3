package actorgameoflife.messages.gui;

import java.io.Serializable;

public class ScrollMessage implements Serializable {

    private final int x;
    private final int y;

    public ScrollMessage(int x, int y) {
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
