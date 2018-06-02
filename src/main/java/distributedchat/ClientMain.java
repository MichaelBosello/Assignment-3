package distributedchat;

import akka.actor.*;
import akka.remote.RemoteScope;
import distributedchat.client.ChatActor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientMain {

    private final static int BASE_PORT = 2553;

    public static void main(String[] args) {

        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        int port = BASE_PORT;
        boolean portFound = false;
        while(!portFound) {
            try (Socket clientSocket = new Socket(ip, port)) {
                portFound = true;
            } catch (IOException e) {
                port++;
            }
        }


        ActorSystem system = ActorSystem.create("chat");
        Address addr = new Address("akka.tcp", "chat", ip, port);
        system.actorOf(Props.create(ChatActor.class).withDeploy(
                new Deploy(new RemoteScope(addr))));

        System.out.println("Client on " + ip + ":" + port);
    }
}
