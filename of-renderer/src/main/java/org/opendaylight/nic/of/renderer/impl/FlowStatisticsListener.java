/*
 * Copyright (c) 2015 NEC Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class log the flow statistics details.
 */
public class FlowStatisticsListener implements AutoCloseable, DataChangeListener {

    /**
     * Logger instance.
     */
    private static final Logger  LOG = LoggerFactory.getLogger(FlowStatisticsListener.class);

    /**
     * DataBroker Object to perform MDSAL operation.
     */
    private DataBroker dataBroker = null;

    /**
     * ListenerRegistration Object to perform registration.
     */
    private ListenerRegistration<DataChangeListener> registration;

    /**
     * Flow Table ID to install flow entries.
     */
    public static final short TABLE_ID = 0;

    /**
     * Class constructor setting the data broker.
     *
     * @param dataBroker the {@link org.opendaylight.controller.md.sal.binding.api.DataBroker}
     */
    public FlowStatisticsListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method to register flowStatistics.
     * @param dataBroker the {@link org.opendaylight.controller.md.sal.binding.api.DataBroker}
     * @param nodeId Node Id.
     * @param flowId Flow Id.
     */
    public void registerFlowStatisticsListener(DataBroker dataBroker, NodeId nodeId, FlowId flowId) {
        InstanceIdentifier<FlowStatistics> path = InstanceIdentifier.builder(Nodes.class).
                child(Node.class, new NodeKey(nodeId)).
                augmentation(FlowCapableNode.class).
                child(Table.class, new TableKey(TABLE_ID)).
                child(Flow.class, new FlowKey(flowId)).
                augmentation(FlowStatisticsData.class).
                child(FlowStatistics.class).
                build();
        registration = this.dataBroker.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, path, this, DataChangeScope.SUBTREE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        if (registration != null) {
            registration.close();
        }
    }

    /**
     * This method is called to listen the flow statistics changes.
     */
    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        createFlowStatistics(changes);
    }

    /**
     * Method invoked when flowStatistics is updated and log statistics details.
     * @param changes
     */
    private void createFlowStatistics(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> newFlow : changes.getCreatedData().entrySet()) {
            if (newFlow.getValue() instanceof FlowStatistics) {
                FlowStatistics flowStatistics = (FlowStatistics)newFlow.getValue();
                LOG.info("Flow Statistics gathering for Byte Count:{}", flowStatistics.getByteCount());
                LOG.info("Flow Statistics gathering for Packet Count:{}", flowStatistics.getPacketCount());
                LOG.info("Flow Statistics gathering for Duration in seconds::{}",
                        flowStatistics.getDuration().getSecond().getValue().intValue());
                LOG.info("Flow Statistics gathering for Duration in Nano seconds::{}",
                        flowStatistics.getDuration().getNanosecond().getValue().intValue());
            }
        }
    }
}
