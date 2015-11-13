package org.opendaylight.nic.mapping.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.api.IntentMappingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;

@RunWith(MockitoJUnitRunner.class)
public class HazelcastMappingServiceImplTest {

    private HazelcastMappingServiceImpl service = new HazelcastMappingServiceImpl();
    private static String BOB = "bob";
    private static String ALICE = "alice";

    private ServiceRegistration<IntentMappingService> nicConsoleRegistration;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public final void testAddTerms() throws Exception {

        service = spy(service);
        PowerMockito.mockStatic(FrameworkUtil.class);

        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceRegistration<IntentMappingService> intentServiceMock = mock(ServiceRegistration.class);

        doReturn(mockBundleContext).when(service).getBundleCtx();
        when(mockBundleContext.registerService(IntentMappingService.class, service, null))
                .thenReturn(intentServiceMock);

        String hostname = "bob-server";

        service.add(BOB, hostname);
        Collection<String> objects = service.retrieve(BOB);
        assertNotNull(objects);
        assertTrue(objects.size() == 1);

        List<String> list = new ArrayList<>();

        String hostname1 = "alice-server1";

        String hostname2 = "alice-server2";

        String hostname3 = "alice-server3";

        list.add(hostname1);
        list.add(hostname2);
        list.add(hostname3);

        service.addList(ALICE, list);

        objects = service.retrieve(ALICE);
        assertNotNull(objects);
        assertTrue(objects.size() == 3);

        assertNotNull(service.stringRepresentation(ALICE));

        for (String o : objects) {
            assertTrue(o.getClass() == String.class);
        }
    }
}
