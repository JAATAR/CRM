package crm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.annotation.*;
import org.glassfish.jaxb.runtime.marshaller.NamespacePrefixMapper;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeoutException;

//annotation are part of jaxb
@XmlRootElement(name = "heartbeat", namespace = "http://ehb.local")
@XmlType(propOrder = {"service", "timestamp", "error", "status"})
public class Heartbeat {
    private Service service;
    private String timestamp;
    private int error;
    private String status;

    private final String QUEUE_NAME_HEARTBEAT = System.getenv("QUEUE_NAME_HEARTBEAT");
    private final String HOST = System.getenv("DEV_HOST");
    private final String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
    private final String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
    private final int RABBITMQ_PORT = Integer.parseInt(System.getenv("RABBITMQ_PORT"));

    public Heartbeat() throws Exception {
        this.service = new Service();
        this.service.setName("crm");
        this.timestamp = generateTimestamp();

        if (isSalesforceAvailable()){
            this.status = "up";
            this.error = 1;
        }else {
            this.error = 550;
            this.status = "down";
        }

    }

    @XmlElement(name = "service", namespace = "http://ehb.local")
    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    @XmlElement(name = "timestamp", namespace = "http://ehb.local")
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @XmlElement(name = "error", namespace = "http://ehb.local")
    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }
    @XmlElement(name = "status", namespace = "http://ehb.local")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String createXML() throws JAXBException{

        System.out.println("calling createXML");
        JAXBContext context = JAXBContext.newInstance(Heartbeat.class); //create a jaxb context for the heartbeat class to use the jaxb api
        Marshaller marshaller = context.createMarshaller();//marshaller converts an object to xml
        System.out.println("xml is creating");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// format the xml for better readability

        //we collect the output to a stringwriter so we can turn the marshaller xml into a string
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(this, stringWriter);

        String xmlString = stringWriter.toString();
        xmlString = xmlString.replaceAll("ns1:", ""); //get rid of jaxb namespace
        xmlString = xmlString.replaceAll("xmlns:ns1=\"http://ehb.local\">", "xmlns=\"http://ehb.local\">");
        xmlString = xmlString.replaceAll("<error>1</error>", "");// if there is no error monitoring doesnt need it

       // System.out.println(xmlString); // Print the XML
        System.out.println(xmlString);
        return xmlString;
    }
    public void sendHeartbeat() throws Exception {
        System.out.println("calling send heartbeat");
        String xsd = "src/main/validation/tests/include.template.xsd";


        //create a connectionfactory and set the host on which rabbitmq runs
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(RABBITMQ_USERNAME);
        factory.setPassword(RABBITMQ_PASSWORD);
        factory.setPort(RABBITMQ_PORT);
        System.out.println("connection made");

        try{
            //create a connection with the server and a channel where we communicate through
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            //create a queue before publishing to it, this line will be ignored if the queue already exists
            channel.queueDeclare(QUEUE_NAME_HEARTBEAT,false,false,false,null);

            // create an xml document
            String xml = createXML();

            //validate the xml against the xsd
            if (!validateXML(xml,xsd)){

                System.out.println("XML validation failed. crm.Heartbeat not sent");
                return; // if validation fails the method stops and heartbeat is not sent
            }

            System.out.println("XML validation succesful");

            //convert it to byte array to send to the exchange
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new StreamSource(new StringReader(xml)),new StreamResult(byteArrayOutputStream));

            byte [] xmlBytes = byteArrayOutputStream.toByteArray();

            //xml sent to the exchange
            channel.basicPublish("", QUEUE_NAME_HEARTBEAT, null, xmlBytes);
            System.out.println("heartbeat has been sent succesfully");

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

        // Define the desired format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // Format the current date and time using the formatter
        String formattedTimestamp = now.format(formatter);

        return formattedTimestamp;
    }

    //validate xml

    public static boolean validateXML(String xml, String xsdPath) {

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); //instance of schemafactory for xml validation
            Schema schema = factory.newSchema(new File(xsdPath)); //instance of schema by parsing the xsd file

            Validator validator=schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xml))); //validating the xml against the xsd using streamsource object created from stringreader containing the xml
        }catch (IOException | SAXException e){
            System.out.println("Exception" + e.getMessage());
            return false;
        }

        return true;
    }

}

