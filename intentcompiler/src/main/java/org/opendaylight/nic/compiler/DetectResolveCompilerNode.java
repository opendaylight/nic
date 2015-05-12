//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.*;
import org.opendaylight.nic.compiler.api.*;

import java.util.List;
import java.util.Set;

public class DetectResolveCompilerNode {
    
    private final List<CompilerNode> list;
    private CompilerNode n1,n2;
    private final SetCalcs sc;
    
    public DetectResolveCompilerNode(CompilerNode n1, CompilerNode n2, List<CompilerNode> list) {
	
	Set<Endpoint> s1 = n1.src();
	Set<Endpoint> s2 = n2.src();
	Set<Endpoint> d1 = n1.dst();
	Set<Endpoint> d2 = n2.dst();
	
	this.n1=n1;
	this.n2=n2;
	this.list=list;
	
	sc = new SetCalcs(s1, s2, d1, d2);
	
    }
    
 // case S1 and S2 D1 and D2 C1 and C2
    public boolean overlap() {
        return !sc.s1Ands2().isEmpty() && !sc.d1Andd2().isEmpty();
               
    }
    
    public void handleOverlap() {
	
	if(n1.isExclusive()) {
	    CompilerNode n = new CompilerNode(sc.s1Ands2(),sc.d1Andd2(),n1.action(),true);
	    list.add(n);
	}
	
	if(n2.isExclusive()) {
	    CompilerNode n = new CompilerNode(sc.s1Ands2(),sc.d1Andd2(),n2.action(),true);
	    list.add(n);
	}

    }
}