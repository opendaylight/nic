/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public interface IntentCommonService {

    void resolveAndApply(final Intent intent);

    void resolveAndApply(final IntentLimiter intentLimiter);

    void resolveAndRemove(final IntentLimiter intentLimiter);

    void resolveAndRemove(final Intent intent);

    void resolveAndApply(final String intentId);
}
