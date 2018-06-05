import distributedchat.ServerMain;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;

public class ChatTest {

    private final static int N_CLIENT = 3;

    @Test
    public void chatTest() throws InterruptedException, IOException {
        System.out.println(System.getProperty("java.class.path"));

        List<Process> processes = new LinkedList<>();

        Process s = new ProcessBuilder("java", "-cp",
                System.getProperty("java.class.path"),
                ServerMain.class.getCanonicalName()).start();

        StreamGobbler sOutputGobbler = new StreamGobbler(s.getInputStream(), System.out::println);
        StreamGobbler sErrorGobbler = new StreamGobbler(s.getErrorStream(), System.out::println);
        new Thread(sOutputGobbler).start();
        new Thread(sErrorGobbler).start();
        processes.add(s);
        sleep(1000);

        for (int i = 0; i < N_CLIENT; i++) {
            Process p = new ProcessBuilder("java", "-cp",
                    System.getProperty("java.class.path"),
                    TalkerMain.class.getCanonicalName(), String.valueOf(i)).start();

            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), System.out::println);
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), System.out::println);
            new Thread(outputGobbler).start();
            new Thread(errorGobbler).start();
            processes.add(p);

            sleep(1000);
        }

        for(Process p : processes){
            p.waitFor();
        }
    }

    /**
     *
     * https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
     *
     * **/
    class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumeInputLine;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
            this.inputStream = inputStream;
            this.consumeInputLine = consumeInputLine;
        }

        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
        }
    }
}
