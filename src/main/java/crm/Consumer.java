package crm;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.rabbitmq.client.*;
import crm.Business;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Consumer {
    private final String CONSUMING_QUEUE = System.getenv("CONSUMING_QUEUE");
    private final String EXCHANGE = System.getenv("EXCHANGE");

    private final String  ROUTINGKEY_USER= System.getenv("ROUTINGKEY_USER");
    private final String ROUTINGKEY_CONSUMPTION = System.getenv("ROUTINGKEY_CONSUMPTION");
    private final String ROUTINGKEY_BUSINESS = System.getenv("ROUTINGKEY_BUSINESS");
    private final String HOST = System.getenv("DEV_HOST");
    private final String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
    private final String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
    private final int RABBITMQ_PORT = Integer.parseInt(System.getenv("RABBITMQ_PORT"));

    private Channel channel;

    //we create a connection within the constructor
    public Consumer() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(RABBITMQ_USERNAME);
        factory.setPassword(RABBITMQ_PASSWORD);
        factory.setPort(RABBITMQ_PORT);

        try{
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(CONSUMING_QUEUE, false, false, false, null);
            channel.queueBind(CONSUMING_QUEUE, EXCHANGE, ROUTINGKEY_USER);
            channel.queueBind(CONSUMING_QUEUE, EXCHANGE, ROUTINGKEY_BUSINESS);
            channel.queueBind(CONSUMING_QUEUE, EXCHANGE, ROUTINGKEY_CONSUMPTION);


        }catch (Exception e){

            e.getMessage();
            e.printStackTrace();
        }

        startConsuming();

    }


    public void startConsuming() throws IOException {
        // instance of Defaultconsumer + create an innerclass to customize handleDelivery at instantiation
        DefaultConsumer consumer = new DefaultConsumer(channel) {

            //callback method from rabbbitmq client that handles messages sent to the consumer
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8"); //convert byte array in string
                System.out.println(" [x] Received '" + message + "'");
                String xsd = "src/main/resources/include.template.xsd";

   //if(!validateXML(message, xsd)){
     //  System.out.println("XML is not valid. Skipping processing.");
    //   return; // stop further processing
   //}

                //System.out.println("validation succesful");
                try {

                    if(message.contains("<participant>")){
                        Participant participant1 = (Participant) unmarshalParticipant(message);
                        System.out.println(participant1.toString());
                    }

                    else if(message.contains("access_code")){
                        Business business1 = (Business) unmarshalBusiness(message);
                        System.out.println(business1.toString());

                    }else if (message.contains("<consumption>")){
                        Consumption consumption1 = (Consumption) unmarshalConsumption(message);
                        System.out.println(consumption1.toString());
                    }


                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }

        };

        // start consuming messages from the queue
        channel.basicConsume(CONSUMING_QUEUE, true, consumer);
        channel.basicConsume(CONSUMING_QUEUE, true, consumer);
        channel.basicConsume(CONSUMING_QUEUE, true, consumer);
    }
    // Unmarshal XML to corresponding objects based on its type
    public Object unmarshalBasedOnType(String xml) throws JAXBException {
        JAXBContext jaxbContext = null;
        Unmarshaller jaxbUnmarshaller = null;

        if (xml.contains("<participant")) {
            jaxbContext = JAXBContext.newInstance(Participant.class);
        } else if (xml.contains("<business")) {
            jaxbContext = JAXBContext.newInstance(Business.class);
        }else if(xml.contains("<consumption")) {
            jaxbContext = JAXBContext.newInstance(Consumption.class);
        }

        if (jaxbContext != null) {
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
            return jaxbUnmarshaller.unmarshal(inputStream);
        }

        return null;
    }
    // Unmarshall crm.Participant-object van XML-string
    public Participant unmarshalParticipant(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Participant.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return (Participant) jaxbUnmarshaller.unmarshal(inputStream);

    }
    //Unmarshall crm.Consumption-object van XML-string
    public Consumption unmarshalConsumption(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Consumption.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return (Consumption) jaxbUnmarshaller.unmarshal(inputStream);
    }

    // Unmarshall crm.Business-object van XML-string
    public Business unmarshalBusiness(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Business.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return (Business) jaxbUnmarshaller.unmarshal(inputStream);
    }

    //validate xml

    public static boolean validateXML(String xml, String xsdPath) {

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); //instance of schemafactory for xml validation
            Schema schema = factory.newSchema(new File(xsdPath)); //instance of schema by parsing the xsd file

            Validator validator =schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml))); //validating the xml against the xsd using streamsource object created from stringreader containing the xml
        }catch (IOException | SAXException e){
            System.out.println("Exception" + e.getMessage());
            return false;
        }

        return true;
    }
    public static ForceApi connectToSalesforce() {
        String SALESFORCE_USERNAME = "ehberasmus@gmail.com";
        String SALESFORCE_PASSWORD = "Event5431";
        String SALESFORCE_SECURITY_TOKEN = "S4lOdXADEdHNLYorrabi2mLg";
        String LOGIN_URL = "https://ehb-dev-ed.develop.my.salesforce.com";
        String CONSUMER_KEY = "3MVG9PwZx9R6_Urc1GPWYVjQmwHmXKY1pQ8t_W_Ql4VXOFeo_9tKJW3O8nLf0JJoMjrOuii6wZ8XdpCcJfOOA";
        String CONSUMER_SECRET = "BA7D5B9E3434948E1751C3C5B51BC366B8FD9165E3DCBD95A62AEF4D06B5C4C9";

        // Combineer wachtwoord en beveiligingstoken
        String loginPassword = SALESFORCE_PASSWORD + SALESFORCE_SECURITY_TOKEN;

        // Configureer de API-configuratie
        ApiConfig config = new ApiConfig()
                .setClientId(CONSUMER_KEY)
                .setClientSecret(CONSUMER_SECRET)
                .setUsername(SALESFORCE_USERNAME)
                .setPassword(loginPassword)
                .setLoginEndpoint(LOGIN_URL);

        ForceApi api = new ForceApi(config);
//            createDeelnemer(api);
//            createBusiness(api);
//            createConsumption(api);
        return api;

    }

    public static void createDeelnemer() {
        ForceApi api = connectToSalesforce();

        Map<String, Object> deelnemerFields = new HashMap<>();
        deelnemerFields.put("Name", "marcelo");
        deelnemerFields.put("Leeftijd__c", 32);
        deelnemerFields.put("Phone__c", "0485009999");
        deelnemerFields.put("Email__c", "marcelo@gmail.com");
        deelnemerFields.put("Bedrijf__c", "real madrid");

        // Maak de Deelnemer aan in Salesforce
        api.createSObject("Deelnemer__c", deelnemerFields);
    }

    public static void createBusiness() {
        ForceApi api = connectToSalesforce();
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("Name", "business soso");
        businessFields.put("VAT__c", "123456999");
        businessFields.put("Email__c", "soso@bedrijf.com");
        businessFields.put("Access_Code__c", 2455);
        businessFields.put("Address__c", "sosostraat 128");

        // Maak het Business object aan in Salesforce
        api.createSObject("Business__c", businessFields);
    }

    public static void createConsumption() {
        ForceApi api = connectToSalesforce();
        Map<String, Object> consumptionFields = new HashMap<>();
        consumptionFields.put("Timestamp__c", new Date());
        consumptionFields.put("Name", "food");
        consumptionFields.put("Products__c", "hotdog");
        consumptionFields.put("Consumer__c", "het is lekker "); // Voorbeeld UUID

        // Maak het Consumption object aan in Salesforce
        api.createSObject("Consumption__c", consumptionFields);
    }


}





