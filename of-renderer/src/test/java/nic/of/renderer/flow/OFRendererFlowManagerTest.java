package nic.of.renderer.flow;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.reflect.Whitebox;

import nic.of.renderer.utils.GenericTransactionUtils;

public class OFRendererFlowManagerTest {

    @Spy
    @InjectMocks
    private OFRendererFlowManager ofRendererFlowManagerMock;

    private final String SRC_END_POINT = "00:00:00:00:00:01";
    private final String DST_END_POINT = "00:00:00:00:00:02";
    private final short DEFAULT_TABLE_ID = 0;
    private final String OUTPUT_PORT = "NORMAL";
    private List<String> endPointGroups;

    @Mock
    private MatchBuilder matchBuiderMock;

    @Mock
    private Action actionMock;

    @Mock
    private DataBroker dataBrokerMock;

    @Mock
    private NodeId nodeIdMock;

    @Mock
    private FlowBuilder flowBuilderMock;

    @Mock
    private Flow flowMock;

    @Mock
    private FlowKey flowKey;

    @Mock
    private NodeKey nodeKeyMock;

    @Mock
    private TableKey tableKeyMock;

    @Mock
    private WriteTransaction modificationMock;

    @Mock
    private InstanceIdentifier<Flow> instanceIdentifierMock;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        whenNew(FlowBuilder.class).withNoArguments().thenReturn(flowBuilderMock);
        when(flowBuilderMock.getKey()).thenReturn(flowKey);
        when(flowBuilderMock.build()).thenReturn(flowMock);
        whenNew(NodeKey.class).withArguments(nodeIdMock).thenReturn(nodeKeyMock);
        when(flowBuilderMock.getTableId()).thenReturn(DEFAULT_TABLE_ID);
        whenNew(TableKey.class).withArguments(flowBuilderMock.getTableId()).thenReturn(tableKeyMock);
        when(dataBrokerMock.newWriteOnlyTransaction()).thenReturn(modificationMock);
        when(GenericTransactionUtils.writeData(dataBrokerMock, LogicalDatastoreType.CONFIGURATION,
                instanceIdentifierMock, flowBuilderMock.build(), true)).thenReturn(true);

        endPointGroups = new ArrayList<String>();
        endPointGroups.add(0, DST_END_POINT);
        endPointGroups.add(1, SRC_END_POINT);
    }

    @Test
    public void testCreateAllowMatch() throws Exception {

        Action actionMock = mock(Allow.class);
        ofRendererFlowManagerMock.pushL2Flow(nodeIdMock, endPointGroups, actionMock);

        Whitebox.invokeMethod(ofRendererFlowManagerMock, "writeData", nodeIdMock, flowBuilderMock);
    }

    @Test
    public void testCreateBlockMatch() throws Exception {
        Action actionMock = mock(Block.class);
        ofRendererFlowManagerMock.pushL2Flow(nodeIdMock, endPointGroups, actionMock);

        Whitebox.invokeMethod(ofRendererFlowManagerMock, "writeData", nodeIdMock, flowBuilderMock);
    }
}
