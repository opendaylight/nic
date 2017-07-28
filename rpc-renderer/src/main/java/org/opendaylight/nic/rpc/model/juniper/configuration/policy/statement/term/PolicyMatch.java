/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.term;

import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationUtils;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyMatchType;

/**
 * Created by yrineu on 21/07/17.
 */
public class PolicyMatch {

    //TODO: Add more parameters in order to attend more Policy Match Types
    private StringBuffer result;
    private PolicyMatchType policyMatchType;
    private Integer vni;

    protected PolicyMatch(final PolicyMatchType policyMatchType,
                          final Integer vni,
                          final StringBuffer result) {
        this.policyMatchType = policyMatchType;
        this.vni = vni;
        this.result = result;
    }

    protected void generateRPCStructure() {
        result.append("<from>");
        switch (policyMatchType) {
            case community:
                result.append("<" + policyMatchType.name() + ">vni" + vni + "</" + policyMatchType.name() + ">");
        }
        result.append("</from>");
    }

    protected static void generateEvpnStaticMatch(final StringBuffer result) {
        result.append("<from>");
        result.append("<community>" + ConfigurationUtils.EVPN_POLICY_COMMUNITY + "</community>");
        result.append("</from>");
    }
}
