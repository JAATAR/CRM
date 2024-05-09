import crm.Business;
import crm.Consumer;
import crm.Consumption;
import crm.Participant;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsumerTest {

    @Test
    void unmarshalParticipant() throws Exception {
        // Arrange
        Consumer consumer = new Consumer();
        String xml = "<participant><firstname>John</firstname><lastname>Doe</lastname><age>30</age></participant>";

        // Act
        Participant participant = consumer.unmarshalParticipant(xml);

        // Assert
        assertEquals("John", participant.getFirstname());
        assertEquals("Doe", participant.getLastname());
        //assertEquals(30, participant.getAge());
    }

    @Test
    void unmarshalBusiness() throws Exception {
        // Arrange
        Consumer consumer = new Consumer();
        String xml = "<business><name>ABC Corp</name><vat>123456789</vat></business>";

        // Act
        Business business = consumer.unmarshalBusiness(xml);

        // Assert
        assertEquals("ABC Corp", business.getName());
        assertEquals("123456789", business.getVat());
    }

    @Test
    void unmarshalConsumption() throws Exception {
        // Arrange
        Consumer consumer = new Consumer();
        String xml = "<consumption><timestamp>2024-05-09T21:00:00</timestamp><name>food</name></consumption>";

        // Act
        Consumption consumption = consumer.unmarshalConsumption(xml);

        // Assert
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date expectedDate = sdf.parse("2024-05-09T21:00:00");

        assertEquals(expectedDate, consumption.getTimestamp());
        assertEquals("food", consumption.getProducts());
    }

    @Test
    void validateXML_ValidXML() {
        // Arrange
        String xml = "<participant><firstname>John</firstname><lastname>Doe</lastname><age>30</age></participant>";
        String xsdPath = "src/main/resources/include.template.xsd";

        // Act
        boolean isValid = Consumer.validateXML(xml, xsdPath);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateXML_InvalidXML() {
        // Arrange
        String xml = "<participant><firstname>John<firstname><lastname>Doe</lastname><age>30</age></participant>";
        String xsdPath = "src/main/resources/include.template.xsd";

        // Act
        boolean isValid = Consumer.validateXML(xml, xsdPath);

        // Assert
        assertFalse(isValid);
    }
}
