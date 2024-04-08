import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

//annotation are part of jaxb
@XmlRootElement
@XmlType(propOrder = {"service", "timestamp", "error", "status"})
public class Heartbeat {

    private String service;
    private String timestamp;
    private String error;
    private int status;

    private String exhangeName = "controlroom_exchange";
    private String host = "10.2.160.9";

    public Heartbeat() throws Exception {
        this.service = "CRM";
        this.timestamp = getCurrentTimestamp();

        if (isSalesforceAvailable()){
            this.error = "No error";
            this.status = 1;
        }else {
            this.error = "Error";
            this.status = 0;
        }

    }


    @XmlElement
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @XmlElement
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
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
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String createXML() throws JAXBException{

        JAXBContext context = JAXBContext.newInstance(Heartbeat.class); //create a jaxb context for the heartbeat class to use the jaxb api
        Marshaller marshaller = context.createMarshaller();//marshaller converts an object to xml
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// format the xml for better readability

        //we collect the output to a stringwriter so we can turn the marshaller xml into a string
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(this, stringWriter);

        String xmlString = stringWriter.toString();
        System.out.println(xmlString); // Print the XML
        return xmlString;
    }
    public void sendHeartbeat() throws Exception {

        //create a connectionfactory and set the host on which rabbitmq runs
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        try{
            //create a connection with the server and a channel where we communicate through
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(exhangeName,"direct");//we declare an exchange on the channel(if the exchange already exists this line will be ignored)

            // create an xml document
            String xml = createXML();

            //convert it to byte array to send to the exchange
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new StreamSource(new StringReader(xml)),new StreamResult(byteArrayOutputStream));

            byte [] xmlBytes = byteArrayOutputStream.toByteArray();

            //xml sent to the exchange
            channel.basicPublish(exhangeName,"",null,xmlBytes);
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
    private String getCurrentTimestamp(){

        //crate a dateformat to format the date the way we want
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date()); //return the date formatted with the format created above
    }

}

