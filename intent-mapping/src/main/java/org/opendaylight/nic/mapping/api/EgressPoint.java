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

public class EgressPoint extends MappedObject {

    private String EGRESS_POINT = "egress_point";
    private String egressPoint;

    @Override
    public String toString() {
        return "[type = egress_point, value=" + egressPoint + "]";
    }

    public String getEgressPoint() {
        return egressPoint == null ? "" : egressPoint;
    }

    public void setEgressPoint(String egressPoint) {
        this.egressPoint = egressPoint;
    }

    @Override
    public String getType() {
        return EGRESS_POINT;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(egressPoint);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        egressPoint = in.readUTF();
    }

}
