package actorgameoflife.mailbox;

import actorgameoflife.messages.BoardMessage;
import actorgameoflife.messages.BoardRequestMessage;
import actorgameoflife.messages.DimensionMessage;
import actorgameoflife.messages.gui.BoardUpdatedMessage;
import actorgameoflife.messages.gui.ScrollMessage;
import actorgameoflife.messages.gui.StartMessage;
import actorgameoflife.messages.gui.StopMessage;
import akka.actor.ActorSystem;
import akka.dispatch.PriorityGenerator;
import akka.dispatch.UnboundedStablePriorityMailbox;
import com.typesafe.config.Config;

public class GUIPriorityMailbox extends UnboundedStablePriorityMailbox {

    // needed for reflective instantiation
    public GUIPriorityMailbox(ActorSystem.Settings settings, Config config) {
        // Create a new PriorityGenerator, lower prio means more important
        super(new PriorityGenerator() {
            @Override
            public int gen(Object message) {
                if (message instanceof StartMessage || message instanceof StopMessage)
                    return 0;// 'highpriority messages should be treated first if possible
                else if (message instanceof ScrollMessage ||
                        message instanceof BoardMessage ||
                        message instanceof BoardRequestMessage ||
                        message instanceof BoardUpdatedMessage ||
                        message instanceof DimensionMessage) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });
    }
}
