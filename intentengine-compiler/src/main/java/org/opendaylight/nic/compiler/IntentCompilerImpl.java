//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import com.google.common.collect.Sets;
import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.IntentCompiler;
import org.opendaylight.nic.compiler.api.Policy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class IntentCompilerImpl implements IntentCompiler {
    @Override
    public Collection<Policy> compile(Collection<Policy> policies) {
        Queue<Policy> conflictingPolicies = new LinkedList<>(policies);
        Collection<Policy> compiledPolicies = new LinkedList<>();
        while (!conflictingPolicies.isEmpty()) {
            Policy policy = conflictingPolicies.remove();
            Iterator<Policy> iterator2 = conflictingPolicies.iterator();
            Collection<Policy> results = new LinkedList<>();
            while (iterator2.hasNext()) {
                Policy policy2 = iterator2.next();
                if (conflicts(policy, policy2)) {
                    iterator2.remove();
                    results.addAll(transform(policy, policy2));
                }
            }
            if (results.isEmpty()) {
                compiledPolicies.add(policy);
            } else {
                conflictingPolicies.addAll(results);
            }
        }
        return compiledPolicies;
    }

    @Override
    public Set<Endpoint> parseEndpointGroup(String csv) throws UnknownHostException {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        String[] ipAdresses = csv.split(",");
        for (String ipAddress : ipAdresses) {
            endpoints.add(new EndpointImpl(InetAddress.getByName(ipAddress)));
        }
        return endpoints;
    }

    @Override
    public Policy createPolicy(Set<Endpoint> source, Set<Endpoint> destination, Set<Action> action) {
        return new PolicyImpl(source, destination, action);
    }

    private Collection<Policy> transform(Policy p1, Policy p2) {
        Collection<Policy> policies = new LinkedList<>();
        Sets.SetView<Endpoint> src, dst;

        src = Sets.difference(p1.src(), p2.src());
        if (!src.isEmpty()) {
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }
        }
        src = Sets.intersection(p1.src(), p2.src());
        if (!src.isEmpty()) {
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, merge(p1.action(), p2.action())));
            }
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }
            dst = Sets.difference(p2.dst(), p1.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }
        }
        src = Sets.difference(p2.src(), p1.src());
        if (!src.isEmpty()) {
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }
            dst = Sets.difference(p2.dst(), p1.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }
        }

        return policies;
    }

    private Set<Action> merge(Set<Action> a1, Set<Action> a2) {
        Set<Action> composebleActions = new LinkedHashSet<>();
        Set<Action> observableActions = new LinkedHashSet<>();
        Set<Action> exclusiveActions = new LinkedHashSet<>();
        for (Action action : Sets.union(a1, a2)) {
            switch (action.getType()) {
                case COMPOSABLE:
                    composebleActions.add(action);
                    break;
                case OBSERVABLE:
                    observableActions.add(action);
                    break;
                case EXCLUSIVE:
                    exclusiveActions.add(action);
                    break;
            }
        }
        if (!exclusiveActions.isEmpty()) {
            if (exclusiveActions.size() == 1) {
                return Sets.union(exclusiveActions, observableActions);
            } else {
                // TODO: Better handle that case
                throw new RuntimeException("Unable to merge exclusive actions");
            }
        }
        return Sets.union(composebleActions, observableActions);
    }

    private boolean conflicts(Policy p1, Policy p2) {
        if (!Sets.intersection(p1.src(), p2.src()).isEmpty()
                && !Sets.intersection(p1.dst(), p2.dst()).isEmpty())
            return true;
        return false;
    }
}
