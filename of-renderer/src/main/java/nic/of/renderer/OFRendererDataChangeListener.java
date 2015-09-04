/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nic.of.renderer.flow.OFRendererFlowService;
import nic.of.renderer.flow.OFRendererFlowServiceFactory;
import nic.of.renderer.utils.IntentUtils;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererDataChangeListener implements DataChangeListener,AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererDataChangeListener.class);

    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> ofRendererListener = null;
    private static final int  NUM_OF_SUPPORTED_EPG = 2;
    private static final int NUM_OF_SUPPORTED_ACTION = 1;
    
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
//        update(asyncDataChangeEvent.getUpdatedData());
//        delete(asyncDataChangeEvent);
    }

    private void create(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Map.Entry<InstanceIdentifier<?>, DataObject> created : changes.entrySet()) {
            if (created.getValue() != null && created.getValue() instanceof Intent) {
                Intent intent = (Intent) created.getValue();
                LOG.info("Creating intent with id {}.", intent);
                if (!IntentUtils.verifyIntent(intent)) {
                    LOG.info("Intent verification failed");
                    return;
                }
                parseIntent(intent);
            }
        }
    }

    private Map<Node, List<NodeConnector>> getNodes() {
        Map<Node, List<NodeConnector>> nodeMap = new HashMap<Node, List<NodeConnector>>();
        Nodes nodeList = new NodesBuilder().build();
        ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            nodeList = tx.read(LogicalDatastoreType.OPERATIONAL,
                    InstanceIdentifier.create(Nodes.class))
                    .checkedGet().get();

            for (Node node : nodeList.getNode()) {
                LOG.info("Node ID : {}", node.getId());
                List<NodeConnector> nodeConnector = node.getNodeConnector();
                nodeMap.put(node, nodeConnector);
            }
        } catch (ReadFailedException e) {
            LOG.error("Error reading Nodes from MD-SAL");
            e.printStackTrace();
        }
        return nodeMap;
    }

    // TODO: Move this to some common NIC utils
    private void parseIntent(Intent intent) {
        // Retrieve the ID
        Uuid uuid = intent.getId();
        String intentID = uuid.getValue();

        // TODO: Extend to support other actions
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionContainer =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action)
                        intent.getActions().get(0).getAction();

        // TODO: Use Mapping service to resolve the subjects
        // Retrieve the end points
        List<Subjects> listSubjects = intent.getSubjects();
        List<String> endPointGroups = new ArrayList<String>();
        for (Subjects subjects: listSubjects) {
            Subject subject = subjects.getSubject();
            if (!(subject instanceof EndPointGroup)
                    && !(subject instanceof EndPointSelector)
                    && !(subject instanceof EndPointGroupSelector)) {
                LOG.info("Subject is not specified: {}", intentID);
                return;
            }
            EndPointGroup endPointGroup = (EndPointGroup) subject;

            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group
                    .EndPointGroup epg = endPointGroup.getEndPointGroup();
            if (epg == null) {
                LOG.info("End Point Group is not specified: {}", intentID);
                return;
            }
            endPointGroups.add(epg.getName());
        }
        //Get all node Id's
        Map<Node, List<NodeConnector>> nodeMap = getNodes();
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
            //Push flow to every node for now
            flowService.pushL2Flow(entry.getKey().getId(), endPointGroups, actionContainer);
        }
    }

    @Override
    public void close() throws Exception {

    }
}