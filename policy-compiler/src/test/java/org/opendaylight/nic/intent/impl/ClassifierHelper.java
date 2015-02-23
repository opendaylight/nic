//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.nic.compiler.impl.IntervalImpl;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.terms.EthTypeTermType;
import org.opendaylight.nic.extensibility.terms.VlanTermType;
import org.opendaylight.nic.intent.impl.ClassifierImpl;
import org.opendaylight.nic.intent.impl.ExpressionImpl;
import org.opendaylight.nic.intent.impl.TermImpl;

/**
 * 
 * 
 * @author Duane Mentze
 *
 */
public class ClassifierHelper {
    
	//expression helper
	public static class ExpHelper {
		TermLabel label;
		int[] startArray;
		int[] endArray;
		ExpHelper(TermLabel label, int[] startArray, int[] endArray) {
			this.label = label;
			this.startArray = startArray;
			this.endArray = endArray;
		}
	}

	//build expression
	static public ExpressionImpl buildExp(List<ExpHelper> helperList) {
		Map<TermLabel,TermImpl> termTypeLabelToTermMap = new HashMap<>();
		for (ExpHelper h: helperList) {
			Collection<IntervalImpl> intervalCollection = new HashSet<IntervalImpl>();
			for (int i=0; i<h.startArray.length; i++) {
				intervalCollection.add(IntervalImpl.getInstance(h.startArray[i],h.endArray[i]));
			}
			termTypeLabelToTermMap.put(h.label, (new TermImpl(h.label, intervalCollection)));
		}
		return new ExpressionImpl(termTypeLabelToTermMap);
	}

	static public ClassifierImpl ethType(int min, int max) {
	    return classifierSingleTerm(EthTypeTermType.getInstance().label(),min,max);
	}

	static public ClassifierImpl ethType(int value) {
	    return ethType(value,value);
	}

	static public ClassifierImpl vlan(int min, int max) {
	    return classifierSingleTerm(VlanTermType.getInstance().label(),min,max);
	}

	static public ClassifierImpl classifierSingleTerm(TermLabel label, int min, int max) {
		List<ExpHelper> helperList;
		List<ExpressionImpl> expressionList = new LinkedList<>();
		ClassifierImpl c1;

		//build c1
		expressionList = new LinkedList<ExpressionImpl>();
		//build e1
		helperList = new LinkedList<ExpHelper>();
		helperList.add(new ExpHelper(label, new int[] {min}, new int[] {max}));
		expressionList.add(buildExp(helperList));
		c1 = new ClassifierImpl(expressionList);
		return c1;
	}

	static public ClassifierImpl classifierEmpty() {
		List<ExpHelper> helperList;
		List<ExpressionImpl> expressionList = new LinkedList<>();
		ClassifierImpl c1;

		//build c1
		expressionList = new LinkedList<ExpressionImpl>();
		//build e1
		helperList = new LinkedList<ExpHelper>();
		expressionList.add(buildExp(helperList));
		c1 = new ClassifierImpl(expressionList);
		return c1;
	}

    

}
