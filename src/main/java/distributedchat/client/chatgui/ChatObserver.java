package distributedchat.client.chatgui;

public interface ChatObserver {

    void joinEvent(String ip);

    void leaveEvent();

    void sendEvent(String message);
}
