//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import java.util.List;

import org.opendaylight.nic.extensibility.TermLabel;

/**
 * A term within a classifier expression. The term is a collection of single
 * values and ranges, ANY of which may match for the term to be considered a
 * match.
 * <p>
 * More specifically, a Term is used as a building block to create
 * {@link Expression}s. It consists of {@link TermLabel} and a set of
 * {@link Interval}s. The Intervals can be thought of as logically OR'd together
 * to make the Term.
 *
 * @author Duane Mentze
 */
public interface Term {

    /**
     * Returns the label for this term.
     *
     * @return term label
     */
    public TermLabel typeLabel();

    /**
     * Returns the list of intervals for this term. The intervals are guaranteed
     * to be ordered from smallest to largest, based upon starting value. The
     * value of each interval is to be interpreted within the context of this
     * term.
     *
     * @return list of intervals
     */
    public List<? extends Interval> getIntervals();

    /**
     * Returns whether or not this term is empty. An empty term is a term which
     * has no intervals specified.
     *
     * @return whether the term is empty
     */
    public boolean isEmpty();

}
