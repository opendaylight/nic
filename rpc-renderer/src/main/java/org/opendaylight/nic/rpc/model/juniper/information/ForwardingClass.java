/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.information;

/**
 * Created by yrineu on 17/07/17.
 */
public enum ForwardingClass {
    ASSURED_FORWARDING("assured-forwarding"),
    BEST_EFFORT("best-effort"),
    EXPEDITED_FORWARDING("expedited-forwarding"),
    NETWORK_CONTROL("network-control");

    private String value;

    ForwardingClass(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ForwardingClass fromValue(final String value) {
        ForwardingClass result = BEST_EFFORT;
        if (value.equals(ASSURED_FORWARDING.getValue())) {
            result = ASSURED_FORWARDING;
        } else if (value.equals(BEST_EFFORT.getValue())) {
            result = BEST_EFFORT;
        } else if (value.equals(EXPEDITED_FORWARDING.getValue())) {
            result = EXPEDITED_FORWARDING;
        } else if (value.equals(NETWORK_CONTROL.getValue())) {
            result = NETWORK_CONTROL;
        }
        return result;
    }
}
