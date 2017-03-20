/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.api.MeterQueueService;
import org.opendaylight.nic.of.renderer.utils.MeterQueueUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.queue.types.rev170316.MeteridObject;

public class MeterQueueServiceImpl implements MeterQueueService {

    private DataBroker dataBroker;
    private MeterQueueUtils meterQueueUtils;

    public MeterQueueServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.meterQueueUtils = new MeterQueueUtils(dataBroker);
    }

    @Override
    public MeteridObject getNextMeterId() {
        return meterQueueUtils.retrieveFreeMeterId();
    }

    @Override
    public void releaseMeterId(long id) {
        meterQueueUtils.releaseMeterId(id);
    }

    @Override
    public void initService() {
        meterQueueUtils.init();
    }
}
