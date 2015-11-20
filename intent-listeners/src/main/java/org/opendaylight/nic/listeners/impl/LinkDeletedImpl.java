/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.LinkDeleted;

import java.sql.Timestamp;
import java.util.Date;

public class LinkDeletedImpl implements LinkDeleted {
    private final Timestamp timeStamp;

    public LinkDeletedImpl() {
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
