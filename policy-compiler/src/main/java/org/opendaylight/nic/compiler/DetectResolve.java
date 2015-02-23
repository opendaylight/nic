//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opendaylight.nic.compiler.impl.CompilerNode;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointId;

/**
 * Detects and resolves overlaps in a {@link CompilerNode} List.
 * <p>
 * Overlaps must exist between two nodes (n1,ns2) in all dimensions.
 * The dimension include:  source endpoint group, destination endpoint group and classifier.
 *
 * @author Duane Mentze
 */
public class DetectResolve {

    private final List<CompilerNode> list;
    Map<EndpointId, Endpoint> endpointMap;

	public DetectResolve(List<CompilerNode> list, Map<EndpointId, Endpoint>endpointMap) {
		this.list = list;
		this.endpointMap = endpointMap;
	}

	/**
	 * Detects and resolves conflicts for a List of CompilerNodes
	 * <p>
	 * Searchers for a single overlap.  If found it resolves it, updates the list and returns.
	 *
	 * @return true if overlap is found, false otherwise
	 */
	public boolean detectAndResolve() {

		if (list.size()<=1) {
			return false;
		}

		int nextItemIndex = 0;
		Iterator<CompilerNode> it1 = list.iterator();
		while (true) {
		    CompilerNode n1 = it1.next();
		    if (!it1.hasNext()) {
		    	break;
		    }
		    nextItemIndex++;  //one greater than current list item index

		    if (n1.delegates().size()>0) {
				continue;
			}

		    List<CompilerNode> list2 = list.subList(nextItemIndex, list.size());
			for (Iterator<CompilerNode> it2 = list2.iterator(); it2.hasNext();) {
			    CompilerNode n2 = it2.next();
			    DetectResolveCompilerNode dcrn = new DetectResolveCompilerNode(n1, n2, list, endpointMap);

			    if (detectAndResolve(n1,n2, dcrn)) {
			    	//overlap found, so start over with new list
			    	return true;
			    }
			}
		}
		//no overlaps found
		return false;
	}

	/**
	 * Detects and resolves conflicts between two CompilerNodes.
	 * @param n1
	 * @param n2
	 * @return true if overlap is found, false otherwise
	 */
	private boolean detectAndResolve(CompilerNode n1, CompilerNode n2, DetectResolveCompilerNode dcrn) {

	    //check for any overlap, case 12.1
	    if (!dcrn.overlap()) {
	    	//n1 and n1 do not overlap, so move to next nodes
	    	return false;
	    }

	    //S1 and S2      D1 and D2      C1 and C2
	    //this is the overlap case
	    dcrn.handleOverlap(n1, n2);
	    
	    
    	//case S1 and not S2   D1 and not D2  C1 and not C2
    	if (dcrn.test_S1andNotS2_D1andNotD2_C1andNotC2()) {
    	    //handle S1 and not S2   D1 and not D2  C1 and not C2	    	    
    	    dcrn.handle_S1andNotS2_D1andNotD2_C1andNotC2(n1, n2);
    	}
    
    	//case S1 and not S2   D1 and not D2  C1 and C2
    	if (dcrn.test_S1andNotS2_D1andNotD2_C1andC2()) {
    	    //handle S1 and not S2   D1 and not D2  C1 and C2	
    	    dcrn.handle_S1andNotS2_D1andNotD2_C1andC2(n1, n2);
    	}
    	
        //case S1 and not S2   D1 and D2      C1 and not C2
    	if (dcrn.test_S1andNotS2_D1andD2_C1andNotC2()) {
    	    //case S1 and not S2   D1 and D2      C1 and not C2
    	    dcrn.handle_S1andNotS2_D1andD2_C1andNotC2(n1, n2);
    	}

   	    //case S1 and not S2   D1 and D2      C1 and C2
    	if (dcrn.test_S1andNotS2_D1andD2_C1andC2()) {
    	    //case S1 and not S2   D1 and D2      C1 and C2
    	    dcrn.handle_S1andNotS2_D1andD2_C1andC2(n1, n2);
    	}
    
    	//case S1 and S2       D1 and not D2  C1 and not C2
    	if (dcrn.test_S1andS2_D1andNotD2_C1andNotC2()) {
    	    //case S1 and S2       D1 and not D2  C1 and not C2
    	    dcrn.handle_S1andS2_D1andNotD2_C1andNotC2(n1, n2);
    	}
    
    	//case S1 and S2       D1 and not D2  C1 and C2
    	if (dcrn.test_S1andS2_D1andNotD2_C1andC2()) {
    	    //case S1 and S2       D1 and not D2  C1 and C2
    	    dcrn.handle_S1andS2_D1andNotD2_C1andC2(n1, n2);
    	}
    
        //case S1 and S2       D1 and D2      C1 and not C2 
    	if (dcrn.test_S1andS2_D1andD2_C1andNotC2()) {
    	    //case S1 and S2       D1 and D2      C1 and not C2 
    	    dcrn.handle_S1andS2_D2andD2_C1andNotC2(n1, n2);
    	}
    
        //case S2 and not s1   D2 and not D1  C2 and not C1
    	if (dcrn.test_S2andNotS2_D2_andNotD1_C2andNotC1()) {
    	    //case S2 and not s1   D2 and not D1  C2 and not C1
    	    dcrn.handle_S2andNotS1_D2andNotD1_C2andNotC1(n1, n2);
    	}
    	
        //case S2 and not s1   D2 and not D1  C2 and C1
    	if (dcrn.test_S2andNotS2_D2_andNotD1_C2andC1()) {
    	    //case S2 and not s1   D2 and not D1  C2 and C1
    	    dcrn.handle_S2andNotS1_D2andNotD1_C2andC1(n1, n2);
    	}
    
        //case S2 and not S1   D1 and D2      C2 and not C1
    	if (dcrn.test_S2andNotS2_D2andD1_C2andNotC1()) {
    	    //case S2 and not S1   D1 and D2      C2and not C1
    	    dcrn.handle_S2andNotS1_D2andD1_C2andNotC1(n1, n2);
    	}
    
        //case S2 and not S1   D1 and D2      C2 and C1
    	if (dcrn.test_S2andNotS2_D2andD1_C2andC1()) {
    	    //case S2 and not S1   D1 and D2      C2 and C1
    	    dcrn.handle_S2andNotS1_D2andD1_C2andC1(n1, n2);
    	}
    	
    	//case S1 and S2       D2 and not D1  C2 and not C1
    	if (dcrn.test_S2andS1_D2andNotD1_C2andNotC1()) {
    	    //case 2.4: S1 and S2       D2 and not D1  C2 and not C1
    	    dcrn.handle_S2andS1_D2andNotD1_C2andNotC1(n1, n2);
    	}
    
    	//case S1 and S2       D2 and not D1  C2 and C1
    	if (dcrn.test_S2andS1_D2andNotD1_C2andC1()) {
    	    //case S1 and S2       D2 and not D1  C2 and C1
    	    dcrn.handle_S2andS1_D2andNotD1_C2andC1(n1, n2);
    	}
    
        //case S1 and S2       D1 and D2      C2 and not C1
    	if (dcrn.test_S2andS2_D2andD1_C2andNotC1()) {
    	    //case S1 and S2       D1 and D2      C2 and not C1
    	    dcrn.handle_S2andS2_D2andD1_C2andNotC1(n1, n2);
    	}
    
	    //overlap found between nodes, return true
	    return true;
	}
}




