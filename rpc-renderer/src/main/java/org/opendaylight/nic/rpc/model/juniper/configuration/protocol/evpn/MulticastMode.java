/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn;

/**
 * Created by yrineu on 20/07/17.
 */
public enum MulticastMode {
    INGRESS_REPLICATION("ingress-replication");

    private String value;

    MulticastMode(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
