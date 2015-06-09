//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opendaylight.nic.compiler.api.TermType;

public class ClassifierHelper {

    // expression helper
    public static class ExpHelper {
        TermType label;
        int[] startArray;
        int[] endArray;

        ExpHelper(TermType label, int[] startArray, int[] endArray) {
            this.label = label;
            this.startArray = startArray;
            this.endArray = endArray;
        }
    }

    // build expression
    public static ExpressionImpl buildExp(List<ExpHelper> helperList) {
        Map<TermType, TermImpl> termTypeLabelToTermMap = new HashMap<>();
        for (ExpHelper eh : helperList) {
            Collection<IntervalImpl> intervalCollection = new HashSet<IntervalImpl>();
            for (int it = 0; it < eh.startArray.length; it++) {
                intervalCollection.add(IntervalImpl.getInstance(
                        eh.startArray[it], eh.endArray[it]));
            }
            termTypeLabelToTermMap.put(eh.label, (new TermImpl(eh.label,
                    intervalCollection)));
        }
        return new ExpressionImpl(termTypeLabelToTermMap);
    }

    public static ClassifierImpl ethType(int min, int max) {
        return classifierSingleTerm(EthTypeTermType.getInstance(), min, max);
    }

    public static ClassifierImpl ethType(int value) {
        return ethType(value, value);
    }

    public static ClassifierImpl vlan(int min, int max) {
        return classifierSingleTerm(VlanTermType.getInstance(), min, max);
    }

    public static ClassifierImpl classifierSingleTerm(TermType label, int min,
            int max) {
        List<ExpHelper> helperList;
        List<ExpressionImpl> expressionList = new LinkedList<>();
        ClassifierImpl c1;

        // build c1
        expressionList = new LinkedList<ExpressionImpl>();
        // build e1
        helperList = new LinkedList<ExpHelper>();
        helperList.add(new ExpHelper(label, new int[] { min },
                new int[] { max }));
        expressionList.add(buildExp(helperList));
        c1 = new ClassifierImpl(expressionList);
        return c1;
    }

    public static ClassifierImpl classifierEmpty() {
        List<ExpHelper> helperList;
        List<ExpressionImpl> expressionList = new LinkedList<>();
        ClassifierImpl c1;

        // build c1
        expressionList = new LinkedList<ExpressionImpl>();
        // build e1
        helperList = new LinkedList<ExpHelper>();
        expressionList.add(buildExp(helperList));
        c1 = new ClassifierImpl(expressionList);
        return c1;
    }

}
