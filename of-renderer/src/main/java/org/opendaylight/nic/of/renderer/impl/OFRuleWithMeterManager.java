/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.strategy.MeterExecutor;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OFRuleWithMeterManager {

    private static final Logger LOG = LoggerFactory.getLogger(OFRuleWithMeterManager.class);
    private final MdsalUtils mdsalUtils;
    private final DataBroker dataBroker;
    private final MeterExecutor meterExecutor;
    private final Long OFP_NO_BUFFER = Long.valueOf(4294967295L);
    private AtomicLong flowCookieInc = new AtomicLong(0x3a00000000000000L);
    private Long ETHER_TYPE = 0x0800L;

    protected OFRuleWithMeterManager(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.mdsalUtils = new MdsalUtils(dataBroker);
        this.meterExecutor = new MeterExecutor(dataBroker);
    }

    public FlowBuilder createFlow(final Dataflow dataFlow,
                                  final MeterId meterId) {

        final Short id = dataFlow.getMeterId();
        final FlowModFlags flowModFlags = new FlowModFlags(false, false, false, false, false);
        final FlowId flowId = new FlowId(dataFlow.getId().toString());
        final FlowKey flowKey = new FlowKey(flowId);

        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setFlowName("NIC_METER"+meterId.getValue());
        flowBuilder.setId(new FlowId(Long.toString(flowBuilder.hashCode())));
        flowBuilder.setMatch(createMatch(dataFlow.getSourceIpAddress()));
        flowBuilder.setInstructions(createInstruction(meterId));
        flowBuilder.setPriority(OFRendererConstants.DEFAULT_PRIORITY);
        flowBuilder.setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())));
        flowBuilder.setBufferId(OFP_NO_BUFFER);
        flowBuilder.setHardTimeout((int)dataFlow.getTimeout());
        flowBuilder.setIdleTimeout((int)dataFlow.getTimeout());
        flowBuilder.setFlags(flowModFlags);
        flowBuilder.setKey(flowKey);

        return flowBuilder;
    }

    public MeterId createMeter(final Dataflow dataflow) {
        final Short id = dataflow.getMeterId();
        final MeterId meterId = (id != 0 ? new MeterId(id.longValue()) : meterExecutor.createMeter(dataflow));
        return meterId;
    }

    public boolean removeMeter(final Dataflow dataflow) {
        return meterExecutor.removeMeter(dataflow);
    }

    private Match createMatch(Ipv4Prefix ipv4Prefix) {
        final Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ipv4MatchBuilder.setIpv4Source(ipv4Prefix);

        Layer3Match layer3Match = ipv4MatchBuilder.build();

        final MatchBuilder matchBuilder = new MatchBuilder();
        final EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        final EthernetTypeBuilder ethernetTypeBuilder = new EthernetTypeBuilder();

        ethernetTypeBuilder.setType(new EtherType(ETHER_TYPE));
        ethernetMatchBuilder.setEthernetType(ethernetTypeBuilder.build());

        matchBuilder.setLayer3Match(layer3Match);
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());

        return matchBuilder.build();
    }

    private Instructions createInstruction(final MeterId meterId) {
        MeterBuilder meterBuilder = new MeterBuilder();
        meterBuilder.setMeterId(meterId);

        Instruction instruction = new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new MeterCaseBuilder()
                        .setMeter(meterBuilder.build()).build()).build();

        List<Instruction> instructions = new ArrayList<>();
        instructions.add(instruction);

        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        instructionsBuilder.setInstruction(instructions);
        return instructionsBuilder.build();
    }

    public boolean sendToMdsal(final FlowBuilder flowBuilder, final NodeId nodeId) {
        final NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setId(nodeId);
        nodeBuilder.setKey(new NodeKey(nodeId));
        return mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, retrieveIdentifier(nodeBuilder, flowBuilder), flowBuilder.build());
    }

    public boolean removeFromMdsal(final FlowBuilder flowBuilder, final NodeId nodeId) {
        final NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.setId(nodeId);
        nodeBuilder.setKey(new NodeKey(nodeId));
        return mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, retrieveIdentifier(nodeBuilder, flowBuilder));
    }

    private InstanceIdentifier<Flow> retrieveIdentifier(final NodeBuilder nodeBuilder,
                                                        final FlowBuilder flowBuilder) {
        flowBuilder.setTableId(OFRendererConstants.FALLBACK_TABLE_ID);
        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeBuilder.getKey())
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId()))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow.class, flowBuilder.getKey())
                .build();
        return flowIID;
    }
}
