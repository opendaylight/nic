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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;

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
     * Creates {@link ArpMatch} containing Reply ARP operation, THA and TPA for the given target
     * address and SPA for the given sender protocol address
     */
//    public static ArpMatch createArpMatch() {
//        return new ArpMatchBuilder().setArpOp(ArpOperation.REPLY.intValue()).build();
//    }

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
     * Create OF action to perform MPLS push or pop label
     * @param order An integer representing the order of the Action
     * within the table.
     * @param popLabel true for MPLS pop action
     * @return Action containing MPLS informations
     */
    public static Action createMPLSAction(int order, boolean popLabel) {
        Action action = null;
        ActionBuilder ab = new ActionBuilder();

        if (popLabel) {
            PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
            // EthernetType will change based on the packet resulting after the topmost MPLS header has been removed
            popMplsActionBuilder.setEthernetType(EtherTypes.MPLSUCAST.intValue());
            ab.setOrder(order).setKey(new ActionKey(order)).setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMplsActionBuilder.build()).build());
            action = ab.build();
        } else {

            PushMplsActionBuilder pushMplsActionBuilder = new PushMplsActionBuilder();
            // EthernetType of MPLS tag
            pushMplsActionBuilder.setEthernetType(EtherTypes.MPLSUCAST.intValue());
            ab.setOrder(order).setKey(new ActionKey(order)).setAction(new PushMplsActionCaseBuilder().setPushMplsAction(pushMplsActionBuilder.build()).build());
            action = ab.build();
        }
        return action;
    }

    /**
     * Create OF action to set the set_field with mpls label
     * @param order An integer representing the order of the Action
     * within the table.
     * @param label MPLS label value
     * @param bos Bottom of Stack value
     * @return Action with an Order, Label and Bottom of Stack value
     */
    public static Action createSetFieldMPLSLabelAction(int order, Long label, Short bos) {
        Action action = null;
        ActionBuilder ab = new ActionBuilder();

        ProtocolMatchFieldsBuilder matchFieldsBuilder = new ProtocolMatchFieldsBuilder().setMplsLabel(label).setMplsBos(bos);
        ab.setOrder(order).setKey(new ActionKey(order)).setAction(new SetFieldCaseBuilder().
                setSetField(new SetFieldBuilder().setProtocolMatchFields(matchFieldsBuilder.build()).build()).build());
        action = ab.build();
        return action;
    }

    /**
     * Create OF action to output to a specific port
     * @param order An integer representing the order of the Action
     * within the table.
     * @param outputPort OVS port to output the packet to
     * @return Action with Order and Output port
     */
    public static Action createOutputToPort(int order, String outputPort) {
        return new ActionBuilder().setOrder(order)
                .setKey(new ActionKey(order))
                .setAction(
                        new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder().setMaxLength(0xffff)
                                .setOutputNodeConnector(new Uri(outputPort)).build())
                                .build())
                                .build();
    }
}
