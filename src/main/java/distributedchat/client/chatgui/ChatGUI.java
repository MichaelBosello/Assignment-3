package distributedchat.client.chatgui;

public interface ChatGUI {

    void newMessage(String message);

    void connected();

    void connectionError(String error);

    void addObserver(ChatObserver observer);

}
