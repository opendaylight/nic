/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.TopologyLinkUpdated;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import java.sql.Timestamp;
import java.util.Date;

public class TopologyLinkUpdatedImpl implements TopologyLinkUpdated {

    private Link link;
    private final Timestamp timeStamp;
    private LinkId linkId;

    public TopologyLinkUpdatedImpl(Link link, LinkId linkId) {
        this.link = link;
        this.linkId = linkId;
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    /**
     * Returns the timestamp at which the
     * Network-Topology Link was brought up.
     */
    @Override
    public Timestamp getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Returns the Network-Topology Link that was brought up.
     * @return Network-Topology Link
     */
    @Override
    public Link getLink() {
        return this.link;
    }

    /**
     * Returns the Network-Topology LinkId that was brought up.
     * @return Network-Topology LinkId
     */
    @Override
    public LinkId getLinkId() {
        return linkId;
    }

}
