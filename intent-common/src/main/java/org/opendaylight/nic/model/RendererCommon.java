/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
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

/**
 * Created by yrineu on 04/10/16.
 */
public class RendererCommon {

    private UUID id;

    protected IpAddress srcIpAddress;
    protected IpAddress dstIpAddress;
    protected MacAddress srcMacAddress;
    protected MacAddress dstMacAddress;
    protected PortNumber srcPortNumber;
    protected PortNumber dstPortNumber;
    protected RendererAction rendererAction;

    public RendererCommon(final IpAddress srcIpAddress,
                          final IpAddress dstIpAddress,
                          final RendererAction rendererAction) {
        this.id = UUID.randomUUID();
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
        this.rendererAction = rendererAction;
    }

    public RendererCommon(final MacAddress srcMacAddress,
                          final MacAddress dstMacAddress,
                          final RendererAction rendererAction) {
        this.id = UUID.randomUUID();
        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
        this.rendererAction = rendererAction;
    }

    public RendererCommon(final IpAddress srcIpAddress,
                          final IpAddress dstIpAddress,
                          final MacAddress srcMacAddress,
                          final MacAddress dstMacAddress,
                          final PortNumber srcPortNumber,
                          final PortNumber dstPortNumber,
                          final RendererAction rendererAction) {
        this.id = UUID.randomUUID();
        this.srcIpAddress = srcIpAddress;
        this.dstIpAddress = dstIpAddress;
        this.srcMacAddress = srcMacAddress;
        this.dstMacAddress = dstMacAddress;
        this.srcPortNumber = srcPortNumber;
        this.dstPortNumber = dstPortNumber;
        this.rendererAction = rendererAction;
    }

    private RendererCommon(){}

    public UUID getId() {
        return id;
    }
}