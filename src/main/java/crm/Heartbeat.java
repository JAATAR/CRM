
package crm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.json.JSONUtil;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.annotation.*;
import org.xml.sax.SAXException;
import java.time.Instant;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

//annotation are part of jaxb
@XmlRootElement(name = "heartbeat", namespace = "http://ehb.local")
@XmlType(propOrder = {"service", "timestamp", "error", "status"})
public class Heartbeat {
    private String service;
    private int timestamp;
    private String error;
    private String status;
    private Timer timer;

    private final String QUEUE_NAME_HEARTBEAT = System.getenv("QUEUE_NAME_HEARTBEAT");
    private final String HOST = System.getenv("DEV_HOST");
    private final String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
    private final String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
    private final int RABBITMQ_PORT = Integer.parseInt(System.getenv("RABBITMQ_PORT"));

    public Heartbeat() throws Exception {
        setService("crm");

        this.timer = new Timer();
    this.timer.schedule(new HeartbeatTask(this), 0, 5000);
        if (isSalesforceAvailable()){
            this.status = "up";
            this.error = "1";
        }else {
            this.error = "550";
            this.status = "down";
        }

    }

    @XmlElement(name = "service", namespace = "http://ehb.local")
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @XmlElement(name = "timestamp", namespace = "http://ehb.local")
    public int getTimestamp() {
        return timestamp;

    }



    @XmlElement(name = "error", namespace = "http://ehb.local")
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    @XmlElement(name = "status", namespace = "http://ehb.local")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    private static class HeartbeatTask extends TimerTask {
        private final Heartbeat heartbeat;

        public HeartbeatTask(Heartbeat heartbeat) {
            this.heartbeat = heartbeat;
        } @Override
        public void run() {
            try {
                if (false) {
                    heartbeat.setError(heartbeat.getError());
                    heartbeat.setStatus("down");
                } else {
                    heartbeat.setStatus("up");
                    heartbeat.setError("");
                }
                heartbeat.sendHeartbeat();

            } catch (Exception e) {
                heartbeat.setError((String.valueOf(e)));
                e.printStackTrace();
            }
        }}


    public String createXML() throws JAXBException{
        String xsd = "src/main/resources/include.template.xsd";

        System.out.println("calling createXML");


        String realXml = "<heartbeat xmlns=\"http://ehb.local\">" +
                "<service>" + this.getService() + "</service>" +
                "<timestamp>" + this.getTimestamp() + "</timestamp>" +
                "<status>" + this.getStatus() + "</status>" +
                "<error>" + this.getError() + "</error>" +
                "</heartbeat>";

       // if (!validateXML(realXml,xsd)){

        //    System.out.println("XML validation failed. crm.Heartbeat not sent");
      //      return null; // if validation fails the method stops and heartbeat is not sent
     //   }

      //  System.out.println("validation succesful");

        if (this.getError() == "1"){

            realXml = realXml.replace("<error>" + this.getError() + "</error>","<error></error>");
        }

        realXml = realXml.replace("xmlns=\"http://ehb.local\"", "");
        realXml = realXml.replaceAll("<heartbeat\\s+", "<heartbeat");

        // Print the XML
        //System.out.println(realXml);


        return realXml;
    }
    public void sendHeartbeat() throws Exception {
        this.timestamp = (int) (Instant.now().getEpochSecond());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            String message = "<heartbeat>" +
                    "<service>" + this.getService() + "</service>" +
                    "<timestamp>" + this.getTimestamp() + "</timestamp>" +
                    "<status>" + this.getStatus() + "</status>" +
                    "<error>" + this.getError() + "</error>" +
                    "</heartbeat>";
            channel.basicPublish("", "heartbeat_queue", null, message.getBytes("UTF-8"));

        }catch(IOException | TimeoutException e){
            System.out.println("heartbeat was not sent due to error");
            e.printStackTrace();

        }
    }


    public static boolean isSalesforceAvailable() throws Exception {

        String endpoint = "https://erasmushogeschool7-dev-ed.develop.lightning.force.com/lightning/page/home"; //url of our salesforce instance

        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //casting to access the http urlconnection functionality
            connection.setRequestMethod("GET"); // send a GET request to the url to see if there is an answer

            int responseCode = connection.getResponseCode(); // retrieve the response code
            if (responseCode == 200) {
                System.out.println("Salesforce is available. Response code: " + responseCode);
                return true;
            } else {
                System.out.println("Salesforce ping failed. Response code: " + responseCode);
                return false;
            }
        } catch (IOException e) {
            System.out.println("Exception occurred: " + e.getMessage());
            return false;
        }
    }

    //create a timestamp
    private long getCurrentTimestamp(){
        return System.currentTimeMillis() / 1000; // Convert milliseconds to seconds
    }

    // New method to generate timestamp
    private String generateTimestamp() {
        // Get current date and time
        LocalDateTime now = LocalDateTime.now();

        // Convert LocalDateTime to epoch time in seconds
        long epochSeconds = now.toEpochSecond(ZoneOffset.UTC);

        // Convert epoch time to string
        String epochString = String.valueOf(epochSeconds);

        return epochString;
    }

}

