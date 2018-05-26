package actorgameoflife.messages;

import java.io.Serializable;

public class DimensionMessage implements Serializable {

    private final int width;
    private final int height;

    public DimensionMessage(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
