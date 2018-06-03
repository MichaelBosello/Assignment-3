package distributedchat;

import akka.actor.*;
import akka.remote.RemoteScope;
import distributedchat.client.ChatActor;
import distributedchat.utility.NetworkUtility;

public class ClientMain {

    public static void main(String[] args) {
        String ip = NetworkUtility.getLanOrLocal();
        int port = NetworkUtility.findNextAviablePort(ip, NetworkUtility.CHAT_BASE_PORT);

        ActorSystem system = ActorSystem.create("chat");
        Address addr = new Address("akka.tcp", "chat", ip, port);
        system.actorOf(Props.create(ChatActor.class).withDeploy(
                new Deploy(new RemoteScope(addr))));

        System.out.println("Client on " + ip + ":" + port);
    }
}
