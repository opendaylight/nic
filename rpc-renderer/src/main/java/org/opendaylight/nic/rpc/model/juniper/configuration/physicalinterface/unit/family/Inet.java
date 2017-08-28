/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface.unit.family;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

/**
 * Created by yrineu on 21/07/17.
 */
public class Inet {

    private StringBuffer result;
    private Ipv4Prefix ipv4Prefix;

    private Inet(final Ipv4Prefix ipv4Prefix,
                 final StringBuffer result) {
        this.ipv4Prefix = ipv4Prefix;
        this.result = result;
    }

    protected static Inet create(final Ipv4Prefix ipv4Prefix,
                                 final StringBuffer buffer) {
        return new Inet(ipv4Prefix, buffer);
    }

    protected String generateRPCStructure() {
        result.append("<inet>");
        result.append("<address>");
        result.append("<name>" + ipv4Prefix.getValue() + "</name>");
        result.append("</address>");
        result.append("</inet>");
        return result.toString();
    }
}
