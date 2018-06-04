package distributedchat.client.chatgui;

import akka.actor.AbstractActor;
import distributedchat.client.messages.chatprotocol.NextMessage;
import distributedchat.client.messages.fromtogui.*;

import javax.swing.*;

public class ChatGUIActor extends AbstractActor {

    private final ChatGUI gui;

    public ChatGUIActor() {
        gui = new DChatGUI();
        gui.addObserver(new ChatObserver() {
            @Override
            public void joinEvent(String host) {
                getContext().parent().tell(new ConnectRequestMessage(host), getSelf());
            }

            @Override
            public void leaveEvent() {
                getContext().parent().tell(new LeaveRequestMessage(), getSelf());
            }

            @Override
            public void sendEvent(String message) {
                getContext().parent().tell(new SendMessage(message), getSelf());
            }
        });
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(NextMessage.class, msg -> {
            SwingUtilities.invokeLater( () -> gui.newMessage(msg.getMessage()));
        }).match(ConnectionResultMessage.class, msg -> {
            if(msg.isSuccess()){
                SwingUtilities.invokeLater(gui::connected);
            }else{
                SwingUtilities.invokeLater( () -> gui.connectionError(msg.getError()) );
            }
        }).build();
    }
}
