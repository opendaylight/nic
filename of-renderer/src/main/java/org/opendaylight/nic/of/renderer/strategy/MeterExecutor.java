/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.api.MeterQueueService;
import org.opendaylight.nic.of.renderer.exception.MeterCreationExeption;
import org.opendaylight.nic.of.renderer.exception.MeterRemovalExeption;
import org.opendaylight.nic.of.renderer.impl.MeterQueueServiceImpl;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.IdManagerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.queue.types.rev170316.MeteridObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public class MeterExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MeterExecutor.class);

    private final DataBroker dataBroker;
    private final MdsalUtils mdsalUtils;
    private MeterQueueService meterQueueService;

    public MeterExecutor(final DataBroker dataBroker,
                         final IdManagerService idManagerService) {
        this.dataBroker = dataBroker;
        this.mdsalUtils = new MdsalUtils(dataBroker);
        this.meterQueueService = new MeterQueueServiceImpl(idManagerService);
    }

    public MeterId createMeter(final Dataflow dataflow) throws MeterCreationExeption {
        return createMeter(dataflow.getId().getValue(), dataflow.getBandwidthRate());
    }

    public MeterId createMeter(final String id, final long dropRate) throws MeterCreationExeption {
        MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        MeterBuilder meterBuilder = new MeterBuilder();

        final long meterIdLong = meterQueueService.getNextMeterId(id);
        final MeterId meterId = new MeterId(meterIdLong);

        MeterFlags meterFlags = new MeterFlags(false, true, false, false);
        meterBuilder.setKey(new MeterKey(meterId));
        meterBuilder.setMeterId(meterId);
        meterBuilder.setFlags(meterFlags);

        int bandKey = 0;
        DropBuilder dropBuilder = new DropBuilder();
        dropBuilder.setDropBurstSize(0L);
        dropBuilder.setDropRate(dropRate);

        MeterBandTypesBuilder meterBandTypesBuilder = new MeterBandTypesBuilder();
        meterBandTypesBuilder.setFlags(new MeterBandType(true, false, false));

        Drop drop = dropBuilder.build();
        BandId bandId = new BandId((long)bandKey);
        meterBandHeaderBuilder.setBandBurstSize(drop.getDropBurstSize());
        meterBandHeaderBuilder.setBandRate(drop.getDropRate());
        meterBandHeaderBuilder.setKey(new MeterBandHeaderKey(bandId));
        meterBandHeaderBuilder.setBandId(bandId);
        meterBandHeaderBuilder.setMeterBandTypes(meterBandTypesBuilder.build());
        meterBandHeaderBuilder.setBandType(drop);
        meterBandHeadersBuilder.setMeterBandHeader(Lists.newArrayList(meterBandHeaderBuilder.build()));
        meterBuilder.setMeterBandHeaders(meterBandHeadersBuilder.build());

        final Meter meter = meterBuilder.build();
        final Map<Node, List<NodeConnector>> nodeListMap = TopologyUtils.getNodes(dataBroker);
        final Set<Boolean> metersCreationResults = new HashSet<>();

        for (Map.Entry<Node, List<NodeConnector>> entry : nodeListMap.entrySet()) {
            final InstanceIdentifier<Meter> instanceIdentifier = retrieveMeterIdentifier(meterId,
                    entry.getKey());
            final boolean result = mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, meter);
            LOG.debug("\nMeter creation finished with ");
            metersCreationResults.add(result);
        }
        //If only one meter was created with success, the result is true.
        if (!metersCreationResults.contains(Boolean.TRUE)) {
            throw new MeterCreationExeption();
        }
        return meter.getMeterId();
    }

    public Future<RpcResult<Void>> removeMeter(final Long meterId, String dataflowId) throws MeterRemovalExeption {
        boolean result = false;
        final MeterId id = new MeterId(meterId);
        final Future<RpcResult<Void>> releaseMeterResult = meterQueueService.releaseMeterId(dataflowId);
        final Map<Node, List<NodeConnector>> nodeListMap = TopologyUtils.getNodes(dataBroker);
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeListMap.entrySet()) {
            final InstanceIdentifier<Meter> instanceIdentifier = retrieveMeterIdentifier(id,
                    entry.getKey());
            result = mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        }
        if (!result) {
            throw new MeterRemovalExeption(Long.toString(meterId));
        }
        return releaseMeterResult;
    }

    private InstanceIdentifier<Meter> retrieveMeterIdentifier(final MeterId meterId, Node node) {
        KeyedInstanceIdentifier<Meter, MeterKey> flowIID = null;
        try {
            final InstanceIdentifier<Node> nodePath = InstanceIdentifier
                    .create(Nodes.class)
                    .child(Node.class, node.getKey());
            flowIID = nodePath
                    .augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(meterId));

        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return flowIID.builder().build();
    }
}
