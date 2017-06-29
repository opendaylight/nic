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
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.IdManagerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 30/05/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FrameworkUtil.class })
public class OFRendererFlowManagerProviderTest {

    @Mock
    private ServiceRegistration<OFRendererFlowService> serviceRegistration;

    @Mock
    private Subject subject;

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
    private DataBroker dataBroker;

    @Mock
    private PipelineManager pipelineManager;

    @Mock
    private Intent intent;

    @Mock
    private Uuid uuidMock;

    @Mock
    private IdManagerService idManagerService;

    @Mock
    private BundleContext bundleContext;

    @Mock
    private Bundle bundle;

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

        when(uuidMock.getValue()).thenReturn(DEFAULT_STR_UUID);

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

    @Test
    public void testInitAndClose() throws Exception {
        when(bundleContext.registerService(OFRendererFlowService.class,
                ofRendererFlowManagerProvider, null))
                        .thenReturn(serviceRegistration);
        ofRendererFlowManagerProvider.start();
        ofRendererFlowManagerProvider.close();
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
