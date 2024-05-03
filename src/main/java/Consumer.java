import com.rabbitmq.client.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.ByteArrayInputStream;
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

                try {

                    // Unmarshall de XML naar de  objecten
                    Participant participant = unmarshalParticipant(message);
                    Event event = unmarshalEvent(message);
                    Session session = unmarshalSession(message);
                    Business business = unmarshalBusiness(message);

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
                    }

                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }

        };

        // start consuming messages from the queue
        channel.basicConsume(queueName, true, consumer);
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

}





