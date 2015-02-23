//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.nic.compiler.impl.IntervalImpl;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.intent.Interval;
import org.opendaylight.nic.intent.Term;

/**
 * TermImpl is used as a building block to create {@link ExpressionImpl}s.
 * It consists of {@link TermLabel} and a set of {@link Intervals.
 * The Intervals can be thought of as logically OR'd together to make the Term.
 * <p>
 * @author Duane Mentze
 *
 */
public class TermImpl implements Term {
	
	private final TermLabel typeLabel;
	private final List<IntervalImpl> intervals;
	
	/** returns the type of the Term */
	@Override
	public TermLabel typeLabel() {
		return typeLabel;
	}

	/** returns the ordered list of non-overlapping Intervals in the Term*/
	@Override
	public List<? extends Interval> getIntervals() {
		return intervals;
	}

	/** returns true if there are no Intervals, otherwise false */
	@Override
	public boolean isEmpty() {
		return intervals.isEmpty();
	}

	public TermImpl(TermLabel typeLabel, Collection<IntervalImpl> intervals) {
		super();
		this.typeLabel = typeLabel;
		this.intervals = new LinkedList<IntervalImpl>(intervals);
	}
	
	@Override
	public String toString() {
		return "Term {label:" + typeLabel + ",intervals:" + intervals + "}";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((intervals == null) ? 0 : intervals.hashCode());
		result = prime * result
				+ ((typeLabel == null) ? 0 : typeLabel.hashCode());
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
		TermImpl other = (TermImpl) obj;
		if (intervals == null) {
			if (other.intervals != null)
				return false;
		} else if (!intervals.equals(other.intervals))
			return false;
		if (typeLabel == null) {
			if (other.typeLabel != null)
				return false;
		} else if (!typeLabel.equals(other.typeLabel))
			return false;
		return true;
	}

}
