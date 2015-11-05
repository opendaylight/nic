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
import org.opendaylight.nic.mapping.api.EgressPoint;
import org.opendaylight.nic.mapping.api.IngressPoint;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.mapping.api.MappedObject;
import org.opendaylight.nic.mapping.api.MplsEgressLabel;
import org.opendaylight.nic.mapping.api.MplsIngressLabel;
import org.opendaylight.nic.mapping.api.TypeHostname;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;

@RunWith(MockitoJUnitRunner.class)
public class HazelcastMappingServiceImplTest {

    private HazelcastMappingServiceImpl service = new HazelcastMappingServiceImpl();
    private static String BOB = "bob";
    private static String ALICE = "alice";
    private static String ADAM = "adam";

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

        assertNotNull(service.stringRepresentation(ALICE));

        for (MappedObject o : objects) {
            assertTrue(o.getType().equals("hostname"));
        }
    }

    @Test
    public final void testAddMPLSEndpointsInfo() throws Exception {

        service = spy(service);
        PowerMockito.mockStatic(FrameworkUtil.class);

        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceRegistration<IntentMappingService> intentServiceMock = mock(ServiceRegistration.class);

        doReturn(mockBundleContext).when(service).getBundleCtx();
        when(mockBundleContext.registerService(IntentMappingService.class, service, null))
                .thenReturn(intentServiceMock);

        List<MappedObject> list = new ArrayList<>();

        MplsEgressLabel mplsEgressLabel = new MplsEgressLabel();
        mplsEgressLabel.setLabel("21");

        MplsIngressLabel mplsIngressLabel = new MplsIngressLabel();
        mplsIngressLabel.setLabel("26");

        IngressPoint ingPoint = new IngressPoint();
        ingPoint.setIngressPoint("router1");

        EgressPoint egPoint = new EgressPoint();
        egPoint.setEgressPoint("router3");

        list.add(mplsEgressLabel);
        list.add(mplsIngressLabel);
        list.add(ingPoint);
        list.add(egPoint);

        service.addList(ADAM, list);
        Collection<MappedObject> objects = service.retrieve(ADAM);
        assertNotNull(objects);
        assertTrue(objects.size() == 4);

        assertNotNull(service.stringRepresentation(ADAM));
    }
}
