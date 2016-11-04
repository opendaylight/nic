/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.of.renderer.strategy.ActionStrategy;
import org.opendaylight.nic.of.renderer.strategy.DefaultExecutor;
import org.opendaylight.nic.of.renderer.strategy.MPLSExecutor;
import org.opendaylight.nic.of.renderer.strategy.QoSExecutor;
import org.opendaylight.nic.of.renderer.strategy.RedirectExecutor;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.classification.constraint.ClassificationConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.qos.constraint.QosConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by yrineu on 30/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FrameworkUtil.class })
public class OFRendererFlowManagerProviderTest {

    @Mock
    private ServiceRegistration<OFRendererFlowService> serviceRegistration;

    @Mock
    private ListenerRegistration<NotificationListener> listener;

    @Mock
    private Subject subject;

    @Mock
    DefaultExecutor defaultExecutor;

    @Mock
    private RedirectFlowManager redirectFlowManager;

    @Mock
    private EndPointGroup source;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup sourceEndPointGroup;

    @Mock
    private Subjects subjectsSource;

    @Mock
    private EndPointGroup destination;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup dstEndPointGroup;

    @Mock
    private Subjects subjectsDestination;

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

    @Mock
    private MPLSExecutor mplsExecutor;

    @Mock
    private ActionStrategy actionStrategy;

    @Mock
    private QoSExecutor qoSExecutor;

    @Mock
    private RedirectExecutor redirectExecutor;

    @Mock
    private BundleContext bundleContext;

    @Mock
    private Bundle bundle;

    private OFRendererFlowManagerProvider ofRendererFlowManagerProvider;

    private final String DEFAULT_STR_UUID = UUID.randomUUID().toString();

