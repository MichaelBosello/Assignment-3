package distributedchat.client.messages.fromtogui;

import java.io.Serializable;

public class NextMessage implements Serializable {
    private final String message;

    public NextMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
