/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.utils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.Dataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.DataflowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CommonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);
    private static final InstanceIdentifier<Dataflows> DATAFLOW_IID = InstanceIdentifier.create(Dataflows.class);
    private static DataBroker dataBroker;

    public CommonUtils(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public static List<Intent> retrieveIntents() {
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

    public static List<IntentLimiter> retrieveIntentLimiters() {
        List<IntentLimiter> listOfIntents = Lists.newArrayList();
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<IntentsLimiter> intents = tx.read(LogicalDatastoreType.CONFIGURATION,
                    IntentUtils.INTENTS_LIMITER_IDD).checkedGet();

            if (intents.isPresent()) {
                listOfIntents = intents.get().getIntentLimiter();
            }
            else {
                LOG.info("\nIntentLimiter tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("\nListIntents: failed: {}", e.getMessage(), e);
        }
        LOG.info("\nListIntentsConfiguration: list of intents retrieved successfully");
        return listOfIntents;
    }

    public static List<Dataflow> retrieveDataflows() {
        final List<Dataflow> result = Lists.newArrayList();
        try {
            ReadOnlyTransaction read = dataBroker.newReadOnlyTransaction();
            Optional<Dataflows> dataflows = read.read(LogicalDatastoreType.CONFIGURATION,
                    DATAFLOW_IID).checkedGet();
            if (dataflows.isPresent()) {
                result.addAll(dataflows.get().getDataflow());
            }
        } catch (ReadFailedException e) {
            LOG.info("\nError when try to retrieve Dataflows. {}",e.getMessage());
        }
        return result;
    }

    public static Dataflow retrieveDataflow(final String intentLimiterId) {
        Dataflow result = null;
        final List<Dataflow> dataflows = retrieveDataflows();
        for (Dataflow dataflow : dataflows) {
            if (dataflow.getId().getValue().equals(intentLimiterId)) {
                result = dataflow;
                break;
            }
        }
        return result;
    }

    public static void pushDataflow(final Dataflow dataflow) {
        List<Dataflow> dataflows = retrieveDataflows();
        final DataflowsBuilder dataflowsBuilder = new DataflowsBuilder();
        dataflows.add(dataflow);
        dataflowsBuilder.setDataflow(dataflows);
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, DATAFLOW_IID, dataflowsBuilder.build());
        writeTransaction.submit();
    }

    public static void removeDataFlow(final Dataflow dataflow) {
        final InstanceIdentifier<Dataflows> identifier =DATAFLOW_IID;
        identifier.child(Dataflow.class, new DataflowKey(dataflow.getId()));
        final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.delete(LogicalDatastoreType.CONFIGURATION, identifier);
        transaction.submit();
    }
}
