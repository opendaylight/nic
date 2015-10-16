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
import org.opendaylight.nic.of.renderer.api.FlowAction;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.of.renderer.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererFlowManagerProvider implements OFRendererFlowService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererFlowManagerProvider.class);

    protected ServiceRegistration<OFRendererFlowService> nicFlowServiceRegistration;
    private IntentFlowManager intentFlowManager;
    private ArpFlowManager arpFlowManager;
    private DataBroker dataBroker;

    public OFRendererFlowManagerProvider(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        LOG.info("OF Renderer Provider Session Initiated");
        // Register this service with karaf
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicFlowServiceRegistration = context.registerService(OFRendererFlowService.class, this, null);
        intentFlowManager = new IntentFlowManager(dataBroker);
        arpFlowManager = new ArpFlowManager(dataBroker);
    }



    @Override
    public void pushIntentFlow(Intent intent, FlowAction flowAction) {
        // TODO: Extend to support other actions
        LOG.info("Intent: {}, FlowAction: {}", intent.toString(), flowAction.getValue());
        Action actionContainer = (Action) intent.getActions().get(0).getAction();
        List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        intentFlowManager.setEndPointGroups(endPointGroups);
        intentFlowManager.setAction(actionContainer);
        //Get all node Id's
        Map<Node, List<NodeConnector>> nodeMap = getNodes();
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
            //Push flow to every node for now
            intentFlowManager.pushFlow(entry.getKey().getId(), flowAction);
        }
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
            LOG.error("Error reading Nodes from MD-SAL");
        }
        return nodeMap;
    }

    @Override
    public void close() throws Exception {
      if (nicFlowServiceRegistration != null) {
          nicFlowServiceRegistration.unregister();
      }
    }
}
