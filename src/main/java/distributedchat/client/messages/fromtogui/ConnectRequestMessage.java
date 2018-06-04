package distributedchat.client.messages.fromtogui;

import java.io.Serializable;

public class ConnectRequestMessage implements Serializable {

    private final String host;

    public ConnectRequestMessage(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
