package crm;

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

public class Consumer {

    private final  String devHost = System.getenv("DEV_HOST");


    private String QUEUEUSER = System.getenv("QUEUE_USER");
    private  String EXCHANGE_USER = System.getenv("EXCHANGE_USER");
    private  String  ROUTINGKEY_USER= System.getenv("ROUTINGKEY_USER");

 /*   private String queueEvent = "crm_queue";
    private String exchangeEvent = "AMQ.topic";
    private String routingKeyEvent = "event";
*/
    private  String QUEUE_CONSUMPTION = System.getenv("QUEUE_CONSUMPTION");
    private String EXCHANGE_CONSUMPTION = System.getenv("EXCHANGE_CONSUMPTION");
    private String ROUTINGKEY_CONSUMPTION = System.getenv("ROUTINGKEY_CONSUMPTION");

    private String QUEUE_BUSINESS = System.getenv("QUEUE_BUSINESS");
    private String EXCHANGE_BUSINESS = System.getenv("EXCHANGE_BUSINESS");
    private String ROUTINGKEY_BUSINESS = System.getenv("ROUTINGKEY_BUSINESS");

    private Channel channel;

    //we create a connection within the constructor
    public Consumer() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(devHost);

        try{
            Connection connection = factory.newConnection();
            channel = connection.createChannel();

//2de queue
            channel.queueDeclare(QUEUEUSER, false, false, false, null);
            channel.queueBind(QUEUEUSER, EXCHANGE_USER, ROUTINGKEY_USER);
 //3de queue

           // channel.queueDeclare(queueEvent, false, false, false, null);
          //  channel.queueBind(queueEvent, exchangeEvent, routingKeyEvent);
//4de queue
            channel.queueDeclare(QUEUE_BUSINESS, false, false, false, null);
            channel.queueBind(QUEUE_BUSINESS, EXCHANGE_BUSINESS, ROUTINGKEY_BUSINESS);
          //5de queue
            channel.queueDeclare(QUEUE_CONSUMPTION, false, false, false, null);
            channel.queueBind(QUEUE_CONSUMPTION, EXCHANGE_CONSUMPTION, ROUTINGKEY_CONSUMPTION);


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

   //if(!validateXML(message, xsd)){
     //  System.out.println("XML is not valid. Skipping processing.");
    //   return; // stop further processing
   //}

                System.out.println("validation succesful");
                try {



                    if(unmarshalParticipant(message) instanceof Participant){
                        Participant participant1 = (Participant) unmarshalParticipant(message);
                    }

                    else if(unmarshalBusiness(message) instanceof Business){
                        Business business1 = (Business) unmarshalBusiness(message);

                    }else if (unmarshalConsumption(message) instanceof Consumption){
                        Consumption consumption1 = (Consumption) unmarshalConsumption(message);
                    }


                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }

        };

        // start consuming messages from the queue
        channel.basicConsume(QUEUEUSER, true, consumer);
      //  channel.basicConsume(queueEvent, true, consumer);
        channel.basicConsume(QUEUE_BUSINESS, true, consumer);
        channel.basicConsume(QUEUE_CONSUMPTION, true, consumer);
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

}





