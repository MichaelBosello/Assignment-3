package distributedchat;

import akka.actor.*;
import akka.remote.RemoteScope;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributedchat.client.ChatActor;
import distributedchat.utility.NetworkUtility;

import java.io.File;

public class ClientMain {

    public static void main(String[] args) {
        String ip = NetworkUtility.getLanOrLocal();
        int port = NetworkUtility.findNextAviablePort(ip, NetworkUtility.CHAT_BASE_PORT);
        System.out.println("Try connection on " + ip + ":" + port);
        Config config = ConfigFactory.parseFile(new File("src/main/resources/min-remote.conf"));
        Config portConfig =
                ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(config);
        ActorSystem system = ActorSystem.create("client", portConfig);
        Address addr = new Address("akka.tcp", "client", ip, port);
        system.actorOf(Props.create(ChatActor.class).withDeploy(
                new Deploy(new RemoteScope(addr))));

        System.out.println("Client on " + ip + ":" + port);
    }
}
