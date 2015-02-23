//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.compiler.impl.ClassifierImpl;
import org.opendaylight.nic.compiler.impl.CompilerNode;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointId;

/**
 *
 * @author Duane Mentze
 */
public class DetectResolveCompilerNode {

    private final ClassifierImpl c1,c2;
    private CompilerEndpointGroup s1e, s2e, d1e, d2e;
    private final SetCalcs sc;
    private final List<CompilerNode> list;
    private final Map<EndpointId, Endpoint> endpointMap;

    public DetectResolveCompilerNode(CompilerNode n1, CompilerNode n2, List<CompilerNode> list, Map<EndpointId, Endpoint>endpointMap) {

		Set<Endpoint> s1 = n1.srcMembers();
	    Set<Endpoint> s2 = n2.srcMembers();
	    Set<Endpoint> d1 = n1.dstMembers();
	    Set<Endpoint> d2 = n2.dstMembers();
	    
	    s1e = n1.srcCompilerEndpointGroup();
	    s2e = n2.srcCompilerEndpointGroup();
	    
	    d1e = n1.dstCompilerEndpointGroup();
	    d2e = n2.dstCompilerEndpointGroup();	    
	    
	    c1 = n1.classifier();
	    c2 = n2.classifier();
	    
	    sc = new SetCalcs(s1, s2, d1, d2, c1, c2);	    
	    
	    this.list = list;
	    this.endpointMap = endpointMap;
	}	    

    //case S1 and S2      D1 and D2      C1 and C2    
    public boolean overlap() {
    	return !sc.s1Ands2().isEmpty() && !sc.d1Andd2().isEmpty() && !sc.c1Andc2().isEmpty();
    }
	
    //case S1 and S2      D1 and D2      C1 and C2    
	public void handleOverlap(CompilerNode n1, CompilerNode n2) {
        n1.resolveConflict(n2, list, endpointMap);		
	}
	
	//test S1 and not S2   D1 and not D2  C1
	public boolean test_S1andNotS2_D1andNotD2_C1() {	// 
		return !sc.s1Subs2().isEmpty() && !sc.d1Subd2().isEmpty() && !c1.isEmpty();	// 
	} 

	//handle S1 and not S2   D1 and not D2  C1	
	public void handle_S1andNotS2_D1andNotD2_C1(CompilerNode n1, CompilerNode n2) {
		n1.createNonOverlapNode(n2,"1&2.1", s1e.andNot(s2e), d1e.andNot(d2e), c1, list, endpointMap);		
	}
	
	//case S1 and not S2   D1 and not D2  C1 and not C2
	public boolean test_S1andNotS2_D1andNotD2_C1andNotC2() {	// 
		return !sc.s1Subs2().isEmpty() && !sc.d1Subd2().isEmpty() && !sc.c1Subc2().isEmpty(); 
	}

	//handle S1 and not S2   D1 and not D2  C1 and not C2	
	public void handle_S1andNotS2_D1andNotD2_C1andNotC2(CompilerNode n1, CompilerNode n2) {
		n1.createNonOverlapNode(n2,"1.1", s1e.andNot(s2e), d1e.andNot(d2e), sc.c1Subc2(), list, endpointMap);		
	}
	
	//case S1 and not S2   D1 and not D2  C1 and C2
	public boolean test_S1andNotS2_D1andNotD2_C1andC2() {	// 
		return !sc.s1Subs2().isEmpty() && !sc.d1Subd2().isEmpty() && !sc.c1Andc2().isEmpty();
	}
	
	//handle S1 and not S2   D1 and not D2  C1 and C2	
	public void handle_S1andNotS2_D1andNotD2_C1andC2(CompilerNode n1, CompilerNode n2) {
		n1.createNonOverlapNode(n2,"2.1", s1e.andNot(s2e), d1e.andNot(d2e), sc.c1Andc2(), list, endpointMap);		
	}
	
    //case S1 and not S2   D1 and D2      C1
	public boolean test_S1andNotS2_D1andD2_C1() {
		return !sc.s1Subs2().isEmpty() && !sc.d1Andd2().isEmpty() && !c1.isEmpty(); 
	}

    //case S1 and not S2   D1 and D2      C1	
	public void handle_S1andNotS2_D1andD2_C1(CompilerNode n1, CompilerNode n2) {
	    n1.createNonOverlapNode(n2,"3&4.1", s1e.andNot(s2e), d1e.and(d2e), c1, list, endpointMap);
	}

    //case S1 and not S2   D1 and D2      C1 and not C2
	public boolean test_S1andNotS2_D1andD2_C1andNotC2() {
		return !sc.s1Subs2().isEmpty() && !sc.d1Andd2().isEmpty() && !sc.c1Subc2().isEmpty(); 
	}

