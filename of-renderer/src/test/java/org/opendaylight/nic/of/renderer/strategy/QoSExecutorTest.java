/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.impl.QosConstraintManager;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by yrineu on 07/08/16.
 */
// TODO: Explore more scenarios for executeMplsIntentFlowManager()
@PrepareForTest({ TopologyUtils.class })
@RunWith(PowerMockRunner.class)
public class QoSExecutorTest {

    @InjectMocks
    private QoSExecutor qoSExecutorMock;

    @Mock
    private QosConstraintManager qosConstraintManagerMock;

    @Mock
    private DataBroker dataBroker;

    @Mock
    private Intent intentMock;

    @Mock
    private Node node;

    @Mock
    private NodeConnector nodeConnector;

    @Mock
    private NodeId nodeIdDeprecatedMock;

    private Map<Node, List<NodeConnector>> nodeMap;

    private List<NodeConnector> nodeConnectorList;

    @Before
    public void setUp() throws Exception {
        nodeConnectorList = Arrays.asList(nodeConnector);

        when(node.getId()).thenReturn(nodeIdDeprecatedMock);

        nodeMap = new HashMap<>();
        nodeMap.put(node, nodeConnectorList);

        PowerMockito.mockStatic(TopologyUtils.class);

        when(TopologyUtils.getNodes(dataBroker)).thenReturn(nodeMap);

        qoSExecutorMock = new QoSExecutor(qosConstraintManagerMock, dataBroker);
    }

    @Test
    public void testExecuteIntentAddFlow() throws Exception {
        qoSExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }
}
