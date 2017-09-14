/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.pod.info.rev700101.pod.infos.PodInfo;

/**
 * This service is responsible to handle events that manipulates Network Mapping changes
 */
public interface NetworkMappingService {

    /**
     * Process the Network Mapping element that was changed.
     * @param networkData as an {@link Object}
     */
    void processNetworkMappingChange(final PodInfo networkData);
}
