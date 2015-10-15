/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.opendaylight.controller.liblldp.EtherTypes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

/**
 * Contains methods creating flow part for ARP flow.
 */
public class FlowUtils {

    private static final String HOST_MASK = "/32";

    /**
     * Creates {@link EthernetMatch} containing ARP ether-type and the given destination MAC address
     */
    public static EthernetMatch createEthernetMatch() {
        return new EthernetMatchBuilder().setEthernetType(
                new EthernetTypeBuilder().setType(new EtherType(Long.valueOf(EtherTypes.ARP.intValue()))).build())
            .build();
    }


//    /**
//     * Creates {@link ArpMatch} containing Reply ARP operation, THA and TPA for the given target
//     * address and SPA for the given sender protocol address
//     */
//    public static ArpMatch createArpMatch() {
//        return new ArpMatchBuilder().setArpOp(ArpOperation.REPLY.intValue()).build();
//    }
    /**
     * Creates {@link Action} representing output to the controller
     *
     * @param order the order for the action
     */
    public static Action createSendToControllerAction(int order) {
        return new ActionBuilder().setOrder(order)
            .setKey(new ActionKey(order))
            .setAction(
                    new OutputActionCaseBuilder().setOutputAction(
                            new OutputActionBuilder().setMaxLength(0xffff)
                                .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                .build()).build())
            .build();
    }

    public static Action createOutputNormal(int order) {
        return new ActionBuilder().setOrder(order)
                .setKey(new ActionKey(order))
                .setAction(
                        new OutputActionCaseBuilder().setOutputAction(
                                new OutputActionBuilder().setMaxLength(0xffff)
                                        .setOutputNodeConnector(new Uri(OutputPortValues.NORMAL.toString()))
                                        .build()).build())
                .build();
    }

}
