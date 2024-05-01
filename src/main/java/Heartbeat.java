import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.*;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

//annotation are part of jaxb
@XmlRootElement(name = "heartbeat")
@XmlType(propOrder = {"service", "timestamp", "error", "status"})
public class Heartbeat {

    private String service;
    private long timestamp;
    private String error;
    private String status;

    private String queuName = "heartbeat_queue";
    private String host = "10.2.160.9";

    public Heartbeat() throws Exception {
        this.service = "crm";
        this.timestamp = getCurrentTimestamp();

        if (isSalesforceAvailable()){
            this.error = "none";
            this.status = "up";
        }else {
            this.error = "error";
            this.status = "down";
        }

    }


    @XmlAttribute
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @XmlElement
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @XmlElement
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @XmlElement
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
        //marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CustomNamespacePrefixMapper());

        //we collect the output to a stringwriter so we can turn the marshaller xml into a string
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(this, stringWriter);

        String xmlString = stringWriter.toString();
        System.out.println(xmlString); // Print the XML
        return xmlString;
    }
    public void sendHeartbeat() throws Exception {
        System.out.println("calling send heartbeat");
        String xsd = "src/main/resources/xmlxsd/v0.1.xsd";

        //create a connectionfactory and set the host on which rabbitmq runs
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        System.out.println("connection made");

        try{
            //create a connection with the server and a channel where we communicate through
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(queuName,false,false,false,null);//we declare a queu on the channel(if the queu already exists this line will be ignored)

            // create an xml document
            String xml = createXML();

            //validate the xml against the xsd
            if (!validateXML(xml,xsd)){

                System.out.println("XML validation failed. Heartbeat not sent");
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
            channel.basicPublish("",queuName,null,xmlBytes);
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

}

