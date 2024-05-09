import crm.Heartbeat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeartbeatTest {

    @Test
    void testCreateXML() throws Exception {
        Heartbeat heartbeat = new Heartbeat();
        String xml = heartbeat.createXML();

        assertNotNull(xml);
        assertTrue(xml.contains("<heartbeat>"));
        assertTrue(xml.contains("</heartbeat>"));
        assertTrue(xml.contains("<service>crm</service>"));
        assertTrue(xml.contains("<status>up</status>"));
    }

    @Test
    void testIsSalesforceAvailable() throws Exception {
        assertTrue(Heartbeat.isSalesforceAvailable());
    }
}
