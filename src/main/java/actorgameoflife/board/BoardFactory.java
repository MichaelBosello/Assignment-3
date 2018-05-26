package actorgameoflife.board;

public class BoardFactory {

    private BoardFactory() {}

    public static Board createSimpleBoard(int row, int column){
        return createRandomBoard(row, column);
    }

    public static Board createRandomBoard(int row, int column){
        ManagedBoard board = new BaseBoard(row, column);
        board.initializeWithRandomState();
        return board;
    }

    public static Board createRandomBoard(int row, int column, long seed){
        ManagedBoard board = new BaseBoard(row, column);
        board.initializeWithRandomState(seed);
        return board;
    }

    public static Board createEmptyBoard(int row, int column){
        ManagedBoard board = new BaseBoard(row, column);
        board.iterateCell((r, c) -> board.setDead(r, c));
        return board;
    }

    public static Board createCopyBoard(Board board){
        return new BaseBoard(board);
    }

    public static Board createSubBoard(Board board, int x, int y, int width, int height){
        return new BaseBoard(board, x, y, width, height);
    }

    public static Board createLWSS(ManagedBoard board, int row, int column){
        board.setAlive(row,column);
        board.setAlive(row,column+3);
        board.setAlive(row+1,column+4);
        board.setAlive(row+2,column);
        board.setAlive(row+2,column+4);
        board.setAlive(row+3,column+1);
        board.setAlive(row+3,column+2);
        board.setAlive(row+3,column+3);
        board.setAlive(row+3,column+4);
        return board;
    }

    public static Board createLotOfLWSS(int row, int column){
        ManagedBoard board = (ManagedBoard) createEmptyBoard(row, column);
        for(int i = 0; i < row; i += 20){
            for(int k = 0; k < column; k += 20){
                createLWSS(board, i, k);
            }
        }
        return board;
    }

    public static Board createGlider(ManagedBoard board, int row, int column){
        board.setAlive(row,column+1);
        board.setAlive(row+1,column+2);
        board.setAlive(row+2,column);
        board.setAlive(row+2,column+1);
        board.setAlive(row+2,column+2);
        return board;
    }

    public static Board createLotOfGlider(int row, int column){
        ManagedBoard board = (ManagedBoard) createEmptyBoard(row, column);
        for(int i = 0; i < row; i += 10){
            for(int k = 0; k < column; k += 10){
                createGlider(board, i, k);
            }
        }
        return board;
    }
}
