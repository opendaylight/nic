/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer.flow;

/**
 * Created by yrineu on 22/09/15.
 */
public enum FlowAction {
    ADD_FLOW,
    REMOVE_FLOW;

    public boolean getValue() {
        boolean result = false;
        switch (this) {
            case ADD_FLOW:
                result = true;
                break;
            case REMOVE_FLOW:
                result = false;
                break;
            default:
                result = true;
        }
        return result;
    }
}
