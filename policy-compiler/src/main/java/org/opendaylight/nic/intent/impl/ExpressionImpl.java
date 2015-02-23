//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.intent.Expression;
import org.opendaylight.nic.intent.Term;

/**
 * ExpressionImpl is used as a building block to create
 * {@link ClassifierImpl}s.  It consists of a {@link Set} of
 * {@link TermImpl}s.
 * The TermImpl can be thought of as logically AND'd together to from
 * the ExpressionImpl.
 *
 * @author Duane Mentze
  */
public class ExpressionImpl implements Expression {

	private final Map<TermLabel, TermImpl> termMap;

	public boolean isEmpty() {
		return this.termMap.isEmpty();
	}

	@Override
	public Set<TermLabel> getTermTypeLabels() {
		return termMap.keySet();
	}

	@Override
	public Collection<TermImpl> getTerms() {
		return termMap.values();
	}

	@Override
	public Set<Entry<TermLabel, TermImpl>> getEntries() {
		return termMap.entrySet();
	}

	@Override
	public Term getTerm(TermLabel termTypeLabel) {
		return termMap.get(termTypeLabel);
	}

	public ExpressionImpl(Map<TermLabel, TermImpl> termMap) {
		super();
		this.termMap = termMap;
	}

	@Override
	public String toString() {
		return "ExpressionRequest { " + termMap + " }";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((termMap == null) ? 0 : termMap.hashCode());
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
		ExpressionImpl other = (ExpressionImpl) obj;
		if (termMap == null) {
			if (other.termMap != null)
				return false;
		} else if (!termMap.equals(other.termMap))
			return false;
		return true;
	}


}
