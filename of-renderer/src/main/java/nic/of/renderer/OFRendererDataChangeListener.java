/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import nic.of.renderer.flow.FlowAction;
import nic.of.renderer.flow.OFRendererFlowService;
import nic.of.renderer.flow.OFRendererFlowServiceFactory;
import nic.of.renderer.utils.IntentUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererDataChangeListener implements DataChangeListener,AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererDataChangeListener.class);

    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> ofRendererListener = null;

    private OFRendererFlowService flowService;

    public OFRendererDataChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        // TODO: This should listen on something else
        ofRendererListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.builder(Intents.class)
                        .child(Intent.class)
                                .build(), this, AsyncDataBroker.DataChangeScope.SUBTREE);
        flowService = OFRendererFlowServiceFactory.getInstance(dataBroker);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> asyncDataChangeEvent) {
        LOG.info("Intent tree changed");
        create(asyncDataChangeEvent.getCreatedData());
        delete(asyncDataChangeEvent);
    }

    private void create(Map<InstanceIdentifier<?>, DataObject> changes) {
        Set<Intent> intents = processCreatedChanges(changes);
        for (Intent intent : intents) {
            pushIntentFlow(intent, FlowAction.ADD_FLOW);
        }
    }

    private void delete(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        LOG.info("Preparing for send changes to remove!");
        Map<InstanceIdentifier<?>, DataObject> original = changes.getOriginalData();
        Set<Intent> intents = processRemovedChanges(changes.getRemovedPaths(), original);
        for (Intent intent : intents) {
            LOG.info("Sending intents for remove: {}", intent.toString());
            pushIntentFlow(intent, FlowAction.REMOVE_FLOW);
        }
    }

    private Set<Intent> processCreatedChanges(Map<InstanceIdentifier<?>, DataObject> changes) {
        Set<Intent> result = new HashSet<>();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> created : changes.entrySet()) {
            if (created.getValue() != null && created.getValue() instanceof Intent) {
                Intent intent = (Intent) created.getValue();
                LOG.info("Creating intent with id {}.", intent);
                if (!IntentUtils.verifyIntent(intent)) {
                    LOG.info("Intent verification failed");
                } else {
                    result.add(intent);
                }
            }
        }
        return result;
    }

    private Set<Intent> processRemovedChanges(Set<InstanceIdentifier<?>> changes,
                                              Map<InstanceIdentifier<?>, DataObject> original) {
        LOG.info("Processing removed changes: {}", changes.toString());
        Set<Intent> result = new HashSet<>();
        for (InstanceIdentifier<?> identifier : changes) {
            DataObject dataObject = original.get(identifier);
            if (dataObject instanceof  Intent) {
                Intent intent = (Intent)dataObject;
                result.add(intent);
            }
        }
        LOG.info("Returning intents processed: {}", result.toString());
        return result;
    }

    private void pushIntentFlow(Intent intent, final FlowAction flowAction) {
        // TODO: Extend to support other actions
        LOG.info("Intent: {}, FlowAction: {}", intent.toString(), flowAction.getValue());
        Action actionContainer = (Action) intent.getActions().get(0).getAction();

        List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        //Get all node Id's
        Map<Node, List<NodeConnector>> nodeMap = getNodes();
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
            //Push flow to every node for now
            flowService.pushL2Flow(entry.getKey().getId(), endPointGroups, actionContainer, flowAction);
        }
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

    }
}