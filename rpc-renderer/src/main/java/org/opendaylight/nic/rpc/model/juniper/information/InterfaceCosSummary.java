/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.information;

import com.fasterxml.jackson.databind.JsonNode;
import org.opendaylight.nic.rpc.exception.JuniperModelNotSupportedException;

import static org.opendaylight.nic.rpc.utils.RESTUtils.extractIntData;
import static org.opendaylight.nic.rpc.utils.RESTUtils.extractStringData;

/**
 * Created by yrineu on 17/07/17.
 */
public class InterfaceCosSummary {

    private Integer forwardingClassesSupported;
    private Integer forwardingClassesInUs;
    private String queueType;
    private Integer numQueuesSupported;
    private Integer numQueuesInUse;

    public InterfaceCosSummary(final Integer forwardingClassesSupported,
                               final Integer forwardingClassesInUs,
                               final String queueType,
                               final Integer numQueuesSupported,
                               final Integer numQueuesInUse) {
        this.forwardingClassesSupported = forwardingClassesSupported;
        this.forwardingClassesInUs = forwardingClassesInUs;
        this.queueType = queueType;
        this.numQueuesSupported = numQueuesSupported;
        this.numQueuesInUse = numQueuesInUse;
    }

    public static InterfaceCosSummary getInstanceBy(final JsonNode mainNode) throws JuniperModelNotSupportedException {
        InterfaceCosSummary interfaceCosSummary;
        try {
            final JsonNode node = mainNode.elements().next();
            final Integer forwardingClassesSupported = extractIntData(node.get("intf-cos-forwarding-classes-supported"));
            final Integer forwardingClassesInUs = extractIntData(node.get("intf-cos-forwarding-classes-in-use"));
            final String queueType = extractStringData(node.get("intf-cos-queue-type"));
            final Integer numQueuesSupported = extractIntData(node.get("intf-cos-num-queues-supported"));
            final Integer numQueuesInUse = extractIntData(node.get("intf-cos-num-queues-in-use"));

            interfaceCosSummary = new InterfaceCosSummary(
                    forwardingClassesSupported,
                    forwardingClassesInUs,
                    queueType,
                    numQueuesSupported,
                    numQueuesInUse);
        } catch (Exception e) {
            throw new JuniperModelNotSupportedException(e.getMessage());
        }
        return interfaceCosSummary;
    }


    public Integer getForwardingClassesSupported() {
        return forwardingClassesSupported;
    }

    public Integer getForwardingClassesInUs() {
        return forwardingClassesInUs;
    }

    public String getQueueType() {
        return queueType;
    }

    public Integer getNumQueuesSupported() {
        return numQueuesSupported;
    }

    public Integer getNumQueuesInUse() {
        return numQueuesInUse;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
