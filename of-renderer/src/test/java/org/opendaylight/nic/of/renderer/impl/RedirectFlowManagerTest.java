/*
 * Copyright (c) 2016 NEC Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.controller.liblldp.HexEncode;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.sfc.provider.api.SfcProviderServiceForwarderAPI;
import org.opendaylight.sfc.provider.api.SfcProviderServiceFunctionAPI;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SffDataPlaneLocatorName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SffName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SfName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.base.SfDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarder.base.SffDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarder;
import org.opendaylight.yang.gen.v1.urn.ericsson.params.xml.ns.yang.sfc.sff.ofs.rev150408.port.details.OfsPort;
import org.opendaylight.yang.gen.v1.urn.ericsson.params.xml.ns.yang.sfc.sff.ofs.rev150408.SffDataPlaneLocator1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Destination;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FrameworkUtil.class, SfcProviderServiceFunctionAPI.class,
        SfcProviderServiceForwarderAPI.class, IntentUtils.class, MatchUtils.class, HexEncode.class, TopologyUtils.class })
public class RedirectFlowManagerTest {

    /**
     * Mock Instance of DataBroker to perform unit testing.
     */
    @Mock
    private DataBroker mockDataBroker;

    /**
     * Mock Instance of PipelineManager to perform unit testing.
     */
    @Mock
    private PipelineManager mockPipelineManager;

    /**
     * Instance of OFRendererGraphService to perform unit testing.
     */
    @Mock
    private OFRendererGraphService mockGraphService;

    /**
     * Instance of RedirectFlowManager to perform unit testing.
     */
    private RedirectFlowManager spyRedirectFlowManager;

    /**
     * Instance of MdsalUtils to perform unit testing.
     */
    @Mock
    private MdsalUtils mockMdsal;

    @Mock
    private PacketReceived mockPacketReceived;

    /**
     * Mock instance of ReadOnlyTransaction to perform unit testing.
     */
    @Mock
    private ReadOnlyTransaction mockReadOnlyTransaction;

    /**
     * Mock instance of WriteTransaction to perform unit testing.
     */
    @Mock
    private WriteTransaction mockWriteTransaction;

    /**
     * String constants to perform unit testing.
     */
    private final String[] nodeData = { "openflow:2", "5", "6", "Ingress", "Egress" };

    private final String[] nodeDataTwo = { "openflow:3", "2", "4", "Ingress", "Egress" };

    private final String[] ofsData = { "openflow:2:5", "openflow:2:6", "openflow:3:2", "openflow:3:4", "openflow:4:2",
            "openflow:3:1" };

    private final String serviceName = "srvc1";

    private final String[] uuid = { "888ec35e-a93f-42ea-ae57-ffa1862677d0", "666ec35e-a93f-42ea-ae57-alffa1862677d0" };

    private final String[] macAddress = { "6e:4f:f7:27:15:c9", "4f:6e:f7:27:12:a8", "00:0d:3f:cd:02:5f"};

    private final String[] invalidMacAddress = { "6e:4f:f7:27:15", "4f:6e:f7:27:12" };

    /**
     * It creates the required objects for every unit test cases.
     *
     */
    @Before
    public void setUp() throws Exception {
        /*
         * Here,it creates required mock objects and defines mocking
         * functionality for mock objects.
         */
        PowerMockito.mockStatic(FrameworkUtil.class);

        final Bundle mockBundle = PowerMockito.mock(Bundle.class);

        PowerMockito.when(mockBundle.getBundleContext()).thenReturn(PowerMockito.mock(BundleContext.class));
        PowerMockito.when(FrameworkUtil.getBundle(RedirectFlowManager.class)).thenReturn(mockBundle);

        spyRedirectFlowManager = PowerMockito.spy(new RedirectFlowManager(mockDataBroker, mockPipelineManager, mockGraphService));
    }

    /**
     * Test case for {@link RedirectFlowManager#onPacketReceived()}. Here
     * checking invalid scenario by passing invalid ethernet type then it
     * should return from method execution.
     */
    @Test
    public void testOnPacketReceivedNonArpPackage() throws Exception {
        byte[] payload = new byte[14];

        payload[12] = 1;
        payload[13] = 2;

        Mockito.when(mockPacketReceived.getPayload()).thenReturn(payload);

        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
    }

    /**
     * Test case for {@link RedirectFlowManager#onPacketReceived()}. Here
     * checking invalid scenario, if source mac is null, then it should return
     * from method execution with out adding node to cache map.
     */
    @Test
    public void testOnPacketReceivedArpPackageWithEmptySourceMacAddress()
            throws Exception {
        byte[] source = new byte[6];
        source[0] = 7;
        source[1] = 8;
        source[2] = 9;
        source[3] = 10;
        source[4] = 11;
        source[5] = 12;

        byte[] payload = new byte[14];
        payload[6] = source[0];
        payload[7] = source[1];
        payload[8] = source[2];
        payload[9] = source[3];
        payload[10] = source[4];
        payload[11] = source[5];

        // EtherType
        payload[12] = 8;
        payload[13] = 6;

        Mockito.when(mockPacketReceived.getPayload()).thenReturn(payload);

        final NodeConnectorId mockNodeConnectorId = Mockito
                .mock(NodeConnectorId.class);
        Mockito.when(mockNodeConnectorId.getValue())
                .thenReturn("mockNode");

        final NodeConnectorKey mockNodeConnectorKey = Mockito
                .mock(NodeConnectorKey.class);
        Mockito.when(mockNodeConnectorKey.getId())
                .thenReturn(mockNodeConnectorId);

        final InstanceIdentifier mockInstanceIdentifier = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, mock(NodeKey.class))
                .child(NodeConnector.class, mockNodeConnectorKey).build();

        final NodeConnectorRef mockNodeConnectorRef = Mockito
                .mock(NodeConnectorRef.class);
        Mockito.when(mockNodeConnectorRef.getValue())
                .thenReturn(mockInstanceIdentifier);

        Mockito.when(mockPacketReceived.getIngress())
                .thenReturn(mockNodeConnectorRef);

        PowerMockito.mockStatic(HexEncode.class);
        PowerMockito
                .when(HexEncode
                        .bytesToHexStringFormat(source))
                .thenReturn(null);

        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
    }

    /**
     * Test case for {@link RedirectFlowManager#onPacketReceived()}. Here
     * checking invalid scenario, if destination mac is null, then it should
     * return from method execution with out adding node to cache map.
     */
    @Test
    public void testOnPacketReceivedWithEmptyDestinationMacAddress()
            throws Exception {
        byte[] destination = new byte[6];
        destination[0] = 1;
        destination[1] = 2;
        destination[2] = 3;
        destination[3] = 4;
        destination[4] = 5;
        destination[5] = 6;

        byte[] source = new byte[6];
        source[0] = 7;
        source[1] = 8;
        source[2] = 9;
        source[3] = 10;
        source[4] = 11;
        source[5] = 12;

        byte[] payload = new byte[14];
        payload[0] = destination[0];
        payload[1] = destination[1];
        payload[2] = destination[2];
        payload[3] = destination[3];
        payload[4] = destination[4];
        payload[5] = destination[5];

        payload[6] = source[0];
        payload[7] = source[1];
        payload[8] = source[2];
        payload[9] = source[3];
        payload[10] = source[4];
        payload[11] = source[5];

        // EtherType
        payload[12] = 8;
        payload[13] = 6;

        when(mockPacketReceived.getPayload()).thenReturn(payload);

        final NodeConnectorId mockNodeConnectorId = Mockito
                .mock(NodeConnectorId.class);
        final NodeConnectorKey mockNodeConnectorKey = Mockito
                .mock(NodeConnectorKey.class);
        when(mockNodeConnectorKey.getId())
                .thenReturn(mockNodeConnectorId);

        final InstanceIdentifier mockInstanceIdentifier = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, mock(NodeKey.class))
                .child(NodeConnector.class, mockNodeConnectorKey).build();

        final NodeConnectorRef mockNodeConnectorRef = Mockito
                .mock(NodeConnectorRef.class);
        when(mockNodeConnectorRef.getValue())
                .thenReturn(mockInstanceIdentifier);

        when(mockPacketReceived.getIngress())
                .thenReturn(mockNodeConnectorRef);

        PowerMockito.mockStatic(HexEncode.class);
        PowerMockito.when(HexEncode.bytesToHexStringFormat(source))
                .thenReturn("07:08:09:10:11:12");
        PowerMockito.when(HexEncode.bytesToHexStringFormat(destination))
                .thenReturn(null);

        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
    }

    /**
     * Test case for {@link RedirectFlowManager#onPacketReceived()}. Here
     * checking invalid scenario, if source, destination mac are valid but node
     * is not internal node, then it should return from method execution with
     * out adding node to cache map.
     */
    @Test
    public void testOnPacketReceivedWithoutNodeConnectorInternal() throws Exception {
        byte[] destination = new byte[6];
        destination[0] = 1;
        destination[1] = 2;
        destination[2] = 3;
        destination[3] = 4;
        destination[4] = 5;
        destination[5] = 6;

        byte[] source = new byte[6];
        source[0] = 7;
        source[1] = 8;
        source[2] = 9;
        source[3] = 10;
        source[4] = 11;
        source[5] = 12;

        byte[] payload = new byte[14];
        payload[0] = destination[0];
        payload[1] = destination[1];
        payload[2] = destination[2];
        payload[3] = destination[3];
        payload[4] = destination[4];
        payload[5] = destination[5];

        payload[6] = source[0];
        payload[7] = source[1];
        payload[8] = source[2];
        payload[9] = source[3];
        payload[10] = source[4];
        payload[11] = source[5];

        // EtherType
        payload[12] = 8;
        payload[13] = 6;

        when(mockPacketReceived.getPayload()).thenReturn(payload);

        final ReadOnlyTransaction mockReadOnlyTransaction = mock(ReadOnlyTransaction.class);
        final CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);
        when(mockReadOnlyTransaction.read(Matchers.any(LogicalDatastoreType.class),
                Matchers.any(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);

        org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId mockNodeId = Mockito
                .mock(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId.class);
        when(mockNodeId.getValue()).thenReturn("host:......");

        String nodeConnectorId = "1";

        final TpId mockTpId = new TpId(nodeConnectorId);
        final Source mockSource = mock(Source.class);
        when(mockSource.getSourceNode()).thenReturn(mockNodeId);
        when(mockSource.getSourceTp()).thenReturn(mockTpId);

        final Destination mockDestination = mock(Destination.class);
        when(mockDestination.getDestNode()).thenReturn(mockNodeId);
        when(mockDestination.getDestTp()).thenReturn(mockTpId);

        final Link mockLink = mock(Link.class);
        when(mockLink.getSource()).thenReturn(mockSource);
        when(mockLink.getDestination()).thenReturn(mockDestination);

        final List mockLinkList = new ArrayList();
        mockLinkList.add(mockLink);

        final Topology mockTopology = mock(Topology.class);
        when(mockTopology.getLink()).thenReturn(mockLinkList);

        final List mockNetworkTopologyList = new ArrayList();
        mockNetworkTopologyList.add(mockTopology);

        final NetworkTopology mockNetworkTopology = mock(NetworkTopology.class);
        when(mockNetworkTopology.getTopology()).thenReturn(mockNetworkTopologyList);

        final Optional mockOptional = mock(Optional.class);
        when(mockOptional.isPresent()).thenReturn(true);
        when(mockOptional.get()).thenReturn(mockNetworkTopology);
        when(mockCheckedFuture.get()).thenReturn(mockOptional);

        final NodeConnectorId mockNodeConnectorId = Mockito
                .mock(NodeConnectorId.class);
        final NodeConnectorKey mockNodeConnectorKey = Mockito
                .mock(NodeConnectorKey.class);
        when(mockNodeConnectorKey.getId())
                .thenReturn(mockNodeConnectorId);
        when(mockNodeConnectorId.getValue()).thenReturn(nodeConnectorId);

        final InstanceIdentifier mockInstanceIdentifier = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, mock(NodeKey.class))
                .child(NodeConnector.class, mockNodeConnectorKey).build();

        final NodeConnectorRef mockNodeConnectorRef = Mockito
                .mock(NodeConnectorRef.class);
        when(mockNodeConnectorRef.getValue())
                .thenReturn(mockInstanceIdentifier);

        when(mockPacketReceived.getIngress())
                .thenReturn(mockNodeConnectorRef);

        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
    }

    /**
     * Test case for {@link RedirectFlowManager#readRedirectSfcData()}. Here
     * verifying readRedirectSfcData() should return empty String[], if the
     * service name is null
     */
    @Test
    public void testReadRedirectSfcDataWithEmptyServiceName() throws Exception {
        String[] output = (String[]) Whitebox.invokeMethod(
                spyRedirectFlowManager, "readRedirectSfcData", null);
        Assert.assertEquals("Length should be two", output.length, 2);
        Assert.assertEquals("Ingress should be null", output[0], null);
        Assert.assertEquals("Egress should be null", output[1], null);
    }

    /**
     * Test case for {@link RedirectFlowManager#readRedirectSfcData()}. Here
     * verifying readRedirectSfcData() should return String[] which contains
     * Ingress and Egress nodes details, if the service name is valid.
     */
    @Test
    public void testReadRedirectSfcData() throws Exception {
        /*
         * Here providing the required mock functionality for SFC classes.
         */
        PowerMockito.mockStatic(SfcProviderServiceFunctionAPI.class);
        PowerMockito.mockStatic(SfcProviderServiceForwarderAPI.class);

        final SffName mockSffName = mock(SffName.class);
        when(mockSffName.getValue()).thenReturn(nodeData[0]);

        SfDataPlaneLocator mockSfDataPlaneLocator = mock(SfDataPlaneLocator.class);
        when(mockSfDataPlaneLocator.getServiceFunctionForwarder()).thenReturn(mockSffName);

        final List<SfDataPlaneLocator> mockList = new ArrayList<SfDataPlaneLocator>();
        mockList.add(mockSfDataPlaneLocator);

        final ServiceFunction mockServiceFunction = mock(ServiceFunction.class);
        when(mockServiceFunction.getSfDataPlaneLocator()).thenReturn(mockList);
        when(SfcProviderServiceFunctionAPI.readServiceFunction(Matchers.any(SfName.class)))
                .thenReturn(mockServiceFunction);

        final OfsPort mockOfsPort = mock(OfsPort.class);
        when(mockOfsPort.getPortId()).thenReturn(nodeData[1], nodeData[2]);

        final SffDataPlaneLocatorName mockSffDataPlaneLocatorName = mock(SffDataPlaneLocatorName.class);
        when(mockSffDataPlaneLocatorName.getValue()).thenReturn(nodeData[3], nodeData[4]);

        final SffDataPlaneLocator1 mockSffDataPlaneLocator1 = mock(SffDataPlaneLocator1.class);
        when(mockSffDataPlaneLocator1.getOfsPort()).thenReturn(mockOfsPort);

        final SffDataPlaneLocator mockSffDataPlaneLocatorOne = mock(SffDataPlaneLocator.class);
        when(mockSffDataPlaneLocatorOne.getName()).thenReturn(mockSffDataPlaneLocatorName);
        when(mockSffDataPlaneLocatorOne.getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);

        final SffDataPlaneLocator mockSffDataPlaneLocatorTwo = mock(SffDataPlaneLocator.class);
        when(mockSffDataPlaneLocatorTwo.getName()).thenReturn(mockSffDataPlaneLocatorName);
        when(mockSffDataPlaneLocatorTwo.getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);

        final List<SffDataPlaneLocator> mockListSffDPL = new ArrayList<SffDataPlaneLocator>();
        mockListSffDPL.add(mockSffDataPlaneLocatorOne);
        mockListSffDPL.add(mockSffDataPlaneLocatorTwo);

        final ServiceFunctionForwarder mockServiceFunctionForwarder = mock(ServiceFunctionForwarder.class);
        when(mockServiceFunctionForwarder.getSffDataPlaneLocator()).thenReturn(mockListSffDPL);
        when(SfcProviderServiceForwarderAPI.readServiceFunctionForwarder(Matchers.any(SffName.class)))
                .thenReturn(mockServiceFunctionForwarder);

        String[] output = (String[]) Whitebox.invokeMethod(spyRedirectFlowManager, "readRedirectSfcData", serviceName);
        Assert.assertEquals("Length of the output array should be 2 ", 2, output.length);
        Assert.assertEquals("Should return expected Ingress node", ofsData[0], output[0]);
        Assert.assertEquals("Should return expected Egress node", ofsData[1], output[1]);
    }

    /**
     * Test case for {@link RedirectFlowManager#addSfcNodeInfoToCache()}. Here
     * checking size of the cache map before and after executing
     * addSfcNodeInfoToCache() and checking whether addSfcNodeInfoToCache()
     * returning expected Ingress/Egress nodes data.
     */
    @Test
    public void testAddSfcNodeInfoToCache() throws Exception {
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.Redirect mockInnerRedirect = Mockito
                .mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.Redirect.class);
        when(mockInnerRedirect.getServiceName()).thenReturn(serviceName);

        PowerMockito.mockStatic(SfcProviderServiceFunctionAPI.class);
        PowerMockito.mockStatic(SfcProviderServiceForwarderAPI.class);

        final SffName mockSffName = mock(SffName.class);
        when(mockSffName.getValue()).thenReturn(nodeDataTwo[0]);

        SfDataPlaneLocator mockSfDataPlaneLocator = mock(SfDataPlaneLocator.class);
        when(mockSfDataPlaneLocator.getServiceFunctionForwarder()).thenReturn(mockSffName);

        final List<SfDataPlaneLocator> mockList = new ArrayList<SfDataPlaneLocator>();
        mockList.add(mockSfDataPlaneLocator);

        final ServiceFunction mockServiceFunction = mock(ServiceFunction.class);
        when(mockServiceFunction.getSfDataPlaneLocator()).thenReturn(mockList);
        PowerMockito.when(SfcProviderServiceFunctionAPI.readServiceFunction(Matchers.any(SfName.class)))
                .thenReturn(mockServiceFunction);

        final OfsPort mockOfsPort = mock(OfsPort.class);
        when(mockOfsPort.getPortId()).thenReturn(nodeDataTwo[1], nodeDataTwo[2]);

        final SffDataPlaneLocatorName mockSffDataPlaneLocatorName = mock(SffDataPlaneLocatorName.class);
        when(mockSffDataPlaneLocatorName.getValue()).thenReturn(nodeDataTwo[3], nodeDataTwo[4]);

        final SffDataPlaneLocator1 mockSffDataPlaneLocator1 = mock(SffDataPlaneLocator1.class);
        when(mockSffDataPlaneLocator1.getOfsPort()).thenReturn(mockOfsPort);

        final SffDataPlaneLocator mockSffDataPlaneLocatorOne = mock(SffDataPlaneLocator.class);
        when(mockSffDataPlaneLocatorOne.getName()).thenReturn(mockSffDataPlaneLocatorName);
        when(mockSffDataPlaneLocatorOne.getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);

        final SffDataPlaneLocator mockSffDataPlaneLocatorTwo = mock(SffDataPlaneLocator.class);
        when(mockSffDataPlaneLocatorTwo.getName()).thenReturn(mockSffDataPlaneLocatorName);
        when(mockSffDataPlaneLocatorTwo.getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);

        final List<SffDataPlaneLocator> mockListSffDPL = new ArrayList<SffDataPlaneLocator>();
        mockListSffDPL.add(mockSffDataPlaneLocatorOne);
        mockListSffDPL.add(mockSffDataPlaneLocatorTwo);

        final ServiceFunctionForwarder mockServiceFunctionForwarder = mock(ServiceFunctionForwarder.class);
        when(mockServiceFunctionForwarder.getSffDataPlaneLocator()).thenReturn(mockListSffDPL);
        PowerMockito.when(SfcProviderServiceForwarderAPI.readServiceFunctionForwarder(Matchers.any(SffName.class)))
                .thenReturn(mockServiceFunctionForwarder);

        final Redirect mockRedirect = mock(Redirect.class);
        when(mockRedirect.getRedirect()).thenReturn(mockInnerRedirect);

        final Actions mockActions = mock(Actions.class);
        when(mockActions.getAction()).thenReturn(mockRedirect);

        final List<Actions> mockActionsList = new ArrayList<Actions>();
        mockActionsList.add(mockActions);

        final Intent mockIntent = mock(Intent.class);
        when(mockIntent.getId()).thenReturn(new Uuid(uuid[0]));
        when(mockIntent.getActions()).thenReturn(mockActionsList);

        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);

        FlowId mockFlowId = new FlowId("arpReplyToController_EthernetType");

        Flow mockFlow = mock(Flow.class);
        when(mockFlow.getId()).thenReturn(mockFlowId);

        List<Flow> mockFlowList = new ArrayList<>();
        mockFlowList.add(mockFlow);

        Table mockTable = mock(Table.class);
        when(mockTable.getFlow()).thenReturn(mockFlowList);

        Optional mockOptional = mock(Optional.class);
        when(mockOptional.isPresent()).thenReturn(true);
        when(mockOptional.get()).thenReturn(mockTable);

        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);
        when(mockReadOnlyTransaction.read(Matchers.eq(LogicalDatastoreType.CONFIGURATION),
                Matchers.any(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        when(mockWriteTransaction.submit()).thenReturn(mockCheckedFuture);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);

        final int beforeSize = spyRedirectFlowManager.getredirectNodeCache().size();

        Whitebox.invokeMethod(spyRedirectFlowManager, "addSfcNodeInfoToCache", mockIntent);

        final int afterSize = spyRedirectFlowManager.getredirectNodeCache().size();
        Assert.assertEquals("Initial size should be zero", 0, beforeSize);
        Assert.assertEquals("After execution, size must be increased", 1, afterSize);

        RedirectNodeData data = spyRedirectFlowManager.getredirectNodeCache().get(mockIntent.getId().getValue());
        Assert.assertEquals("Should return expected Ingress", ofsData[2], data.getIngressNodeId());
        Assert.assertEquals("Should return expected Egress", ofsData[3], data.getEgressNodeId());
    }

    /**
     * Test case for {@link RedirectFlowManager#addMacNodeToCache()}
     */

    @Test
    public void testAddMacNodeToCache() throws Exception {
        /*
         * Here checking invalid scenario by passing mac address which does not
         * exist in the cache, it should not add any data to that cache
         */

        final int beforeSize = spyRedirectFlowManager.getredirectNodeCache().size();
        Whitebox.invokeMethod(spyRedirectFlowManager, "addMacNodeToCache", macAddress[0], ofsData[4]);

        final int afterSize = spyRedirectFlowManager.getredirectNodeCache().size();
        Assert.assertEquals("Initial size should be zero", 0, beforeSize);
        Assert.assertEquals("Size should be same for invalid scenario", beforeSize, afterSize);

        /*
         * Here providing mock functionality for required classes.
         */
        final RedirectNodeData mockDataOne = mock(RedirectNodeData.class);
        when(mockDataOne.toString()).thenReturn(" mock object :" + macAddress[0]);
        when(mockDataOne.getSrcMacNodeId()).thenReturn(macAddress[0]);
        when(mockDataOne.getIngressNodeId()).thenReturn(macAddress[1]);
        when(mockDataOne.getEgressNodeId()).thenReturn(macAddress[0]);
        when(mockDataOne.getDestMacNodeId()).thenReturn(macAddress[1]);

        final RedirectNodeData mockDataTwo = mock(RedirectNodeData.class);
        when(mockDataTwo.toString()).thenReturn(" mock object :" + macAddress[1]);
        when(mockDataTwo.getSrcMacNodeId()).thenReturn(macAddress[1]);
        when(mockDataTwo.getIngressNodeId()).thenReturn(macAddress[0]);
        when(mockDataTwo.getEgressNodeId()).thenReturn(macAddress[1]);
        when(mockDataTwo.getDestMacNodeId()).thenReturn(macAddress[0]);

        spyRedirectFlowManager.getredirectNodeCache().put(uuid[0], mockDataOne);

        PowerMockito.mockStatic(IntentUtils.class);

        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);

        PowerMockito.when(IntentUtils.extractEndPointGroup(null)).thenReturn(epgList);

        /*
         * Here checking valid scenario by passing mac address, which exist in
         * the cache map and it should add node id to particular record in the
         * cache map. If record contains all nodes data and should starts
         * writing flow, after finishing need to change flag value.
         */

        Whitebox.invokeMethod(spyRedirectFlowManager, "addMacNodeToCache", macAddress[0], ofsData[4]);

        Mockito.verify(mockDataOne).setSrcMacNodeId(Matchers.any(String.class));
        Mockito.verify(mockDataOne).setFlowApplied(true);

        spyRedirectFlowManager.getredirectNodeCache().put(uuid[1], mockDataTwo);

        Whitebox.invokeMethod(spyRedirectFlowManager, "addMacNodeToCache", macAddress[1], ofsData[5]);

        Mockito.verify(mockDataTwo).setDestMacNodeId(Matchers.any(String.class));
        Mockito.verify(mockDataTwo, Mockito.times(1)).setFlowApplied(true);
    }

    /**
     * Test case for {@link RedirectFlowManager#redirectFlowConstruction()}
     */
    @Test
    public void testRedirectFlowConstruction() throws Exception {
        /*
         * Here creating required mock objects.
         */
        PowerMockito.mockStatic(SfcProviderServiceFunctionAPI.class);
        PowerMockito.mockStatic(SfcProviderServiceForwarderAPI.class);

        final Intent mockIntent = mock(Intent.class);
        when(mockIntent.getId()).thenReturn(new Uuid(uuid[0]));

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.Redirect mockInnerRedirect = Mockito
                .mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.Redirect.class);
        when(mockInnerRedirect.getServiceName())
                .thenReturn(serviceName);

        final Redirect mockRedirect = mock(Redirect.class);
        when(mockRedirect.getRedirect())
                .thenReturn(mockInnerRedirect);

        final Actions mockActions = mock(Actions.class);
        when(mockActions.getAction()).thenReturn(mockRedirect);

        final List<Actions> mockActionsList = new ArrayList<Actions>();
        mockActionsList.add(mockActions);
        when(mockIntent.getActions()).thenReturn(mockActionsList);

        final FlowAction addFlow = FlowAction.ADD_FLOW;
        final FlowAction removeFlowAction = FlowAction.REMOVE_FLOW;

        final SffName mockSffName = mock(SffName.class);
        when(mockSffName.getValue()).thenReturn(nodeDataTwo[0]);

        SfDataPlaneLocator mockSfDataPlaneLocator = Mockito
                .mock(SfDataPlaneLocator.class);
        when(mockSfDataPlaneLocator.getServiceFunctionForwarder())
                .thenReturn(mockSffName);

        final List<SfDataPlaneLocator> mockListSfDataPlaceLocator = new ArrayList<SfDataPlaneLocator>();
        mockListSfDataPlaceLocator.add(mockSfDataPlaneLocator);

        final ServiceFunction mockServiceFunction = Mockito
                .mock(ServiceFunction.class);
        when(mockServiceFunction.getSfDataPlaneLocator())
                .thenReturn(mockListSfDataPlaceLocator);
        PowerMockito
                .when(SfcProviderServiceFunctionAPI
                        .readServiceFunction(Matchers.any(SfName.class)))
                .thenReturn(mockServiceFunction);

        final OfsPort mockOfsPort = mock(OfsPort.class);
        when(mockOfsPort.getPortId()).thenReturn(nodeDataTwo[1],
                nodeDataTwo[2]);

        final SffDataPlaneLocatorName mockSffDataPlaneLocatorName = Mockito
                .mock(SffDataPlaneLocatorName.class);
        when(mockSffDataPlaneLocatorName.getValue())
                .thenReturn(nodeDataTwo[3], nodeDataTwo[4]);

        final SffDataPlaneLocator1 mockSffDataPlaneLocator1 = Mockito
                .mock(SffDataPlaneLocator1.class);
        when(mockSffDataPlaneLocator1.getOfsPort())
                .thenReturn(mockOfsPort);

        final SffDataPlaneLocator mockSffDataPlaneLocatorOne = Mockito
                .mock(SffDataPlaneLocator.class);
        when(mockSffDataPlaneLocatorOne.getName())
                .thenReturn(mockSffDataPlaneLocatorName);
        Mockito
                .when(mockSffDataPlaneLocatorOne
                        .getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);

        final SffDataPlaneLocator mockSffDataPlaneLocatorTwo = Mockito
                .mock(SffDataPlaneLocator.class);
        when(mockSffDataPlaneLocatorTwo.getName())
                .thenReturn(mockSffDataPlaneLocatorName);
        Mockito
                .when(mockSffDataPlaneLocatorTwo
                        .getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);

        final List<SffDataPlaneLocator> mockListSffDPL = new ArrayList<SffDataPlaneLocator>();
        mockListSffDPL.add(mockSffDataPlaneLocatorOne);
        mockListSffDPL.add(mockSffDataPlaneLocatorTwo);

        final ServiceFunctionForwarder mockServiceFunctionForwarder = Mockito
                .mock(ServiceFunctionForwarder.class);
        when(mockServiceFunctionForwarder.getSffDataPlaneLocator())
                .thenReturn(mockListSffDPL);
        PowerMockito.when(SfcProviderServiceForwarderAPI
                .readServiceFunctionForwarder(Matchers.any(SffName.class)))
                .thenReturn(mockServiceFunctionForwarder);

        /*
         * Here checking invalid scenarios by passing intent, flow action
         * objects as null. If either intent or flow action is null, it should
         * return.
         */
        spyRedirectFlowManager.redirectFlowConstruction(mockIntent, null);

        spyRedirectFlowManager.redirectFlowConstruction(null, addFlow);

        /*
         * Here checking valid scenarios by passing valid intent, flow action
         * objects. If flow action is add, it should add particular intent
         * record to cache map. If flow action is remove, it should delete
         * particular intent record to cache map.
         */

        spyRedirectFlowManager.getredirectNodeCache().put(uuid[0],
                mock(RedirectNodeData.class));

        final int beforeSize = spyRedirectFlowManager.getredirectNodeCache()
                .size();

        Assert.assertEquals("Record should exist", beforeSize, 1);
        Assert.assertNotNull("Record should exist",
                spyRedirectFlowManager.getredirectNodeCache().get(uuid[0]));

        org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId mockNodeId = Mockito
                .mock(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId.class);
        when(mockNodeId.getValue()).thenReturn("mockNode");

        org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node mockNode = Mockito
                .mock(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node.class);
        when(mockNode.getNodeId()).thenReturn(mockNodeId);

        List<Topology> mockTopologyList = new ArrayList<Topology>();
        Topology mockTopology = PowerMockito.mock(Topology.class);
        mockTopologyList.add(mockTopology);

        List<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node> mockList = new ArrayList<org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node>();
        mockList.add(mockNode);
        PowerMockito.doReturn(mockList).when(mockTopology, "getNode");

        NetworkTopology mockNetworkTopology = PowerMockito
                .mock(NetworkTopology.class);
        PowerMockito.doReturn(mockTopologyList).when(mockNetworkTopology,
                "getTopology");

        Optional<NetworkTopology> mockOptional = mock(Optional.class);
        when(mockOptional.isPresent()).thenReturn(true);
        when(mockOptional.get()).thenReturn(mockNetworkTopology);

        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);

        mockReadOnlyTransaction = mock(ReadOnlyTransaction.class);
        when(mockReadOnlyTransaction.read(
                Matchers.eq(LogicalDatastoreType.OPERATIONAL),
                Matchers.any(InstanceIdentifier.class)))
                .thenReturn(mockCheckedFuture);
        when(mockDataBroker.newReadOnlyTransaction())
                .thenReturn(mockReadOnlyTransaction);

        FlowId mockFlowId = new FlowId("arpReplyToController_EthernetType");
        Flow mockFlow = mock(Flow.class);
        when(mockFlow.getId()).thenReturn(mockFlowId);

        List<Flow> mockListTable = new ArrayList<Flow>();
        mockListTable.add(mockFlow);

        Table mockTable = mock(Table.class);
        when(mockTable.getFlow()).thenReturn(mockListTable);

        Optional<Table> mockOptionalTable = mock(Optional.class);
        when(mockOptionalTable.isPresent()).thenReturn(true);
        when(mockOptionalTable.get()).thenReturn(mockTable);

        CheckedFuture mockCheckedFutureTable = Mockito
                .mock(CheckedFuture.class);
        when(mockCheckedFutureTable.checkedGet())
                .thenReturn(mockOptionalTable);

        when(mockReadOnlyTransaction.read(
                Matchers.eq(LogicalDatastoreType.CONFIGURATION),
                Matchers.any(InstanceIdentifier.class)))
                .thenReturn(mockCheckedFutureTable);
        when(mockWriteTransaction.submit())
                .thenReturn(mockCheckedFutureTable);
        when(mockDataBroker.newWriteOnlyTransaction())
                .thenReturn(mockWriteTransaction);

        PowerMockito.mockStatic(IntentUtils.class);

        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);

        PowerMockito.when(IntentUtils.extractEndPointGroup(mockIntent))
                .thenReturn(epgList);

        spyRedirectFlowManager = PowerMockito.spy(new RedirectFlowManager(
                mockDataBroker, mockPipelineManager, mockGraphService));

        spyRedirectFlowManager.redirectFlowConstruction(mockIntent,
                removeFlowAction);
        final int afterSize = spyRedirectFlowManager.getredirectNodeCache()
                .size();

        Assert.assertEquals("Record should be deleted", afterSize, 0);
        Assert.assertNull("Record should not exist.",
                spyRedirectFlowManager.getredirectNodeCache().get(uuid[0]));
    }

    /**
     * Test case for {@link RedirectFlowManager#generateRedirectFlows()}
     */
    @Test
    public void testGenerateRedirectFlows() throws Exception {
        PowerMockito.mockStatic(TopologyUtils.class);
        /*
         * Here creating required objects.
         */
        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[0]);
        final FlowAction addFlow = FlowAction.ADD_FLOW;
        final org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId mockNodeId = mock(
                org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId.class);
        /*
         * Here checking valid scenarios, by passing valid graphService which
         * does not having any shortest path links.
         */
        when(mockNodeId.getValue()).thenReturn("mockNode");

        PowerMockito.when(TopologyUtils.extractTopologyNodeId(Matchers.any(String.class))).thenReturn(mockNodeId);

        OFRendererGraphService mockOFRendererGraphService = mock(OFRendererGraphService.class);
        when(mockOFRendererGraphService.getShortestPath(mockNodeId, mockNodeId)).thenReturn(new ArrayList());

        Whitebox.setInternalState(spyRedirectFlowManager, "graphService", mockOFRendererGraphService);

        PowerMockito.mockStatic(MatchUtils.class);
        MatchUtils.createEthMatch(Matchers.any(MatchBuilder.class), Matchers.any(MacAddress.class),
                Matchers.any(MacAddress.class));

        final ReadWriteTransaction mockWriteOnlyTransaction = Mockito.spy(
                ReadWriteTransaction.class);
        final CheckedFuture mockCheckedFuture = Mockito.spy(CheckedFuture.class);
        when(mockWriteOnlyTransaction.submit()).thenReturn(mockCheckedFuture);
        when(mockDataBroker.newWriteOnlyTransaction())
                .thenReturn(mockWriteOnlyTransaction);

        Whitebox.invokeMethod(spyRedirectFlowManager, "generateRedirectFlows", epgList, "", "", addFlow);

        /*
         * Here checking valid scenarios, by passing valid graphService which
         * having shortest path links.
         */
        final TpId mockTpId = PowerMockito.mock(TpId.class);
        when(mockTpId.getValue()).thenReturn("01");

        final Source mockSource = PowerMockito.mock(Source.class);
        PowerMockito.when(mockSource.getSourceNode()).thenReturn(mockNodeId);
        PowerMockito.when(mockSource.getSourceTp()).thenReturn(mockTpId);

        final Destination mockDestination = PowerMockito.mock(Destination.class);
        PowerMockito.when(mockDestination.getDestNode()).thenReturn(mockNodeId);
        PowerMockito.when(mockDestination.getDestTp()).thenReturn(mockTpId);

        final Link mockLink = PowerMockito.mock(Link.class);
        PowerMockito.when(mockLink.getSource()).thenReturn(mockSource);
        PowerMockito.when(mockLink.getDestination()).thenReturn(mockDestination);

        final List<Link> mockList = new ArrayList<Link>();
        mockList.add(mockLink);

        when(mockOFRendererGraphService.getShortestPath(mockNodeId, mockNodeId)).thenReturn(mockList);

        Whitebox.setInternalState(spyRedirectFlowManager, "graphService", mockOFRendererGraphService);

        /*
         * Here checking write flows in both the Last and intermediate node to
         * last node in the path.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "generateRedirectFlows", epgList, "srcCNId1", "destCNId1",
                addFlow);

        when(mockNodeId.getValue()).thenReturn("mockNode", "mockNode", "mockNode", "mockNode", "mockNode2",
                "mockNode3", null);

        /*
         * Here checking write flows to intermediate node in the path.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "generateRedirectFlows", epgList, "srcCNId1", "destCNId1",
                addFlow);
    }

    /**
     * Test case for {@link RedirectFlowManager#isNodeConnectorInternal()}
     */
    @Test
    public void testIsNodeConnectorInternal() throws Exception {
        /*
         * Here creating required mock objects.
         */
        final ReadOnlyTransaction mockReadOnlyTransaction = mock(ReadOnlyTransaction.class);
        final CheckedFuture mockCheckedFuture = PowerMockito.mock(CheckedFuture.class);
        when(mockReadOnlyTransaction.read(Matchers.any(LogicalDatastoreType.class),
                Matchers.any(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);
        /*
         * Here checking invalid scenario by passing invalid network topology
         * then it should return false.
         */
        boolean output = (Boolean) Whitebox.invokeMethod(spyRedirectFlowManager, "isNodeConnectorInternal", "");
        Assert.assertFalse("Should return false", output);

        /*
         * Here cheking valid scenario by passing valid network topology then it
         * should read links form topology, perform validations and sould return
         * true.
         */
        org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId mockNodeId = mock(
                org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId.class);
        when(mockNodeId.getValue()).thenReturn("host:......");
        final TpId mockTpId = new TpId("");
        final Source mockSource = mock(Source.class);
        final Destination mockDestination = mock(Destination.class);
        when(mockSource.getSourceNode()).thenReturn(mockNodeId);
        when(mockSource.getSourceTp()).thenReturn(mockTpId);
        when(mockDestination.getDestNode()).thenReturn(mockNodeId);
        when(mockDestination.getDestTp()).thenReturn(mockTpId);
        final Link mockLink = mock(Link.class);
        when(mockLink.getSource()).thenReturn(mockSource);
        when(mockLink.getDestination()).thenReturn(mockDestination);
        final List mockLinkList = new ArrayList();
        mockLinkList.add(mockLink);
        final Topology mockTopology = mock(Topology.class);
        when(mockTopology.getLink()).thenReturn(mockLinkList);
        final List mockNetworkTopologyList = new ArrayList();
        mockNetworkTopologyList.add(mockTopology);
        final NetworkTopology mockNetworkTopology = mock(NetworkTopology.class);
        when(mockNetworkTopology.getTopology()).thenReturn(mockNetworkTopologyList);
        final Optional mockOptional = mock(Optional.class);
        when(mockOptional.isPresent()).thenReturn(true);
        when(mockOptional.get()).thenReturn(mockNetworkTopology);
        when(mockCheckedFuture.get()).thenReturn(mockOptional);
        output = (Boolean) Whitebox.invokeMethod(spyRedirectFlowManager, "isNodeConnectorInternal", "");
        Assert.assertTrue("Should return true", output);
        /*
         * Here checking invalid scenario if an exception occurred when reading
         * network topology, then it should return false.
         */
        when(mockCheckedFuture.get()).thenThrow(ExecutionException.class);
        output = (Boolean) Whitebox.invokeMethod(spyRedirectFlowManager, "isNodeConnectorInternal", "");
        Assert.assertFalse(output);
    }

    /**
     * Test case for {@link RedirectFlowManager#pushRedirectFlow()}
     */
    @Test
    public void testPushRedirectFlow() throws Exception {
        /*
         * Here creating required mock objects.
         */
        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[0]);

        final NodeId mockNodeId = PowerMockito.mock(NodeId.class);
        final FlowAction addFlow = FlowAction.ADD_FLOW;
        /*
         * Here checking invalid scenarios, by passing invalid flow action and
         * it should return from that method.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "pushRedirectFlow", epgList, mockNodeId, "1", "4", null);

        /*
         * Here checking valid scenarios, by passing valid endpoint groups and
         * valid flow actions and It should write flows those node id , flow
         * builder and flow action.
         */        
        PowerMockito.mockStatic(MatchUtils.class);

        MatchUtils.createEthMatch(Matchers.any(MatchBuilder.class), Matchers.any(MacAddress.class),
                Matchers.any(MacAddress.class));

        final ReadWriteTransaction mockWriteOnlyTransaction = Mockito.spy(
                ReadWriteTransaction.class);
        final CheckedFuture mockCheckedFuture = Mockito.spy(CheckedFuture.class);
        when(mockWriteOnlyTransaction.submit()).thenReturn(mockCheckedFuture);
        when(mockDataBroker.newWriteOnlyTransaction())
                .thenReturn(mockWriteOnlyTransaction);

        Whitebox.invokeMethod(spyRedirectFlowManager, "pushRedirectFlow", epgList, mockNodeId, "1", "4", addFlow);

        Mockito.verify(spyRedirectFlowManager, Mockito.times(1)).writeDataTransaction(Matchers.any(NodeId.class),
                Matchers.any(FlowBuilder.class), Matchers.any(FlowAction.class));
    }

    /**
     * Test case for {@link RedirectFlowManager#createEthMatch()}
     */
    @Test
    public void testCreateEthMatch() throws Exception {
        /*
         * Here checking invalid scenarios, by passing invalid mac addresses and
         * It should raise IllegalArgumentException for these invalid mac
         * addresses.
         */
        final List<String> epgList = new ArrayList<String>();
        epgList.add(invalidMacAddress[0]);
        epgList.add(invalidMacAddress[0]);
        final MatchBuilder mockMatchBuilder = PowerMockito.mock(MatchBuilder.class);
        PowerMockito.mockStatic(MatchUtils.class);
        Whitebox.invokeMethod(spyRedirectFlowManager, "createEthMatch", epgList, mockMatchBuilder);
        PowerMockito.verifyStatic(Mockito.times(0));
        MatchUtils.createEthMatch(Matchers.any(MatchBuilder.class), Matchers.any(MacAddress.class),
                Matchers.any(MacAddress.class));
        /*
         * Here checking valid scenarios, by passing valid mac addresses and It
         * should create eth match for these invalid mac addresses.
         */
        epgList.clear();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);
        Whitebox.invokeMethod(spyRedirectFlowManager, "createEthMatch", epgList, mockMatchBuilder);
        PowerMockito.verifyStatic(Mockito.times(1));
        MatchUtils.createEthMatch(Matchers.any(MatchBuilder.class), Matchers.any(MacAddress.class),
                Matchers.any(MacAddress.class));
    }

    /**
     * Test case for {@link RedirectFlowManager#redirectFlowEntry()}
     */

    @Test
    public void testRedirectFlowEntry() throws Exception {
        /*
         * Here creating required mock objects.
         */
        final RedirectNodeData mockData = mock(RedirectNodeData.class);
        final Intent mockIntent = PowerMockito.mock(Intent.class);
        when(mockData.getIntent()).thenReturn(mockIntent);

        PowerMockito.mockStatic(IntentUtils.class);
        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);

        PowerMockito.when(IntentUtils.extractEndPointGroup(mockIntent)).thenReturn(epgList);

        final org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId mockNodeId = mock(
                org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId.class);
        when(mockNodeId.getValue()).thenReturn("mockNode");

        PowerMockito.mockStatic(TopologyUtils.class);
        PowerMockito.when(TopologyUtils.extractTopologyNodeId(Matchers.any(String.class))).thenReturn(mockNodeId);

        OFRendererGraphService mockOFRendererGraphService = mock(OFRendererGraphService.class);
        when(mockOFRendererGraphService.getShortestPath(mockNodeId, mockNodeId)).thenReturn(null);

        Whitebox.setInternalState(spyRedirectFlowManager, "graphService", mockOFRendererGraphService);

        /*
         * Here checking flow entries for redirect action.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "redirectFlowEntry", mockData);
    }

    /**
     * Test case for {@link RedirectFlowManager#close()}
     */
    @Test
    public void testClose() throws Exception {
        /*
         * Here creating required mock objects.
         */
        final ServiceRegistration mockServiceRegistrationOne = PowerMockito.mock(ServiceRegistration.class);
        final ServiceRegistration mockServiceRegistrationTwo = PowerMockito.mock(ServiceRegistration.class);
        final Set<ServiceRegistration> mockSet = new HashSet<ServiceRegistration>();
        mockSet.add(mockServiceRegistrationOne);
        mockSet.add(mockServiceRegistrationTwo);
        /*
         * Here checking, whether it unregistered all service which are
         * register with serviceRegistration.
         */
        Whitebox.setInternalState(spyRedirectFlowManager, "serviceRegistration", mockSet);
        spyRedirectFlowManager.close();
        Mockito.verify(mockServiceRegistrationOne, Mockito.times(1)).unregister();
        Mockito.verify(mockServiceRegistrationTwo, Mockito.times(1)).unregister();
    }

    /**
     * Test case for {@link RedirectFlowManager#createFlowBuilder()}
     */
    @Test
    public void testCreateFlowBuilder() throws Exception {
        /*
         * Here creating required mock objects.
         */
        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);

        final Match mockMatch = PowerMockito.mock(Match.class);
        final MatchBuilder mockMatchBuilder = mock(MatchBuilder.class);
        when(mockMatchBuilder.build()).thenReturn(mockMatch);

        /*
         * Here checking, it should return flow builder object by injecting all
         * necessary element into that builder object.
         */
        final FlowBuilder outputFlowBuilder = (FlowBuilder) Whitebox.invokeMethod(spyRedirectFlowManager, "createFlowBuilder",
                epgList, mockMatchBuilder);
        Assert.assertNotNull(outputFlowBuilder);
        Assert.assertEquals("Should be return expected object", mockMatch, outputFlowBuilder.getMatch());
        Assert.assertEquals("Should be return expected flow name", OFRendererConstants.INTENT_L2_FLOW_NAME + macAddress[0] + macAddress[1], outputFlowBuilder.getFlowName());
    }

    /**
     * Test case for {@link RedirectFlowManager#addIntentToCache()}
     */
    @Test
    public void testAddIntentToCache() throws Exception {
        /*
         * Here providing mock functionality for required classes.
         */
        final RedirectNodeData mockData = PowerMockito.mock(RedirectNodeData.class);
        final Intent mockIntent = PowerMockito.mock(Intent.class);
        PowerMockito.when(mockIntent.getId()).thenReturn(new Uuid(uuid[0]));
        spyRedirectFlowManager.getredirectNodeCache().put(uuid[0], mockData);
        /*
         * Here checking, when add request come to add Intent to cache, it
         * should find the record which matcher this intent id in the cache, and
         * it should add to that record.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "addIntentToCache", mockIntent);
        Mockito.verify(mockData).setIntent(mockIntent);
    }

    /**
     * Test case for {@link RedirectFlowManager#createRedirectFlowName()}
     */
    @Test
    public void testCreateRedirectFlowName() throws Exception {
        /*
         * Here checking, it required read end point group names and based on
         * those name it should create flow name in predefined format.
         */
        final List<String> epgList = new ArrayList<String>();
        epgList.add("epg-1");
        epgList.add("epg-2");
        final Object output = Whitebox.invokeMethod(spyRedirectFlowManager, "createRedirectFlowName", epgList);
        Assert.assertNotNull(output);
        Assert.assertEquals("Should return flow name in predefined format", "L2_Rule_epg-1epg-2", output);
    }

    /**
     * Test case for {@link RedirectFlowManager#getEtherType()}
     */
    @Test
    public void testGetEtherType() throws Exception {
        /*
         * Here checking, whether it retriving ethernet type from given payload
         * properly or not.
         */
        final byte payload[] = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0x00,
                (byte) 0x0d, (byte) 0x3f, (byte) 0xcd, (byte) 0x02, (byte) 0x5f, (byte) 0xf, (byte) 0xa };
        final short output = (Short) Whitebox.invokeMethod(spyRedirectFlowManager, "getEtherType", payload);
        Assert.assertEquals("Should return expected ether net type", 3850, output);
    }

    /**
     * Test case for {@link RedirectFlowManager#packShort()}
     */
    @Test
    public void testPackShort() throws Exception {
        /*
         * Here checking whether it is converting 2-byte array to a short
         * properly or not.
         */
        final byte[] byteArray = new byte[] { (byte) 0xf, (byte) 0xa };
        final short output = (Short) Whitebox.invokeMethod(spyRedirectFlowManager, "packShort", byteArray);
        Assert.assertEquals(3850, output);
    }

    /**
     * Test case for {@link RedirectFlowManager#getDstMacStr()}
     */
    @Test
    public void testGetDstMacStr() throws Exception {
        /*
         * Here checking, whether it retrieving destination mac address from
         * given payload properly or not.
         */
        final byte payload[] = new byte[] { (byte) 0x00, (byte) 0x0d, (byte) 0x3f, (byte) 0xcd, (byte) 0x02, (byte) 0x5f };
        final String expected = macAddress[2];;
        String output = (String) Whitebox.invokeMethod(spyRedirectFlowManager, "getDstMacStr", payload);
        Assert.assertEquals("Should return expected mac address", expected, output);
    }

    /**
     * Test case for {@link RedirectFlowManager#getSrcMacStr()}
     */
    @Test
    public void testGetSrcMacStr() throws Exception {
        /*
         * Here checking, whether it retrieving source mac address from given
         * payload properly or not.
         */
        byte payload[] = new byte[] { (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0x00,
                (byte) 0x0d, (byte) 0x3f, (byte) 0xcd, (byte) 0x02, (byte) 0x5f };
        String expected = macAddress[2];
        String output = (String) Whitebox.invokeMethod(spyRedirectFlowManager, "getSrcMacStr", payload);
        Assert.assertEquals("Should return expected mac address", expected, output);
    }

    /**
     * Test case for {@link RedirectFlowManager#deleteArpFlow()}
     */
    @Test
    public void testDeleteArpFlow() throws Exception {
        mockReadOnlyTransaction = mock(ReadOnlyTransaction.class);
        mockWriteTransaction = mock(WriteTransaction.class);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);
        FlowId mockFlowId = new FlowId("arpReplyToController_EthernetType");
        Flow mockFlow = mock(Flow.class);
        when(mockFlow.getId()).thenReturn(mockFlowId);
        List mockList = new ArrayList();
        mockList.add(mockFlow);
        Table mockTable = mock(Table.class);
        when(mockTable.getFlow()).thenReturn(mockList);
        Optional mockOptional = mock(Optional.class);
        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);
        when(mockOptional.isPresent()).thenReturn(true);
        when(mockOptional.get()).thenReturn(mockTable);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);
        when(mockReadOnlyTransaction.read(Matchers.eq(LogicalDatastoreType.CONFIGURATION),
                Matchers.any(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        when(mockWriteTransaction.submit()).thenReturn(mockCheckedFuture);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);
        Whitebox.invokeMethod(spyRedirectFlowManager, "deleteArpFlow", mock(NodeId.class));
        Mockito.verify(mockReadOnlyTransaction, Mockito.times(1)).read(Matchers.eq(LogicalDatastoreType.CONFIGURATION),
                Matchers.any(InstanceIdentifier.class));
        Mockito.verify(mockWriteTransaction, Mockito.times(1)).submit();
    }

    /**
     * Test case for {@link RedirectFlowManager#removeRedirectFlow()}
     */
    @Test
    public void testRemoveRedirectFlow() throws Exception {
        mockReadOnlyTransaction = mock(ReadOnlyTransaction.class);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);

        mockWriteTransaction = mock(WriteTransaction.class);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);

        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);

        PowerMockito.mockStatic(IntentUtils.class);
        PowerMockito.when(IntentUtils.extractEndPointGroup(Mockito.any(Intent.class))).thenReturn(epgList);

        FlowId mockFlowId = new FlowId(OFRendererConstants.INTENT_L2_FLOW_NAME + macAddress[0] + macAddress[1]);
        Flow mockFlow = mock(Flow.class);
        when(mockFlow.getId()).thenReturn(mockFlowId);

        List mockList = new ArrayList();
        mockList.add(mockFlow);

        Table mockTable = mock(Table.class);
        when(mockTable.getFlow()).thenReturn(mockList);

        Optional mockOptional = mock(Optional.class);
        when(mockOptional.isPresent()).thenReturn(true);
        when(mockOptional.get()).thenReturn(mockTable);

        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);
        when(mockReadOnlyTransaction.read(Matchers.eq(LogicalDatastoreType.CONFIGURATION),
                Matchers.any(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        when(mockWriteTransaction.submit()).thenReturn(mockCheckedFuture);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);

//        PowerMockito.doReturn("L2_Rule_00:00:00:00:00:0100:00:00:00:00:05").when(spyRedirectFlowManager, "createRedirectFlowName", Matchers.any(List.class));

        Whitebox.invokeMethod(spyRedirectFlowManager, "removeRedirectFlow", mock(NodeId.class), mock(Intent.class));

        Mockito.verify(mockReadOnlyTransaction, Mockito.times(1)).read(Matchers.eq(LogicalDatastoreType.CONFIGURATION),
                Matchers.any(InstanceIdentifier.class));
        Mockito.verify(mockWriteTransaction, Mockito.times(1)).submit();
    }
}
