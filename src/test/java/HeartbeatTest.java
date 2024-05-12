/*import crm.Heartbeat;
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
        assertTrue(xml.contains("<error>0</error>")); // Since Salesforce is available, error should be 0
    }

    @Test
    void testIsSalesforceAvailable() throws Exception {

        boolean salesforceAvailable = Heartbeat.isSalesforceAvailable();


        assertTrue(salesforceAvailable);
    }
}
*/