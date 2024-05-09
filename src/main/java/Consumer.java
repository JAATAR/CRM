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

public class Consumer {

    private final String host = "10.2.160.10";


    private String queueUser = "crm_queue";
    private String exchangeUser = "amq.topic";
    private String routingKeyUser = "user";

 /*   private String queueEvent = "crm_queue";
    private String exchangeEvent = "AMQ.topic";
    private String routingKeyEvent = "event";
*/
    private String queueConsumption = "crm_queue";
    private String exchangeConsumption = "amq.topic";
    private String routingKeyConsumption = "consumption";

    private String queueBusiness = "crm_queue";
    private String exchangeBusiness = "amq.topic";
    private String routingKeyBusiness = "business";

    private Channel channel;

    //we create a connection within the constructor
    public Consumer() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        try{
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

//2de queue
            channel.queueDeclare(queueUser, false, false, false, null);
            channel.queueBind(queueUser, exchangeUser, routingKeyUser);
 //3de queue

           // channel.queueDeclare(queueEvent, false, false, false, null);
          //  channel.queueBind(queueEvent, exchangeEvent, routingKeyEvent);
//4de queue
            channel.queueDeclare(queueBusiness, false, false, false, null);
            channel.queueBind(queueBusiness, exchangeBusiness, routingKeyBusiness);
          //5de queue
            channel.queueDeclare(queueConsumption, false, false, false, null);
            channel.queueBind(queueConsumption, exchangeConsumption, routingKeyConsumption);


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
                String xsd = "src/main/validation/main.xsd";

   if(!validateXML(message, xsd)){
       System.out.println("XML is not valid. Skipping processing.");
       return; // stop further processing
   }

                System.out.println("validation succesful");
                try {



                    if(unmarshalParticipant(message) instanceof Participant){
                        Participant participant1 = (Participant) unmarshalParticipant(message);
                    }
                    else if(unmarshalEvent(message) instanceof Event)
                    {
                        Event event1 = (Event) unmarshalEvent(message);
                    }
                    else if(unmarshalBusiness(message) instanceof Business){

                        Business business1 = (Business) unmarshalBusiness(message);

                    } else if (unmarshalSession(message) instanceof Session){

                        Session session1 = (Session) unmarshalSession(message);
                    }else if (unmarshalConsumption(message) instanceof Consumption){
                        Consumption consumption1 = (Consumption) unmarshalConsumption(message);
                    }


                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }

        };

        // start consuming messages from the queue
        channel.basicConsume("frontend_queue", true, consumer); // Start met consumeren van berichten voor de tweede queue
        channel.basicConsume(queueUser, true, consumer);
      //  channel.basicConsume(queueEvent, true, consumer);
        channel.basicConsume(queueBusiness, true, consumer);
        channel.basicConsume(queueConsumption, true, consumer);
    }
    // Unmarshal XML to corresponding objects based on its type
    public Object unmarshalBasedOnType(String xml) throws JAXBException {
        JAXBContext jaxbContext = null;
        Unmarshaller jaxbUnmarshaller = null;

        if (xml.contains("<participant")) {
            jaxbContext = JAXBContext.newInstance(Participant.class);
        } else if (xml.contains("<event")) {
            jaxbContext = JAXBContext.newInstance(Event.class);
        } else if (xml.contains("<session")) {
            jaxbContext = JAXBContext.newInstance(Session.class);
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
    // Unmarshall Participant-object van XML-string
    public Participant unmarshalParticipant(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Participant.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return (Participant) jaxbUnmarshaller.unmarshal(inputStream);

    }
    // Unmarshall Event-object van XML-string
    public Event unmarshalEvent(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Event.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return (Event) jaxbUnmarshaller.unmarshal(inputStream);

    }
    //Unmarshall Consumption-object van XML-string
    public Consumption unmarshalConsumption(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Consumption.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return (Consumption) jaxbUnmarshaller.unmarshal(inputStream);
    }
    // Unmarshall Session-object van XML-string
    public Session unmarshalSession(String xml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Session.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        return (Session) jaxbUnmarshaller.unmarshal(inputStream);

    }

    // Unmarshall Business-object van XML-string
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

}





