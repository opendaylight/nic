/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.of.renderer.impl.IntentFlowManager;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by yrineu on 07/08/16.
 */
@PrepareForTest({DefaultExecutor.class, IntentUtils.class, TopologyUtils.class, InstanceIdentifier.class})
@RunWith(PowerMockRunner.class)
public class DefaultExecutorTest {

    @InjectMocks
    private DefaultExecutor defaultExecutorMock;
    @Mock
    private IntentFlowManager intentFlowManagerMock;
    @Mock
    private DataBroker dataBrokerMock;
    @Mock
    private Intent intentMock;
    @Mock
    private Action actionMock;
    @Mock
    private Actions actionsMock;
    @Mock
    private Node nodeMock;
    @Mock
    private NodeConnector nodeConnectorMock;
    @Mock
    private ReadOnlyTransaction readOnlyTransactionMock;
    @Mock
    private InstanceIdentifier<Nodes> instanceIdentifierMock;
    @Mock
    private CheckedFuture<Optional<Nodes>, ReadFailedException> checkedFutureMock;
    @Mock
    private Optional<Nodes> nodesOptionalMock;
    @Mock
    private Nodes nodeListMock;
    @Mock
    private NodeId nodeIdMock;
    @Mock
    private Set<Map.Entry<Node, List<NodeConnector>>> entrySetMock;

    @Before
    public void setUp() throws IntentInvalidException, ReadFailedException {
        PowerMockito.mockStatic(IntentUtils.class);
        PowerMockito.mockStatic(TopologyUtils.class);
        PowerMockito.mockStatic(InstanceIdentifier.class);

        final List<Actions> actions = Arrays.asList(actionsMock);
        final List<NodeConnector> nodeConnectors = Arrays.asList(nodeConnectorMock);
        final Map<Node, List<NodeConnector>> nodeConnectorsByNode = new HashMap<>();
        nodeConnectorsByNode.put(nodeMock, nodeConnectors);

        Mockito.when(intentMock.getActions()).thenReturn(actions);
        Mockito.when(actionsMock.getAction()).thenReturn(actionMock);
        Mockito.when(intentMock.getId()).thenReturn(Uuid.getDefaultInstance(UUID.randomUUID().toString()));
        Mockito.when(TopologyUtils.getNodes(dataBrokerMock)).thenReturn(nodeConnectorsByNode);
        Mockito.when(dataBrokerMock.newReadOnlyTransaction()).thenReturn(readOnlyTransactionMock);
        Mockito.when(InstanceIdentifier.create(Nodes.class)).thenReturn(instanceIdentifierMock);
        Mockito.when(readOnlyTransactionMock.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifierMock)).thenReturn(checkedFutureMock);
        Mockito.when(checkedFutureMock.checkedGet()).thenReturn(nodesOptionalMock);
        Mockito.when(nodesOptionalMock.get()).thenReturn(nodeListMock);

        defaultExecutorMock = new DefaultExecutor(intentFlowManagerMock, dataBrokerMock);
    }

    @Test (expected = IntentInvalidException.class)
    public void testShouldThrowsNoSuchElementExceptionWhenExecuteIntent() throws IntentInvalidException {
        defaultExecutorMock.execute(null, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteIntentByFlowAction() throws ReadFailedException, IntentInvalidException {
        defaultExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }
}
