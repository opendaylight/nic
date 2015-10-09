package org.opendaylight.nic.mapping.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.mapping.api.MappedObject;
import org.opendaylight.nic.mapping.api.TypeHostname;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class HazelcastMappingServiceTest {

    private IntentMappingService service = new HazelcastMappingServiceImpl();
    private static String BOB = "bob";
    private static String ALICE = "alice";

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public final void testAddTerms() throws Exception {

            TypeHostname hostname = new TypeHostname();
            hostname.setHostname("bob-server");

            service.add(BOB, hostname);
            Collection<MappedObject> objects = service.retrieve(BOB);
            assertNotNull(objects);
            assertTrue(objects.size() == 1);

            List<MappedObject> list = new ArrayList<>();

            TypeHostname hostname1 = new TypeHostname();
            hostname1.setHostname("alice-server1");

            TypeHostname hostname2 = new TypeHostname();
            hostname2.setHostname("alice-server2");

            TypeHostname hostname3 = new TypeHostname();
            hostname3.setHostname("alice-server3");

            list.add(hostname1);
            list.add(hostname2);
            list.add(hostname3);

            service.addList(ALICE, list);

            objects = service.retrieve(ALICE);
            assertNotNull(objects);
            assertTrue(objects.size() == 3);
    }
}
