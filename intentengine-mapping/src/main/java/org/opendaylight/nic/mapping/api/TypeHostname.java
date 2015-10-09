/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapping.api;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

import java.io.IOException;

public class TypeHostname extends MappedObject {

    private static final String HOSTNAME = "hostname";
    private String hostname;

    public TypeHostname() {

        hostname = new String();
    }

    @Override
    public String getType() {
        return HOSTNAME;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "[type = hostname, name=" + hostname + "]";
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(hostname);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        hostname = in.readUTF();
    }
}
