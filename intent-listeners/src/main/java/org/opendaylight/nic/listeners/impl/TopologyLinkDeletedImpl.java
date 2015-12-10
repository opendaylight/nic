/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.opendaylight.nic.listeners.api.TopologyLinkDeleted;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

public class TopologyLinkDeletedImpl implements TopologyLinkDeleted {

    private final Timestamp timeStamp;
    private final Link link;

    public TopologyLinkDeletedImpl(Link link) {
      this.link = link;
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

}
