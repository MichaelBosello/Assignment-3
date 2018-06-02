package distributedchat;

import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Deploy;
import akka.actor.Props;
import akka.remote.RemoteScope;
import distributedchat.server.PeerRegistryActor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerMain {

    private final static int SERVER_PORT = 2552;

    public static void main(String[] args) {

        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        ActorSystem system = ActorSystem.create("chat");
        Address addr = new Address("akka.tcp", "chat", ip, SERVER_PORT);
        system.actorOf(Props.create(PeerRegistryActor.class).withDeploy(
                new Deploy(new RemoteScope(addr))), "registry");

        System.out.println("Server on " + ip + ":" + SERVER_PORT);
    }
}