    private FlowAction flowAction = FlowAction.ADD_FLOW;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(bundle.getBundleContext()).thenReturn(bundleContext);

        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.class, "getBundle",
                RedirectFlowManager.class).thenReturn(bundle);

        when(uuidMock.getValue()).thenReturn(DEFAULT_STR_UUID);
        when(notificationProviderService.registerNotificationListener(
                Mockito.any(RedirectFlowManager.class))).thenReturn(listener);

        initIntentWithSourceAndDestination();

        ofRendererFlowManagerProvider = spy(
                new OFRendererFlowManagerProvider(dataBroker, pipelineManager,
                        intentMappingService, notificationProviderService));
        PowerMockito
                .when(FrameworkUtil.class, "getBundle",
                        ofRendererFlowManagerProvider.getClass())
                .thenReturn(bundle);
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
    public void testInitAndClose() throws Exception {
        when(bundleContext.registerService(OFRendererFlowService.class,
                ofRendererFlowManagerProvider, null))
                        .thenReturn(serviceRegistration);
        ofRendererFlowManagerProvider.init();
        ofRendererFlowManagerProvider.close();
    }

    @Test
    public void testIsQoS() throws Exception {
        when(actions.getAction()).thenReturn(redirect);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraintBuilder()
                .setQosConstraint(
                        new QosConstraintBuilder().setQosName("test").build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

        ofRendererFlowManagerProvider.init();

        Assert.assertTrue(ofRendererFlowManagerProvider.isQoS(intent));
    }

    @Test
    public void testIsQoSWithNameNull() throws Exception {
        when(actions.getAction()).thenReturn(redirect);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraintBuilder()
                .setQosConstraint(
                        new QosConstraintBuilder().setQosName(null).build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

        ofRendererFlowManagerProvider.init();
        Assert.assertFalse(ofRendererFlowManagerProvider.isQoS(intent));
    }

    @Test
    public void testIsQoSWithNoQoSImpl() throws Exception {
        when(actions.getAction()).thenReturn(redirect);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ClassificationConstraintBuilder()
                .setClassificationConstraint(
                        new ClassificationConstraintBuilder()
                                .setClassifier("test").build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

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

    @Test
    public void testUpdateWithQoSConfiguration() throws Exception {
        when(subject.getUpdate(ofRendererFlowManagerProvider))
                .thenReturn(intent);

        ofRendererFlowManagerProvider.setSubject(subject);
        ofRendererFlowManagerProvider.update();
    }

    @Test
    public void testUpdateWithIntentNull() throws Exception {
        when(subject.getUpdate(ofRendererFlowManagerProvider)).thenReturn(null);

        ofRendererFlowManagerProvider.setSubject(subject);
        ofRendererFlowManagerProvider.update();
    }

    @Test
    public void testPushIntentFlowWithQoSConfiguration()
            throws IntentInvalidException {
        ofRendererFlowManagerProvider.pushIntentFlow(intent, flowAction);
    }

    @Ignore
    @Test
    public void testPushIntentMPLS() throws Exception {
        Map<String, String> mapSource = new HashMap<>();
        mapSource.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        Map<String, String> mapDestination = new HashMap<>();
        mapDestination.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        when(intentMappingService.get(sourceEndPointGroup.getName()))
                .thenReturn(mapSource);
        when(intentMappingService.get(dstEndPointGroup.getName()))
                .thenReturn(mapDestination);

        when(intent.getQosConfig()).thenReturn(null);

        PowerMockito.whenNew(MPLSExecutor.class).withAnyArguments()
                .thenReturn(mplsExecutor);

        PowerMockito.doNothing().when(actionStrategy).execute(intent,
                flowAction);

        ofRendererFlowManagerProvider.pushIntentFlow(intent, flowAction);
    }

    @Ignore
    @Test
    public void testPushIntentQoS() throws Exception {
        Map<String, String> mapSource = new HashMap<>();
        mapSource.put(sourceEndPointGroup.getName(), "");

        Map<String, String> mapDestination = new HashMap<>();
        mapDestination.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        when(intentMappingService.get(sourceEndPointGroup.getName()))
                .thenReturn(mapSource);
        when(intentMappingService.get(dstEndPointGroup.getName()))
                .thenReturn(mapDestination);

        when(actions.getAction()).thenReturn(redirect);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraintBuilder()
                .setQosConstraint(
                        new QosConstraintBuilder().setQosName("test").build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

        ofRendererFlowManagerProvider.init();

        when(intent.getQosConfig()).thenReturn(null);

        PowerMockito.whenNew(QoSExecutor.class).withAnyArguments()
                .thenReturn(qoSExecutor);

        PowerMockito.doNothing().when(qoSExecutor).execute(intent, flowAction);

        ofRendererFlowManagerProvider.pushIntentFlow(intent, flowAction);
    }

    @Ignore
    @Test
    public void testPushIntentRedirect() throws Exception {
        Map<String, String> mapSource = new HashMap<>();
        mapSource.put(sourceEndPointGroup.getName(), "");

        Map<String, String> mapDestination = new HashMap<>();
        mapDestination.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        when(intentMappingService.get(sourceEndPointGroup.getName()))
                .thenReturn(mapSource);
        when(intentMappingService.get(dstEndPointGroup.getName()))
                .thenReturn(mapDestination);

        when(actions.getAction()).thenReturn(redirect);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraintBuilder()
                .setQosConstraint(
                        new QosConstraintBuilder().setQosName(null).build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

        PowerMockito.whenNew(RedirectFlowManager.class).withAnyArguments()
                .thenReturn(redirectFlowManager);
        PowerMockito.doNothing().when(redirectFlowManager)
                .redirectFlowConstruction(intent, flowAction);

        PowerMockito.doNothing().when(redirectFlowManager)
                .redirectFlowConstruction(intent, flowAction);

        PowerMockito.whenNew(RedirectExecutor.class).withAnyArguments()
                .thenReturn(redirectExecutor);

        ofRendererFlowManagerProvider.init();

        when(intent.getQosConfig()).thenReturn(null);

        ofRendererFlowManagerProvider.pushIntentFlow(intent, flowAction);
    }

    @Ignore
    @Test
    public void testPushIntentDefault() throws Exception {
        Map<String, String> mapSource = new HashMap<>();
        mapSource.put(sourceEndPointGroup.getName(), "");

        Map<String, String> mapDestination = new HashMap<>();
        mapDestination.put(OFRendererConstants.MPLS_LABEL_KEY, "");

        when(intentMappingService.get(sourceEndPointGroup.getName()))
                .thenReturn(mapSource);
        when(intentMappingService.get(dstEndPointGroup.getName()))
                .thenReturn(mapDestination);

        when(actions.getAction()).thenReturn(action);

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraintBuilder()
                .setQosConstraint(
                        new QosConstraintBuilder().setQosName(null).build())
                .build();

        when(constraints.getConstraints()).thenReturn(constraint);

        List<Constraints> contraintsList = spy(new ArrayList<>());
        contraintsList.add(constraints);

        when(intent.getConstraints()).thenReturn(contraintsList);

        PowerMockito.whenNew(DefaultExecutor.class).withAnyArguments()
                .thenReturn(defaultExecutor);
        PowerMockito.doNothing().when(defaultExecutor).execute(intent,
                flowAction);

        ofRendererFlowManagerProvider.init();

        when(intent.getQosConfig()).thenReturn(null);

        ofRendererFlowManagerProvider.pushIntentFlow(intent, flowAction);
    }

}
