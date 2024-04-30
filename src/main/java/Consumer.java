import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Consumer {

    private final String host = "10.2.160.9";
    private final String queueName = "inschrijving_crm_queue";
    private final String exchangeName = "inschrijving_exchange";
    private final String routingKey = "inschrijving";

    private Channel channel;

    //we create a connection within the constructor
    public Consumer(){

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        try{
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(queueName, false, false, false, null);
            channel.queueBind(queueName, exchangeName,routingKey); //use of the routing key to bind the queue to the exchange



        }catch (Exception e){

            e.getMessage();
            e.printStackTrace();
        }



    }


    public void startConsuming() throws IOException {
        // instance of Defaultconsumer + create an innerclass to customize handleDelivery at instantiation
        DefaultConsumer consumer = new DefaultConsumer(channel) {

            //callback method from rabbbitmq client that handles messages sent to the consumer
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8"); //convert byte array in string
                System.out.println(" [x] Received '" + message + "'");
            }
        };

        // start consuming messages from the queue
        channel.basicConsume(queueName, true, consumer);
        System.out.println("ready to consumer");
    }
    public void saveToSalesforce(){
        ApiConfig config = new ApiConfig().setClientId("3MVG9PwZx9R6_UrfopP9UuSYm9.9btZdAiMG6rKyTdaV8nUXzfEiZJ9oT9XyY4lKvsxSv0W9L28QibW7MWtmD")
                .setClientSecret("8F01848AA8E6016D0D1EEA3DC0BA2C0B270C0EF3106DFFAEE09CC384F058B10C")
                .setUsername("ehbevent")
                .setPassword("Event5431");
        ForceApi api = new ForceApi(config);
        // Create a Java object representing your dataCustomObject
        Customobject customObject = new Customobject("Test Object", "This is a test object created from Java.");
        // Convert the Java object to a Map for Salesforce API
        Map<String, Object> objectMap = new HashMap<>();
         objectMap.put("Name__c", customObject.getName());
        objectMap.put("Description__c", customObject.getDescription());
        // Save the object to Salesforce
         api.createSObject("Custom_Object__c", objectMap);
         }


    }








