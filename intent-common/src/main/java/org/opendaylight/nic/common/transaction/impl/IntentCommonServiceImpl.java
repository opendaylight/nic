/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.Dataflow.DataflowMeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IntentCommonServiceImpl  implements IntentCommonService {
    private static final Logger LOG = LoggerFactory.getLogger(IntentCommonServiceImpl.class);

    private DataBroker dataBroker;
    private OFRendererFlowService ofRendererFlowService;

    private IntentCommonServiceImpl() {}

    public IntentCommonServiceImpl(final DataBroker dataBroker,
                                   final OFRendererFlowService ofRendererFlowService) {
        this.dataBroker = dataBroker;
        this.ofRendererFlowService = ofRendererFlowService;
    }
    @Override
    public void resolveAndApply(final Intent intent) {
        LOG.info("\n##### Ready to apply intent!!!!");
    }

    @Override
    public void resolveAndApply(IntentLimiter intentLimiter) {
        try {
            createFlowData(intentLimiter);
        } catch (IntentInvalidException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void resolveAndRemove(IntentLimiter intentLimiter) {

    }

    @Override
    public void resolveAndRemove(Intent intent) {

    }

    @Override
    public void resolveAndApply(String intentId) {
        LOG.info("\n##### Resolving Intent with ID: {}", intentId);
        Intent result = null;
        for(Intent intent : retrieveIntents()) {
            if(intent.getId().equals(intentId)) {
                result = intent;
                LOG.info("\n#### Intent founded in datastore: {}", result.toString());
                break;
            }
        }
        if(null != result) {
            resolveAndApply(result);
        }
    }

    private List<Intent> retrieveIntents() {
        List<Intent> listOfIntents = Lists.newArrayList();
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<Intents> intents = tx.read(LogicalDatastoreType.CONFIGURATION,
                    IntentUtils.INTENTS_IID).checkedGet();

            if (intents.isPresent()) {
                listOfIntents = intents.get().getIntent();
            }
            else {
                LOG.info("Intent tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("ListIntents: failed: {}", e.getMessage(), e);
        }
        LOG.info("ListIntentsConfiguration: list of intents retrieved successfully");
        return listOfIntents;
    }

    private void createFlowData(IntentLimiter intent) throws IntentInvalidException {
        final Ipv4Prefix sourceIp = intent.getSourceIp();
        DataflowBuilder dataflowBuilder = new DataflowBuilder();
        dataflowBuilder.setIsFlowMeter(true);
        dataflowBuilder.setId(intent.getId());
        dataflowBuilder.setTimeout(intent.getDuration());
        dataflowBuilder.setDataflowMeterBandType(DataflowMeterBandType.OFMBTDROP);
        dataflowBuilder.setMeterFlags(Dataflow.MeterFlags.METERKBPS);
        dataflowBuilder.setSourceIpAddress(sourceIp);
        dataflowBuilder.setRendererAction(Dataflow.RendererAction.ADD);
        dataflowBuilder.setBandwidthRate(intent.getBandwidthLimit());
        dataflowBuilder.setFlowType(Dataflow.FlowType.L3);

        LOG.info("\n#### Creating FlowData for SRC: {}", sourceIp);
        ofRendererFlowService.pushFlowData(dataflowBuilder.build());
    }
}
