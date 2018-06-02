package distributedchat.client.messages.fromtogui;

import java.io.Serializable;

public class JoinRequestMessage implements Serializable {

    private final String host;

    public JoinRequestMessage(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
