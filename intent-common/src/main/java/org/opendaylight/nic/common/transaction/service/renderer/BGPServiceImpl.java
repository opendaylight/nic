/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.intent.isp.prefixes.IntentIspPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflows.BgpDataflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by yrineu on 19/06/17.
 */
public class BGPServiceImpl implements BGPService {
    private static final Logger LOG = LoggerFactory.getLogger(BGPServiceImpl.class);

    private final CommonUtils commonUtils;

    protected BGPServiceImpl(final CommonUtils commonUtils) {
        this.commonUtils = commonUtils;
    }

    @Override
    public void evaluateAction(String id) throws RendererServiceException {
        final IntentIspPrefix intentIspPrefix = commonUtils.retrieveIntentIspPrefix(id);
        try {
            final Map<Ipv4Address, BgpDataflow> bgpDataFlowMap = commonUtils.createBGPDataFlow(intentIspPrefix);
            bgpDataFlowMap.entrySet().forEach(entry -> commonUtils.pushBgpDataflow(entry.getValue()));
        } catch (IntentInvalidException e) {
            LOG.error(e.getMessage());
            throw new RendererServiceException(e.getMessage());
        }
    }

    @Override
    public void evaluateRollBack(String id) throws RendererServiceException {
        LOG.info("\n#### Trying to evaluate Rollback");
        //TODO: Implement RollBack
    }

    @Override
    public void stopSchedule(String id) {
        //DO_NOTHING
    }

    @Override
    public void execute(BgpDataflow dataflow) {
//        rendererService.advertiseRoute(dataflow);
    }
}
