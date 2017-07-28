/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement;

import com.google.common.collect.Sets;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.Community;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.term.Term;

import java.util.Set;

/**
 * Created by yrineu on 21/07/17.
 */
public class PolicyOptionsConfig {

    private StringBuffer result;
    private String name;
    private Set<Term> terms;
    private Set<Integer> vnis;

    private PolicyOptionsConfig(final String name,
                                final Set<Term> terms,
                                final Set<Integer>vnis,
                                final StringBuffer result) {
        this.name = name;
        this.terms = terms;
        this.vnis = vnis;
        this.result = result;
    }

    public static PolicyOptionsConfig create(final String name,
                                             final Set<Integer> vnis,
                                             final PolicyMatchType policyMatchType,
                                             final PolicyActionType policyActionType,
                                             final StringBuffer buffer) {
        final Set<Term> terms = Sets.newConcurrentHashSet();
        vnis.forEach(vni -> terms.add(Term.create(vni, policyMatchType, policyActionType, buffer)));
        return new PolicyOptionsConfig(name, terms, vnis, buffer);
    }

    public void generateRPCStructure() {
        result.append("<policy-options>");
        result.append("<policy-statement>");
        result.append("<name>" + name.replace(" ", "_") + "</name>");
        terms.forEach(term -> term.generateRPCStructure());
        result.append("</policy-statement>");
        vnis.forEach(vni -> Community.create(vni, result).generateRPCStructure());
        result.append("</policy-options>");
    }
}
