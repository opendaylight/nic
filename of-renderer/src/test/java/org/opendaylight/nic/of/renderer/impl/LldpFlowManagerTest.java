/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@PrepareForTest({LldpFlowManager.class, FlowBuilder.class})
@RunWith(PowerMockRunner.class)
public class LldpFlowManagerTest {

    private LldpFlowManager lldpFlowManager;
    private static final String LLDP_EXPECTED_STRING = "lldpReplyToController_EthernetType_";
    private static final String MOCK_FLOW_NAME = "mock flow name";

    @Before
    public void setUp() {
        lldpFlowManager = mock(LldpFlowManager.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void testCreateFlowName() throws Exception {
        LldpFlowManager lldpFlowManager = new LldpFlowManager(mock(DataBroker.class), mock(PipelineManager.class));
        String flowName = lldpFlowManager.createFlowName();
        assertTrue(flowName.contains(LLDP_EXPECTED_STRING));
    }

    @Test
    public void testPushFlow() throws Exception {
        MemberModifier.suppress(MemberMatcher.method(LldpFlowManager.class, "writeDataTransaction", NodeId.class, FlowBuilder.class, FlowAction.class));
        MemberModifier.suppress(MemberMatcher.method(LldpFlowManager.class, "createLldpReplyToControllerFlow"));

        when(lldpFlowManager.writeDataTransaction(any(NodeId.class), any(FlowBuilder.class), any(FlowAction.class))).thenReturn(true);
        NodeId nodeId = mock(NodeId.class);
        lldpFlowManager.pushFlow(nodeId, FlowAction.ADD_FLOW);
        Mockito.verify(lldpFlowManager).writeDataTransaction(any(NodeId.class), any(FlowBuilder.class), any(FlowAction.class));
    }
}
