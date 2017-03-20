/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MeterExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MeterExecutor.class);

    private DataBroker dataBroker;
    private MdsalUtils mdsalUtils;

    public MeterExecutor(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.mdsalUtils = new MdsalUtils(dataBroker);
    }

    public MeterId createMeter(Dataflow dataflow) {
        MeterBandHeadersBuilder meterBandHeadersBuilder = new MeterBandHeadersBuilder();
        MeterBandHeaderBuilder meterBandHeaderBuilder = new MeterBandHeaderBuilder();
        MeterBuilder meterBuilder = new MeterBuilder();

        final MeterId meterId = new MeterId(1L);

        MeterFlags meterFlags = new MeterFlags(false, true, false, false);
        meterBuilder.setKey(new MeterKey(meterId));
        meterBuilder.setFlags(meterFlags);

        int bandKey = 0;
        DropBuilder dropBuilder = new DropBuilder();
        dropBuilder.setDropBurstSize(0L);
        dropBuilder.setDropRate(dataflow.getBandwidthRate());

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

        final Map<Node, List<NodeConnector>> nodeListMap = TopologyUtils.getNodes(dataBroker);

        for (Map.Entry<Node, List<NodeConnector>> entry : nodeListMap.entrySet()) {
            final InstanceIdentifier<Meter> instanceIdentifier = retrieveMeterIdentifier(meterId,
                    entry.getKey());
            mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, meterBuilder.build());
        }
        return meterBuilder.build().getMeterId();
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
            e.printStackTrace();
        }
        return flowIID.builder().build();
    }
}
