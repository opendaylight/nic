/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.IntentIspPrefix;

/**
 * Event when an Intent ISP is removed.
 */
public interface IntentIspPrefixRemoved extends NicNotification {

    /**
     * Retrieve the Intent ISP removed
     * @return the {@link IntentIspPrefix}
     */
    IntentIspPrefix getIntent();
}
