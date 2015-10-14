/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.flow;

/**
 * Created by yrineu on 22/09/15.
 */
public enum FlowAction {
    ADD_FLOW(0),
    REMOVE_FLOW(1);

    private final int value;

    private FlowAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
