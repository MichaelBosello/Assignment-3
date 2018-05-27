package actorgameoflife.gameoflifegui.mainpanel;

public interface MainPanelObserver {

    void startEvent();

    void stopEvent();

    void boardUpdated();

    void scrollEvent(int x, int y);
}
