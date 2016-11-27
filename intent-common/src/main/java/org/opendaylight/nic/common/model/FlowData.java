/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.model;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

import java.util.UUID;

public abstract class FlowData {

    private UUID id;

    protected IpAddress srcIpAddress;
    protected IpAddress dstIpAddress;
    protected MacAddress srcMacAddress;
    protected MacAddress dstMacAddress;
    protected PortNumber srcPortNumber;
    protected PortNumber dstPortNumber;
    protected FlowAction flowAction;
    protected IntentModifier intentModifier;

    public FlowData(final IpAddress srcIpAddress,
                    final IpAddress dstIpAddress,
                    final FlowAction flowAction) {
        this.id = UUID.randomUUID();
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
        this.flowAction = flowAction;
    }

    public FlowData(final MacAddress srcMacAddress,
                    final MacAddress dstMacAddress,
                    final FlowAction flowAction) {
        this.id = UUID.randomUUID();
        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
        this.flowAction = flowAction;
    }

    public FlowData(final PortNumber srcPortNumber,
                    final PortNumber dstPortNumber,
                    final FlowAction flowAction) {
        this.id = UUID.randomUUID();
        this.srcPortNumber = srcPortNumber;
        this.dstPortNumber = dstPortNumber;
        this.flowAction = flowAction;
    }

    public FlowData(final IpAddress srcIpAddress,
                    final IpAddress dstIpAddress,
                    final MacAddress srcMacAddress,
                    final MacAddress dstMacAddress,
                    final PortNumber srcPortNumber,
                    final PortNumber dstPortNumber,
                    final FlowAction flowAction) {
        this.id = UUID.randomUUID();
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
        this.srcPortNumber = srcPortNumber;
        this.dstPortNumber = dstPortNumber;
        this.flowAction = flowAction;
    }

    private FlowData(){}

    public UUID getId() {
        return id;
    }

    public void setIntentModifier(final IntentModifier intentModifier) {
        this.intentModifier = intentModifier;
    }
}
