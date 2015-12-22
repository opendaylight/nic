/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.nic.graph.api.Term;
import org.opendaylight.nic.graph.api.TermLabel;
import org.opendaylight.nic.graph.api.TermType;

import java.util.*;

public final class TermImpl implements Term {
    private final TermType type;
    private final List<IntervalImpl> intervals;

    @Override
    public TermLabel typeLabel() {
        return type.label();
    }

    @Override
    public List<IntervalImpl> getIntervals() {
        return intervals;
    }

    @Override
    public boolean isEmpty() {
        return (intervals.isEmpty());
    }

    /**
     * @param tt term
     * @return term
     */
    public static TermImpl getInstance(TermType tt) {
        return new TermImpl(tt);
    }

    /**
     * @param type term type
     * @param interval interval type
     * @return term
     */
    public static TermImpl getInstance(TermType type, IntervalImpl interval) {
        if (interval.isNull() || !type.isLegal(interval)) {
            return getInstance(type);
        }
        return new TermImpl(type, interval, false);
    }

    /**
     * @param type term type
     * @return term
     */
    public static TermImpl getInstanceMax(TermType type) {
        IntervalImpl in = IntervalImpl.getInstance(type.min(), type.max());
        return new TermImpl(type, in, true);
    }

    /**
     * @param type term type
     * @param intervalCollection collection of interval
     * @return term
     */
    public static TermImpl getInstance(TermType type,
                                       Collection<IntervalImpl> intervalCollection) {
        return new TermImpl(type, intervalCollection);
    }

    /**
     * @return term
     */
    TermType getType() {
        return type;
    }

    /**
     * @param in interval
     * @param allowMax boolean
     */
    private void addInterval(IntervalImpl in, boolean allowMax) {
        if (type.isMax(in)) {
            if (allowMax) {
                intervals.add(in);
            }
            return;
        }
        intervals.add(in);
    }

    /**
     * @param type term type
     * @param interval interval
     * @param allowMax boolean
     */
    private TermImpl(TermType type, IntervalImpl interval, boolean allowMax) {
        this.type = type;
        intervals = new LinkedList<IntervalImpl>();
        addInterval(interval, allowMax);
    }

    /**
     * @param type term type
     */
    private TermImpl(TermType type) {
        this.type = type;
        intervals = new LinkedList<IntervalImpl>();
    }

    /**
     * @param type term type
     * @param intervalCollection interval
     */
    public TermImpl(TermType type, Collection<IntervalImpl> intervalCollection) {
        this.type = type;
        intervals = new LinkedList<IntervalImpl>();
        for (IntervalImpl in : intervalCollection) {
            if (type.isLegal(in)) {
                addInterval(in, false);
            }
        }
        IntervalImpl.sortAndCombine(intervals);
    }

    @Override
    public String toString() {
        return "Term {type:" + type + intervals + "}";
    }

    /**
     * Method performs operator "greater than" logic: this "greater than" other.
     * <p>
     * Returns True if the {@link TermTypeImpl}s are the same and every
     * {@link IntervalImpl} in other are contained by Intervals in this. Terms
     * are sorted and combined, so no overlaps or adjacent Intervals exist.
     *
     */
    public boolean greaterThan(TermImpl other) {
        if (this.type != other.getType()) {
            return false;
        }
        for (IntervalImpl otherI : other.getIntervals()) {
            boolean found = false;
            for (IntervalImpl in : intervals) {
                if (in.greaterThan(otherI)) {
                    found = true;
                    continue;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;

    }

    /**
     * Method performs operator "less than" logic: this "less than" other.
     * <p>
     * Returns True if the {@link TermTypeImpl}s are the same and every
     * {@link IntervalImpl} in this are contained by Intervals in other. Terms
     * are sorted and combined, so no overlaps or adjacent Intervals exist.
     * @param other term
     * @return boolean
     *
     */
    public boolean lessThan(TermImpl other) {
        return other.greaterThan(this);
    }

    /**
     * The <em>add</em> operator is union of this and other Term.
     * <p>
     * Performs union of the list of {@link IntervalImpl}s.
     * @param other term
     * @return term
     */
    public TermImpl add(TermImpl other) {
        if (!(this.type == other.getType())) {
            return this;
        }
        Collection<IntervalImpl> co = new HashSet<IntervalImpl>();
        co.addAll(intervals);
        co.addAll(other.getIntervals());
        return getInstance(this.type, co);
    }

    /**
     * The <em>and</em> operator is intersection of this and the other Term.
     * <p>
     * Performs and of the list of {@link IntervalImpl}s.
     * @param other term
     * @return term
     */
    public TermImpl and(TermImpl other) {
        if (!(this.type == other.getType())) {
            return getInstance(this.type);
        }
        Collection<IntervalImpl> co = new HashSet<IntervalImpl>();
        for (IntervalImpl in : this.intervals) {
            for (IntervalImpl in2 : other.getIntervals()) {
                IntervalImpl overlap = in.and(in2);
                if (!overlap.isNull()) {
                    co.add(overlap);
                }
            }
        }
        return getInstance(this.type, co);
    }

    /**
     * The <em>sub</em> operator is intersection of this and the other Term.
     * <p>
     * Performs and of the list of {@link IntervalImpl}s.
     * @param other term
     * @return term
     */
    public TermImpl sub(TermImpl other) {
        if (!(this.type == other.getType())) {
            return this;
        }

        // this holds intervals which are resolved
        // since other is sorted, once the 'o' is past a 'w', the 'w' is
        // resolved
        Collection<IntervalImpl> resolved = new HashSet<IntervalImpl>();

        Collection<IntervalImpl> unresolved = new HashSet<IntervalImpl>(
                this.getIntervals());

        // loop across the Intervals to subtract 'o'
        for (IntervalImpl in : other.getIntervals()) {

            // base the working set off of the current result
            List<IntervalImpl> workingSet = new LinkedList<IntervalImpl>(
                    unresolved);
            Collections.sort(workingSet);
            unresolved.clear();

            // loop over intervals being subtracted from 'w'
            int windex = 0; // used to efficiently calculate sublists
            for (IntervalImpl wo : workingSet) {
                // if o is past w, move w to resolved
                if (wo.end() < in.start()) {
                    resolved.add(wo);
                    windex++;
                    continue; // continue the current 'o', move to next 'w'
                }

                // if the subtracted Interval is before the working set
                // Interval,
                // then move the rest of working set unresolved and move to the
                // next 'o'
                if (wo.start() > in.end()) {
                    unresolved.addAll(workingSet.subList(windex,
                            workingSet.size()));
                    break;
                }

                // 'o' is not past or before 'w', so subtract them
                List<IntervalImpl> newIntervalList = wo.sub(in);
                if (!newIntervalList.get(0).isNull()) {
                    unresolved.addAll(newIntervalList);
                }

                windex++; // move to next 'w'
            }
        }
        resolved.addAll(unresolved);
        return getInstance(this.type, resolved);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((intervals == null) ? 0 : intervals.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    public boolean notEquals(Object obj) {
        return !equals(obj);
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
        TermImpl other = (TermImpl) obj;
        if (intervals == null) {
            if (other.intervals != null) {
                return false;
            }
        } else if (!intervals.equals(other.intervals)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
