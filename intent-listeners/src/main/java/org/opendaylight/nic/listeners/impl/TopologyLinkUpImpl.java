/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.TopologyLinkUp;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import java.sql.Timestamp;
import java.util.Date;

public class TopologyLinkUpImpl implements TopologyLinkUp {

    private Link link;
    private final Timestamp timeStamp;
    private LinkId linkId;

    public TopologyLinkUpImpl(Link link, LinkId linkId) {
        this.link = link;
        this.linkId = linkId;
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public Timestamp getTimeStamp() {
        return this.timeStamp;
    }

    @Override
    public Link getLink() {
        return this.link;
    }

    @Override
    public LinkId getLinkId() {
        return linkId;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public void setLinkId(LinkId linkId) {
        this.linkId = linkId;
    }
}
