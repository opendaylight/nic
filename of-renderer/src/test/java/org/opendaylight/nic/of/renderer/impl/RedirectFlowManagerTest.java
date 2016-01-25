/*
 * Copyright (c) 2016 NEC Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.sfc.provider.api.SfcProviderServiceForwarderAPI;
import org.opendaylight.sfc.provider.api.SfcProviderServiceFunctionAPI;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SffDataPlaneLocatorName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SffName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SfName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.entry.SfDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.service.function.forwarder.SffDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.ServiceFunctionForwarder;
import org.opendaylight.yang.gen.v1.urn.ericsson.params.xml.ns.yang.sfc.sff.ofs.rev150408.port.details.OfsPort;
import org.opendaylight.yang.gen.v1.urn.ericsson.params.xml.ns.yang.sfc.sff.ofs.rev150408.SffDataPlaneLocator1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.mockito.Matchers;
import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FrameworkUtil.class, RedirectFlowManager.class, SfcProviderServiceFunctionAPI.class,
        SfcProviderServiceForwarderAPI.class, IntentUtils.class, MatchUtils.class })
public class RedirectFlowManagerTest {

    /**
     * Mock Instance of DataBroker to perform unit testing.
     */
    private DataBroker mockDataBroker;
    /**
     * Mock Instance of PipelineManager to perform unit testing.
     */
    private PipelineManager mockPipelineManager;
    /**
     * Instance of OFRendererGraphService to perform unit testing.
     */
    private OFRendererGraphService mockGraphService;
    /**
     * Instance of RedirectFlowManager to perform unit testing.
     */
    private RedirectFlowManager spyRedirectFlowManager;
    /**
     * String constants to perform unit testing.
     */
    private final String[] nodeData = { "openflow:2", "5", "6", "Ingress", "Egress" };
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
        mockDataBroker = PowerMockito.mock(DataBroker.class);
        mockPipelineManager = PowerMockito.mock(PipelineManager.class);
        mockGraphService = PowerMockito.mock(OFRendererGraphService.class);
        spyRedirectFlowManager = PowerMockito.spy(new RedirectFlowManager(mockDataBroker, mockPipelineManager, mockGraphService));
    }

   /**
     * Test case for {@link RedirectFlowManager#onPacketReceived()}
     */
    @Test
    public void testOnPacketReceived() throws Exception {
        /*
         * Here creating required mock objects.
         */
        final PacketReceived mockPacketReceived = PowerMockito.mock(PacketReceived.class);
        /*
         * Here checking invalid scenario by passing invalid ether net type then
         * it should return from method execution.
         */
        PowerMockito.doReturn((short) 10).when(spyRedirectFlowManager, "getEtherType", Matchers.any(byte[].class));
        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("getSrcMacStr",
                Matchers.any(byte[].class));

        /*
         * Here creating required mock objets.
         */
        PowerMockito.doReturn((short) 0x0806).when(spyRedirectFlowManager, "getEtherType", Matchers.any(byte[].class));
        final NodeConnectorId mockNodeConnectorId = PowerMockito.mock(NodeConnectorId.class);
        PowerMockito.when(mockNodeConnectorId.getValue()).thenReturn("mockNode");
        final NodeConnectorKey mockNodeConnectorKey = PowerMockito.mock(NodeConnectorKey.class);
        PowerMockito.when(mockNodeConnectorKey.getId()).thenReturn(mockNodeConnectorId);
        final InstanceIdentifier mockInstanceIdentifier = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, PowerMockito.mock(NodeKey.class)).child(NodeConnector.class, mockNodeConnectorKey)
                .build();
        final NodeConnectorRef mockNodeConnectorRef = PowerMockito.mock(NodeConnectorRef.class);
        PowerMockito.when(mockNodeConnectorRef.getValue()).thenReturn(mockInstanceIdentifier);
        PowerMockito.when(mockPacketReceived.getIngress()).thenReturn(mockNodeConnectorRef);
        /*
         * Here checking invalid scenario, if source mac is null, then it should
         * return from method execution with out adding node to cache map.
         */
        PowerMockito.doReturn(null).when(spyRedirectFlowManager, "getSrcMacStr", Matchers.any(PacketReceived.class));
        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("getSrcMacStr",
                Matchers.any(byte[].class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("getDstMacStr",
                Matchers.any(byte[].class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("isNodeConnectorInternal",
                Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("addMacNodeToCache",
                Matchers.any(String.class), Matchers.any(String.class));
        /*
         * Here checking invalid scenario, if destination mac is null, then it
         * should return from method execution with out adding node to cache
         * map.
         */
        PowerMockito.doReturn(macAddress[0]).when(spyRedirectFlowManager, "getSrcMacStr",
                Matchers.any(PacketReceived.class));
        PowerMockito.doReturn(null).when(spyRedirectFlowManager, "getDstMacStr", Matchers.any(PacketReceived.class));
        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(2)).invoke("getSrcMacStr",
                Matchers.any(byte[].class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("getDstMacStr",
                Matchers.any(byte[].class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("isNodeConnectorInternal",
                Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("addMacNodeToCache",
                Matchers.any(String.class), Matchers.any(String.class));
        /*
         * Here checking invalid scenario, if source, destination mac are valid
         * but node is not internal node, then it should return from method
         * execution with out adding node to cache map.
         */
        PowerMockito.doReturn(macAddress[0]).when(spyRedirectFlowManager, "getSrcMacStr",
                Matchers.any(PacketReceived.class));
        PowerMockito.doReturn(macAddress[0]).when(spyRedirectFlowManager, "getDstMacStr",
                Matchers.any(PacketReceived.class));
        PowerMockito.doReturn(false).when(spyRedirectFlowManager, "isNodeConnectorInternal",
                Matchers.any(String.class));
        spyRedirectFlowManager.onPacketReceived(mockPacketReceived);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(3)).invoke("getSrcMacStr",
                Matchers.any(byte[].class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(2)).invoke("getDstMacStr",
                Matchers.any(byte[].class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("isNodeConnectorInternal",
                Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("addMacNodeToCache",
                Matchers.any(String.class), Matchers.any(String.class));
    }

    /**
     * Test case for {@link RedirectFlowManager#readRedirectSfcData()}
     */
    @Test
    public void testReadRedirectSfcData() throws Exception {
        /*
         * Here verifying readRedirectSfcData() should return empty String[], if
         * the service name is null.
         */
        String[] output;
        output = (String[]) Whitebox.invokeMethod(spyRedirectFlowManager, "readRedirectSfcData", null);
        Assert.assertEquals("Length should be two", output.length, 2);
        Assert.assertEquals("Ingress should be null", output[0], null);
        Assert.assertEquals("Egress should be null", output[1], null);
        /*
         * Here providing the required mock functionality for SFC classes.
         */
        PowerMockito.mockStatic(SfcProviderServiceFunctionAPI.class);
        PowerMockito.mockStatic(SfcProviderServiceForwarderAPI.class);
        final ServiceFunction mockServiceFunction = PowerMockito.mock(ServiceFunction.class);
        final List<SfDataPlaneLocator> mockList = new ArrayList<SfDataPlaneLocator>();
        SfDataPlaneLocator mockSfDataPlaneLocator = PowerMockito.mock(SfDataPlaneLocator.class);
        mockList.add(mockSfDataPlaneLocator);
        PowerMockito.when(mockServiceFunction.getSfDataPlaneLocator()).thenReturn(mockList);
        final SffName mockSffName = PowerMockito.mock(SffName.class);
        PowerMockito.when(mockSffName.getValue()).thenReturn(nodeData[0]);
        PowerMockito.when(mockSfDataPlaneLocator.getServiceFunctionForwarder()).thenReturn(mockSffName);
        final List<SffDataPlaneLocator> mockListSffDPL = new ArrayList<SffDataPlaneLocator>();
        final SffDataPlaneLocator mockSffDataPlaneLocatorOne = PowerMockito.mock(SffDataPlaneLocator.class);
        final SffDataPlaneLocator mockSffDataPlaneLocatorTwo = PowerMockito.mock(SffDataPlaneLocator.class);
        final OfsPort mockOfsPort = PowerMockito.mock(OfsPort.class);
        PowerMockito.when(mockOfsPort.getPortId()).thenReturn(nodeData[1], nodeData[2]);
        final SffDataPlaneLocatorName mockSffDataPlaneLocatorName = PowerMockito.mock(SffDataPlaneLocatorName.class);
        PowerMockito.when(mockSffDataPlaneLocatorName.getValue()).thenReturn(nodeData[3], nodeData[4]);
        final SffDataPlaneLocator1 mockSffDataPlaneLocator1 = PowerMockito.mock(SffDataPlaneLocator1.class);
        PowerMockito.when(mockSffDataPlaneLocator1.getOfsPort()).thenReturn(mockOfsPort);
        PowerMockito.when(mockSffDataPlaneLocatorOne.getName()).thenReturn(mockSffDataPlaneLocatorName);
        PowerMockito.when(mockSffDataPlaneLocatorOne.getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);
        PowerMockito.when(mockSffDataPlaneLocatorTwo.getName()).thenReturn(mockSffDataPlaneLocatorName);
        PowerMockito.when(mockSffDataPlaneLocatorTwo.getAugmentation(SffDataPlaneLocator1.class))
                .thenReturn(mockSffDataPlaneLocator1);
        final ServiceFunctionForwarder mockServiceFunctionForwarder = PowerMockito.mock(ServiceFunctionForwarder.class);
        mockListSffDPL.add(mockSffDataPlaneLocatorOne);
        mockListSffDPL.add(mockSffDataPlaneLocatorTwo);
        PowerMockito.when(mockServiceFunctionForwarder.getSffDataPlaneLocator()).thenReturn(mockListSffDPL);
        PowerMockito.when(SfcProviderServiceFunctionAPI.readServiceFunction(Matchers.any(SfName.class)))
                .thenReturn(mockServiceFunction);
        PowerMockito.when(SfcProviderServiceForwarderAPI.readServiceFunctionForwarder(Matchers.any(SffName.class)))
                .thenReturn(mockServiceFunctionForwarder);
        /*
         * Here verifying readRedirectSfcData() should return String[] which
         * contains Ingress and Egress nodes details, if the service name is
         * valid.
         */
        output = (String[]) Whitebox.invokeMethod(spyRedirectFlowManager, "readRedirectSfcData", serviceName);
        Assert.assertEquals("Length of the output array should be 2 ", 2, output.length);
        Assert.assertEquals("Should return expected Ingress node", ofsData[0], output[0]);
        Assert.assertEquals("Should return expected Egress node", ofsData[1], output[1]);
    }

    /**
     * Test case for {@link RedirectFlowManager#addSfcNodeInfoToCache()}
     */
    @Test
    public void testAddSfcNodeInfoToCache() throws Exception {
        /*
         * Here providing the mock functionality for required classes.
         */
        final Intent mockIntent = PowerMockito.mock(Intent.class);
        final List<Actions> mockList = new ArrayList<Actions>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.Redirect mockInnerRedirect = PowerMockito
                .mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.Redirect.class);
        PowerMockito.when(mockInnerRedirect.getServiceName()).thenReturn(serviceName);
        final Redirect mockRedirect = PowerMockito.mock(Redirect.class);
        PowerMockito.when(mockRedirect.getRedirect()).thenReturn(mockInnerRedirect);
        final Actions mockActions = PowerMockito.mock(Actions.class);
        PowerMockito.when(mockActions.getAction()).thenReturn(mockRedirect);
        mockList.add(mockActions);
        PowerMockito.when(mockIntent.getId()).thenReturn(new Uuid(uuid[0]));
        PowerMockito.when(mockIntent.getActions()).thenReturn(mockList);
        PowerMockito.doReturn(new String[] { ofsData[2], ofsData[3] }).when(spyRedirectFlowManager,
                "readRedirectSfcData", serviceName);
        /*
         * Here checking size of the cache map before and after executing
         * addSfcNodeInfoToCache() and checking whether addSfcNodeInfoToCache()
         * returning expected Ingress/Egress nodes data.
         */
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
        final RedirectNodeData mockDataOne = PowerMockito.mock(RedirectNodeData.class);
        final RedirectNodeData mockDataTwo = PowerMockito.mock(RedirectNodeData.class);
        spyRedirectFlowManager.getredirectNodeCache().put(uuid[0], mockDataOne);
        PowerMockito.mockStatic(IntentUtils.class);
        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);
        PowerMockito.when(IntentUtils.extractEndPointGroup(null)).thenReturn(epgList);
        PowerMockito.when(mockDataOne.toString()).thenReturn(" mock object :" + macAddress[0]);
        PowerMockito.when(mockDataTwo.toString()).thenReturn(" mock object :" + macAddress[1]);
        PowerMockito.doNothing().when(spyRedirectFlowManager, "redirectFlowEntry", mockDataOne);
        PowerMockito.doNothing().when(spyRedirectFlowManager, "redirectFlowEntry", mockDataTwo);
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
        final Intent mockIntent = PowerMockito.mock(Intent.class);
        PowerMockito.when(mockIntent.getId()).thenReturn(new Uuid(uuid[0]));
        final FlowAction addFlow = FlowAction.ADD_FLOW;
        final FlowAction removeFlow = FlowAction.REMOVE_FLOW;
        /*
         * Here checking invalid scenarios by passing intent, flow action
         * objects as null. If either intent or flow action is null, it should
         * return.
         */
        spyRedirectFlowManager.redirectFlowConstruction(mockIntent, null);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("addSfcNodeInfoToCache",
                Matchers.any(Intent.class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("addIntentToCache",
                Matchers.any(Intent.class));
        spyRedirectFlowManager.redirectFlowConstruction(null, addFlow);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("addSfcNodeInfoToCache",
                Matchers.any(Intent.class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("addIntentToCache",
                Matchers.any(Intent.class));
        /*
         * Here checking valid scenarios by passing valid intent, flow action
         * objects. If flow action is add, it should add particular intent
         * record to cache map. If flow action is remove, it should delete
         * particular intent record to cache map.
         */
        PowerMockito.doNothing().when(spyRedirectFlowManager, "addSfcNodeInfoToCache", mockIntent);
        PowerMockito.doNothing().when(spyRedirectFlowManager, "addIntentToCache", mockIntent);
        spyRedirectFlowManager.redirectFlowConstruction(mockIntent, addFlow);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("addSfcNodeInfoToCache",
                Matchers.any(Intent.class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("addIntentToCache",
                Matchers.any(Intent.class));
        spyRedirectFlowManager.getredirectNodeCache().put(uuid[0], PowerMockito.mock(RedirectNodeData.class));
        final int beforeSize = spyRedirectFlowManager.getredirectNodeCache().size();
        Assert.assertEquals("Record should exist", beforeSize, 1);
        Assert.assertNotNull("Record should exist", spyRedirectFlowManager.getredirectNodeCache().get(uuid[0]));
        spyRedirectFlowManager.redirectFlowConstruction(mockIntent, removeFlow);
        final int afterSize = spyRedirectFlowManager.getredirectNodeCache().size();
        Assert.assertEquals("Record should be deleted", afterSize, 0);
        Assert.assertNull("Record should not exist.", spyRedirectFlowManager.getredirectNodeCache().get(uuid[0]));
    }

    /**
     * Test case for {@link RedirectFlowManager#generateRedirectFlows()}
     */
    @Test
    public void testGenerateRedirectFlows() throws Exception {
        /*
         * Here creating required objects.
         */
        final List<String> epgList = new ArrayList<String>();
        final FlowAction addFlow = FlowAction.ADD_FLOW;
        final org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId mockNodeId = PowerMockito
                .mock(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId.class);
        /*
         * Here checking valid scenarios, by passing valid graphService which
         * does not having any shortest path links.
         */
        PowerMockito.when(mockNodeId.getValue()).thenReturn("mockNode");
        PowerMockito.doReturn(mockNodeId).when(spyRedirectFlowManager, "extractTopologyNodeId",
                Matchers.any(String.class));
        OFRendererGraphService mockOFRendererGraphService = PowerMockito.mock(OFRendererGraphService.class);
        PowerMockito.doReturn(new ArrayList()).when(mockOFRendererGraphService, "getShortestPath", mockNodeId,
                mockNodeId);
        Whitebox.setInternalState(spyRedirectFlowManager, "graphService", mockOFRendererGraphService);
        PowerMockito.doNothing().when(spyRedirectFlowManager, "pushRedirectFlow", Matchers.any(List.class),
                Matchers.any(NodeId.class), Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(FlowAction.class));
        Whitebox.invokeMethod(spyRedirectFlowManager, "generateRedirectFlows", epgList, "", "", addFlow);
        /*
         * Here checking valid scenarios, by passing valid graphService which
         * having shortest path links.
         */
        final TpId mockTpId = PowerMockito.mock(TpId.class);
        final Source mockSource = PowerMockito.mock(Source.class);
        final Destination mockDestination = PowerMockito.mock(Destination.class);
        PowerMockito.when(mockSource.getSourceNode()).thenReturn(mockNodeId);
        PowerMockito.when(mockSource.getSourceTp()).thenReturn(mockTpId);
        PowerMockito.when(mockDestination.getDestNode()).thenReturn(mockNodeId);
        PowerMockito.when(mockDestination.getDestTp()).thenReturn(mockTpId);
        final Link mockLink = PowerMockito.mock(Link.class);
        PowerMockito.when(mockLink.getSource()).thenReturn(mockSource);
        PowerMockito.when(mockLink.getDestination()).thenReturn(mockDestination);
        final List<Link> mockList = new ArrayList<Link>();
        mockList.add(mockLink);
        PowerMockito.doReturn(mockList).when(mockOFRendererGraphService, "getShortestPath", mockNodeId, mockNodeId);
        Whitebox.setInternalState(spyRedirectFlowManager, "graphService", mockOFRendererGraphService);
        PowerMockito.doNothing().when(spyRedirectFlowManager, "pushRedirectFlow", Matchers.any(List.class),
                Matchers.any(NodeId.class), Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(FlowAction.class));
        /*
         * Here checking write flows in both the Last and intermediate node to
         * last node in the path.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "generateRedirectFlows", epgList, "srcCNId1", "destCNId1",
                addFlow);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(3)).invoke("pushRedirectFlow",
                Matchers.any(List.class), Matchers.any(NodeId.class), Matchers.any(String.class),
                Matchers.any(String.class), Matchers.any(FlowAction.class));
        PowerMockito.when(mockNodeId.getValue()).thenReturn("mockNode", "mockNode", "mockNode", "mockNode", "mockNode2",
                "mockNode3", null);
        /*
         * Here checking write flows to intermediate node in the path.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "generateRedirectFlows", epgList, "srcCNId1", "destCNId1",
                addFlow);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(4)).invoke("pushRedirectFlow",
                Matchers.any(List.class), Matchers.any(NodeId.class), Matchers.any(String.class),
                Matchers.any(String.class), Matchers.any(FlowAction.class));
    }

    /**
     * Test case for {@link RedirectFlowManager#isNodeConnectorInternal()}
     */
    @Test
    public void testIsNodeConnectorInternal() throws Exception {
        /*
         * Here creating required mock objects.
         */
        final ReadOnlyTransaction mockReadOnlyTransaction = PowerMockito.mock(ReadOnlyTransaction.class);
        final CheckedFuture mockCheckedFuture = PowerMockito.mock(CheckedFuture.class);
        PowerMockito.when(mockReadOnlyTransaction.read(Matchers.any(LogicalDatastoreType.class),
                Matchers.any(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        PowerMockito.when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);
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
        org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId mockNodeId = PowerMockito
                .mock(org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId.class);
        PowerMockito.when(mockNodeId.getValue()).thenReturn("host:......");
        final TpId mockTpId = new TpId("");
        final Source mockSource = PowerMockito.mock(Source.class);
        final Destination mockDestination = PowerMockito.mock(Destination.class);
        PowerMockito.when(mockSource.getSourceNode()).thenReturn(mockNodeId);
        PowerMockito.when(mockSource.getSourceTp()).thenReturn(mockTpId);
        PowerMockito.when(mockDestination.getDestNode()).thenReturn(mockNodeId);
        PowerMockito.when(mockDestination.getDestTp()).thenReturn(mockTpId);
        final Link mockLink = PowerMockito.mock(Link.class);
        PowerMockito.when(mockLink.getSource()).thenReturn(mockSource);
        PowerMockito.when(mockLink.getDestination()).thenReturn(mockDestination);
        final List mockLinkList = new ArrayList();
        mockLinkList.add(mockLink);
        final Topology mockTopology = PowerMockito.mock(Topology.class);
        PowerMockito.when(mockTopology.getLink()).thenReturn(mockLinkList);
        final List mockNetworkTopologyList = new ArrayList();
        mockNetworkTopologyList.add(mockTopology);
        final NetworkTopology mockNetworkTopology = PowerMockito.mock(NetworkTopology.class);
        PowerMockito.when(mockNetworkTopology.getTopology()).thenReturn(mockNetworkTopologyList);
        final Optional mockOptional = PowerMockito.mock(Optional.class);
        PowerMockito.when(mockOptional.isPresent()).thenReturn(true);
        PowerMockito.when(mockOptional.get()).thenReturn(mockNetworkTopology);
        PowerMockito.when(mockCheckedFuture.get()).thenReturn(mockOptional);
        output = (Boolean) Whitebox.invokeMethod(spyRedirectFlowManager, "isNodeConnectorInternal", "");
        Assert.assertTrue("Should return true", output);
        /*
         * Here checking invalid scenario if an exception occured when reading
         * network topology, then it should return false.
         */
        PowerMockito.when(mockCheckedFuture.get()).thenThrow(ExecutionException.class);
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
        final List mockList = PowerMockito.mock(List.class);
        final NodeId mockNodeId = PowerMockito.mock(NodeId.class);
        final FlowAction addFlow = FlowAction.ADD_FLOW;
        /*
         * Here checking invalid scenarios, by passing invalid flow action and
         * it should return from that method.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "pushRedirectFlow", mockList, mockNodeId, "1", "4", null);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(0)).invoke("createEthMatch",
                Matchers.any(List.class), Matchers.any(FlowAction.class));
        /*
         * Here checking valid scenarios, by passing valid endpoint groups and
         * valid flow actions and It should write flows those node id , flow
         * builder and flow action.
         */
        PowerMockito.doNothing().when(spyRedirectFlowManager, "createEthMatch", Matchers.any(List.class),
                Matchers.any(FlowAction.class));
        PowerMockito.doReturn(PowerMockito.mock(FlowBuilder.class)).when(spyRedirectFlowManager, "createFlowBuilder",
                Matchers.any(List.class), Matchers.any(FlowAction.class));
        PowerMockito.doReturn(true).when(spyRedirectFlowManager, "writeDataTransaction", Matchers.any(NodeId.class),
                Matchers.any(FlowBuilder.class), Matchers.any(FlowAction.class));
        Whitebox.invokeMethod(spyRedirectFlowManager, "pushRedirectFlow", mockList, mockNodeId, "1", "4", addFlow);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("createEthMatch",
                Matchers.any(List.class), Matchers.any(FlowAction.class));
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(1)).invoke("createFlowBuilder",
                Matchers.any(List.class), Matchers.any(FlowAction.class));
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
        final RedirectNodeData mockData = PowerMockito.mock(RedirectNodeData.class);
        final Intent mockIntent = PowerMockito.mock(Intent.class);
        PowerMockito.when(mockData.getIntent()).thenReturn(mockIntent);
        PowerMockito.mockStatic(IntentUtils.class);
        final List<String> epgList = new ArrayList<String>();
        epgList.add(macAddress[0]);
        epgList.add(macAddress[1]);
        PowerMockito.when(IntentUtils.extractEndPointGroup(mockIntent)).thenReturn(epgList);
        PowerMockito.doNothing().when(spyRedirectFlowManager, "generateRedirectFlows", Matchers.any(List.class),
                Matchers.any(String.class), Matchers.any(String.class), Matchers.any(FlowAction.class));
        /*
         * Here checking flow entries for redirect action.
         */
        Whitebox.invokeMethod(spyRedirectFlowManager, "redirectFlowEntry", mockData);
        PowerMockito.verifyPrivate(spyRedirectFlowManager, Mockito.times(3)).invoke("generateRedirectFlows",
                Matchers.any(List.class), Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(FlowAction.class));
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
        final List mockList = PowerMockito.mock(List.class);
        final Match mockMatch = PowerMockito.mock(Match.class);
        final MatchBuilder mockMatchBuilder = PowerMockito.mock(MatchBuilder.class);
        PowerMockito.when(mockMatchBuilder.build()).thenReturn(mockMatch);
        PowerMockito.doReturn("testFlow1").when(spyRedirectFlowManager, "createRedirectFlowName", mockList);
        /*
         * Here checking, it should return flow builder object by injecting all
         * necessary element into that builder object.
         */
        final FlowBuilder outputFlowBuilder = (FlowBuilder) Whitebox.invokeMethod(spyRedirectFlowManager, "createFlowBuilder",
                mockList, mockMatchBuilder);
        Assert.assertNotNull(outputFlowBuilder);
        Assert.assertEquals("Should be return expected object", mockMatch, outputFlowBuilder.getMatch());
        Assert.assertEquals("Should be return expected flow name", "testFlow1", outputFlowBuilder.getFlowName());
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
     * Test case for {@link RedirectFlowManager#extractTopologyNodeId()}
     */
    @Test
    public void testExtractTopologyNodeId() throws Exception {
        /*
         * Here checking whether it extracting particular portion from the given
         * node id.
         */
        final org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId expectedNodeId = new org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId(
                "openflow:3");
        final Object output = Whitebox.invokeMethod(spyRedirectFlowManager, "extractTopologyNodeId", ofsData[2]);
        Assert.assertNotNull("It should not be null", output);
        Assert.assertEquals("It should return expected node id", expectedNodeId, output);
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
}

