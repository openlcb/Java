package org.openlcb.cdi.jdom;

import org.openlcb.*;
import org.openlcb.implementations.*;

import org.junit.*;

import javax.swing.*;
/**
 * @author  Bob Jacobsen   Copyright 2012
 * @version $Revision: 34 $
 */
public class CdiMemConfigReaderTest {

    NodeID nidHere = new NodeID(new byte[]{0,0,0,0,0,1});
    NodeID nidThere = new NodeID(new byte[]{0,0,0,0,0,2});
    
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {}
    };
    
    MimicNodeStore store;
    MemoryConfigurationService service;

    DatagramService dgs;
    String testString = "string to be checked which involves more than 64 characters, so its three datagrams at least, because two originally worked and perhaps three did not ";
    byte[] content;
    
    @Before
    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, nidHere);
        dgs = new DatagramService(null, null);
        
        store.addNode(nidThere);
        
        content = testString.getBytes();
        content[content.length-1] = 0;

        // TODO: 5/2/16 replace this with a proper mock
        service = new MemoryConfigurationService(nidHere, dgs) {
            public void requestRead(NodeID dest, int space, long address, int len, McsReadHandler
                    cb) {
                byte[] data = new byte[Math.min(len, (int)(content.length - address))];
                System.arraycopy(content, (int)address, data, 0, data.length);
                cb.handleReadData(nidThere, space, address, data);
            }
        };
        
    }
    
    @After
    public void tearDown() {
       store.dispose();
       service.dispose();
       store = null;
       service = null;
    }
         
    @Test
    @Ignore("setup doesn't actually configure the class under test, and we have a constructor test")
    public void testSetup() {
        Assert.assertNotNull("exists",service);
    }
          
    @Test 
    public void testCtor() {
        CdiMemConfigReader cmcr = new CdiMemConfigReader(nidHere, store, service);   
        Assert.assertNotNull("exists",cmcr);
    }

    java.io.Reader rdr;
    long bytesRead;
    @Test
    public void testCycle() throws java.io.IOException {
        CdiMemConfigReader cmcr = new CdiMemConfigReader(nidHere, store, service);
        CdiMemConfigReader.ReaderAccess a = new CdiMemConfigReader.ReaderAccess() {
            @Override
            public void progressNotify(long _bytesRead, long totalBytes) {
                bytesRead = _bytesRead;
            }

            public void provideReader(java.io.Reader r) {
                rdr = r;
            }
        };
        
        rdr = null;
        cmcr.startLoadReader(a);
        Assert.assertTrue(rdr != null);
        Assert.assertEquals("1", testString.getBytes()[0], rdr.read());
        Assert.assertEquals("2", testString.getBytes()[1], rdr.read());
        Assert.assertEquals(testString.length()-1, bytesRead);
        int count = 2;
        while (rdr.read() > 0) count++;
        Assert.assertEquals("length", testString.length()-1, count); // -1 for trailing zero on input
    }
}
