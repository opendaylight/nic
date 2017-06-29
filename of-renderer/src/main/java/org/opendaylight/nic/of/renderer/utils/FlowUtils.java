/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.opendaylight.controller.liblldp.EtherTypes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

/**
 * Contains methods creating flow part for ARP flow.
 */
public class FlowUtils {

    private FlowUtils() {
    }

    /**
     * Creates {@link EthernetMatch} containing ARP ether-type and the given destination MAC address
     * @return EthernetMatch
     */
    public static EthernetMatch createEthernetMatch() {
        return new EthernetMatchBuilder().setEthernetType(
                new EthernetTypeBuilder().setType(new EtherType(Long.valueOf(EtherTypes.ARP.intValue()))).build())
            .build();
    }


    /**
     * Creates {@link Action} representing output to the controller
     * @param order the order for the action
     * @return Action
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

    /**
     * @param order An integer representing the order of the Action
     * withinin the table.
     * @return Action with an order
     */
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

    /**
     *
     * @param order An integer representing the order of the Action
     * within the table.
     * @param macAddress Destination MAC address
     * @return Action with an order
     */
    public static Action createSetFieldDestinationMacAddress(int order, String macAddress) {
        Action action;
        ActionBuilder ab = new ActionBuilder();

        MacAddress address = MacAddress.getDefaultInstance(macAddress);
        EthernetDestination destination = new EthernetDestinationBuilder().setAddress(address).build();

        EthernetMatchBuilder builder = new EthernetMatchBuilder();
        builder.setEthernetDestination(destination);

        EthernetMatch ethernetMatch = builder.build();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        setFieldBuilder.setEthernetMatch(ethernetMatch);
        SetField setField = setFieldBuilder.build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action acction = new SetFieldCaseBuilder().
                setSetField(setField).build();
        ab.setOrder(order).setKey(new ActionKey(order)).setAction(acction);
        action = ab.build();
        return action;
    }
}
