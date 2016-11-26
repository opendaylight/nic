/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.model;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

import java.util.UUID;

public class RendererCommon {

    private UUID id;

    protected IpAddress srcIpAddress;
    protected IpAddress dstIpAddress;
    protected MacAddress srcMacAddress;
    protected MacAddress dstMacAddress;
    protected PortNumber srcPortNumber;
    protected PortNumber dstPortNumber;

    public RendererCommon(final IpAddress srcIpAddress,
                          final IpAddress dstIpAddress) {
        this.id = UUID.randomUUID();
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
    }

    public RendererCommon(final MacAddress srcMacAddress,
                          final MacAddress dstMacAddress) {
        this.id = UUID.randomUUID();
        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
    }

    public RendererCommon(final IpAddress srcIpAddress,
                          final IpAddress dstIpAddress,
                          final MacAddress srcMacAddress,
                          final MacAddress dstMacAddress,
                          final PortNumber srcPortNumber,
                          final PortNumber dstPortNumber) {
        this.id = UUID.randomUUID();
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
        this.srcPortNumber = srcPortNumber;
        this.dstPortNumber = dstPortNumber;
    }

    private RendererCommon(){}

    public UUID getId() {
        return id;
    }
}
