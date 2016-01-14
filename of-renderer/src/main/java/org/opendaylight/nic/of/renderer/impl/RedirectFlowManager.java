/*
 * Copyright (c) 2016 NEC Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import org.opendaylight.controller.liblldp.HexEncode;
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
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SfName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.function.entry.SfDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sf.rev140701.service.functions.ServiceFunction;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sff.rev140701.service.function.forwarders.service.function.forwarder.SffDataPlaneLocator;
import org.opendaylight.yang.gen.v1.urn.ericsson.params.xml.ns.yang.sfc.sff.ofs.rev150408.port.details.OfsPort;
import org.opendaylight.yang.gen.v1.urn.ericsson.params.xml.ns.yang.sfc.sff.ofs.rev150408.SffDataPlaneLocator1;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedirectFlowManager extends AbstractFlowManager implements PacketProcessingListener, AutoCloseable {

    private final static Logger LOG = LoggerFactory.getLogger(RedirectFlowManager.class);
    private final static int PACKET_OFFSET_ETHERTYPE = 12;
    private final static int PACKET_OFFSET_ETHERNET = 0;
    private final static int PACKET_OFFSET_MAC_DST = PACKET_OFFSET_ETHERNET ;
    private final static int PACKET_OFFSET_MAC_SRC = PACKET_OFFSET_ETHERNET + 6;
    public  final static int ETHERTYPE_ARP = 0x0806;
    private DataBroker dataBroker;
    private OFRendererGraphService graphService;
    private Set<ServiceRegistration<?>> serviceRegistration;
    private FlowAction flowAction;
    private Map<String, RedirectNodeData> redirectNodeCache = new ConcurrentHashMap<String, RedirectNodeData>();

    public RedirectFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
        this.dataBroker = dataBroker;
        graphService = new NetworkGraphManager();
        this.serviceRegistration = new HashSet<ServiceRegistration<?>>();
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        serviceRegistration.add(context.registerService(OFRendererGraphService.class, graphService, null));
     }

     public Map<String, RedirectNodeData> getredirectNodeCache() {
        return redirectNodeCache;
    }

    @Override
    public void onPacketReceived(PacketReceived packetIn) {
        final byte[] rawPacket = packetIn.getPayload();
        // Get the EtherType and check that its an ARP packet
        if (getEtherType(rawPacket) != ETHERTYPE_ARP) {
            LOG.debug("RedirectFlowManager discarding NON-ARP");
            return;
        }
        final String switchPortId = packetIn.getIngress().getValue()
            .firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId()
            .getValue();
        LOG.trace("Node Name ::::: {}", switchPortId);
        // Get the SrcMac Addresses
        String pktSrcMacStr = getSrcMacStr(rawPacket);
        LOG.trace("Packet Source MAC Address ::::: {}", pktSrcMacStr);
        if (pktSrcMacStr == null) {
            LOG.error("RedirectFlowManager Can't get Src MAC address, discarding packet");
            return;
        }
        // Get the DstMac Addresses
        String pktDstMacStr = getDstMacStr(rawPacket);
        LOG.trace("Packet Destination MAC Address ::::: {}",pktDstMacStr);
        if (pktDstMacStr == null) {
            LOG.error("RedirectFlowManager Can't get Dst MAC address, discarding packet");
            return;
        }
        if (!isNodeConnectorInternal(switchPortId)) {
            addMacNodeToCache(pktSrcMacStr, switchPortId);
        }
    }

    /**
     * Given a raw packet, return the EtherType
     *
     * @param rawPacket
     * @return etherType
     */
    private short getEtherType(final byte[] rawPacket) {
        final byte[] etherTypeBytes = Arrays.copyOfRange(rawPacket, PACKET_OFFSET_ETHERTYPE, PACKET_OFFSET_ETHERTYPE+2);
        return packShort(etherTypeBytes);
    }

    /**
     * Given a raw packet, return the SrcMac
     *
     * @param rawPacket
     * @return srcMac String
     */
    private String getSrcMacStr(final byte[] rawPacket) {
        final byte[] macSrcBytes = Arrays.copyOfRange(rawPacket, PACKET_OFFSET_MAC_SRC, PACKET_OFFSET_MAC_SRC+6);
        String pktSrcMacStr = null;
        pktSrcMacStr = HexEncode.bytesToHexStringFormat(macSrcBytes);
        return pktSrcMacStr;
    }

    /**
     * Given a raw packet, return the DstMac
     *
     * @param rawPacket
     * @return dstMac String
     */
    private String getDstMacStr(final byte[] rawPacket) {
        final byte[] macDstBytes = Arrays.copyOfRange(rawPacket, PACKET_OFFSET_MAC_DST, PACKET_OFFSET_MAC_DST+6);
        String pktDstMacStr = null;
        pktDstMacStr = HexEncode.bytesToHexStringFormat(macDstBytes);
        return pktDstMacStr;
    }

    /**
     * Validating the node connector for checking internal or external port
     *
     * @param nodeConnector
     * @return true/false Boolean
     */
    private boolean isNodeConnectorInternal(String nodeConnector) {
        TpId tpId = new TpId(nodeConnector);
        InstanceIdentifier<NetworkTopology> ntII
                = InstanceIdentifier.builder(NetworkTopology.class).build();
        ListenableFuture<Optional<NetworkTopology>> lfONT;
        try (ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction()) {
            lfONT = rot.read(LogicalDatastoreType.OPERATIONAL, ntII);
            rot.close();
        }
        Optional<NetworkTopology> oNT;
        try {
            oNT = lfONT.get();
        } catch (InterruptedException | ExecutionException ex) {
            LOG.info(ex.getLocalizedMessage());
            return false;
        }
        if (oNT != null && oNT.isPresent()) {
            NetworkTopology networkTopo = oNT.get();
            for (Topology t : networkTopo.getTopology()) {
                if (t.getLink() != null) {
                    for (Link l : t.getLink()) {
                        if ((l.getSource().getSourceTp().equals(tpId)
                                && !l.getDestination().getDestTp().getValue().startsWith("host:"))
                                || (l.getDestination().getDestTp().equals(tpId)
                                && !l.getSource().getSourceTp().getValue().startsWith("host:"))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Simple internal utility function to convert from a 2-byte array to a short
     *
     * @param bytes
     * @return the bytes packed into a short
     */
    private short packShort(byte[] bytes) {
        short val = (short) 0;
        for (int i = 0; i < 2; i++) {
          val <<= 8;
          val |= bytes[i] & 0xff;
        }
        return val;
    }

    /**
     * Redirect flow entry
     * @param nodeData :RedirectNodeData contains redirect node details
     */
    public void redirectFlowEntry(RedirectNodeData nodeData) {
        LOG.trace("Redirect Node data {}", nodeData.toString());
        String sourceNodeId = nodeData.getSrcMacNodeId();
        String ingressNodeId = nodeData.getIngressNodeId();
        String egressNodeId = nodeData.getEgressNodeId();
        String descNodeId = nodeData.getDestMacNodeId();
        List<String> endPointGroups = IntentUtils.extractEndPointGroup(nodeData.getIntent());
        // write the reverse flow from destination node to source node
        // changing the src/dst mac order for reverse flow.
        Collections.reverse(endPointGroups);
        generateRedirectFlows(endPointGroups, descNodeId, sourceNodeId, flowAction);
        // reversing it back for normal intent
        Collections.reverse(endPointGroups);
        // write flows from service's egress node  to destination node
        generateRedirectFlows(endPointGroups, egressNodeId, descNodeId, flowAction);
        //write flows from source node to services ingress node
        generateRedirectFlows(endPointGroups, sourceNodeId, ingressNodeId, flowAction);
    }

    /**
     * generate redirect flow computation
     * @param endPointGroups :endPointGroups of the switch on which the push redirect should be performed
     * @param sourceNodeConnectorId : Source Node ID
     * @param targetNodeConnectorId : Target Node ID
     * @param flowAction :Add flow action
     */
    private void generateRedirectFlows(List<String> endPointGroups, String sourceNodeConnectorId,
                 String targetNodeConnectorId, FlowAction flowAction) {
        org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId sourceNodeId = extractTopologyNodeId(sourceNodeConnectorId);
        org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId targetNodeId = extractTopologyNodeId(targetNodeConnectorId);
        List<Link> shortestPath = graphService.getShortestPath(sourceNodeId, targetNodeId);
        LOG.trace("Redirect Source NodeId {}", sourceNodeId.getValue());
        LOG.trace("Redirect sourceNodeConnectorId {}", sourceNodeConnectorId);
        LOG.trace("Redirect Target NodeId {}", targetNodeId.getValue());
        LOG.trace("Redirect targetNodeConnectorId {}", targetNodeConnectorId);
        LOG.trace("Retrieved shortest path, there are {} hops.", shortestPath.size());
        if (shortestPath != null) {
            if (shortestPath.isEmpty()) {
                // Host and Destination ports are in the same switch.
                if (sourceNodeId.getValue().equals(targetNodeId.getValue())) {
                    NodeId srcNodeId = new NodeId(sourceNodeId.getValue());
                    pushRedirectFlow(endPointGroups, srcNodeId, sourceNodeConnectorId,  targetNodeConnectorId, flowAction);
                }
            } else {
                // Host and Destination ports of in different switches
                String linkPrevTargetTp = sourceNodeConnectorId;
                for (Link link: shortestPath) {
                    NodeId linkSourceNodeId = new NodeId(link.getSource().getSourceNode().getValue());
                    String linkSourceTp = link.getSource().getSourceTp().getValue();
                    LOG.trace("Redirect Source Port ID {}", linkSourceTp);
                    NodeId linkTargetNodeId = new NodeId(link.getDestination().getDestNode().getValue());
                    String linkTargetTp = link.getDestination().getDestTp().getValue();
                    if (linkTargetNodeId.getValue().equals(targetNodeId.getValue())) {
                        // Link is connected to Last Node.
                        // Write flows in both the Last and intermediate node to last node in the path
                        pushRedirectFlow(endPointGroups, linkSourceNodeId, linkPrevTargetTp,  linkSourceTp, flowAction);
                        pushRedirectFlow(endPointGroups, linkTargetNodeId, linkTargetTp, targetNodeConnectorId, flowAction);
                    } else if (sourceNodeId.getValue().equals(linkSourceNodeId.getValue())) {
                        // Write flows to First node in the path
                        pushRedirectFlow(endPointGroups, linkSourceNodeId, linkPrevTargetTp,  linkSourceTp, flowAction);
                    } else {
                        // Write flows to intermediate node in the path.
                        pushRedirectFlow(endPointGroups, linkSourceNodeId, linkPrevTargetTp,  linkSourceTp, flowAction);
                    }
                    linkPrevTargetTp = link.getDestination().getDestTp().getValue();
                }
            }
        }
    }

    /**
     * push redirect on switch
     * @param endPointGroups :endPointGroups of the switch on which the push redirect should be performed
     * @param nodeId :NodeID of the switch on which the push redirect should be performed
     * @param inPort :Long ingress port on a switch
     * @param outputPort :Port to which packet should be sent to after pushing label
     * @param flowAction :Add flow action
     */
    private void pushRedirectFlow(List<String> endPointGroups, NodeId nodeId, String inPort,
                 String outputPort, FlowAction flowAction) {
        if (endPointGroups == null || flowAction == null) {
            LOG.error("Endpoints and action cannot be null");
            return;
        }
        LOG.trace("pushRedirectFlow on Node: {}, inport {}, outport {}",
                    nodeId.getValue(), inPort, outputPort);
        // Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        // Creating Flow object
        FlowBuilder flowBuilder = new FlowBuilder();
        NodeConnectorId srcNcId = new NodeConnectorId(inPort);
        createEthMatch(endPointGroups, matchBuilder);
        MatchUtils.createInPortMatch(matchBuilder, srcNcId);
        // Create Flow
        flowBuilder = createFlowBuilder(endPointGroups, matchBuilder);
        Instructions buildedInstructions = createRedirectIntentInstructions(outputPort);
        flowBuilder.setInstructions(buildedInstructions);
        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    /**
     * Create Ethernet Source Match
     *
     * @param endPointGroups The list contains a source MAC and destination MAC
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param srcMac     String representing a source MAC
     * @param dstMac     String representing a destination MAC
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    private void createEthMatch(List<String> endPointGroups, MatchBuilder matchBuilder) {
        String endPointSrc = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        MacAddress srcMac = null;
        MacAddress dstMac = null;
        LOG.trace("Creating intent for endpoints: source{} destination {}", endPointSrc, endPointDst);
            try {
                if (!endPointSrc.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                    srcMac = new MacAddress(endPointSrc);
                }
                if (!endPointDst.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                    dstMac = new MacAddress(endPointDst);
                }
                MatchUtils.createEthMatch(matchBuilder, srcMac, dstMac);
            } catch (IllegalArgumentException e) {
                LOG.error("Can only accept valid MAC addresses as subjects", e);
            }
        }

    private FlowBuilder createFlowBuilder(List<String> endPointGroups, MatchBuilder matchBuilder) {
        final Match match = matchBuilder.build();
        // Flow named for convenience and uniqueness
        String flowName = createRedirectFlowName(endPointGroups);
        final FlowId flowId = new FlowId(flowName);
        final FlowKey key = new FlowKey(flowId);
        final FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(match);
        flowBuilder.setId(flowId);
        flowBuilder.setKey(key);
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(OFRendererConstants.DEFAULT_PRIORITY);
        flowBuilder.setFlowName(flowName);
        flowBuilder.setHardTimeout(OFRendererConstants.DEFAULT_HARD_TIMEOUT);
        flowBuilder.setIdleTimeout(OFRendererConstants.DEFAULT_IDLE_TIMEOUT);
        return flowBuilder;
    }

    /**
     * Derivate a {@link org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId}
     * from a {@link NodeConnector}.
     * @param nodeConnectorId The Inventory NodeConnectorId
     * @return NodeId contained in Network-Topology
     */
    private org.opendaylight.yang.gen.v1.urn
                .tbd.params.xml.ns.yang.network
                .topology.rev131021.NodeId extractTopologyNodeId(String nodeConnectorId) {
        List<String> split = Arrays.asList(nodeConnectorId.split(":"));
        return new org.opendaylight.yang.gen.v1.urn
                       .tbd.params.xml.ns.yang.network
                       .topology.rev131021.NodeId(split.get(0) +
                               ":" +
                               split.get(1));
    }

    @Override
    protected String createFlowName() {
        // TODO Auto-generated method stub
        return null;
    }

    private String createRedirectFlowName(List<String> endPointGroups) {
        StringBuilder sb = new StringBuilder();
        sb.append(OFRendererConstants.INTENT_L2_FLOW_NAME);
        sb.append(endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX));
        sb.append(endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX));
        return sb.toString();
    }

    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        // TODO Auto-generated method stub
    }

    /**
     * To set the redirect intent data
     * @param intent :intent
     */
    private void addIntentToCache(Intent intent) {
        RedirectNodeData redirectNodeData;
        String intentId = intent.getId().getValue();
        if (redirectNodeCache.get(intentId) == null) {
            redirectNodeData = new RedirectNodeData();
            redirectNodeData.setIntent(intent);
            redirectNodeCache.put(intentId, redirectNodeData);
        } else {
            redirectNodeData = redirectNodeCache.get(intentId);
            redirectNodeData.setIntent(intent);
        }
    }

    private void addMacNodeToCache(String macAddress, String nodeId) {
        if (!redirectNodeCache.toString().contains(macAddress)) {
            return;
       }
       Collection<RedirectNodeData> redirectNodeDataList = redirectNodeCache.values();
       for (RedirectNodeData redirectNodeData : redirectNodeDataList) {
            if (redirectNodeData.toString().contains(macAddress)) {
                List<String> endPointGroups = IntentUtils.extractEndPointGroup(redirectNodeData.getIntent());
                String srcMacAddress = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
                String destMacAddress = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
                if (srcMacAddress.equals(macAddress))
                    redirectNodeData.setSrcMacNodeId(nodeId);
                else if (destMacAddress.equals(macAddress))
                    redirectNodeData.setDestMacNodeId(nodeId);
            }
            if (!redirectNodeData.toString().contains("null")) {
                if (!redirectNodeData.isFlowApplied()) {
                    redirectFlowEntry(redirectNodeData);
                    redirectNodeData.setFlowApplied(true);
                }
            }
       }
    }

    /**
     * To set the ingress and egress data to redirect cache
     * @param intent :intent
     */
    private void addSfcNodeInfoToCache(Intent intent) {
        String intentId = intent.getId().getValue();
        RedirectNodeData redirectNodeData = redirectNodeCache.get(intentId);
        if (redirectNodeData == null) {
            addIntentToCache(intent);
            redirectNodeData = redirectNodeCache.get(intentId);
        }
        Redirect actionContainer = (Redirect) intent.getActions().get(0).getAction();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.Redirect
            redirectForService = ((Redirect)actionContainer).getRedirect();
        String serviceName = redirectForService.getServiceName();
        String[] redirectSfcDataList = readRedirectSfcData(serviceName);
        redirectNodeData.setIngressNodeId(redirectSfcDataList[0]);
        redirectNodeData.setEgressNodeId(redirectSfcDataList[1]);
    }

    /**
     * To read the redirect SFC egress and ingress
     * @param serviceName : SFC Service name
     */
    private String[] readRedirectSfcData(String serviceName) {
        LOG.trace("Redirect SFC service name {}", serviceName);
        String[] sfcDataList = new String[2];
        if (serviceName == null) {
            LOG.info("Unable to retrieve service info.");
        } else {
            SfName sfName = new SfName(serviceName);
            ServiceFunction serviceFunction = SfcProviderServiceFunctionAPI.readServiceFunction(sfName);
            List<SfDataPlaneLocator> listOfDPL = serviceFunction.getSfDataPlaneLocator();
            for (SfDataPlaneLocator sfdpl : listOfDPL) {
                if (sfdpl.getServiceFunctionForwarder() == null)
                    continue;
                String nodeId = sfdpl.getServiceFunctionForwarder().getValue();
                if (SfcProviderServiceForwarderAPI
                        .readServiceFunctionForwarder(sfdpl.getServiceFunctionForwarder()) == null)
                    continue;
                List<SffDataPlaneLocator> listOfSffdpl = SfcProviderServiceForwarderAPI
                        .readServiceFunctionForwarder(sfdpl.getServiceFunctionForwarder()).getSffDataPlaneLocator();
                for (SffDataPlaneLocator sffdpl : listOfSffdpl) {
                    SffDataPlaneLocatorName sffdplName = sffdpl.getName();
                    SffDataPlaneLocator1 sffdpl1 = sffdpl.getAugmentation(SffDataPlaneLocator1.class);
                    OfsPort ofsPort = sffdpl1.getOfsPort();
                    if (sffdplName.getValue().toUpperCase().equals("INGRESS"))
                        sfcDataList[0] = nodeId + ":" + ofsPort.getPortId();
                    else if (sffdplName.getValue().toUpperCase().equals("EGRESS"))
                        sfcDataList[1] = nodeId + ":" + ofsPort.getPortId();
                }
            }
        }
        return sfcDataList;
    }

    /**
     * Redirect flow construction and deletion entry
     * @param intent : intent
     * @param flowAction :Add flow action
     */
    public void redirectFlowConstruction(Intent intent, FlowAction flowAction) {
        if (intent == null || flowAction == null) {
            LOG.error("intent and action cannot be null");
            return;
        }
        this.flowAction = flowAction;
        if (flowAction.equals(FlowAction.ADD_FLOW)) {
            addSfcNodeInfoToCache(intent);
            addIntentToCache(intent);
        } else if (flowAction.equals(FlowAction.REMOVE_FLOW)) {
            LOG.trace("Removed redirect intent data from cache {}", intent.getId().getValue());
            redirectNodeCache.remove(intent.getId().getValue());
            // TODO To clear the redirect flow instructions
        }
    }

    @Override
    public void close() throws Exception {
         for (ServiceRegistration<?> service: serviceRegistration) {
            if (service != null) {
                service.unregister();
            }
        }
    }
}

