/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.information;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import org.opendaylight.nic.rpc.exception.JuniperModelNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by yrineu on 17/07/17.
 */
public class InterfaceInformation {
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceInformation.class);

    private Set<PhysicalInterface> physicalInterfaces;

    private InterfaceInformation(final Set<PhysicalInterface> physicalInterfaces) {
        this.physicalInterfaces = physicalInterfaces;
    }

    public Set<PhysicalInterface> getPhysicalInterfaces() {
        return physicalInterfaces;
    }

    public static InterfaceInformation getInstanceFrom(final JsonNode mainNode) {
        final Set<PhysicalInterface> interfaces = Sets.newConcurrentHashSet();
        InterfaceInformation interfaceInformation;
        try {
            final Iterator<JsonNode> mainNodeElements = mainNode.elements();
            mainNodeElements.forEachRemaining(element -> {
                interfaces.addAll(PhysicalInterface.getInstanceBy(element.get("physical-interface")));
            });
            interfaceInformation = new InterfaceInformation(interfaces);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new JuniperModelNotSupportedException(e.getMessage());
        }
        return interfaceInformation;
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
