/*
 * Copyright (c) 2016 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.Qos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.DscpType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.QosConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.subjects.rev150122.EndPointGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@PrepareForTest({MatchUtils.class, QosConstraintManager.class, FlowBuilder.class})
@RunWith(PowerMockRunner.class)
public class QosConstraintManagerTest {

    /**
     * Instance of Allow to perform unit testing.
     */
    @Mock private Allow action;
    /**
     * Instance of QosConstraint to perform unit testing.
     */
    @Mock private QosConstraint constraint;
    /**
     * Instance of Action to perform unit testing.
     */
    @Mock private Action actions;
    /**
     * Instance of Constraints to perform unit testing.
     */
    @Mock private Constraints constraints;
    /**
     * List of instance for EndPointGroups to perform unit testing.
     */
    private List<String> endPointGroups = null;
    /**
     * EndPointGroup Source String.
     */
    private static final String EPG_SRC = "00:00:00:00:00:01";
    /**
     * EndPointGroup Destination String.
     */
    private static final String EPG_DST = "00:00:00:00:00:02";
    /**
     * Flow Name String.
     */
    private static final String INTENT_EXPECTED_STRING = "L2_Rule_";
    /**
     * QoS Constraint name String.
     */
    private static final String QOS_NAME = "HIGH";
    /**
     * QoS Profile name String.
     */
    private static final String PROFILE_NAME = "HIGH";
    /**
     * Instance of QosConstraintManager to perform unit testing.
     */
    private QosConstraintManager qosConstraintManager;
    /**
     * Mock instance of ReadOnlyTransaction to perform unit testing.
     */
    private ReadOnlyTransaction mockReadOnlyTransaction;
    /**
     * Mock instance of DataBroker to perform unit testing.
     */
    private DataBroker mockDataBroker;

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public void setUp() throws Exception {
        endPointGroups = new ArrayList<>();
        endPointGroups.add(EPG_SRC);
        endPointGroups.add(EPG_DST);
        PowerMockito.whenNew(MatchBuilder.class).withNoArguments().thenReturn(mock(MatchBuilder.class));
        FlowBuilder flowBuilder = mock(FlowBuilder.class);
        PowerMockito.whenNew(FlowBuilder.class).withNoArguments().thenReturn(flowBuilder);
        mockReadOnlyTransaction = mock(ReadOnlyTransaction.class);
        mockDataBroker = mock(DataBroker.class);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);
        qosConstraintManager = PowerMockito.spy(new QosConstraintManager(mockDataBroker, mock(PipelineManager.class)));
        qosConstraintManager.setEndPointGroups(endPointGroups);
        qosConstraintManager.setAction(actions);
        qosConstraintManager.setConstraint(constraints);
        qosConstraintManager.setQosName(QOS_NAME);
        MemberModifier.field(QosConstraintManager.class, "endPointGroups").set(qosConstraintManager, endPointGroups);
        MemberModifier.field(QosConstraintManager.class, "action").set(qosConstraintManager, action);
        MemberModifier.field(QosConstraintManager.class, "constraint").set(qosConstraintManager, constraint);
        Instructions buildedInstructions = mock(Instructions.class);
        OutputPortValues[] port = { OutputPortValues.NORMAL, OutputPortValues.CONTROLLER };
        PowerMockito.doReturn(buildedInstructions).when(qosConstraintManager, "createQoSInstructions", any(Dscp.class), eq(port));
    }

    /**
     * Test case for {@link QosConstraintManager#pushFlow(NodeId, FlowAction)}.
     */
    @Test
    public void testPushFlow() throws Exception {

        List<Intent> mockIntentsConf = new ArrayList<Intent>();
        List<QosConfig> mockListOfQosConfig = mock(List.class);
        QosConfig mockQosConfig = mock(QosConfig.class);
        Intent mockIntent = mock(Intent.class);
        Intents mockIntents = mock(Intents.class);
        Qos mockQos = mock(DscpType.class);
        Dscp mockDscp = mock(Dscp.class);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.dscp.type.DscpType mockDscpType =
                mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.dscp.type.DscpType.class);
        Optional mockOptional = mock(Optional.class);
        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);

        // Positive case- Getting the list of intents.
        when(mockIntents.getIntent()).thenReturn(mockIntentsConf);
        when(mockOptional.isPresent()).thenReturn(true);
        when(mockOptional.get()).thenReturn(mockIntents);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);
        when(mockReadOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION,
                QosConstraintManager.INTENTS_IID)).thenReturn(mockCheckedFuture);
        List<Intent> actualListOfIntent = Whitebox.invokeMethod(qosConstraintManager, "listIntents");
        PowerMockito.verifyPrivate(qosConstraintManager).invoke("listIntents");
        actualListOfIntent.add(mockIntent);
        when(mockIntent.getQosConfig()).thenReturn(mockListOfQosConfig);
        when(mockListOfQosConfig.get(0)).thenReturn(mockQosConfig);
        when(mockListOfQosConfig.get(1)).thenReturn(mockQosConfig);
        when(mockQosConfig.getQos()).thenReturn(mockQos);
        when(mockDscpType.getName()).thenReturn(PROFILE_NAME);
        when(mockDscpType.getDscp()).thenReturn(mockDscp);
        when(((DscpType)mockQos).getDscpType()).thenReturn(mockDscpType);
        PowerMockito.doReturn(true).when(qosConstraintManager, "writeDataTransaction", any(NodeId.class), any(FlowBuilder.class), any(FlowAction.class));
        qosConstraintManager.pushFlow(mock(NodeId.class), FlowAction.ADD_FLOW);

        //Negative case- QoS name and Constraint profile are different.
        when(mockDscpType.getName()).thenReturn("profile");
        when(((DscpType)mockQos).getDscpType()).thenReturn(mockDscpType);
        qosConstraintManager.pushFlow(mock(NodeId.class), FlowAction.ADD_FLOW);

        //Negative case- QoS name is null.
        when(mockDscpType.getName()).thenReturn(null);
        when(((DscpType)mockQos).getDscpType()).thenReturn(mockDscpType);
        qosConstraintManager.pushFlow(mock(NodeId.class), FlowAction.ADD_FLOW);

        //Negative case- List of QoS Configuration is null.
        when(mockIntent.getQosConfig()).thenReturn(null);
        qosConstraintManager.pushFlow(mock(NodeId.class), FlowAction.ADD_FLOW);

        //Negative case- Endpoints and action are null.
        MemberModifier.field(QosConstraintManager.class, "endPointGroups").set(qosConstraintManager, null);
        MemberModifier.field(QosConstraintManager.class, "action").set(qosConstraintManager, null);
        qosConstraintManager.pushFlow(mock(NodeId.class), FlowAction.ADD_FLOW);
    }

    /**
     * Test case for {@link QosConstraintManager#createFlowBuilder(MatchBuilder)}.
     */
    @Test
    public void testCreateFlowBuilder() throws Exception {
        MatchBuilder matchBuilder = mock(MatchBuilder.class);
        FlowBuilder actual = Whitebox.invokeMethod(qosConstraintManager, "createFlowBuilder", matchBuilder);
        PowerMockito.verifyPrivate(qosConstraintManager).invoke("createFlowBuilder", matchBuilder);
        assertTrue(actual instanceof FlowBuilder);
    }

    /**
     * Test case for {@link QosConstraintManager#createEthMatch(EndPointGroups, MatchBuilder)}.
     */
    @Test
    public void testCreateEthMatch() throws Exception {
        //Negative case - Invalid EndPointGroups.
        List<String> endPointGroup = new ArrayList<>();
        endPointGroup.add("any");
        endPointGroup.add("any");
        MatchBuilder matchBuilder = mock(MatchBuilder.class);
        Whitebox.invokeMethod(qosConstraintManager, "createEthMatch", endPointGroup, matchBuilder);
        PowerMockito.verifyPrivate(qosConstraintManager, times(1)).invoke("createEthMatch", any(EndPointGroups.class), any(MatchBuilder.class));
    }

    /**
     * Test case for {@link QosConstraintManager#createFlowName()}.
     */
    @Test
    public void testCreateFlowName() {
        QosConstraintManager qosConstraintManager = new QosConstraintManager(mock(DataBroker.class), mock(PipelineManager.class));
        qosConstraintManager.setEndPointGroups(endPointGroups);
        String flowName = qosConstraintManager.createFlowName();
        assertTrue(flowName.contains(INTENT_EXPECTED_STRING));
    }

    /**
     * Test case for {@link QosConstraintManager#listIntents()}.
     */
    @Test
    public void testListIntents() throws Exception {
        Optional mockOptional = mock(Optional.class);
        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);

        //Negative case- Optical is absent.
        when(mockOptional.isPresent()).thenReturn(false);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);
        when(mockReadOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION,
                QosConstraintManager.INTENTS_IID)).thenReturn(mockCheckedFuture);
        List<Intent> intents = Whitebox.invokeMethod(qosConstraintManager, "listIntents");
        PowerMockito.verifyPrivate(qosConstraintManager, times(1)).invoke("listIntents");
        assertNull(intents);
    }
}
