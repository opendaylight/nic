//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.nic.extensibility.TermLabel;

/**
 * An expression within a classifier. The expression is a collection of terms,
 * ALL of which must match the traffic for the expression to be considered a match.
 * <p>
 * More specifically, the Terms can be thought of as logically AND'd together to form
 * the Expressions.
 *
 * @author Duane Mentze
  */
public interface Expression {

    /**
     * Returns the set of term labels in this expression.
     *
     * @return set of term labels
     */
	public Set<? extends TermLabel> getTermTypeLabels();

	/**
	 * Returns the set of terms in this expression. A term includes
	 * both the term label and term interval.
	 *
	 * @return set of terms
	 */
	public Collection<? extends Term> getTerms();

	/**
	 * Returns the set of entries in this expression, where each entry
	 * is a unique term label along with the term to which it applies.
	 *
	 * @return set of entries
	 */
	public Set<? extends Entry<? extends TermLabel, ? extends Term>> getEntries();

	/**
	 * Returns the term associated with the given term label.
	 *
	 * @param label term label
	 * @return associated term
	 */
	public Term getTerm(TermLabel label);

}
