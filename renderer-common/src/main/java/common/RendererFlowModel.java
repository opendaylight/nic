/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package common;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

import java.util.List;

/**
 * Created by yrineu on 30/03/16.
 */
public class RendererFlowModel {

    private List<String> intentIds;
    private IpAddress srcIpAddress;
    private IpAddress dstIpAddress;
    private MacAddress srcMacAddress;
    private MacAddress dstMacAddress;
    private PortNumber srcPortNumber;
    private PortNumber dstPortNumber;
    private RendererAction action;

    public void setIntentId(List<String> intentIds) {
        this.intentIds = intentIds;
    }

    public void setSrcIpAddress(IpAddress srcIpAddress) {
        this.srcIpAddress = srcIpAddress;
    }

    public void setDstIpAddress(IpAddress dstIpAddress) {
        this.dstIpAddress = dstIpAddress;
    }

    public void setSrcMacAddress(MacAddress srcMacAddress) {
        this.srcMacAddress = srcMacAddress;
    }

    public void setDstMacAddress(MacAddress dstMacAddress) {
        this.dstMacAddress = dstMacAddress;
    }

    public void setSrcPortNumber(PortNumber srcPortNumber) {
        this.srcPortNumber = srcPortNumber;
    }

    public void setDstPortNumber(PortNumber dstPortNumber) {
        this.dstPortNumber = dstPortNumber;
    }

    public void setAction(RendererAction action) {
        this.action = action;
    }
}
