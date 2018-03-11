/*
 * Copyright (c) 2018 Lumina Networks.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.listener;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkEvents implements DataTreeChangeListener<Node>, NetworkEventsService {
    private static final Logger LOG = LoggerFactory.getLogger(NetworkEvents.class);

    private final Set<TopologyListener> topologyListeners;
    private static final InstanceIdentifier<Node> NODE_IDENTIFIER = InstanceIdentifier
            .create(Nodes.class).child(Node.class);

    private final DataBroker dataBroker;
    private ListenerRegistration<?> nodeListener;

    public NetworkEvents(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.topologyListeners = Sets.newConcurrentHashSet();
    }

    @Override
    public void start() {
        final DataTreeIdentifier<Node> identifier =  new DataTreeIdentifier
                (LogicalDatastoreType.OPERATIONAL, NODE_IDENTIFIER);
        nodeListener = dataBroker.registerDataTreeChangeListener(identifier, this);
        LOG.info("\nNetwork Events listener initialized with success.");
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Node>> changes) {
        changes.forEach(modification -> {
            final DataObjectModification<Node> rootNode = modification.getRootNode();
            if (isSwitchAdded(rootNode)) {
                final Node nodeAdded = rootNode.getDataAfter();
                final NodeId nodeId = nodeAdded.getId();
                LOG.debug("\nSwitch added: {}", nodeId.getValue());
                topologyListeners.forEach(listener -> listener.onSwitchAdd(nodeId));
            } else {
                final Node nodeRemoved = rootNode.getDataBefore();
                final NodeId nodeId = nodeRemoved.getId();
                LOG.debug("\nSwitch removed: {}", nodeId.getValue());
                topologyListeners.forEach(listener -> listener.onSwitchRemoved(nodeId));
            }
        });
    }

    private synchronized boolean isSwitchAdded(final DataObjectModification<Node> modification) {
        Boolean result = false;
        if (modification.getDataAfter() != null) {
            result = true;
        }
        return result;
    }

    @Override
    public void register(TopologyListener topologyListener) {
        this.topologyListeners.add(topologyListener);
    }

    @Override
    public void unRegister(TopologyListener topologyListener) {
        this.topologyListeners.remove(topologyListener);
    }

    @Override
    public void close() throws Exception {
        topologyListeners.clear();
        nodeListener.close();
    }
}
