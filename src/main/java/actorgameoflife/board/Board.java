package actorgameoflife.board;

public interface Board {

    enum BoardType { RANDOM, LWSS, GLIDER }

    @FunctionalInterface
    interface cellIterator {
        void doForEachCell(int row, int column);
    }

    void iterateCell(cellIterator toPerform);

    void iterateSubCell(int startRow, int startColumn, int endRow, int endColumn, cellIterator toPerform);

    boolean isCellAlive(int row, int column);

    int getColumn();

    int getRow();

}
