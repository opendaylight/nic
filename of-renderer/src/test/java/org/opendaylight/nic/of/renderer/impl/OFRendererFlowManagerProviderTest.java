/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.qos.constraint.QosConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 30/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class OFRendererFlowManagerProviderTest {

    private Set<ServiceRegistration<?>> serviceRegistration;
    private IntentFlowManager intentFlowManager;
    private ArpFlowManager arpFlowManager;
    private LldpFlowManager lldpFlowManager;

    private OFRendererGraphService ofRendererGraphService;
    private MplsIntentFlowManager mplsIntentFlowManager;
    private QosConstraintManager qosConstraintManager;
    private Registration registration;
    private RedirectFlowManager redirectFlowManager;
    private Subject subject;

    @Mock
    private EndPointGroup source;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup sourceEndPointGroup;

    @Mock
    Subjects subjectsSource;

    @Mock
    private EndPointGroup destination;

    @Mock
    org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup dstEndPointGroup;

    @Mock
    Subjects subjectsDestination;

    @Mock
    private List<Actions> actionsList;

    @Mock
    private Actions actions;

    @Mock
    private Action action;

    @Mock
    Redirect redirect;

    @Mock
    Constraints constraints;

    @Mock
    QosConstraint qosConstraint;

    @Mock
    private DataBroker dataBroker;

    @Mock
    private PipelineManager pipelineManager;

    @Mock
    private NotificationProviderService notificationProviderService;

    @Mock
    private Intent intent;

    @Mock
    private Uuid uuidMock;

    @Mock
    private IntentMappingService intentMappingService;

    private OFRendererFlowManagerProvider ofRendererFlowManagerProvider;
    private final String DEFAULT_STR_UUID = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(uuidMock.getValue()).thenReturn(DEFAULT_STR_UUID);
        initIntentWithSourceAndDestination();
        ofRendererGraphService = spy(new NetworkGraphManager());
        mplsIntentFlowManager = spy(
                new MplsIntentFlowManager(dataBroker, pipelineManager));
        arpFlowManager = spy(new ArpFlowManager(dataBroker, pipelineManager));
        lldpFlowManager = spy(new LldpFlowManager(dataBroker, pipelineManager));
        qosConstraintManager = spy(
                new QosConstraintManager(dataBroker, pipelineManager));
        ofRendererFlowManagerProvider = spy(
                new OFRendererFlowManagerProvider(dataBroker, pipelineManager,
                        intentMappingService, notificationProviderService));
    }

    private void initIntentWithSourceAndDestination() {
        when(sourceEndPointGroup.getName()).thenReturn("source");
        when(source.getEndPointGroup()).thenReturn(sourceEndPointGroup);
        when(dstEndPointGroup.getName()).thenReturn("destination");
        when(destination.getEndPointGroup()).thenReturn(dstEndPointGroup);
        when(subjectsSource.getSubject()).thenReturn(source);
        when(subjectsSource.getOrder()).thenReturn((short) 1);
        when(subjectsDestination.getSubject()).thenReturn(destination);
        when(subjectsDestination.getOrder()).thenReturn((short) 2);

        List<Subjects> subjectsList = spy(new ArrayList<>());
        subjectsList.add(subjectsSource);
        subjectsList.add(subjectsDestination);

        when(intent.getSubjects()).thenReturn(subjectsList);
        when(intent.getId()).thenReturn(uuidMock);
        when(intent.getActions()).thenReturn(actionsList);
        when(actionsList.get(0)).thenReturn(actions);
    }

    @Test(expected = NoSuchElementException.class)
    public void testIsRedirectWithNoSuchElementException() {
        when(actions.getAction()).thenReturn(null);
        ofRendererFlowManagerProvider.isRedirect(intent);
    }

    @Test
    public void testIsRedirect() {
        when(actions.getAction()).thenReturn(redirect);
        Assert.assertTrue(ofRendererFlowManagerProvider.isRedirect(intent));
    }

    @Test
    public void testIsRedirectFalse() {
        when(actions.getAction()).thenReturn(action);
        Assert.assertFalse(ofRendererFlowManagerProvider.isRedirect(intent));
    }

    @Test(expected = NoSuchElementException.class)
    public void testQoStWithNoSuchElementException() {
        when(actions.getAction()).thenReturn(null);
        ofRendererFlowManagerProvider.isQoS(intent);
    }

    @Test
    public void testIsQoS() throws Exception {
        when(actions.getAction()).thenReturn(redirect);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraintBuilder()
                .setQosConstraint(
                        new QosConstraintBuilder().setQosName("teste").build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

        Bundle bundle = mock(Bundle.class);
        BundleContext bundleContext = mock(BundleContext.class);
        when(bundle.getBundleContext()).thenReturn(bundleContext);

        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito
                .when(FrameworkUtil.class, "getBundle",
                        ofRendererFlowManagerProvider.getClass())
                .thenReturn(bundle);
        PowerMockito.when(FrameworkUtil.class, "getBundle",
                RedirectFlowManager.class).thenReturn(bundle);

        ofRendererFlowManagerProvider.init();

        Assert.assertTrue(ofRendererFlowManagerProvider.isQoS(intent));
    }

    @Test
    public void testIsQoStFalse() throws Exception {
        when(actions.getAction()).thenReturn(redirect);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraintBuilder()
                .setQosConstraint(
                        new QosConstraintBuilder().setQosName(null).build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

        Bundle bundle = mock(Bundle.class);
        BundleContext bundleContext = mock(BundleContext.class);
        when(bundle.getBundleContext()).thenReturn(bundleContext);

        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito
                .when(FrameworkUtil.class, "getBundle",
                        ofRendererFlowManagerProvider.getClass())
                .thenReturn(bundle);
        PowerMockito.when(FrameworkUtil.class, "getBundle",
                RedirectFlowManager.class).thenReturn(bundle);

        ofRendererFlowManagerProvider.init();
        Assert.assertFalse(ofRendererFlowManagerProvider.isQoS(intent));
    }

    @Test(expected = IntentElementNotFoundException.class)
    public void testIsMPLSWithEmptyResultOnIntentMappingService()
            throws Exception {
        initIntentWithSourceAndDestination();

        Map<String, String> map = new HashMap<>();
        when(intentMappingService.get(Mockito.any(String.class)))
                .thenReturn(map);

        ofRendererFlowManagerProvider.isMPLS(intent);
    }

    @Test
    public void testIsMPLSWithNoMappingForDestination()
            throws IntentInvalidException {
        Map<String, String> mapSource = new HashMap<>();
        mapSource.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        Map<String, String> mapDestination = new HashMap<>();
        mapDestination.put(dstEndPointGroup.getName(), "");

        when(intentMappingService.get(sourceEndPointGroup.getName()))
                .thenReturn(mapSource);
        when(intentMappingService.get(dstEndPointGroup.getName()))
                .thenReturn(mapDestination);

        Assert.assertFalse(ofRendererFlowManagerProvider.isMPLS(intent));
    }

    @Test
    public void testIsMPLSWithNoMappingForSource()
            throws IntentInvalidException {
        Map<String, String> mapSource = new HashMap<>();
        mapSource.put(sourceEndPointGroup.getName(), "");

        Map<String, String> mapDestination = new HashMap<>();
        mapDestination.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        when(intentMappingService.get(sourceEndPointGroup.getName()))
                .thenReturn(mapSource);
        when(intentMappingService.get(dstEndPointGroup.getName()))
                .thenReturn(mapDestination);

        Assert.assertFalse(ofRendererFlowManagerProvider.isMPLS(intent));
    }

    @Test
    public void testIsMPLS() throws IntentInvalidException {
        Map<String, String> mapSource = new HashMap<>();
        mapSource.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        Map<String, String> mapDestination = new HashMap<>();
        mapDestination.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        when(intentMappingService.get(sourceEndPointGroup.getName()))
                .thenReturn(mapSource);
        when(intentMappingService.get(dstEndPointGroup.getName()))
                .thenReturn(mapDestination);

        Assert.assertTrue(ofRendererFlowManagerProvider.isMPLS(intent));
    }
}
