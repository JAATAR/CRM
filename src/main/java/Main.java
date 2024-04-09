import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {


        // we create a ScheduledExecutorService to schedule the heartbeats to be sent within an interval
        ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1); // use of one thread to execute the scheduled heartbeats

        // we schedule the task to send heartbeats every second
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                Heartbeat heartbeat = new Heartbeat();
                heartbeat.sendHeartbeat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);

    }
}


