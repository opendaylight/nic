/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.mapping.api;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class MplsEgressLabel extends MappedObject {

    private static final String MPLS_LABEL = "mpls_egress_label";
    private String label;

    @Override
    public String getType() {
        return MPLS_LABEL;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(label);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        label = in.readUTF();
    }

    @Override
    public String toString() {
        return "[type = " + MPLS_LABEL + ", value=" + label + "]";
    }

    public String getLabel() {
        return label == null ? "" : label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
