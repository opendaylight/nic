/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.term;

import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationUtils;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyActionType;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyMatchType;

/**
 * Created by yrineu on 21/07/17.
 */
public class Term implements TermInterface {

    private StringBuffer result;
    private Integer vni;
    private PolicyMatch policyMatch;
    private PolicyAction policyAction;
    private Boolean isADeleteSchema = false;

    private Term(final Integer vni,
                 final PolicyMatch policyMatch,
                 final PolicyAction policyAction,
                 final StringBuffer result) {
        this.vni = vni;
        this.policyMatch = policyMatch;
        this.policyAction = policyAction;
        this.result = result;
    }

    public static Term create(final Integer vni,
                              final PolicyMatchType policyMatchType,
                              final PolicyActionType policyActionType,
                              final StringBuffer buffer) {
        final PolicyMatch policyMatch = new PolicyMatch(policyMatchType, vni, buffer);
        final PolicyAction policyAction = new PolicyAction(policyActionType, buffer);
        return new Term(vni, policyMatch, policyAction, buffer);
    }

    @Override
    public void generateRPCStructure() {
        if (!isADeleteSchema) {
            result.append("<term>");
        } else {
            result.append("<term delete=\"delete\">");
        }
        result.append("<name>vni" + vni + "</name>");
        policyMatch.generateRPCStructure();
        policyAction.generateRPCStructure();
        result.append("</term>");
    }

    public static void generateEvpnStaticTerm(final StringBuffer result) {
        result.append("<term>");
        result.append("<name>" + ConfigurationUtils.EVPN_POLICY_COMMUNITY + "</name>");
        PolicyMatch.generateEvpnStaticMatch(result);
        PolicyAction.generateEvpnStaticAction(result);
        result.append("</term>");
    }

    @Override
    public void generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        generateRPCStructure();
    }
}
