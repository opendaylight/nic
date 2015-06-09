//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.classifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opendaylight.nic.compiler.Classifier;

/**
 * An implementation of {@link Classifier}.
 * 
 */
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

    public ClassifierImpl(Collection<ExpressionImpl> c) {
        expressions = new HashSet<ExpressionImpl>();
        expressions.addAll(c);
    }

    public ClassifierImpl(Set<ExpressionImpl> expressions) {
        this.expressions = new HashSet<ExpressionImpl>(expressions);
    }

    public ClassifierImpl(ClassifierImpl cr) {
        this.expressions = new HashSet<ExpressionImpl>(cr.getExpressions());
    }

    @Override
    public String toString() {
        return "Classifier { Expressions:" + expressions + "}";
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassifierImpl other = (ClassifierImpl) obj;
        if (expressions == null) {
            if (other.expressions != null)
                return false;
        } else if (!expressions.equals(other.expressions))
            return false;
        return true;
    }

}
