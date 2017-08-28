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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.opendaylight.nic.rpc.utils.RESTUtils.*;

/**
 * Created by yrineu on 17/07/17.
 */
public class PhysicalInterface {
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalInterface.class);

    private String name;
    private InterfaceStatus adminStatus;
    private InterfaceStatus operStatus;
    private Integer localIndex;
    private Integer snmpIndex;
    private QueueCounters queueCounters;

    public PhysicalInterface(final String name,
                             final InterfaceStatus adminStatus,
                             final InterfaceStatus operStatus,
                             final Integer localIndex,
                             final Integer snmpIndex,
                             final QueueCounters queueCounters) {
        this.name = name;
        this.adminStatus = adminStatus;
        this.operStatus = operStatus;
        this.localIndex = localIndex;
        this.snmpIndex = snmpIndex;
        this.queueCounters = queueCounters;
    }

    public static Set<PhysicalInterface> getInstanceBy(final JsonNode mainNode) {
        Set<PhysicalInterface> physicalInterfaces = new HashSet<>();
        try {
            final Iterator<JsonNode> elementsIterator = mainNode.elements();
            while (elementsIterator.hasNext()) {
                final JsonNode node = elementsIterator.next();
                if (!isEmptyNode(node)) {
                    final String name = extractStringData(node.get("name"));
                    final InterfaceStatus adminStatus = InterfaceStatus.valueOf(extractStringData(node.get("admin-status")));
                    final InterfaceStatus operStatus = InterfaceStatus.valueOf(extractStringData(node.get("oper-status")));
                    final Integer localIndex = extractIntData(node.get("local-index"));
                    final Integer snmpIndex = extractIntData(node.get("snmp-index"));
                    final QueueCounters queueCounters = QueueCounters.getInstanceFrom(node.get("queue-counters"));

                    physicalInterfaces.add(new PhysicalInterface(name, adminStatus, operStatus, localIndex, snmpIndex, queueCounters));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new JuniperModelNotSupportedException(e.getMessage());
        }
        return physicalInterfaces;
    }

    public String getName() {
        return name;
    }

    public InterfaceStatus getAdminStatus() {
        return adminStatus;
    }

    public InterfaceStatus getOperStatus() {
        return operStatus;
    }

    public Integer getLocalIndex() {
        return localIndex;
    }

    public Integer getSnmpIndex() {
        return snmpIndex;
    }

    public QueueCounters getQueueCounters() {
        return queueCounters;
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
