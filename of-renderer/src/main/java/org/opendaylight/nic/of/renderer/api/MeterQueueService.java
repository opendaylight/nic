/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.queue.types.rev170316.MeteridObject;

public interface MeterQueueService {

    MeteridObject getNextMeterId();

    void releaseMeterId(long id);

    void initService();
}
