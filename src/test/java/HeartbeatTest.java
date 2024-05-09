import crm.Heartbeat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeartbeatTest {

    @Test
    void testCreateXML() throws Exception {
        // Arrange
        Heartbeat heartbeat = new Heartbeat();

        // Act
        String xml = heartbeat.createXML();

        // Assert
        assertNotNull(xml);
        assertTrue(xml.contains("<heartbeat>"));
        assertTrue(xml.contains("</heartbeat>"));
        assertTrue(xml.contains("<service>crm</service>"));
        assertTrue(xml.contains("<status>up</status>"));
        assertTrue(xml.contains("<error>1</error>")); // Since Salesforce is available, error should be 1
    }

    @Test
    void testIsSalesforceAvailable() throws Exception {
        // Act
        boolean salesforceAvailable = Heartbeat.isSalesforceAvailable();

        // Assert
        assertTrue(salesforceAvailable);
    }
}
