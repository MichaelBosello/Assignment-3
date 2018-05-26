package actorgameoflife.board;

public interface ManagedBoard extends Board {

    void initializeWithRandomState(long seed);

    void initializeWithRandomState();

    void setAlive(int row, int column);

    void setDead(int row, int column);

}
