/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.utils;

import com.google.common.base.Preconditions;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

public class MatchUtils {

    private MatchUtils() {
    }

    /**
     * Create Ethernet Source Match
     *
     * @param matchBuilder MatchBuilder Object without a match yet
     * @param srcMac     String representing a source MAC
     * @param dstMac     String representing a destination MAC
     * @return matchBuilder Map MatchBuilder Object with a match
     */
    public static MatchBuilder createEthMatch(MatchBuilder matchBuilder, MacAddress srcMac, MacAddress dstMac) {
        Preconditions.checkNotNull(matchBuilder);
        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        if (srcMac != null) {
            EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
            ethSourceBuilder.setAddress(srcMac);
            ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        }
        if (dstMac != null) {
            EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
            ethDestinationBuilder.setAddress(dstMac);
            ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        }
        matchBuilder.setEthernetMatch(ethernetMatch.build());
        return matchBuilder;
    }
}
