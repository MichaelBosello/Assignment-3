package distributedchat;

import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Deploy;
import akka.actor.Props;
import akka.remote.RemoteScope;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributedchat.server.PeerRegistryActor;
import distributedchat.utility.NetworkUtility;

import java.io.File;

public class ServerMain {



    public static void main(String[] args) {

        String ip = NetworkUtility.getLanOrLocal();
        Config config = ConfigFactory.parseFile(new File("src/main/resources/min-remote.conf"));
        ActorSystem system = ActorSystem.create("chat", config);
        Address addr = new Address("akka.tcp", "chat", ip, NetworkUtility.CHAT_SERVER_PORT);
        system.actorOf(Props.create(PeerRegistryActor.class).withDeploy(
                new Deploy(new RemoteScope(addr))), "registry");

        System.out.println("Server on " + NetworkUtility.ipPortConcat(ip, NetworkUtility.CHAT_SERVER_PORT));
    }
}
