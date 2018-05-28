package akkaswingreactivitytest;

import akka.actor.ActorSystem;
import akka.actor.Props;

import javax.swing.*;
import java.awt.*;

public class Main {
    /***
     *
     * TEST
     * AKKA STARVE EDT
     *
     * (1) Actors are executed in their own thread pool and EDT has a greater priority level
     * (2) Actors and JFrame are in no way linked
     * Nevertheless the JFrame slows down or even blocks if there is many messages (Test A) or many Actors (Test B)
     *
     *
     * NOTE: If actors access System.out EDT DOESN'T starve.
     *
     ***/

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + " Priority: " + Thread.currentThread().getPriority());
        new TestFrame();
        ActorSystem system = ActorSystem.create("MySystem");


        /*
        *
        * Test A
        *
        * Only two actor witch duplicate every message exchanged
        *
        * Pinger -> Ponger
        * Pinger <- Ponger
        *
        * Pinger -> Ponger
        * Pinger -> Ponger
        * Pinger <- Ponger
        * Pinger <- Ponger
        *
        * Pinger -> Ponger
        * Pinger -> Ponger
        * Pinger -> Ponger
        * Pinger <- Ponger
        * Pinger <- Ponger
        * Pinger <- Ponger
        *
        * And so on..
        *
        * with 2*1 factor EDT slows and eventually blocks
        * with 8*8 factor EDT blocks immediately (then, not really lot of messages are needed for blocks..)
        *
        *
        * */

        system.actorOf(Props.create(PingActor.class));



        /*
        *
        * Test B
        *
        * 1'000'000 actors exchange request response
        *
        * Or you can use less actors and send more message in preStart (so they not reproduce)
        *
        * */

        /*for(int i = 0; i < 1000000; i++)
            system.actorOf(Props.create(PingActor.class));*/

        System.out.println("All created");

    }

    public static class TestFrame extends JFrame
    {

        /*
        *
        * simply frame to test scrollbar/button reactivity
        *
        * */
        public TestFrame()
        {
            JButton tryme = new JButton("Try me!");

            JScrollBar horizontalScroller = new JScrollBar(JScrollBar.HORIZONTAL);
            horizontalScroller.setMinimum (0);
            horizontalScroller.setMaximum(1000);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(tryme, BorderLayout.CENTER);
            getContentPane().add(horizontalScroller, BorderLayout.PAGE_END);
            pack();
            setVisible(true);
        }
    }



}
