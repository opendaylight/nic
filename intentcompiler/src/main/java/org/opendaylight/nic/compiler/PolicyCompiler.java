//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.Policy;
import org.opendaylight.nic.compiler.CompilerNode;

import java.util.LinkedList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

// A policy compiler to convert policies to compiler nodes and pass them to detect and resolve

public class PolicyCompiler {

    private Set<CompilerNode> nodes;

    public PolicyCompiler() {
        nodes= new LinkedHashSet<CompilerNode>();
    }

    /**
     * Compiles the given set of policies into a set of compiled policies.
     */

    public Set<Policy> compile(Set<Policy> policyRequests) {
	// TODO: Use policyrequests to create initial set of compiler nodes and then compile them
	
	List<CompilerNode> nodeList = new LinkedList<CompilerNode>();
        for (Policy pr : policyRequests) {
            CompilerNode n = CompilerNode.createNode(pr);
            nodeList.add(n);
        }
        
        detectAndResolve(nodeList);
	
	//TODO form and return compiled policies from compiler nodes
	
	Set<Policy> cops = new LinkedHashSet<>();
	return cops;
    }
    
    private void detectAndResolve(List<CompilerNode> list) {
	
	DetectResolve dr =new DetectResolve(list);
	
	while(dr.detectAndResolve()){
	    continue;
	}
	
    }

}