//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.Policy;

import com.google.common.collect.Sets;

public class Transform {

    public Collection<Policy> resolve(Policy p1, Policy p2) {

        Collection<Policy> policies = new LinkedList<>();
        Sets.SetView<Endpoint> src, dst;

        // All the possible cases below

        src = Sets.difference(p1.src(), p2.src());
        if (!src.isEmpty()) {

            // Case: S1 and not S2 , D1 and not D2
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }

            // Case: S1 and not S2 , D1 and D2
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }
        }
        src = Sets.intersection(p1.src(), p2.src());
        if (!src.isEmpty()) {

            // Case: S1 and S2 , D1 and D2
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, merge(p1.action(),
                        p2.action())));
            }

            // Case: S1 and S2 , D1 and not D2
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }

            // Case: S1 and S2 , D2 and not D1
            dst = Sets.difference(p2.dst(), p1.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }
        }
        src = Sets.difference(p2.src(), p1.src());
        if (!src.isEmpty()) {

            // Case: S2 and not S1 , D1 and D2
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }

            // Case: S2 and not S1 , D2 and not D1
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
            case OBSERVER:
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
}