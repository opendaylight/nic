/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.utils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.common.transaction.exception.RemoveDataflowException;
import org.opendaylight.nic.utils.exceptions.PushDataflowException;
import org.opendaylight.nic.common.transaction.exception.RemoveDelayconfigException;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.Dataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.DataflowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.DelayConfigs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.DelayConfigsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.delay.configs.DelayConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.delay.configs.DelayConfigKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);
    private final InstanceIdentifier<Dataflows> DATAFLOW_IID = InstanceIdentifier.create(Dataflows.class);
    private final InstanceIdentifier<DelayConfigs> DELAY_CONFIGS_IID = InstanceIdentifier.create(DelayConfigs.class);
    private DataBroker dataBroker;

    public CommonUtils(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public List<Intent> retrieveIntents() {
        List<Intent> listOfIntents = Lists.newArrayList();
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<Intents> intents = tx.read(LogicalDatastoreType.CONFIGURATION,
                    IntentUtils.INTENTS_IID).checkedGet();

            if (intents.isPresent()) {
                listOfIntents = intents.get().getIntent();
            } else {
                LOG.info("Intent tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("ListIntents: failed: {}", e.getMessage(), e);
        }
        LOG.info("ListIntentsConfiguration: list of intents retrieved successfully");
        return listOfIntents;
    }

    public List<IntentLimiter> retrieveIntentLimiters() {
        List<IntentLimiter> listOfIntents = Lists.newArrayList();
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<IntentsLimiter> intents = tx.read(LogicalDatastoreType.CONFIGURATION,
                    IntentUtils.INTENTS_LIMITER_IDD).checkedGet();

            if (intents.isPresent()) {
                listOfIntents = intents.get().getIntentLimiter();
            } else {
                LOG.info("\nIntentLimiter tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("\nListIntents: failed: {}", e.getMessage(), e);
        }
        LOG.info("\nListIntentsConfiguration: list of intents retrieved successfully");
        return listOfIntents;
    }

    public IntentLimiter retrieveIntentLimiter(final String id) {
        final List<IntentLimiter> intentLimiters = retrieveIntentLimiters();
        List<IntentLimiter> result = Lists.newArrayList();
        intentLimiters.forEach(intentLimiter -> {
            if (intentLimiter.getId().getValue().equals(id)) {
                result.add(intentLimiter);
            }
        });
        return result.iterator().next();
    }

    public List<Dataflow> retrieveDataflowList() {
        final List<Dataflow> result = Lists.newArrayList();
        Dataflows dataflows = retrieveDataflows();
        if (null != dataflows) {
            result.addAll(dataflows.getDataflow());
        }
        return result;
    }

    public Dataflows retrieveDataflows() {
        Dataflows result = null;
        try {
            ReadOnlyTransaction read = dataBroker.newReadOnlyTransaction();
            Optional<Dataflows> dataflows = read.read(LogicalDatastoreType.CONFIGURATION,
                    DATAFLOW_IID).checkedGet();
            if (dataflows.isPresent()) {
                result = dataflows.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("\nError when try to retrieve Dataflows. {}", e.getMessage());
        }
        return result;
    }

    public DelayConfigs retrieveDelayConfigs() {
        DelayConfigs result = null;
        try {
            ReadOnlyTransaction read = dataBroker.newReadOnlyTransaction();
            Optional<DelayConfigs> configs = read.read(LogicalDatastoreType.CONFIGURATION,
                    DELAY_CONFIGS_IID).checkedGet();
            if (configs.isPresent()) {
                result = configs.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("\nError when try to retrieve DelayConfigs. {}", e.getMessage());
        }
        return result;
    }

    public DelayConfig retrieveDelayConfig(final String id) {
        DelayConfigs configs = retrieveDelayConfigs();
        final Set<DelayConfig> delayConfigs = new HashSet<>();
        configs.getDelayConfig().forEach(delayConfig -> {
            if (delayConfig.getId().getValue().equals(id)) {
                delayConfigs.add(delayConfig);
            }
        });
        return delayConfigs.iterator().next();
    }

    public Dataflow retrieveDataflow(final String intentLimiterId) {
        Dataflow result = null;
        final List<Dataflow> dataflows = retrieveDataflowList();
        for (Dataflow dataflow : dataflows) {
            if (dataflow.getId().getValue().equals(intentLimiterId)) {
                result = dataflow;
                break;
            }
        }
        return result;
    }

    public void saveDataflow(final Dataflow dataflow) throws PushDataflowException {
        List<Dataflow> dataflows = retrieveDataflowList();
        final DataflowsBuilder dataflowsBuilder = new DataflowsBuilder();
        dataflows.add(dataflow);
        dataflowsBuilder.setDataflow(dataflows);
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, DATAFLOW_IID, dataflowsBuilder.build());
        CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = writeTransaction.submit();
        try {
            checkedFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
            throw new PushDataflowException(e.getMessage());
        }
    }

    public void saveDelayConfig(final DelayConfig delayConfig) {
        final DelayConfigs delayConfigs = retrieveDelayConfigs();
        final DelayConfigsBuilder delayConfigsBuilder = (delayConfigs != null ? new DelayConfigsBuilder(delayConfigs) : new DelayConfigsBuilder());
        final List<DelayConfig> delayConfigList = delayConfigsBuilder.getDelayConfig();
        if (null != delayConfigList) {
            delayConfigList.add(delayConfig);
            delayConfigsBuilder.setDelayConfig(delayConfigList);
        } else {
            delayConfigsBuilder.setDelayConfig(Lists.newArrayList(delayConfig));
        }
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, DELAY_CONFIGS_IID, delayConfigsBuilder.build());
        writeTransaction.submit();
    }

    public void removeDelayConfig(final String delayConfigId) throws RemoveDelayconfigException {
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        final InstanceIdentifier<DelayConfig> identifier = InstanceIdentifier.create(DelayConfigs.class).
                child(DelayConfig.class, new DelayConfigKey(Uuid.getDefaultInstance(delayConfigId)));
        writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, identifier);
        CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = writeTransaction.submit();
        try {
            checkedFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            throw new RemoveDelayconfigException(e.getMessage());
        }
    }

    public void removeDataFlow(final String dataflowId) throws RemoveDataflowException {
        final InstanceIdentifier<Dataflow> identifier = InstanceIdentifier.create(Dataflows.class).
                child(Dataflow.class, new DataflowKey(Uuid.getDefaultInstance(dataflowId)));
        final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.delete(LogicalDatastoreType.CONFIGURATION, identifier);
        CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = transaction.submit();
        try {
            checkedFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            throw new RemoveDataflowException(e.getMessage());
        }
    }

    public Dataflow createFlowData(final IntentLimiter intent, final MeterId meterId) throws IntentInvalidException {
        final Ipv4Prefix sourceIp = intent.getSourceIp();
        DataflowBuilder dataflowBuilder = new DataflowBuilder();
        dataflowBuilder.setCreationTime(String.valueOf(System.currentTimeMillis()));
        dataflowBuilder.setIsFlowMeter(true);
        dataflowBuilder.setId(intent.getId());
        dataflowBuilder.setTimeout(intent.getDuration());
        dataflowBuilder.setDataflowMeterBandType(org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow
                .rev170309.Dataflow.DataflowMeterBandType.OFMBTDROP);
        dataflowBuilder.setMeterFlags(Dataflow.MeterFlags.METERKBPS);
        dataflowBuilder.setSourceIpAddress(sourceIp);
        dataflowBuilder.setRendererAction(Dataflow.RendererAction.ADD);
        dataflowBuilder.setBandwidthRate(intent.getBandwidthLimit());
        dataflowBuilder.setFlowType(Dataflow.FlowType.L3);
        dataflowBuilder.setMeterId(meterId.getValue().shortValue());
        dataflowBuilder.setStatus(Dataflow.Status.PROCESSING);
        dataflowBuilder.setIsRefreshable(true);
        return dataflowBuilder.build();
    }

    public static void waitUnlock() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }
}