    //case S1 and not S2   D1 and D2      C1 and not C2
	public void handle_S1andNotS2_D1andD2_C1andNotC2(CompilerNode n1, CompilerNode n2) {
	    n1.createNonOverlapNode(n2,"3.1", s1e.andNot(s2e), d1e.and(d2e), sc.c1Subc2(), list, endpointMap);
	}
    //case S1 and not S2   D1 and D2      C1 and C2
	public boolean test_S1andNotS2_D1andD2_C1andC2() {
		return !sc.s1Subs2().isEmpty() && !sc.d1Andd2().isEmpty() && !sc.c1Andc2().isEmpty();
	}

    //case S1 and not S2   D1 and D2      C1 and C2
	public void handle_S1andNotS2_D1andD2_C1andC2(CompilerNode n1, CompilerNode n2) {
	    n1.createNonOverlapNode(n2,"4.1", s1e.andNot(s2e), d1e.and(d2e), sc.c1Andc2(), list, endpointMap);
	}

	//case S1 and S2       D1 and not D2  C1
	public boolean test_S1andS2_D1andNotD2_C1() {
		return !sc.s1Ands2().isEmpty() && !sc.d1Subd2().isEmpty() && !c1.isEmpty(); 
	}

    //case S1 and S2       D1 and not D2  C1
	public void handle_S1andS2_D1andNotD2_C1(CompilerNode n1, CompilerNode n2) {
	    n1.createNonOverlapNode(n2,"5&6.1", s1e.and(s2e), d1e.andNot(d2e), c1, list, endpointMap);
	}
	
	//case S1 and S2       D1 and not D2  C1 and not C2
	public boolean test_S1andS2_D1andNotD2_C1andNotC2() {
		return !sc.s1Ands2().isEmpty() && !sc.d1Subd2().isEmpty() && !sc.c1Subc2().isEmpty(); 
	}

    //case S1 and S2       D1 and not D2  C1 and not C2
	public void handle_S1andS2_D1andNotD2_C1andNotC2(CompilerNode n1, CompilerNode n2) {
	    n1.createNonOverlapNode(n2,"5.1", s1e.and(s2e), d1e.andNot(d2e), sc.c1Subc2(), list, endpointMap);
	}	
	
	//case S1 and S2       D1 and not D2  C1 and C2
	public boolean test_S1andS2_D1andNotD2_C1andC2() {
		return !sc.s1Ands2().isEmpty() && !sc.d1Subd2().isEmpty() && !sc.c1Andc2().isEmpty(); 
	}

    //case S1 and S2       D1 and not D2  C1 and C2
	public void handle_S1andS2_D1andNotD2_C1andC2(CompilerNode n1, CompilerNode n2) {
	    n1.createNonOverlapNode(n2,"6.1", s1e.and(s2e), d1e.andNot(d2e), sc.c1Andc2(), list, endpointMap);
	}	
        
    //case S1 and S2       D1 and D2      C1 and not C2 
	public boolean test_S1andS2_D1andD2_C1andNotC2() {
		return !sc.s1Ands2().isEmpty() && !sc.d1Andd2().isEmpty() && !sc.c1Subc2().isEmpty();
	}

    //case S1 and S2       D1 and D2      C1 and not C2 
	public void handle_S1andS2_D2andD2_C1andNotC2(CompilerNode n1, CompilerNode n2) {
	    n1.createNonOverlapNode(n2,"7.1", s1e.and(s2e), d1e.and(d2e), sc.c1Subc2(), list, endpointMap);
	}

    //case 2.3: S2 and not s1   D2 and not D1  C2
	public boolean test_S2andNotS2_D2_andNotD1_C2() {
		return !sc.s2Subs1().isEmpty() && !sc.d1Subd2().isEmpty() && !c2.isEmpty(); 
	}

