package org.opendaylight.nic.of.renderer.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.of.renderer.utils.FlowUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by yrineu on 30/05/16.
 */
@PrepareForTest(ArpFlowManager.class)
@RunWith(PowerMockRunner.class)
public class ArpFlowManagerTest {

    private ArpFlowManager arpFlowManager;
    private DataBroker dataBroker;
    private PipelineManager pipelineManager;
    private FlowBuilder flowBuilder;
    private NodeId nodeId;
    private FlowAction flowAction;
    private WriteTransaction writeTransaction;
    private CheckedFuture<Void, TransactionCommitFailedException> future;

    @Before
    public void setUp() throws TransactionCommitFailedException {
        dataBroker = mock(DataBroker.class);
        pipelineManager = mock(PipelineManager.class);
        nodeId = mock(NodeId.class);
        writeTransaction = mock(WriteTransaction.class);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        when(writeTransaction.submit()).thenReturn(future);
        arpFlowManager = spy(new ArpFlowManager(dataBroker, pipelineManager));
        flowAction = FlowAction.ADD_FLOW;
    }

    @Test
    public void testCreateArpReplyControllerFlow() {
        flowBuilder = arpFlowManager.createArpReplyToControllerFlow();
        Assert.assertEquals(flowBuilder.getPriority().intValue(),
                OFRendererConstants.ARP_REPLY_TO_CONTROLLER_FLOW_PRIORITY);
        Assert.assertEquals(flowBuilder.getIdleTimeout().intValue(), 0);
        Assert.assertEquals(flowBuilder.getHardTimeout().intValue(), 0);
        Assert.assertNotNull(flowBuilder.getCookie());
        Assert.assertNotNull(flowBuilder.getFlags());

        final Match match = flowBuilder.getMatch();
        final EthernetMatch ethernetMatch = FlowUtils.createEthernetMatch();

        Assert.assertEquals(match.getEthernetMatch(), ethernetMatch);
    }

    @Test
    public void testPushFlow() {
        MemberModifier.suppress(MemberMatcher.method(LldpFlowManager.class, "writeDataTransaction",
                NodeId.class, FlowBuilder.class, FlowAction.class));
        arpFlowManager.pushFlow(nodeId, flowAction);
        Mockito.verify(arpFlowManager).writeDataTransaction(any(NodeId.class),
                any(FlowBuilder.class), any(FlowAction.class));
    }
}
