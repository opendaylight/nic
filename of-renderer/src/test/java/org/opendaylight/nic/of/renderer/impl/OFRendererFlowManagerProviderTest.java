package org.opendaylight.nic.of.renderer.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 30/05/16.
 */
public class OFRendererFlowManagerProviderTest {

    private Set<ServiceRegistration<?>> serviceRegistration;
    private IntentFlowManager intentFlowManager;
    private ArpFlowManager arpFlowManager;
    private LldpFlowManager lldpFlowManager;
    private IntentMappingService intentMappingService;
    private DataBroker dataBroker;
    private PipelineManager pipelineManager;
    private OFRendererGraphService ofRendererGraphService;
    private MplsIntentFlowManager mplsIntentFlowManager;
    private QosConstraintManager qosConstraintManager;
    private Registration registration;
    private RedirectFlowManager redirectFlowManager;
    private Subject subject;
    private NotificationProviderService notificationProviderService;
    private Intent intent;
    private EndPointGroup source;
    private EndPointGroup target;

    private OFRendererFlowManagerProvider ofRendererFlowManagerProvider;

    @Before
    public void setUp() {
        ofRendererGraphService = spy(new NetworkGraphManager());
        dataBroker = mock(DataBroker.class);
        pipelineManager = mock(PipelineManager.class);
        mplsIntentFlowManager = spy(new MplsIntentFlowManager(dataBroker, pipelineManager));
        arpFlowManager = spy(new ArpFlowManager(dataBroker, pipelineManager));
        lldpFlowManager = spy(new LldpFlowManager(dataBroker, pipelineManager));
        qosConstraintManager = spy(new QosConstraintManager(dataBroker, pipelineManager));
        notificationProviderService = mock(NotificationProviderService.class);
        intent = mock(Intent.class);
        ofRendererFlowManagerProvider = spy(new OFRendererFlowManagerProvider(dataBroker, pipelineManager,
                intentMappingService, notificationProviderService));
    }

    @Test (expected = IntentInvalidException.class)
    public void testIsMPLSWithInvalidIntent() throws IntentInvalidException {
        ofRendererFlowManagerProvider.isMPLS(intent);
    }

    @Test(expected = IntentInvalidException.class)
    public void test() throws Exception {
        PowerMockito.mockStatic(IntentUtils.class);
        List<EndPointGroup> endPointGroups = spy(new ArrayList<>());
        List<Subjects> subjectses = spy(new ArrayList<>());
        when(intent.getSubjects()).thenReturn(subjectses);
        source = mock(EndPointGroup.class);
        target = mock(EndPointGroup.class);
        endPointGroups.add(source);
        endPointGroups.add(target);
        when(endPointGroups.get(0)).thenReturn(source);
        when(endPointGroups.get(1)).thenReturn(target);
        PowerMockito.when(IntentUtils.class, "extractEndPointGroup", endPointGroups, 0).thenReturn(source);
        PowerMockito.when(IntentUtils.class, "extractEndPointGroup", endPointGroups, 1).thenReturn(target);

        when(IntentUtils.extractSrcEndPointGroup(intent)).thenReturn(source);
        when(IntentUtils.extractDstEndPointGroup(intent)).thenReturn(target);
        ofRendererFlowManagerProvider.isMPLS(intent);
    }

    @Test(expected = IntentElementNotFoundException.class)
    public void testIsRedirect() {
        ofRendererFlowManagerProvider.isRedirect(intent);
    }
}
