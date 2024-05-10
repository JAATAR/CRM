package crm;

import com.force.api.ApiConfig;
import com.force.api.ForceApi;
import com.force.api.QueryResult;
import com.rabbitmq.client.*;
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
import java.util.Objects;

public class Consumer {
    private final String CONSUMING_QUEUE = System.getenv("CONSUMING_QUEUE");
    private final String EXCHANGE = System.getenv("EXCHANGE");

    private final String ROUTINGKEY_USER = System.getenv("ROUTINGKEY_USER");
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

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(CONSUMING_QUEUE, false, false, false, null);
            channel.queueBind(CONSUMING_QUEUE, EXCHANGE, ROUTINGKEY_USER);
            channel.queueBind(CONSUMING_QUEUE, EXCHANGE, ROUTINGKEY_BUSINESS);
            channel.queueBind(CONSUMING_QUEUE, EXCHANGE, ROUTINGKEY_CONSUMPTION);


        } catch (Exception e) {

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

                    if (message.contains("<participant>")) {
                        Participant participant = (Participant) unmarshalParticipant(message);
                        System.out.println(participant.toString());
                        System.out.println("participant unmarshalled");

                        if (Objects.equals(participant.getMethod(), "create")) {
                            createDeelnemer(participant);
                            System.out.println("particpant created");

                        } else if (Objects.equals(participant.getMethod(), "update")) {
                            updateDeelnemer(participant.getUuid(),participant);

                        }else if(Objects.equals(participant.getMethod(), "delete")){
                            //deleteDeelnemer(participant.getUuid());
                            System.out.println("particpant deleted");
                        }

                    } else if (message.contains("access_code")) {
                        Business business1 = (Business) unmarshalBusiness(message);
                        System.out.println(business1.toString());
                        createBusiness(business1);
                        System.out.println("business created");

                    } else if (message.contains("<consumption>")) {
                        Consumption consumption1 = (Consumption) unmarshalConsumption(message);
                        System.out.println(consumption1.toString());
                        createConsumption(consumption1);
                        System.out.println("consumption created");
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

            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml))); //validating the xml against the xsd using streamsource object created from stringreader containing the xml
        } catch (IOException | SAXException e) {
            System.out.println("Exception" + e.getMessage());
            return false;
        }

        return true;
    }

    public ForceApi connectToSalesforce() {
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
        return api;

    }

    public void createDeelnemer(Participant participant) {
        ForceApi api = connectToSalesforce();

        Map<String, Object> deelnemerFields = new HashMap<>();
        deelnemerFields.put("Name", participant.getFirstname());
        deelnemerFields.put("familie_naam__c", participant.getLastname());
        deelnemerFields.put("Phone__c", participant.getPhone());
        deelnemerFields.put("Email__c", participant.getEmail());
        deelnemerFields.put("Bedrijf__c", participant.getBusiness());
        deelnemerFields.put("date_of_birth__c", participant.getDateOfBirth());
        deelnemerFields.put("Deelnemer_uuid__c", participant.getUuid());
        deelnemerFields.put("from_business__c",participant.getFromBusiness());

        // Maak de Deelnemer aan in Salesforce
        api.createSObject("Deelnemer__c", deelnemerFields);
    }

    public  void createBusiness(Business business) {
        ForceApi api = connectToSalesforce();
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("Name", business.getName());
        businessFields.put("VAT__c", business.getVat());
        businessFields.put("Email__c", business.getEmail());
        businessFields.put("Access_Code__c", business.getAccessCode());
        businessFields.put("Address__c", business.getAddress());
        //businessFields.put("Bedrijf_uuid__c",business.getUuid());

        // Maak het Business object aan in Salesforce
        api.createSObject("Business__c", businessFields);
    }

    public  void createConsumption(Consumption consumption) {
        ForceApi api = connectToSalesforce();
        Map<String, Object> consumptionFields = new HashMap<>();
        consumptionFields.put("Timestamp__c", new Date());
        consumptionFields.put("Name", "food");
        consumptionFields.put("Products__c", consumption.getProducts());
        consumptionFields.put("Consumer_uuid__c", consumption.getUuid());

        // Maak het Consumption object aan in Salesforce
        api.createSObject("Consumption__c", consumptionFields);
    }

    public void updateDeelnemer(String uuid, Participant updatedParticipant) {
        ForceApi api = connectToSalesforce();

        // Retrieve the Deelnemer__c record based on UUID
        QueryResult<Map<String, Object>> queryResult = retrieveDeelnemerByUUID(api, uuid);

        if (queryResult.getTotalSize() > 0) {
            // Get the first record from the query result
            Map<String, Object> deelnemerRecord = queryResult.getRecords().get(0);

            // Prepare fields to update
            Map<String, Object> updatedFields = new HashMap<>();
            updatedFields.put("Name", updatedParticipant.getFirstname());
            updatedFields.put("familie_naam__c", updatedParticipant.getLastname());
            updatedFields.put("Phone__c", updatedParticipant.getPhone());
            updatedFields.put("Email__c", updatedParticipant.getEmail());
            updatedFields.put("Bedrijf__c", updatedParticipant.getBusiness());
            updatedFields.put("date_of_birth__c", updatedParticipant.getDateOfBirth());

            // Get the record ID
            String deelnemerUuid = (String) deelnemerRecord.get("Deelnemer_uuid__c");

            // Update the Deelnemer in Salesforce using the retrieved ID and updated fields
            api.updateSObject("Deelnemer__c", deelnemerUuid, updatedFields);
            System.out.println("particpant updated");
        } else {
            System.out.println("No Deelnemer record found with UUID: " + uuid);
        }
    }

   /* public  void deleteDeelnemer(String uuid) {
        ForceApi api = connectToSalesforce();
        // Retrieve Deelnemer by UUID
        QueryResult<Map> queryResult = retrieveDeelnemerByUUID(api,uuid);
        if (queryResult.getTotalSize() > 0) {
            String deelnemerId = queryResult.getRecords().get(0).get("Id").toString();
            // Delete the Deelnemer
            api.deleteSObject("Deelnemer__c", deelnemerId);
            System.out.println("Deelnemer deleted successfully.");
        } else {
            System.out.println("Deelnemer not found.");
        }
    }*/

    public QueryResult<Map<String, Object>> retrieveDeelnemerByUUID(ForceApi api, String uuid) {
        // Query the Deelnemer__c record by UUID
        String query = "SELECT Name, familie_naam__c, Phone__c, Email__c, Bedrijf__c, date_of_birth__c, Deelnemer_uuid__c FROM Deelnemer__c WHERE Deelnemer_uuid__c = '" + uuid + "'";

        // Perform the query
        QueryResult<Map> queryResult = api.query(query);

        // Cast the QueryResult to the appropriate generic type
        return (QueryResult<Map<String, Object>>) (QueryResult<?>) queryResult;
    }

    public void showDeelnemer(String givenuuid){
        ForceApi api = connectToSalesforce(); // Assume you have a method to connect to Salesforce

        String uuid = givenuuid;
        QueryResult<Map<String, Object>> queryResult = retrieveDeelnemerByUUID(api, uuid);

        // Print the retrieved records
        System.out.println("Retrieved Deelnemer records:");
        for (Map<String, Object> record : queryResult.getRecords()) {
            System.out.println("Name: " + record.get("Name"));
            System.out.println("Familie Naam: " + record.get("familie_naam__c"));
            System.out.println("Phone: " + record.get("Phone__c"));
            System.out.println("Email: " + record.get("Email__c"));
            System.out.println("Bedrijf: " + record.get("Bedrijf__c"));
            System.out.println("Date of Birth: " + record.get("date_of_birth__c"));
            System.out.println("Deelnemer UUID: " + record.get("Deelnemer_uuid__c"));
            System.out.println("-----------------------------");
        }
    }



    // Method to update a Business__c object
    public  void updateBusiness(Business business) {
        ForceApi api = connectToSalesforce();

        // Retrieve the Business__c record by UUID
        QueryResult<Map> businessQueryResult = retrieveBusinessByUUID(api, business.getUuid());

        // Check if a Business with the provided UUID exists
        if (businessQueryResult.getTotalSize() > 0) {
            // Get the Business__c record ID
            String businessUUID = (String) businessQueryResult.getRecords().get(0).get("Bedrijf_uuid__c");

            // Prepare fields to update
            Map<String, Object> businessFields = new HashMap<>();
            businessFields.put("Name", business.getName());
            businessFields.put("VAT__c", business.getVat());
            businessFields.put("Email__c", business.getEmail());
            businessFields.put("Access_Code__c", business.getAccessCode());
            businessFields.put("Address__c", business.getAddress());

            // Update the Business__c object in Salesforce
            api.updateSObject("Business__c", businessUUID, businessFields);
            System.out.println("Business updated successfully.");
        } else {
            System.out.println("No Business found with UUID " + business.getUuid());

        }
    }

    // Method to delete a Business__c object
    public  void deleteBusinessByUUID(String uuid) {

        ForceApi api = connectToSalesforce();
        // Retrieve the Business__c record by UUID
        QueryResult<Map> businessQueryResult = retrieveBusinessByUUID(api, uuid);

        // Check if a Business with the provided UUID exists
        if (businessQueryResult.getTotalSize() > 0) {
            // Get the Business__c record ID
            String businessUUID = (String) businessQueryResult.getRecords().get(0).get("Bedrijf_uuid__c");

            // Delete the Business__c object in Salesforce
            api.deleteSObject("Business__c", businessUUID);
            System.out.println("Business deleted successfully.");
        } else {
            System.out.println("No Business found with UUID " + uuid);
        }
    }

    public static QueryResult<Map> retrieveBusinessByUUID(ForceApi api, String uuid) {
        // SOQL query to retrieve Business__c record by Bedrijf_uuid__c field
        String query = "SELECT Id, Name, VAT__c, Email__c, Access_Code__c, Address__c FROM Business__c WHERE Bedrijf_uuid__c = '" + uuid + "'";

        // Perform the query
        return api.query(query);
    }



    }









