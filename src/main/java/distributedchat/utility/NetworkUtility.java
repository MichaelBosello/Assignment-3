package distributedchat.utility;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkUtility {

    public final static int CHAT_SERVER_PORT = 2552;
    public final static int CHAT_BASE_PORT = 2553;

    public static String getLANIP(){
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static String getLanOrLocal(){
        return getLANIP() != null? NetworkUtility.getLANIP() : "127.0.0.1";
    }

    public static int findNextAviablePort(String ip, int port){
        boolean portFound = false;
        while(!portFound) {
            try (Socket clientSocket = new Socket(ip, port)) {
                portFound = true;
            } catch (IOException e) {
                port++;
            }
        }
        return port;
    }

    public static String ipPortConcat(String ip, int port){
        return ip + ":" + port;
    }
}
