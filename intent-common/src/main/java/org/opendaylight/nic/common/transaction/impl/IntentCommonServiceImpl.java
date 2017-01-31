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
import org.opendaylight.nic.common.model.FlowAction;
import org.opendaylight.nic.common.model.FlowData;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.common.utils.FlowDataUtils;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

//TODO: This class is WIP
public class IntentCommonServiceImpl  implements IntentCommonService {
    private static final Logger LOG = LoggerFactory.getLogger(IntentCommonServiceImpl.class);

    private DataBroker dataBroker;

    private IntentCommonServiceImpl() {}

    public IntentCommonServiceImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }
    @Override
    public void resolveAndApply(final Intent intent) {
        try {
            final EndPointGroup srcEndPoint = IntentUtils.extractSrcEndPointGroup(intent);
            final EndPointGroup dstEndPoint = IntentUtils.extractDstEndPointGroup(intent);

            final Action actionContainer = IntentUtils.getAction(intent);
            final FlowData flowData = FlowDataUtils.generateFlowData(srcEndPoint, dstEndPoint, FlowAction.ALLOW);
            LOG.info("\n##### Ready to apply intent!!!!");
            //TODO: Find out some way to call a specific renderer and send this FlowData
        } catch (IntentInvalidException e) {
            //TODO: Add a valid exception
        }
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
}
