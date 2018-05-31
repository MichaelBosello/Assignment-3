package actorgameoflife.messages;

import java.io.Serializable;

public class BoardRequestMessage implements Serializable {

    private int startRow;
    private int startColumn;
    private int endRow;
    private int endColumn;

    public BoardRequestMessage(int startRow, int startColumn, int endRow, int endColumn) {
        this.startRow = startRow;
        this.startColumn = startColumn;
        this.endRow = endRow;
        this.endColumn = endColumn;
    }

    public int getStartRow() {
        return startRow;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getEndColumn() {
        return endColumn;
    }
}
