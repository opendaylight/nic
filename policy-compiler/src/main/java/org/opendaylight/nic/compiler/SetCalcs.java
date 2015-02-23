//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.nic.compiler.impl.ClassifierImpl;
import org.opendaylight.nic.intent.Endpoint;

/**
 * A grouping of utility methods related to operations on sets of data.
 *
 * @author Duane Mentze
 */
class SetCalcs {
    private final Set<Endpoint> s1, s2, d1, d2;
    private final ClassifierImpl c1,c2;

    private Set<Endpoint> s1Ands2, d1Andd2, s1Subs2, d1Subd2, s2Subs1, d2Subd1;
    private ClassifierImpl c1Andc2, c1Subc2, c2Subc1;


	public SetCalcs(Set<Endpoint> s1, Set<Endpoint> s2, Set<Endpoint> d1,
			Set<Endpoint> d2, ClassifierImpl c1, ClassifierImpl c2) {
		this.s1 = s1;
		this.s2 = s2;
		this.d1 = d1;
		this.d2 = d2;
		this.c1 = c1;
		this.c2 = c2;
	}

	public Set<Endpoint> s1Ands2() {
		if (s1Ands2==null) {
		    s1Ands2 = new HashSet<Endpoint>(s1);
		    s1Ands2.retainAll(s2);
		}
		return s1Ands2;
	}

	public Set<Endpoint> d1Andd2() {
		if (d1Andd2==null) {
		    d1Andd2 = new HashSet<Endpoint>(d1);
		    d1Andd2.retainAll(d2);
		}
		return d1Andd2;
	}

	public Set<Endpoint> s1Subs2() {
		if (s1Subs2==null) {
	        s1Subs2 = new HashSet<Endpoint>(s1);
	        s1Subs2.removeAll(s2);
		}
		return s1Subs2;
	}

	public Set<Endpoint> d1Subd2() {
		if (d1Subd2==null) {
	        d1Subd2 = new HashSet<Endpoint>(d1);
	        d1Subd2.removeAll(d2);
		}
		return d1Subd2;
	}

	public Set<Endpoint> s2Subs1() {
		if (s2Subs1==null) {
	        s2Subs1 = new HashSet<Endpoint>(s2);
	        s2Subs1.removeAll(s1);
		}
		return s2Subs1;
	}

	public Set<Endpoint> d2Subd1() {
		if (d2Subd1==null) {
	        d2Subd1 = new HashSet<Endpoint>(d2);
	        d2Subd1.removeAll(d1);
		}
		return d2Subd1;
	}

	public ClassifierImpl c1Andc2() {
		if (c1Andc2==null) {
			c1Andc2 = c1.and(c2);
		}
		return c1Andc2;
	}

	public ClassifierImpl c1Subc2() {
		if (c1Subc2==null) {
			c1Subc2 = c1.sub(c2);
		}
		return c1Subc2;
	}

	public ClassifierImpl c2Subc1() {
		if (c2Subc1==null) {
	        c2Subc1 = c2.sub(c1);
		}
		return c2Subc1;
	}

	@Override
	public String toString() {
		return "SetCalcs [s1=" + s1 + ", s2=" + s2 + ", d1=" + d1 + ", d2="
				+ d2 + ", c1=" + c1 + ", c2=" + c2 + ", s1Ands2=" + s1Ands2
				+ ", d1Andd2=" + d1Andd2 + ", s1Subs2=" + s1Subs2
				+ ", d1Subd2=" + d1Subd2 + ", s2Subs1=" + s2Subs1
				+ ", d2Subd1=" + d2Subd1 + ", c1Andc2=" + c1Andc2
				+ ", c1Subc2=" + c1Subc2 + ", c2Subc1=" + c2Subc1 + "]";
	}
}