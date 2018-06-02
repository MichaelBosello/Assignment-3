package distributedchat.client.messages.fromtogui;

import java.io.Serializable;

public class JoinResultMessage implements Serializable {

    private final boolean success;
    private final String error;

    public JoinResultMessage(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}
