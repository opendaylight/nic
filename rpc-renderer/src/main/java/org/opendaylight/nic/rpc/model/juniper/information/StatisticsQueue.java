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

import static org.opendaylight.nic.rpc.utils.RESTUtils.*;

/**
 * Created by yrineu on 17/07/17.
 */
public class StatisticsQueue {

    private Integer queueNumber;
    private Long queuedPackets;
    private Long queuedPacketsRate;
    private Long transPackets;
    private Long transPacketRate;

    private Long queuedBytes;
    private Long queuedBytesRate;
    private Long transBytes;
    private Long transBytesRate;

    private Long tailDropPacketsNa;

    private Long rateLimitDropPackets;
    private Long rateLimitDropPacketsRate;

    private Long rateLimitDropBytes;
    private Long rateLimitDropBytesRate;

    private Long totalDropPackets;
    private Long totalDropPacketsRate;
    private Long totalDropBytes;
    private Long totalDropBytesRate;

    private ForwardingClass forwardingClass;

    public StatisticsQueue(final Integer queueNumber,
                           final ForwardingClass forwardingClass,
                           final Long queuedPackets,
                           final Long queuedPacketsRate,
                           final Long transPackets,
                           final Long transPacketRate,
                           final Long queuedBytes,
                           final Long queuedBytesRate,
                           final Long transBytes,
                           final Long transBytesRate,
                           final Long tailDropPacketsNa,
                           final Long rateLimitDropPackets,
                           final Long rateLimitDropPacketsRate,
                           final Long rateLimitDropBytes,
                           final Long rateLimitDropBytesRate,
                           final Long totalDropPackets,
                           final Long totalDropPacketsRate,
                           final Long totalDropBytes,
                           final Long totalDropBytesRate) {
        this.queueNumber = queueNumber;
        this.forwardingClass = forwardingClass;
        this.queuedPackets = queuedPackets;
        this.queuedPacketsRate = queuedPacketsRate;
        this.transPackets = transPackets;
        this.transPacketRate = transPacketRate;
        this.queuedBytes = queuedBytes;
        this.queuedBytesRate = queuedBytesRate;
        this.transBytes = transBytes;
        this.transBytesRate = transBytesRate;
        this.tailDropPacketsNa = tailDropPacketsNa;
        this.rateLimitDropPackets = rateLimitDropPackets;
        this.rateLimitDropPacketsRate = rateLimitDropPacketsRate;
        this.rateLimitDropBytes = rateLimitDropBytes;
        this.rateLimitDropBytesRate = rateLimitDropBytesRate;
        this.totalDropPackets = totalDropPackets;
        this.totalDropPacketsRate = totalDropPacketsRate;
        this.totalDropBytes = totalDropBytes;
        this.totalDropBytesRate = totalDropBytesRate;
    }

    public static StatisticsQueue getInstanceBy(final JsonNode mainNode) {
        try {
            final JsonNode node = mainNode.elements().next();
            final Integer queueNumber = extractIntData(node.get("queue-number"));
            final ForwardingClass forwardingClass = ForwardingClass.fromValue(extractStringData(node.get("forwarding-class-name")));
            final Long queuedPackets = extractLongData(node.get("queue-counters-queued-packets"));
            final Long queuedPacketsRate = extractLongData(node.get("queue-counters-queued-packets-rate"));
            final Long queuedBytes = extractLongData(node.get("queue-counters-queued-bytes"));
            final Long queuedBytesRate = extractLongData(node.get("queue-counters-queued-bytes-rate"));
            final Long transPackets = extractLongData(node.get("queue-counters-trans-packets"));
            final Long transPacketRate = extractLongData(node.get("queue-counters-trans-packets-rate"));
            final Long transBytes = extractLongData(node.get("queue-counters-trans-bytes"));
            final Long transBytesRate = extractLongData(node.get("queue-counters-trans-bytes-rate"));
            final Long tailDropPacketsNa = extractLongData(node.get("queue-counters-tail-drop-packets-na"));
            final Long rateLimitDropPackets = extractLongData(node.get("queue-counters-rate-limit-drop-packets"));
            final Long rateLimitDropPacketsRate = extractLongData(node.get("queue-counters-rate-limit-drop-packets-rate"));
            final Long rateLimitDropBytes = extractLongData(node.get("queue-counters-rate-limit-drop-bytes"));
            final Long rateLimitDropBytesRate = extractLongData(node.get("queue-counters-rate-limit-drop-bytes-rate"));
            final Long totalDropPackets = extractLongData(node.get("queue-counters-total-drop-packets"));
            final Long totalDropPacketsRate = extractLongData(node.get("queue-counters-total-drop-packets-rate"));
            final Long totalDropBytes = extractLongData(node.get("queue-counters-total-drop-bytes"));
            final Long totalDropBytesRate = extractLongData(node.get("queue-counters-total-drop-bytes-rate"));

            return new StatisticsQueue(
                    queueNumber,
                    forwardingClass,
                    queuedPackets,
                    queuedPacketsRate,
                    transPackets,
                    transPacketRate,
                    queuedBytes,
                    queuedBytesRate,
                    transBytes,
                    transBytesRate,
                    tailDropPacketsNa,
                    rateLimitDropPackets,
                    rateLimitDropPacketsRate,
                    rateLimitDropBytes,
                    rateLimitDropBytesRate,
                    totalDropPackets,
                    totalDropPacketsRate,
                    totalDropBytes,
                    totalDropBytesRate);
        } catch (Exception e) {
            throw new JuniperModelNotSupportedException(e.getMessage());
        }
    }

    public Integer getQueueNumber() {
        return queueNumber;
    }

    public ForwardingClass getForwardingClass() {
        return forwardingClass;
    }

    public Long getQueuedPackets() {
        return queuedPackets;
    }

    public Long getQueuedPacketsRate() {
        return queuedPacketsRate;
    }

    public Long getTransPackets() {
        return transPackets;
    }

    public Long getTransPacketRate() {
        return transPacketRate;
    }

    public Long getQueuedBytes() {
        return queuedBytes;
    }

    public Long getQueuedBytesRate() {
        return queuedBytesRate;
    }

    public Long getTransBytes() {
        return transBytes;
    }

    public Long getTransBytesRate() {
        return transBytesRate;
    }

    public Long getTailDropPacketsNa() {
        return tailDropPacketsNa;
    }

    public Long getRateLimitDropPackets() {
        return rateLimitDropPackets;
    }

    public Long getRateLimitDropPacketsRate() {
        return rateLimitDropPacketsRate;
    }

    public Long getRateLimitDropBytes() {
        return rateLimitDropBytes;
    }

    public Long getRateLimitDropBytesRate() {
        return rateLimitDropBytesRate;
    }

    public Long getTotalDropPackets() {
        return totalDropPackets;
    }

    public Long getTotalDropPacketsRate() {
        return totalDropPacketsRate;
    }

    public Long getTotalDropBytes() {
        return totalDropBytes;
    }

    public Long getTotalDropBytesRate() {
        return totalDropBytesRate;
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
