import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) throws IOException, JAXBException {


        // we create a ScheduledExecutorService to schedule the heartbeats to be sent within an interval
       //ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1); // use of one thread to execute the scheduled heartbeats

        // we schedule the task to send heartbeats every second
       //heartbeatScheduler.scheduleAtFixedRate(() -> {
        //   try {
            //    Heartbeat heartbeat = new Heartbeat();
             //   heartbeat.sendHeartbeat();
            //} catch (Exception e) {
              //  e.printStackTrace();
           // }
       // }, 0, 5, TimeUnit.SECONDS);

         String test = "<participant id=\"123\">\n" +
                 "    <method>CREATE</method>\n" +
                 "    <firstname>John</firstname>\n" +
                 "    <lastname>Rooney</lastname>\n" +
                 "    <email>john.doe@example.com</email>\n" +
                 "    <phone>1234567890</phone>\n" +
                 "    <business>Assets</business>\n" +
                 "    <age>30</age>\n" +
                 "</participant>";
         String test2 = "<event>\n" +
                 "    <name>Conference</name>\n" +
                 "    <time>2024-05-01T09:00:00</time>\n" +
                 "    <location>New York City</location>\n" +
                 "    <session>\n" +
                 "        <speakers>\n" +
                 "            <participant>\n" +
                 "                <firstname>Alice</firstname>\n" +
                 "                <lastname>Smith</lastname>\n" +
                 "                <email>alice@example.com</email>\n" +
                 "            </participant>\n" +
                 "            <participant>\n" +
                 "                <firstname>Bob</firstname>\n" +
                 "                <lastname>Jones</lastname>\n" +
                 "                <email>bob@example.com</email>\n" +
                 "            </participant>\n" +
                 "        </speakers>\n" +
                 "        <participants>\n" +
                 "            <participant>\n" +
                 "                <firstname>Charlie</firstname>\n" +
                 "                <lastname>Brown</lastname>\n" +
                 "                <email>charlie@example.com</email>\n" +
                 "            </participant>\n" +
                 "            <participant>\n" +
                 "                <firstname>Diana</firstname>\n" +
                 "                <lastname>Lewis</lastname>\n" +
                 "                <email>diana@example.com</email>\n" +
                 "            </participant>\n" +
                 "        </participants>\n" +
                 "    </session>\n" +
                 "</event>\n";
         String test3 = "<business id=\"1\">\n" +
                 "    <name>Acme Corporation</name>\n" +
                 "    <vat>123456789</vat>\n" +
                 "</business>\n";
         String test4 = "<session>\n" +
                 "    <speakers>\n" +
                 "        <participant>\n" +
                 "            <firstname>Alice</firstname>\n" +
                 "            <lastname>Smith</lastname>\n" +
                 "            <email>alice@example.com</email>\n" +
                 "        </participant>\n" +
                 "        <participant>\n" +
                 "            <firstname>Bob</firstname>\n" +
                 "            <lastname>Jones</lastname>\n" +
                 "            <email>bob@example.com</email>\n" +
                 "        </participant>\n" +
                 "    </speakers>\n" +
                 "    <participants>\n" +
                 "        <participant>\n" +
                 "            <firstname>Charlie</firstname>\n" +
                 "            <lastname>Brown</lastname>\n" +
                 "            <email>charlie@example.com</email>\n" +
                 "        </participant>\n" +
                 "        <participant>\n" +
                 "            <firstname>Diana</firstname>\n" +
                 "            <lastname>Lewis</lastname>\n" +
                 "            <email>diana@example.com</email>\n" +
                 "        </participant>\n" +
                 "    </participants>\n" +
                 "</session>\n";
        Consumer consumer = new Consumer();
        consumer.startConsuming();





      /*  Business business = (consumer.unmarshalBusiness(test3));
        System.out.println(business.getVat());
*/
       /*

       Session session = consumer.unmarshalSession(test4);
        for (Participant participant : session.getParticipants()) {
            System.out.println(participant.getFirstname() + " " + participant.getLastname());
            session.printParticipantDetails();
        }

        */



    }
}


