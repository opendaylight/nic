/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import org.opendaylight.nic.common.model.FlowAction;
import org.opendaylight.nic.common.model.FlowData;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.common.utils.FlowDataUtils;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//TODO: This class is WIP
public class IntentCommonServiceImpl  implements IntentCommonService {
    private static final Logger LOG = LoggerFactory.getLogger(IntentCommonServiceImpl.class);

    @Override
    public void resolveAndApply(final Intent intent) {
        try {
            final EndPointGroup srcEndPoint = IntentUtils.extractSrcEndPointGroup(intent);
            final EndPointGroup dstEndPoint = IntentUtils.extractDstEndPointGroup(intent);

            final Action actionContainer = IntentUtils.getAction(intent);
            final FlowData flowData = FlowDataUtils.generateFlowData(srcEndPoint, dstEndPoint, FlowAction.ALLOW);
            //TODO: Find out some way to call a specific renderer and send this FlowData
        } catch (IntentInvalidException e) {
            //TODO: Add a valid exception
        }
    }

    @Override
    public void resolveAndRemove(Intent intent) {

    }
}
