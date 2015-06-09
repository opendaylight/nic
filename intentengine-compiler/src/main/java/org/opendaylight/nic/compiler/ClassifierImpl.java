//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ClassifierImpl implements Classifier {

    private final Set<ExpressionImpl> expressions;

    @Override
    public Set<ExpressionImpl> getExpressions() {
        return expressions;
    }

    @Override
    public boolean isEmpty() {
        return (expressions.isEmpty());
    }

    public static ClassifierImpl getInstance(ExpressionImpl exp) {
        Collection<ExpressionImpl> co = new HashSet<ExpressionImpl>();
        co.add(exp);
        return getInstance(co);
    }

    public static ClassifierImpl getInstance(
            Collection<ExpressionImpl> expressioncollection) {
        return new ClassifierImpl(expressioncollection);
    }

    public ClassifierImpl(Collection<ExpressionImpl> co) {
        expressions = new HashSet<ExpressionImpl>();
        expressions.addAll(co);

        // TODO
        // while (removeOverlaps(self.expressions) == True):
        // continue

        // self.expressions.sort(key = lambda e: e.expression, reverse=True)

    }

    @Override
    public String toString() {
        return "{" + expressions + "}";
    }

    /**
     * Operator <em>sub</em> finds the difference between two Classifiers.
     * <p>
     * Calculates this - other. Subtracting or'd items in a list is done using
     * subtraction Each subtracted item can do 1 of 4 things to the 'self'
     * items:
     * <ul>
     * <li>1. miss - have no impact
     * <li>2. overlap and remove part of it
     * <li>3. split the 'self' item and remove some
     * <li>4. remove the entire 'self' item
     * 
     */
    public ClassifierImpl sub(ClassifierImpl other) {

        List<ExpressionImpl> workList = new LinkedList<ExpressionImpl>(
                this.expressions);
        List<ExpressionImpl> difference = new LinkedList<ExpressionImpl>();

        while (!workList.isEmpty()) {
            ExpressionImpl eleft = workList.get(0);
            boolean noOverlap = true;
            for (ExpressionImpl eother : other.getExpressions()) {
                Set<ExpressionImpl> resultSet = eleft.sub(eother);
                if (resultSet.isEmpty()) {
                    // case #4: subtraction removed entire expression
                    noOverlap = false;
                    workList.remove(0);
                    break;
                } else if ((resultSet.size() == 1) && resultSet.contains(eleft)) {
                    // case #1: subtraction had no impact
                    continue;
                } else {
                    // cases 2,3: overlap happened. Add result to work list.
                    noOverlap = false;
                    workList.remove(eleft);
                    workList.addAll(resultSet);
                    break;
                }
            }
            if (noOverlap) {
                difference.add(eleft);
                workList.remove(eleft);
            }

        }
        return getInstance(difference);
    }

    public boolean greaterThan(ClassifierImpl other) {
        ClassifierImpl difference = this.sub(other);
        return !difference.isEmpty();
    }

    public boolean lessThan(ClassifierImpl other) {
        return other.greaterThan(this);
    }

    public boolean greaterThanOrEqual(ClassifierImpl other) {
        return (this.equals(other)) || greaterThan(other);
    }

    public ClassifierImpl and(ClassifierImpl other) {
        Collection<ExpressionImpl> result = new HashSet<ExpressionImpl>();
        for (ExpressionImpl exp : this.expressions) {
            for (ExpressionImpl oe : other.getExpressions()) {
                ExpressionImpl overlap = exp.and(oe);
                if (overlap.isNull()) {
                    continue;
                }
                result.add(overlap);
            }
        }
        return getInstance(result);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((expressions == null) ? 0 : expressions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ClassifierImpl other = (ClassifierImpl) obj;
        if (expressions == null) {
            if (other.expressions != null) {
                return false;
            }
        } else if (!expressions.equals(other.expressions)) {
            return false;
        }
        return true;
    }

}
