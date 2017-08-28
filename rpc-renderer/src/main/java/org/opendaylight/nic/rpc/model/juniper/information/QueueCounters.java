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

/**
 * Created by yrineu on 17/07/17.
 */
public class QueueCounters {

    private StatisticsQueue statisticsQueue;
    private InterfaceCosSummary interfaceCosSummary;

    public QueueCounters(final StatisticsQueue statisticsQueue,
                         final InterfaceCosSummary interfaceCosSummary) {
        this.statisticsQueue = statisticsQueue;
        this.interfaceCosSummary = interfaceCosSummary;
    }

    public static QueueCounters getInstanceFrom(final JsonNode mainNode) {
        QueueCounters queueCounters;
        try {
            final JsonNode node = mainNode.elements().next();
            final StatisticsQueue statisticsQueue = StatisticsQueue.getInstanceBy(node.get("queue"));
            final InterfaceCosSummary interfaceCosSummary = InterfaceCosSummary.getInstanceBy(node.get("interface-cos-summary"));
            queueCounters = new QueueCounters(statisticsQueue, interfaceCosSummary);
        } catch (Exception e) {
            throw new JuniperModelNotSupportedException(e.getMessage());
        }
        return queueCounters;
    }

    public InterfaceCosSummary getInterfaceCosSummary() {
        return interfaceCosSummary;
    }

    public StatisticsQueue getStatisticsQueue() {
        return statisticsQueue;
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