    //case 2.3: S2 and not s1   D2 and not D1  C2
	public void handle_S2andNotS1_D2andNotD1_C2(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"1&2.2", s2e.andNot(s1e), d1e.andNot(d2e), c2, list, endpointMap);
	}	
	
    //case 2.3: S2 and not s1   D2 and not D1  C2 and not C1
	public boolean test_S2andNotS2_D2_andNotD1_C2andNotC1() {
		return !sc.s2Subs1().isEmpty() && !sc.d1Subd2().isEmpty() && !sc.c2Subc1().isEmpty();
	}

    //case 2.3: S2 and not s1   D2 and not D1  C2 and not C1
	public void handle_S2andNotS1_D2andNotD1_C2andNotC1(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"1.2", s2e.andNot(s1e), d1e.andNot(d2e), sc.c2Subc1(), list, endpointMap);
	}	
	
    //case 2.3: S2 and not s1   D2 and not D1  C2 and C1
	public boolean test_S2andNotS2_D2_andNotD1_C2andC1() {
		return !sc.s2Subs1().isEmpty() && !sc.d1Subd2().isEmpty() && !sc.c1Andc2().isEmpty(); 
	}

    //case 2.3: S2 and not s1   D2 and not D1  C2 and C1
	public void handle_S2andNotS1_D2andNotD1_C2andC1(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"2.2", s2e.andNot(s1e), d1e.andNot(d2e), sc.c1Andc2(), list, endpointMap);
	}	
	
    //case S2 and not S1   D1 and D2      C2
	public boolean test_S2andNotS2_D2andD1_C2() {
		return !sc.s2Subs1().isEmpty() && !sc.d1Andd2().isEmpty() && !c2.isEmpty(); 
	}

    //case S2 and not S1   D1 and D2      C2
	public void handle_S2andNotS1_D2andD1_C2(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"3&4.2", s2e.andNot(s1e), d1e.and(d2e), c2, list, endpointMap);
	}
    
    //case S2 and not S1   D1 and D2      C2 and not C1
	public boolean test_S2andNotS2_D2andD1_C2andNotC1() {
		return !sc.s2Subs1().isEmpty() && !sc.d1Andd2().isEmpty() && !sc.c2Subc1().isEmpty(); 
	}

    //case S2 and not S1   D1 and D2      C2and not C1
	public void handle_S2andNotS1_D2andD1_C2andNotC1(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"3.2", s2e.andNot(s1e), d1e.and(d2e), sc.c2Subc1(), list, endpointMap);
	}
		
    //case S2 and not S1   D1 and D2      C2 and C1
	public boolean test_S2andNotS2_D2andD1_C2andC1() {
		return !sc.s2Subs1().isEmpty() && !sc.d1Andd2().isEmpty() && !sc.c1Andc2().isEmpty(); 
	}

    //case S2 and not S1   D1 and D2      C2 and C1
	public void handle_S2andNotS1_D2andD1_C2andC1(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"4.2", s2e.andNot(s1e), d1e.and(d2e), sc.c1Andc2(), list, endpointMap);
	}
	
	//case S1 and S2       D2 and not D1  C2
	public boolean test_S2andS1_D2andNotD1_C2() {
		return !sc.s1Ands2().isEmpty() && !sc.d2Subd1().isEmpty() && !c2.isEmpty(); 
	}

    //case S1 and S2       D2 and not D1  C2
	public void handle_S2andS1_D2andNotD1_C2(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"5&6.2", s2e.and(s1e), d2e.andNot(d1e), c2, list, endpointMap);
	}

	//case S1 and S2       D2 and not D1  C2 and not C1
	public boolean test_S2andS1_D2andNotD1_C2andNotC1() {
		return !sc.s1Ands2().isEmpty() && !sc.d2Subd1().isEmpty() && !sc.c2Subc1().isEmpty(); 
	}

    //case S1 and S2       D2 and not D1  C2 and not C1
	public void handle_S2andS1_D2andNotD1_C2andNotC1(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"5.2", s2e.and(s1e), d2e.andNot(d1e), sc.c2Subc1(), list, endpointMap);
	}

	//case S1 and S2       D2 and not D1  C2 and C1
	public boolean test_S2andS1_D2andNotD1_C2andC1() {
		return !sc.s1Ands2().isEmpty() && !sc.d2Subd1().isEmpty() && !sc.c1Andc2().isEmpty(); 
	}

    //case S1 and S2       D2 and not D1  C2 and C1
	public void handle_S2andS1_D2andNotD1_C2andC1(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"6.2", s2e.and(s1e), d2e.andNot(d1e), sc.c1Andc2(), list, endpointMap);
	}

    //case S1 and S2       D1 and D2      C2 and not C1
	public boolean test_S2andS2_D2andD1_C2andNotC1() {
		return !sc.s1Ands2().isEmpty() && !sc.d1Andd2().isEmpty() && !sc.c2Subc1().isEmpty(); 
	}

    //case S1 and S2       D1 and D2      C2 and not C1
	public void handle_S2andS2_D2andD1_C2andNotC1(CompilerNode n1, CompilerNode n2) {
		n2.createNonOverlapNode(n1,"7.2", s2e.and(s1e), d1e.and(d2e), sc.c2Subc1(), list, endpointMap);
	}

	@Override
	public String toString() {
		return "DetectResolveCompilerNode [c1=" + c1 + ", c2=" + c2 + ", s1e="
				+ s1e + ", s2e=" + s2e + ", d1e=" + d1e + ", d2e=" + d2e
				+ ", sc=" + sc + ", list=" + list + ", endpointMap="
				+ endpointMap + "]";
	}
        
	
}
