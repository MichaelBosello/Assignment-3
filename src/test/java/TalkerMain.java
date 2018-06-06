import akka.actor.*;
import akka.remote.RemoteScope;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributedchat.ClientMain;
import distributedchat.client.ChatActor;
import distributedchat.client.messages.fromtogui.ConnectRequestMessage;
import distributedchat.client.messages.fromtogui.SendMessage;
import distributedchat.utility.NetworkUtility;

import java.io.File;

import static java.lang.Thread.sleep;

public class TalkerMain{

    private final static int N_MESSAGE = 200;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("talker process started");
        ActorRef client = ClientMain.deployClientActor();
        String id = args.length > 0 ? args[0] : client.toString();
        sleep(1000);
        client.tell(
                new ConnectRequestMessage((
                        NetworkUtility.ipPortConcat(
                                NetworkUtility.getLanOrLocal(), NetworkUtility.CHAT_SERVER_PORT))), null);
        sleep(20000);
        for(int i = 0; i < N_MESSAGE; i++) {
            if(i == 150){
                client.tell(new SendMessage(":enter-cs"), null);
            }
            client.tell(new SendMessage("User" + id + ": message" + i), null);
            sleep(200);
        }
    }
}
