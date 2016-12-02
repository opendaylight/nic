/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.common.model.FlowAction;
import org.opendaylight.nic.common.model.FlowData;
import org.opendaylight.nic.common.model.FlowDataL3;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentNBIAdded;
import org.opendaylight.nic.listeners.api.IntentNBIRemoved;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentDefinition;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.intent.definition.objects.mapping.object.type.media.flow.MediaFlow;


public class IntentNBINotificationSubscriberImpl implements IEventListener<NicNotification> {

    private OFRendererFlowService flowService;

    public IntentNBINotificationSubscriberImpl(OFRendererFlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (IntentNBIAdded.class.isInstance(event)) {
            IntentNBIAdded addedEvent = (IntentNBIAdded) event;
            IntentDefinition intent = addedEvent.getIntent();

            //TODO: Abstract pushIntentFlow method to receive the new Intent-NBI

            //Associations
            intent.getAssociations().forEach(assoc-> {
                assoc.getModifierName();
            });

            //Objects
            intent.getObjects().forEach(obj -> {
                if (obj.getMapping().getObjectType() instanceof MediaFlow){
                    final MediaFlow mediaFlow = ((MediaFlow) obj.getMapping().getObjectType());
                    final FlowData flowDataL3 =
                            new FlowDataL3(mediaFlow.getSourceIpPrefix(),
                            mediaFlow.getDestinationIpPrefix(), FlowAction.ALLOW);

                    //TODO: Create flowId to represent each association to be able to remove an specific flow when an object is out of a group
                    flowService.pushIntentFlow(flowDataL3);
                }
            });

            //Object groups
            intent.getObjectGroups().forEach(objGrp -> {
                objGrp.getMemberObjectGroups().forEach(memberGrp -> {
                    memberGrp.getObjectGroupName();
                });
            });

            FlowData l3 = new FlowDataL3(null, null, FlowAction.ALLOW);
            flowService.pushIntentFlow(l3);
        }
        if (IntentNBIRemoved.class.isInstance(event)) {
            IntentNBIRemoved deleteEvent = (IntentNBIRemoved) event;
            //TODO: Abstract pushIntentFlow method to receive the new Intent-NBI
            //flowService.pushIntentFlow(deleteEvent.getIntent(), FlowAction.REMOVE_FLOW);
        }
    }
}
