/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.term;

import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyActionType;

/**
 * Created by yrineu on 21/07/17.
 */
public class PolicyAction {

    //TODO: Add more parameters in order to attend more policy action types
    private StringBuffer result;
    private PolicyActionType policyActionType;

    protected PolicyAction(final PolicyActionType policyActionType,
                           final StringBuffer result) {
        this.policyActionType = policyActionType;
        this.result = result;
    }

    protected void generateRPCStructure() {
        result.append("<then>");
        switch (policyActionType) {
            case accept:
                result.append("<"+policyActionType.name().toLowerCase()+"/>");
        }
        result.append("</then>");
    }

    protected static void generateEvpnStaticAction(final StringBuffer result) {
        result.append("<then>");
        result.append("<" + PolicyActionType.accept.name().toLowerCase() + "/>");
        result.append("</then>");
    }
}
