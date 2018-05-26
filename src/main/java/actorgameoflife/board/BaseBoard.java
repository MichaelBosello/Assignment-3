package actorgameoflife.board;

import java.util.Random;

public class BaseBoard implements ManagedBoard {
    protected int row, column;
    protected boolean[][] board;

    public BaseBoard(final int row, final int column) {
        if(row <= 0 || column <= 0){
            throw new IllegalArgumentException();
        }
        this.row = row;
        this.column = column;
        board = new boolean[row][column];
    }

    public BaseBoard(Board board){
        this(board.getRow(), board.getColumn());
        board.iterateCell((row, column) -> this.board[row][column] = board.isCellAlive(row, column));
    }

    public BaseBoard(Board board, int startingRow, int startingColumn, int width, int height){
        this(width, height);
        board.iterateSubCell(startingRow, startingColumn, startingRow+width, startingColumn+height, (row, column) -> this.board[row][column] = board.isCellAlive(row, column));
    }

    @Override
    public void initializeWithRandomState(){
        initializeWithRandomState(System.nanoTime());
    }

    @Override
    public void initializeWithRandomState(long seed){
        final Random randomForBoard = new Random();
        randomForBoard.setSeed(seed);
        iterateCell((row, column) -> board[row][column] = randomForBoard.nextBoolean());
    }

    @Override
    public void iterateCell(cellIterator toPerform){
        iterateSubCell(0, 0, getRow(), 0, toPerform);
    }

    @Override
    public void iterateSubCell(int startRow, int startColumn, int endRow, int endColumn, cellIterator toPerform) {
        int row;
        int column;
        int boardColumn = startRow == endRow ? endColumn : getColumn();

        for (column = startColumn; column < boardColumn; column++){
            toPerform.doForEachCell(startRow, column);
        }
        for (row = startRow + 1; row < endRow; row++){
            for (column = 0; column < boardColumn; column++){
                toPerform.doForEachCell(row, column);
            }
        }
        for (column = 0; column < endColumn; column++){
            toPerform.doForEachCell(endRow, column);
        }
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public boolean isCellAlive(final int row, final int column){
        return board[row][column];
    }

    @Override
    public void setAlive(final int row, final int column){
        board[row][column] = true;
    }

    @Override
    public void setDead(final int row, final int column){
        board[row][column] = false;
    }
}
