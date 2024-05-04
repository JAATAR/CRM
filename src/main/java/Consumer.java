import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.rabbitmq.client.*;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;


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



        public void connectToSalesforceAndSendData() {
            String SALESFORCE_USERNAME = "hamza.amghar@student.ehb.be";
            String SALESFORCE_PASSWORD = "Event5431";
            String SALESFORCE_SECURITY_TOKEN = "QdhyxDHRs9hyqkvhKwQfz4aLl";
            String LOGIN_URL = "https://erasmushogeschool7-dev-ed.develop.my.salesforce.com";
            String CONSUMER_KEY = "3MVG9PwZx9R6_UrfopP9UuSYm9.9btZdAiMG6rKyTdaV8nUXzfEiZJ9oT9XyY4lKvsxSv0W9L28QibW7MWtmD";
            String CONSUMER_SECRET = "8F01848AA8E6016D0D1EEA3DC0BA2C0B270C0EF3106DFFAEE09CC384F058B10C";

            // Combineer wachtwoord en beveiligingstoken
            String loginPassword = SALESFORCE_PASSWORD + SALESFORCE_SECURITY_TOKEN;

            // Configureer de API-configuratie
            ApiConfig config = new ApiConfig()
                    .setClientId(CONSUMER_KEY)
                    .setClientSecret(CONSUMER_SECRET)
                    .setUsername(SALESFORCE_USERNAME)
                    .setPassword(SALESFORCE_PASSWORD)
                    .setLoginEndpoint(LOGIN_URL);

            ForceApi api = new ForceApi(config);
            System.out.printf("ik werk ");
            // Maak de gegevens voor de aan te maken Deelnemer
            Map<String, Object> deelnemerFields = new HashMap<>();
            System.out.println("test een ");
            deelnemerFields.put("Name", "Mike Tyson");
            System.out.println("test twee");
            deelnemerFields.put("Leeftijd__c", 25);
            System.out.println("test drie ");
            deelnemerFields.put("Nummertelefoon__c", "0485009987");
            System.out.println("test vier ");
            deelnemerFields.put("Email__c", "miketyson@gmail.com");
            System.out.println("test vijf ");
            deelnemerFields.put("Bedrijf__c", "erasmus");
            System.out.println("test last ");

            // Maak de Deelnemer aan in Salesforce
            api.createSObject("Deelnemer__c", deelnemerFields);
            System.out.println("object createt");
        }


}








