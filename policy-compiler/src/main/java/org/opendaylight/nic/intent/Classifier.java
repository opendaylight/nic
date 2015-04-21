//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import java.util.Set;

/**
 * A classifier of network traffic which determines whether a policy will apply
 * to the given traffic. Each classifier is a collection of expressions, ANY of
 * which may match for the policy to be applied.
 * <p>
 * Put in more specific terms, a classifier has a list of {@link Expression}s [
 * e1, e2, ...] which are logically combined with ORs: e1 OR e2 OR ...
 * <p>
 * An {@link Expression} has a list of unique {@link Terms}s: [ t1, t2 which are
 * logically combined with ANDs: t1 AND t2 AND ...
 * <p>
 * A {@link Term} has a name/type and list of {@link Interval}s: [i1, i2, ...]
 * which are logically combined with ORs: i1 OR i2 OR ...
 * <p>
 * An Interval has a start and an end.
 * <p>
 * Putting all of this together, a classifier has this implied layout: c = e1 or
 * e2 or ... e = t1 & t2 & ... t = <name, i1 or i2 or ...>
 * <p>
 * Here is a more concrete example: t.a = IP_PROT[10,30] or [50,60] t.b =
 * VLAN[1,5] e.1 = t.a & t.b = (IP_PROT[10,30] or [50,60]) AND (VLAN[1,5])
 *
 * t.c = IP_PROT[1,5] t.d = VLAN[14] e.2 = t.c & t.d = IP_PROT[1,5] and VLAN[14]
 *
 * c = e1 or e2 = (IP_PROT[10,30] or [50,60]) AND (VLAN[1,5]) OR IP_PROT[1,5]
 * and VLAN[14]
 *
 * @author Duane Mentze
 *
 */
public interface Classifier {

    /**
     * Returns the set of expressions which make up this classifier.
     *
     * @return set of expressions
     */
    public Set<? extends Expression> getExpressions();

    /**
     * Returns whether or not this classifier is empty. An empty classifier is a
     * classifier with no specified expressions.
     *
     * @return whether the classifier is empty
     */
    public boolean isEmpty();

}
