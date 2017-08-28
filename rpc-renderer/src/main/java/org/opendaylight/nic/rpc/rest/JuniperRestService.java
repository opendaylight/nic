/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import org.opendaylight.yangtools.yang.binding.DataObject;

import java.util.List;

/**
 * Service to evaluate HTTP request to Juniper devices
 */
public interface JuniperRestService {

    /**
     * Send Configuration to a set of devices via HTTP request.
     * @param dataflowList as a {@link List} of {@link T}
     *                               with all information needed to send that request.
     */
    <T extends DataObject> void sendConfiguration(final List<T> dataflowList,
                                                  final Boolean isADeleteSchema);

}
