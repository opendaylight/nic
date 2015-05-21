//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.Policy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class IntentCompiler2Impl extends IntentCompilerImpl {
    @Override
    public Collection<Policy> compile(Collection<Policy> policies) {
        PolicyCompiler compiler = new PolicyCompiler();
        List<Policy> compilerNodes = compiler.compile(new LinkedList<>(policies));
        Collection<Policy> compiledPolicies = new LinkedList<>();
        for (Policy p : compilerNodes) {
            compiledPolicies.add(new PolicyImpl(p.src(), p.dst(), p.action()));
        }
        return compiledPolicies;
    }
}
