import crm.Business;
import crm.Consumer;
import crm.Consumption;
import crm.Participant;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerTest {

    @Test
    void unmarshalBasedOnType_shouldUnmarshalParticipant() throws JAXBException, IOException, jakarta.xml.bind.JAXBException {

        String xml = "<participant><firstname>John</firstname><lastname>Doe</lastname></participant>";
        Consumer consumer = new Consumer();

        Object result = consumer.unmarshalBasedOnType(xml);

        assertInstanceOf(Participant.class, result);
    }

    @Test
    void unmarshalBasedOnType_shouldUnmarshalBusiness() throws JAXBException, IOException, jakarta.xml.bind.JAXBException {

        String xml = "<business><name>ABC Company</name><vat>123456789</vat></business>";
        Consumer consumer = new Consumer();

        Object result = consumer.unmarshalBasedOnType(xml);

        assertInstanceOf(Business.class, result);
    }

    @Test
    void unmarshalBasedOnType_shouldUnmarshalConsumption() throws JAXBException, IOException, jakarta.xml.bind.JAXBException {

        String xml = "<consumption><products>Product A, Product B</products></consumption>";
        Consumer consumer = new Consumer();

        Object result = consumer.unmarshalBasedOnType(xml);

        assertInstanceOf(Consumption.class, result);
    }

    @Test
    void validateXML_shouldReturnTrueForValidXML() {

        String xml = "<participant><firstname>John</firstname><lastname>Doe</lastname></participant>";
        String xsdPath = "src/main/resources/include.template.xsd";

        boolean result = Consumer.validateXML(xml, xsdPath);

        assertTrue(result);
    }

    @Test
    void validateXML_shouldReturnFalseForInvalidXML() {

        String xml = "<participant><firstname>John</firstname><lastname>Doe</lastname></participant>";
        String xsdPath = "src/main/resources/include.template.xsd";

        boolean result = Consumer.validateXML(xml, xsdPath);

        assertFalse(result);
    }
}
