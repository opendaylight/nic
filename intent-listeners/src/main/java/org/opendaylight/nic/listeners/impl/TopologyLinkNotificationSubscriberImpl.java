/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.TopologyLinkDeleted;
import org.opendaylight.nic.listeners.api.TopologyLinkUp;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.utils.IidFactory;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyLinkNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private OFRendererGraphService graphService;
    private MdsalUtils mdsalUtils;
    private static final Logger LOG = LoggerFactory.getLogger(TopologyLinkNotificationSubscriberImpl.class);

    public TopologyLinkNotificationSubscriberImpl(OFRendererGraphService graphService,
                                                  MdsalUtils mdsalUtils) {
        this.graphService = graphService;
        this.mdsalUtils = mdsalUtils;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (TopologyLinkUp.class.isInstance(event)) {
            LOG.trace("TOPOLOGY LINK ADDED");
            Topology topo = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL,
                    IidFactory.getTopologyLinkInstanceIdentifier());
            if (graphService.getGraph() != null) {
                graphService.setLinks(topo.getLink());
            }
        }
        if (TopologyLinkDeleted.class.isInstance(event)) {
            LOG.trace("TOPOLOGY LINK REMOVED");
            Topology topo = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL,
                    IidFactory.getTopologyLinkInstanceIdentifier());
            if (graphService.getGraph() != null) {
                graphService.setLinks(topo.getLink());
            }
        }
    }

}
