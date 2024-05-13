package crm;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {

        // Create a ScheduledExecutorService to schedule the heartbeats to be sent within an interval
        ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1); // Use one thread to execute the scheduled heartbeats

        // Schedule the task to send heartbeats every second
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                Heartbeat heartbeat = new Heartbeat();
                heartbeat.sendHeartbeat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 5, TimeUnit.SECONDS);

        //Create an ExecutorService to manage concurrent execution of the Consumer task
       ExecutorService executor = Executors.newCachedThreadPool();

        // Start the Consumer task
        executor.execute(() -> {
            try {
                Consumer consumer = new Consumer();
                consumer.startConsuming();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Salesforce salesforce = new Salesforce();
        // Thread for continuously checking for new users in Salesforce
        executor.execute(() -> {

            salesforce.continuouslyCheckForNewUsers("Deelnemer__c");
        });

        executor.execute(()->{
            salesforce.continuouslyCheckForNewUpdatedUsers("gewijzigde_Deelnemer__c");
        });

        executor.execute(()->{
            salesforce.continuouslyCheckForDeletedUsers("delete_deelnemer__c");
        });

        // Shutdown the executor when no longer needed
        executor.shutdown();
    }

    }

