package actorgameoflife.gameoflifegui.mainpanel;

import java.awt.image.BufferedImage;

public interface GameOfLifeGUI {

    void updateBoard(BufferedImage boardImage);

    void updateLivingCellLabel(int livingCell);

    void addObserver(MainPanelObserver observer);

}
