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


/**
 * Detects and resolves overlaps in a {@link CompilerNode} List.
 */

public class DetectResolve {
    
    private final List<CompilerNode> list;
    
    public DetectResolve(List<CompilerNode> list) {
	 this.list=list;
    }
   
    /**
     * Detects and resolves conflicts for a List of CompilerNodes
     * <p>
     * Searchers for a single overlap. If found it resolves it, updates the list
     * and returns.
     *
     * @return true if overlap is found, false otherwise
     */
    
    public boolean detectAndResolve() {
	 if (list.size() <= 1) {
	            return false;
	        }

	        int nextItemIndex = 0;
	        Iterator<CompilerNode> it1 = list.iterator();
	        while (true) {
	            CompilerNode n1 = it1.next();
	            if (!it1.hasNext()) {
	                break;
	            }
	            nextItemIndex++; // one greater than current list item index
	            
	            if (n1.delegates().size() > 0) {
	                continue;
	            }
	            
	            if(n1.isIndependent()) {
	        	continue;
	            }

	            List<CompilerNode> list2 = list.subList(nextItemIndex, list.size());
	            for (Iterator<CompilerNode> it2 = list2.iterator(); it2.hasNext();) {
	                CompilerNode n2 = it2.next();
	                DetectResolveCompilerNode dcrn = new DetectResolveCompilerNode(
	                        n1, n2, list);

	                if (detectAndResolve(dcrn)) {
	                    // overlap found, so start over with new list
	                    return true;
	                }
	            }
	            // n1 do not overlap with any node - make independent  
	            n1.makeIndependent();
	        }
	        // no overlaps found
       return false;
    }    
    
    public boolean detectAndResolve(DetectResolveCompilerNode dcrn) {
	
	if (!dcrn.overlap()) {
            // n1 and n2 do not overlap, so move to next nodes
            return false;
        }
	
	dcrn.handleOverlap();
	
	
	  // case S1 and not S2 D1 and not D2 
        if (dcrn.test_S1andNotS2_D1andNotD2()) {
            // handle S1 and not S2 D1 and not D2 
            dcrn.handle_S1andNotS2_D1andNotD2();
        }
        // s1 and not s2 , d1 and d2
        if(dcrn.test_S1andNotS2_D1andD2()){
            dcrn.handle_S1andNotS2_D1andD2();
        }
        
        if(dcrn.test_S1andS2_D1andNotD2()){
            dcrn.handle_S1andS2_D1andNotD2();
        }
        
        if(dcrn.test_S1andS2_D2andNotD1()){
            dcrn.handle_S1andS2_D2andNotD1();
        }
        
        if(dcrn.test_S2andNotS1_D1andD2()){
            dcrn.handle_S2andNotS1_D1andD2();
        }
        
         if(dcrn.test_S2andNotS1_D2andNotD1()) {
             dcrn.handle_S2andNotS1_D2andNotD1();
        }
        
	
	return true;
    }
}

