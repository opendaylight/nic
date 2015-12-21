package org.opendaylight.nic.mapping.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.mapping.hazelcast.impl.HazelcastMappingServiceImpl;

@RunWith(MockitoJUnitRunner.class)
public class HazelcastMappingServiceImplTest {

    private HazelcastMappingServiceImpl service = new HazelcastMappingServiceImpl();
    private static String BOB = "bob";
    private static String ALICE = "alice";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testAddTerms() throws Exception {

        service = spy(service);

        String hostname = "bob-server";

        Map<String, String> map1 = new HashMap<>();
        map1.put("1", hostname);

        service.add(BOB, map1);

        Map<String, String> objects = service.get(BOB);
        assertNotNull(objects);
        assertTrue(objects.size() == 1);

        Map<String, String> map2 = new HashMap<>();

        String hostname1 = "alice-server1";

        String hostname2 = "alice-server2";

        String hostname3 = "alice-server3";

        map2.put("1", hostname1);
        map2.put("2", hostname2);
        map2.put("3", hostname3);

        service.add(ALICE, map2);

        objects = service.get(ALICE);
        assertNotNull(objects);
        assertTrue(objects.size() == 3);

        assertNotNull(service.get(BOB));

        for (String o : objects.values()) {
            assertTrue(o.getClass() == String.class);
        }
    }
}
