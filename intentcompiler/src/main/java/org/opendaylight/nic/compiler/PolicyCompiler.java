//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.CompiledPolicy;
import org.opendaylight.nic.compiler.api.Policy;
import org.opendaylight.nic.compiler.CompilerNode;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;

// A policy compiler to convert policies to compiler nodes and pass them to detect and resolve

public class PolicyCompiler {
    
    private List<CompilerNode> nodes;
    
    public PolicyCompiler() {
	nodes= new LinkedList<CompilerNode>();
    }
    
    /**
     * Compiles the given set of policies into a set of compiled policies.
     */
    
    public List<CompiledPolicy> compile(Set<Policy> policyRequests) {
	// TODO: Use policyrequests to create initial set of compiler nodes and then compile them
	List<CompiledPolicy> cops = new LinkedList<>();
	return cops;
    }
    
}