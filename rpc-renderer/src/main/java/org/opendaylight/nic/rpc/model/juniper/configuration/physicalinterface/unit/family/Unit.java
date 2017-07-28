/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface.unit.family;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

import java.util.Set;

/**
 * Created by yrineu on 21/07/17.
 */
public class Unit {

    private StringBuffer result;
    private EthernetSwitching ethernetSwitching;
    private Inet inet;
    private int unitName = 0;

    private Unit(final EthernetSwitching ethernetSwitching,
                 final StringBuffer result) {
        this.ethernetSwitching = ethernetSwitching;
        this.result = result;
    }

    private Unit(final Inet inet,
                 final StringBuffer result) {
        this.inet = inet;
        this.result = result;
    }

    private Unit(final EthernetSwitching ethernetSwitching,
                 final Inet inet,
                 final StringBuffer result) {
        this.ethernetSwitching = ethernetSwitching;
        this.inet = inet;
        this.result = result;
    }

    public static Unit create(final Set<String> vlans,
                              final StringBuffer buffer) {
        final EthernetSwitching ethernetSwitching = EthernetSwitching.create(vlans, buffer);
        return new Unit(ethernetSwitching, buffer);
    }

    public static Unit create(final Ipv4Prefix ipv4Prefix,
                              final StringBuffer buffer) {
        final Inet inet = Inet.create(ipv4Prefix, buffer);
        return new Unit(inet, buffer);
    }

    public void generateRPCStructure() {
        result.append("<unit>");
        result.append("<name>" + unitName + "</name>");
        result.append("<family>");
        ethernetSwitching.generateRPCStructure();
        result.append("</family>");
        result.append("</unit>");
    }
}
