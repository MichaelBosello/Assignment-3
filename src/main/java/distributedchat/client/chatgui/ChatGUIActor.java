package distributedchat.client.chatgui;

import akka.actor.AbstractActor;

public class ChatGUIActor extends AbstractActor {

    public ChatGUIActor() {
        ChatGUI gui = new DChatGUI();
        gui.addObserver(new ChatObserver() {
            @Override
            public void joinEvent(String ip) {

            }

            @Override
            public void leaveEvent() {

            }

            @Override
            public void sendEvent(String message) {

            }
        });
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
