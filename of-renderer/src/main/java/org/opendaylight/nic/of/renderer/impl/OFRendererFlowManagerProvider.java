/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

import org.opendaylight.nic.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererFlowManagerProvider implements OFRendererFlowService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OFRendererFlowManagerProvider.class);

    private Set<ServiceRegistration<?>> serviceRegistration;
    private IntentFlowManager intentFlowManager;
    private ArpFlowManager arpFlowManager;
    private LldpFlowManager lldpFlowManager;
    private IntentMappingService intentMappingService;
    private DataBroker dataBroker;
    private final PipelineManager pipelineManager;
    private OFRendererGraphService graphService;
    private MplsIntentFlowManager mplsIntentFlowManager;

    public OFRendererFlowManagerProvider(DataBroker dataBroker, PipelineManager pipelineManager) {
        this.dataBroker = dataBroker;
        this.pipelineManager = pipelineManager;
        this.serviceRegistration = new HashSet<ServiceRegistration<?>>();
        this.graphService = new NetworkGraphManager();
        this.mplsIntentFlowManager = new MplsIntentFlowManager(dataBroker, pipelineManager);
    }

    public void init() {
        LOG.info("OF Renderer Provider Session Initiated");
        // Register this service with karaf
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        serviceRegistration.add(context.registerService(OFRendererFlowService.class, this, null));
        serviceRegistration.add(context.registerService(OFRendererGraphService.class,
                                                        graphService,
                                                        null));
        ServiceReference<?> serviceReference = context.getServiceReference(IntentMappingService.class);
        intentMappingService = (IntentMappingService) context.getService(serviceReference);
        intentFlowManager = new IntentFlowManager(dataBroker, pipelineManager);
        arpFlowManager = new ArpFlowManager(dataBroker, pipelineManager);
        lldpFlowManager = new LldpFlowManager(dataBroker, pipelineManager);
    }

    @Override
    public void pushIntentFlow(Intent intent, FlowAction flowAction) {
        // TODO: Extend to support other actions
        LOG.info("Intent: {}, FlowAction: {}", intent.toString(), flowAction.getValue());
        Action actionContainer = (Action) intent.getActions().get(0).getAction();
        List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        // FIXME use a factory pattern here ?
        intentFlowManager.setEndPointGroups(endPointGroups);
        intentFlowManager.setAction(actionContainer);
        // MPLS stuff
        mplsIntentFlowManager.setEndPointGroups(endPointGroups);
        mplsIntentFlowManager.setAction(actionContainer);
        Subjects source = intent.getSubjects().get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        Subjects target = intent.getSubjects().get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        if (intentMappingService.get(source.getSubject().toString())
                                .containsKey(OFRendererConstants.MPLS_LABEL_KEY)
                && intentMappingService.get(target.getSubject().toString())
                                       .containsKey(OFRendererConstants.MPLS_LABEL_KEY)) {
            generateMplsFlows(source, target);
        } else {
            //Get all node Id's
            Map<Node, List<NodeConnector>> nodeMap = getNodes();
            for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
                //Push flow to every node for now
                intentFlowManager.pushFlow(entry.getKey().getId(), flowAction);
            }
        }
    }

    private void generateMplsFlows(Subjects source, Subjects target) {
        String sourceNodeConnectorId = intentMappingService.get(source.getSubject().toString())
                                                           .get(OFRendererConstants.SWITCH_PORT_KEY);
        String targetNodeConnectorId = intentMappingService.get(target.getSubject().toString())
                                                           .get(OFRendererConstants.SWITCH_PORT_KEY);
        org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId sourceNodeId = extractTopologyNodeId(sourceNodeConnectorId);
        org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId targetNodeId = extractTopologyNodeId(targetNodeConnectorId);
        List<Link> shortestPath = graphService.getShortestPath(sourceNodeId, targetNodeId);
        for (Link link: shortestPath) {
            String sourceLinkTp = link.getSource().getSourceTp().getValue();
            String targetLinkTp = link.getDestination().getDestTp().getValue();
            if (sourceLinkTp.equals(sourceNodeConnectorId)) {
                NodeId sourceInvNodeId = new NodeId(sourceNodeId.getValue());
                mplsIntentFlowManager.pushMplsFlow(sourceInvNodeId, FlowAction.ADD_FLOW, sourceLinkTp);
            } else if(targetLinkTp.equals(targetNodeConnectorId)) {
                NodeId destInvNodeId = new NodeId(targetLinkTp);
                mplsIntentFlowManager.popMplsFlow(destInvNodeId, FlowAction.ADD_FLOW);
            } else {
                NodeId currentNodeId = new NodeId(link.getSource().getSourceNode());
                String outputPort = link.getDestination().getDestTp().getValue();
                mplsIntentFlowManager.forwardMplsFlow(currentNodeId, FlowAction.ADD_FLOW, outputPort);
            }
            link.getSource().getSourceTp().getValue();
        }
    }

    //FIXME move to a utility class
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
    public void pushARPFlow(NodeId nodeId, FlowAction flowAction) {
        arpFlowManager.pushFlow(nodeId, flowAction);
    }

    private Map<Node, List<NodeConnector>> getNodes() {
        Map<Node, List<NodeConnector>> nodeMap = new HashMap<Node, List<NodeConnector>>();
        Nodes nodeList = new NodesBuilder().build();
        ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            final InstanceIdentifier<Nodes> nodesIdentifier = InstanceIdentifier.create(Nodes.class);
            final CheckedFuture<Optional<Nodes>, ReadFailedException> txCheckedFuture = tx.read(LogicalDatastoreType
                    .OPERATIONAL, nodesIdentifier);
            nodeList = txCheckedFuture.checkedGet().get();

            for (Node node : nodeList.getNode()) {
                LOG.info("Node ID : {}", node.getId());
                List<NodeConnector> nodeConnector = node.getNodeConnector();
                nodeMap.put(node, nodeConnector);
            }
        } catch (ReadFailedException e) {
            //TODO: Perform fail over
            LOG.error("Error reading Nodes from MD-SAL", e);
        }
        return nodeMap;
    }

    /**
     * Creates a hashmap of the mapping information map for every subject
     * @param endPointGroups :list of endpoint group from Intent request
     * @return :double hashmap with mapping details of sujects
     */
    private Map<String, Map<String, String>> extractSubjectDetails(List<String> endPointGroups) {
        Map<String, Map<String, String>> subjectsMapping = new HashMap<String, Map<String, String>>();
        for (String id : endPointGroups) {
            Map<String, String> values = intentMappingService.get(id);
            if( values != null && values.size() >0 ) {
                subjectsMapping.put(id, values);
            } else {
                LOG.warn("No key found for {} in IntentMappingService", id);
            }
        }
        return subjectsMapping;
    }

    @Override
    public void close() throws Exception {
        for (ServiceRegistration<?> service: serviceRegistration) {
            if (service != null) {
                service.unregister();
            }
        }
    }

    @Override
    public void pushLLDPFlow(NodeId nodeId, FlowAction flowAction) {
        lldpFlowManager.pushFlow(nodeId, flowAction);
    }
}
