//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.IntentCompiler;
import org.opendaylight.nic.compiler.api.IntentCompilerException;
import org.opendaylight.nic.compiler.api.Policy;

import com.google.common.collect.Sets;

public class IntentCompilerImpl implements IntentCompiler {

    private final Transform transform;

    @Override
    public Collection<Policy> compile(Collection<Policy> policies) throws IntentCompilerException {
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
                    results.addAll(transform.resolve(policy, policy2));
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
    public Set<Endpoint> parseEndpointGroup(String csv)
            throws UnknownHostException {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        String[] ipAdresses = csv.split(",");
        for (String ipAddress : ipAdresses) {
            endpoints.add(new EndpointImpl(InetAddress.getByName(ipAddress)));
        }
        return endpoints;
    }

    @Override
    public Policy createPolicy(Set<Endpoint> source, Set<Endpoint> destination,
            Set<Action> action) {
        return new PolicyImpl(source, destination, action);
    }

    public IntentCompilerImpl() {
        transform = new Transform();
    }

    private boolean conflicts(Policy p1, Policy p2) {
        if (!Sets.intersection(p1.src(), p2.src()).isEmpty()
                && !Sets.intersection(p1.dst(), p2.dst()).isEmpty()) {
            return true;
        }
        return false;
    }
}
