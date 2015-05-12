//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.Policy;
import org.opendaylight.nic.compiler.CompilerNode;

import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Nodes are used in the compilation process as an intermediate representation
 * for Policies. They contain a source, destination and classifier along with
 * the ActionSets of all composed policies used to create this Node.
 */

public class CompilerNode implements Policy {

    private Set<Endpoint> srcMembers;
    private Set<Endpoint> dstMembers;
//    private Set<Classifier> classifier;
    private Action actionSet;
    private boolean isExclusive;
    private boolean isIndependent;
//    private boolean isObserver;
//    private long maxActionPrecedence;
   private Set<CompilerNode> delegates;
//    private Set<CompilerNode> parents;
//    private CompilerNode dominant;

    @Override
    public Set<Endpoint> src() {
        return srcMembers;
    }

    @Override
    public Set<Endpoint> dst() {
        return dstMembers;
    }

    @Override
    public Action action() {
        return actionSet;
    }
    
    public boolean isExclusive() {
        return isExclusive;
    }
    
    public boolean isIndependent() {
	return isIndependent;
    }
    public void makeIndependent() {
	this.isIndependent=true;
	
    }
    
    public Set<CompilerNode> delegates() {
        return delegates;
    }
    
    public CompilerNode(Set<Endpoint> srcMembers,Set<Endpoint> dstMembers,Action actionSet, boolean isExclusive) {
	this.srcMembers=srcMembers;
	this.dstMembers=dstMembers;
	this.actionSet=actionSet;
	this.isExclusive=isExclusive;
	this.delegates = new LinkedHashSet<>();

	
    }



    public static CompilerNode createNode(Policy policy){

	return new CompilerNode(policy.src(),policy.dst(),policy.action(),policy.action().equals(Action.BLOCK));
    }

}
