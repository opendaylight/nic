//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.common.Application;
import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.ActionType;
import org.opendaylight.nic.extensibility.CodeGenerator;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermType;
import org.opendaylight.nic.intent.Policy;
import org.opendaylight.nic.intent.PolicyId;
import org.opendaylight.nic.intent.PolicyStatus;
import org.opendaylight.nic.services.ApplicationService;
import org.opendaylight.nic.services.PolicyFramework;
import org.opendaylight.nic.services.PolicyService;

/**
 * This is an an implementation of several services to provide an overall
 * capability create and enforce Policys on a network domain.
 * <p>
 *
 * @author Shaun Wackerly
 * @author Duane Mentze
 */
public class PolicyFrameworkImpl implements PolicyFramework, PolicyService {

	/** A mapping from action label to implementation. */
    private final Map<ActionLabel,ActionType> actions;

    /** A mapping from term type label to implementation. */
    private final Map<TermLabel, TermType> termTypes;

    /** Registered code generators. */
    private final Set<CodeGenerator<?,?,?>> codeGenerators;

    private final ApplicationService as;

    /** The set of registered policies, by application */
    private final Map<AppId,Map<PolicyId,Policy>> policies;

    /** access to policies */
    public Set<Policy> getPolicies() {
    	Set<Policy> all = new HashSet<Policy>();
    	for (Map<PolicyId, Policy> e:  policies.values() ) {
    		all.addAll(e.values());
    	}
    	return all;
    }

    /** access to actions */
    public Map<ActionLabel, ActionType> getActions() {
    	return actions;
    }

    /** access to terms */
    public Map<TermLabel,TermType> getTermTypes() {
    	return termTypes;
    }

    /**
     * Constructs a policy framework implementation.
     */
    public PolicyFrameworkImpl(ApplicationService as) {
        this.as = as;

        actions = new HashMap<>();
        termTypes = new HashMap<>();
        codeGenerators = new HashSet<>();
        policies = new HashMap<>();
    }

    @Override
    public void register(ActionType act) {
        if (actions.containsKey(act.label()))
            throw new IllegalArgumentException("Action already registered for label: " + act.label());

        actions.put(act.label(), act);
    }

    @Override
    public ActionType getAction(ActionLabel label) {
        return actions.get(label);
    }

    @Override
    public void register(TermType tt) {
        if (termTypes.containsKey(tt.label()))
            throw new IllegalArgumentException("Term type already registered for label: " + tt.label());

        termTypes.put(tt.label(), tt);
    }

    @Override
    public TermType getTermType(TermLabel label) {
        return termTypes.get(label);
    }

    @Override
    public void register(CodeGenerator<?,?,?> codegen) {
        codeGenerators.add(codegen);
    }

	@Override
	public PolicyStatus add(Policy policy, AppId id) {
	    Application app = as.get(id);
		if (app == null) {
            throw new IllegalStateException("App not registered: " + id);
		}

		if (policies.get(id) == null)
		    policies.put(id, new HashMap<PolicyId,Policy>());

		if (policies.get(id).containsKey(policy.PolicyId())) {
            throw new IllegalStateException("Policy already added: " + policy.PolicyId());
		}

		policies.get(id).put(policy.PolicyId(), policy);
		return PolicyStatus.PreEnforcement;
	}


	@Override
	public PolicyStatus update(Policy current, Policy updated, AppId id) {
        Application app = as.get(id);
        if (app == null) {
            throw new IllegalStateException("App not registered: " + id);
        }

        if (policies.get(id) == null)
            throw new IllegalStateException("No policies registered for app "+id);

        if (!current.PolicyId().equals(updated.PolicyId()))
            throw new IllegalArgumentException("Current and updated policy do not have the same ID");

        if (!policies.get(id).containsKey(current.PolicyId())) {
            throw new IllegalStateException("Policy doesn't exist: " + current.PolicyId());
        }

        policies.get(id).put(updated.PolicyId(), updated);
        return PolicyStatus.PreEnforcement;
	}


	@Override
	public void remove(Policy policy, AppId id) {
        Application app = as.get(id);
        if (app == null) {
            throw new IllegalStateException("App not registered: " + id);
        }

        if (policies.get(id) == null)
            throw new IllegalStateException("No policies registered for app "+id);

		policies.get(id).remove(policy.PolicyId());
	}

}
