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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({MatchUtils.class, MplsIntentFlowManager.class})
@RunWith(PowerMockRunner.class)
public class MplsIntentFlowManagerTest {
    private static final String MPLS_INTENT_EXPECTED_STRING = "MPLS_Rule_";
    private static final String EPG_SRC = "Site Src";
    private static final String EPG_DST = "Site Dst";
    private static final String MPLS_LABEL_KEY = "mpls_label";
    private static final String IP_PREFIX_KEY = "ip_prefix";
    private static final String SWITCH_PORT_KEY = "switch_port";
    private List<String> endPointGroups = null;
    private Map<String, Map<String, String>> subjectsMapping = null;
    @Mock private Allow action;
    private MplsIntentFlowManager mplsIntentFlowManager;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        mplsIntentFlowManager = mock(MplsIntentFlowManager.class, Mockito.CALLS_REAL_METHODS);
        endPointGroups = new ArrayList<>();
        endPointGroups.add(EPG_SRC);
        endPointGroups.add(EPG_DST);

        subjectsMapping = new HashMap<>();
        Map<String, String> srcMapping = new HashMap<>();
        srcMapping.put(MPLS_LABEL_KEY, "15");
        srcMapping.put(IP_PREFIX_KEY, "10.0.0.1/32");
        srcMapping.put(SWITCH_PORT_KEY, "openflow:1:2");
        subjectsMapping.put(EPG_SRC, srcMapping);

        Map<String, String> dstMapping = new HashMap<>();
        dstMapping.put(MPLS_LABEL_KEY, "1");
        dstMapping.put(IP_PREFIX_KEY, "10.0.0.2/32");
        dstMapping.put(SWITCH_PORT_KEY, "openflow:2:2");
        subjectsMapping.put(EPG_DST, dstMapping);

        MemberModifier.field(MplsIntentFlowManager.class, "endPointGroups").set(mplsIntentFlowManager, endPointGroups);
        MemberModifier.field(MplsIntentFlowManager.class, "subjectsMapping").set(mplsIntentFlowManager, subjectsMapping);
        MemberModifier.field(MplsIntentFlowManager.class, "action").set(mplsIntentFlowManager, action);

        PowerMockito.whenNew(MatchBuilder.class).withNoArguments().thenReturn(mock(MatchBuilder.class));
        FlowBuilder flowBldr = mock(FlowBuilder.class);
        PowerMockito.whenNew(FlowBuilder.class).withNoArguments().thenReturn(flowBldr);
        PowerMockito.whenNew(Ipv4Prefix.class).withAnyArguments().thenReturn(mock(Ipv4Prefix.class));

        PowerMockito.mockStatic(MatchUtils.class);
        PowerMockito.doNothing().when(MatchUtils.class, "createIPv4PrefixMatch", any(Ipv4Prefix.class), any(Ipv4Prefix.class), any(MatchBuilder.class));

        Instructions buildedInstructions = mock(Instructions.class);
        MemberModifier.suppress(MemberMatcher.method(MplsIntentFlowManager.class, "createMPLSIntentInstructions", List.class, boolean.class, Short.class, String.class, boolean.class));
        PowerMockito.when(mplsIntentFlowManager.createMPLSIntentInstructions(any(List.class), anyBoolean(), anyShort(), anyString(), anyBoolean())).thenReturn(buildedInstructions);

        when(flowBldr.setInstructions(any(Instructions.class))).thenReturn(flowBldr);
        MemberModifier.suppress(MemberMatcher.method(MplsIntentFlowManager.class, "writeDataTransaction", NodeId.class, FlowBuilder.class, FlowAction.class));
        when(mplsIntentFlowManager.writeDataTransaction(any(NodeId.class), any(FlowBuilder.class), any(FlowAction.class))).thenReturn(true);
    }

    @Test
    public void testPushMplsFlow() {
        mplsIntentFlowManager.pushMplsFlow(mock(NodeId.class), FlowAction.ADD_FLOW, "2");
        verify(mplsIntentFlowManager).writeDataTransaction(any(NodeId.class), any(FlowBuilder.class), any(FlowAction.class));
    }

    @Test
    public void testPopMplsFlow() {
      mplsIntentFlowManager.popMplsFlow(mock(NodeId.class), FlowAction.ADD_FLOW, new String("openflow:1"));
      verify(mplsIntentFlowManager).writeDataTransaction(any(NodeId.class), any(FlowBuilder.class), any(FlowAction.class));
    }

    @Test
    public void testForwardMplsFlow() throws Exception {
        mplsIntentFlowManager.forwardMplsFlow(mock(NodeId.class), FlowAction.ADD_FLOW, "2");
        verify(mplsIntentFlowManager).writeDataTransaction(any(NodeId.class), any(FlowBuilder.class), any(FlowAction.class));
    }

    @Test
    public void testCreateFlowName() {
        MplsIntentFlowManager mplsIntentFlowManager = new MplsIntentFlowManager(mock(DataBroker.class), mock(PipelineManager.class));
        mplsIntentFlowManager.setEndPointGroups(endPointGroups);
        String flowName = mplsIntentFlowManager.createFlowName();
        assertTrue(flowName.contains(MPLS_INTENT_EXPECTED_STRING));
    }
}
