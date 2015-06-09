//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Set;

import org.opendaylight.nic.compiler.api.Expression;

/*
 * A classifier of network traffic which determines whether a policy will apply
 * to the given traffic. Each classifier is a collection of expressions, ANY of
 * which may match for the policy to be applied.
 *
 *
 * Put in more specific terms, a classifier has a list of Expressions [ e1, e2,
 * ...] which are logically combined with ORs: e1 OR e2 OR ...
 *
 * An Expression has a list of unique Terms: [ t1, t2 which are logically
 * combined with ANDs: t1 AND t2 AND ...
 *
 * A Term has a name/type and list of Intervals: [i1, i2, ...] which are
 * logically combined with ORs: i1 OR i2 OR ...
 *
 * An Interval has a start and an end.
 *
 * Putting all of this together, a classifier has this implied layout: c = e1 or
 * e2 or ... e = t1 & t2 & ... t = <name, i1 or i2 or ...>
 *
 * Here is a more concrete example: t.a = IP_PROT[10,30] or [50,60] t.b =
 * VLAN[1,5] e.1 = t.a & t.b = (IP_PROT[10,30] or [50,60]) AND (VLAN[1,5])
 *
 * t.c = IP_PROT[1,5] t.d = VLAN[14] e.2 = t.c & t.d = IP_PROT[1,5] and VLAN[14]
 *
 * c = e1 or e2 = (IP_PROT[10,30] or [50,60]) AND (VLAN[1,5]) OR IP_PROT[1,5]
 * and VLAN[14]
 */

public interface Classifier {

    public boolean isEmpty();

    public Set<? extends Expression> getExpressions();

}
