/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.api;

import org.opendaylight.nic.of.renderer.exception.MeterCreationExeption;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.PushDataflowException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

public interface OFRendererFlowService {

    /**
     * Start OFRenderer Flow Services
     */
    void start();

    /**
     * Push OF rules to add an Intent
     * @param intent The {@link Intent} that has been created
     * @param flowAction The {@link FlowAction} (Add or Remove)
     */
    void pushIntentFlow(Intent intent, FlowAction flowAction);

    /**
     * Push ARP flows on node-up event
     * @param nodeId The OF {@link NodeId}
     * @param flowAction The {@link FlowAction}
     */
    void pushARPFlow(NodeId nodeId, FlowAction flowAction);

    /**
     * Push OF rules to forward LLDP packets to controller
     * @param nodeId The OF {@link NodeId}
     * @param flowAction The {@link FlowAction}
     */
    void pushLLDPFlow(NodeId nodeId, FlowAction flowAction);

    /**
     * Push rules based in a given Dataflow
     * @param dataFlow The {@link Dataflow}
     * @return {@link Dataflow}
     * @throws PushDataflowException
     */
    Dataflow pushDataFlow(Dataflow dataFlow) throws PushDataflowException;

    /**
     * Push rules for a given NodeId based in a Dataflow
     * @param nodeId the {@link NodeId}
     * @param dataflow the {@link Dataflow}
     */
    void pushDataFlow(NodeId nodeId, Dataflow dataflow);

    /**
     * Create OpenFlow meters
     * @param id the Dataflow ID as {@link String}
     * @param dropRate the bandwidth drop rate as {@link Long}
     * @return {@link MeterId}
     * @throws MeterCreationExeption
     */
    MeterId createMeter(String id, long dropRate) throws MeterCreationExeption;

    /**
     * Remove OpenFlow meters
     * @param meterId the MeterID as {@link Long}
     * @param dataflowId the Dataflow ID as {@link String}
     * @throws PushDataflowException
     */
    void removeMeter(Long meterId, String dataflowId) throws PushDataflowException;

    /**
     * Stop OF Renderer services.
     */
    void stop();
}
