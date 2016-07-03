/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.of.renderer.exception.InvalidIntentParameterException;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Log;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ClassificationConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by yrineu on 31/05/16.
 */
public class IntentFlowManagerTest {

    private DataBroker dataBroker;
    private PipelineManager pipelineManager;
    private FlowStatisticsListener flowStatisticsListener;
    private Intent intent;
    private IntentFlowManager intentFlowManager;
    private NodeId nodeId;
    private List<String> endPointGroups;
    private Action blockAction;
    private Action allowAction;
    private Action logAction;
    private WriteTransaction transaction;
    private MatchBuilder matchBuilder;
    private CheckedFuture<Void, TransactionCommitFailedException> future;
    private Constraints classificationConstraint;
    private List<Constraints> classificationConstraints;
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.
            rev150122.intent.constraints.Constraints innerConstraints;

    private static final String SRC_END_POINT = "60:6c:66:8b:bb:e5";
    private static final String DST_END_POINT = "60:6c:66:8b:bb:e6";
    private static final String SRC_PORT = "01";
    private static final String DST_PORT = "02";
    private static final String INTENT_ID = "38400000-8cf0-11bd-b23e-10b96e4ef00d";

    @Before
    public void setUp() {
        dataBroker = mock(DataBroker.class);
        pipelineManager = mock(PipelineManager.class);
        flowStatisticsListener = mock(FlowStatisticsListener.class);
        endPointGroups = spy(new ArrayList<String>());
        blockAction = mock(Action.class, withSettings().extraInterfaces(Block.class));
        allowAction = mock(Action.class, withSettings().extraInterfaces(Allow.class));
        logAction = mock(Action.class, withSettings().extraInterfaces(Log.class));

        intent = mock(Intent.class);
        nodeId = mock(NodeId.class);
        transaction = mock(WriteTransaction.class);
        matchBuilder = mock(MatchBuilder.class);
        future = mock(CheckedFuture.class);
        classificationConstraint = mock(Constraints.class,
                withSettings().extraInterfaces(ClassificationConstraint.class));
        innerConstraints = mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122
                .intent.constraints.Constraints.class, withSettings().extraInterfaces(ClassificationConstraint.class));
        classificationConstraints = spy(new ArrayList<>());
        classificationConstraints.add(classificationConstraint);

        when(intent.getId()).thenReturn(Uuid.getDefaultInstance(INTENT_ID));
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(transaction);
        when(transaction.submit()).thenReturn(future);
        when(intent.getConstraints()).thenReturn(classificationConstraints);
        when(classificationConstraint.getConstraints()).thenReturn(innerConstraints);

        intentFlowManager = spy(new IntentFlowManager(dataBroker, pipelineManager));
        intentFlowManager.setEndPointGroups(endPointGroups);
        intentFlowManager.setIntent(intent);
    }

    @Test (expected = InvalidIntentParameterException.class)
    public void testWithoutEndPointGroups() {
        intentFlowManager.setAction(blockAction);
        intentFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Test (expected = InvalidIntentParameterException.class)
    public void testShouldDoNothingWhenActionIsNull() {
        intentFlowManager.setAction(null);
        intentFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Test (expected = InvalidIntentParameterException.class)
    public void testShouldDoNothingWhenEndPointGroupsIsNull() {
        intentFlowManager.setEndPointGroups(null);
        intentFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Test
    public void testShouldDoNothingWithAnInvalidAction() {
        Action invalidAction = mock(Action.class);
        endPointGroups.add(SRC_END_POINT);
        endPointGroups.add(DST_END_POINT);

        intentFlowManager.setAction(invalidAction);
        intentFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Test
    public void testPushAddBlockFlow() {
        endPointGroups.add(SRC_END_POINT);
        endPointGroups.add(DST_END_POINT);

        intentFlowManager.setAction(blockAction);
        intentFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Test
    public void testPushAddAllowFlow() {
        endPointGroups.add(SRC_END_POINT);
        endPointGroups.add(DST_END_POINT);

        intentFlowManager.setAction(allowAction);
        intentFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Test (expected = InvalidParameterException.class)
    public void testPushPortFlowWithouConstraintsShouldDoNothing() {
        endPointGroups.add(SRC_PORT);
        endPointGroups.add(DST_PORT);

        intentFlowManager.setAction(allowAction);
        intentFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
    }
}
