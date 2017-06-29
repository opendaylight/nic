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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.of.renderer.strategy.ActionStrategy;
import org.opendaylight.nic.of.renderer.strategy.DefaultExecutor;
import org.opendaylight.nic.of.renderer.strategy.QoSExecutor;
import org.opendaylight.nic.of.renderer.strategy.RedirectExecutor;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.IdManagerService;
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
    private IdManagerService idManagerService;

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

    @Mock
    private OFRuleWithMeterManager ofRuleWithMeterManagerMock;

    @Mock
    private WriteTransaction writeTransactionMock;

    private OFRendererFlowManagerProvider ofRendererFlowManagerProvider;

    private final String DEFAULT_STR_UUID = UUID.randomUUID().toString();

    private FlowAction flowAction = FlowAction.ADD_FLOW;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransactionMock);

        when(bundle.getBundleContext()).thenReturn(bundleContext);

        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.class, "getBundle",
                RedirectFlowManager.class).thenReturn(bundle);

        when(uuidMock.getValue()).thenReturn(DEFAULT_STR_UUID);
        when(notificationProviderService.registerNotificationListener(
                Mockito.any(RedirectFlowManager.class))).thenReturn(listener);

        initIntentWithSourceAndDestination();

        ofRendererFlowManagerProvider = spy(
                new OFRendererFlowManagerProvider(dataBroker, pipelineManager, idManagerService));
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
        ofRendererFlowManagerProvider.start();
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

        ofRendererFlowManagerProvider.start();

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

        ofRendererFlowManagerProvider.start();
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

        ofRendererFlowManagerProvider.start();
        Assert.assertFalse(ofRendererFlowManagerProvider.isQoS(intent));
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
}
