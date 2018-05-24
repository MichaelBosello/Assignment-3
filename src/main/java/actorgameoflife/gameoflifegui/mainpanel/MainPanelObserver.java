package actorgameoflife.gameoflifegui.mainpanel;

public interface MainPanelObserver {

    void startEvent();

    void stopEvent();

    void boardUpdated(int x, int y);
}
