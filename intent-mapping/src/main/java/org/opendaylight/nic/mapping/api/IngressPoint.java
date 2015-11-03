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

public class IngressPoint extends MappedObject {

    private String INGRESS_POINT = "ingress_point";
    private String ingressPoint;

    @Override
    public String toString() {
        return "[type = egress_point, value=" + ingressPoint + "]";
    }

    public String getIngressPoint() {
        return ingressPoint == null ? "" : ingressPoint;
    }

    public void setIngressPoint(String ingressPoint) {
        this.ingressPoint = ingressPoint;
    }

    @Override
    public String getType() {
        return INGRESS_POINT;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(ingressPoint);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        ingressPoint = in.readUTF();
    }

}
