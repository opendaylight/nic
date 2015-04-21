//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermType;
import org.opendaylight.nic.intent.Term;

/**
 * An implementation of {@link Term} which implifies the use within the policy
 * compiler.
 *
 * @author Duane Mentze
 *
 */
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

    public static TermImpl getInstance(TermType tt) {
        return new TermImpl(tt);
    }

    public static TermImpl getInstance(TermType type, IntervalImpl interval) {
        if (interval.isNull() || !type.isLegal(interval)) {
            return getInstance(type);
        }
        return new TermImpl(type, interval, false);
    }

    public static TermImpl getInstanceMax(TermType type) {
        IntervalImpl i = IntervalImpl.getInstance(type.min(), type.max());
        return new TermImpl(type, i, true);
    }

    public static TermImpl getInstance(TermType type,
            Collection<IntervalImpl> intervalCollection) {
        return new TermImpl(type, intervalCollection);
    }

    TermType getType() {
        return type;
    }

    private void addInterval(IntervalImpl i, boolean allowMax) {
        if (type.isMax(i)) {
            if (allowMax) {
                intervals.add(i);
            }
            return;
        }
        intervals.add(i);
    }

    private TermImpl(TermType type, IntervalImpl interval, boolean allowMax) {
        this.type = type;
        intervals = new LinkedList<IntervalImpl>();
        addInterval(interval, allowMax);
    }

    private TermImpl(TermType type) {
        this.type = type;
        intervals = new LinkedList<IntervalImpl>();
    }

    private TermImpl(TermType type, Collection<IntervalImpl> intervalCollection) {
        this.type = type;
        intervals = new LinkedList<IntervalImpl>();
        for (IntervalImpl i : intervalCollection) {
            if (type.isLegal(i)) {
                addInterval(i, false);
            }
        }
        IntervalImpl.sortAndCombine(intervals);
    }

    @Override
    public String toString() {
        return "Term {type:" + type + intervals + "}";
    }

    /**
     * Method performs operator > logic: this > other.
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
            for (IntervalImpl i : intervals) {
                if (i.greaterThan(otherI)) {
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
     * Method performs operator < logic: this < other.
     * <p>
     * Returns True if the {@link TermTypeImpl}s are the same and every
     * {@link IntervalImpl} in this are contained by Intervals in other. Terms
     * are sorted and combined, so no overlaps or adjacent Intervals exist.
     *
     */
    public boolean lessThan(TermImpl other) {
        return other.greaterThan(this);
    }

    /**
     * The <em>add</em> operator is union of this and other Term.
     * <p>
     * Performs union of the list of {@link IntervalImpl}s.
     */
    public TermImpl add(TermImpl other) {
        if (!(this.type == other.getType())) {
            return this;
        }
        Collection<IntervalImpl> c = new HashSet<IntervalImpl>();
        c.addAll(intervals);
        c.addAll(other.getIntervals());
        return getInstance(this.type, c);
    }

    /**
     * The <em>and</em> operator is intersection of this and the other Term.
     * <p>
     * Performs and of the list of {@link IntervalImpl}s.
     */
    public TermImpl and(TermImpl other) {
        if (!(this.type == other.getType())) {
            return getInstance(this.type);
        }
        Collection<IntervalImpl> c = new HashSet<IntervalImpl>();
        for (IntervalImpl i : this.intervals) {
            for (IntervalImpl j : other.getIntervals()) {
                IntervalImpl overlap = i.and(j);
                if (!overlap.isNull()) {
                    c.add(overlap);
                }
            }
        }
        return getInstance(this.type, c);
    }

    /**
     * The <em>sub</em> operator is intersection of this and the other Term.
     * <p>
     * Performs and of the list of {@link IntervalImpl}s.
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
        for (IntervalImpl o : other.getIntervals()) {

            // base the working set off of the current result
            List<IntervalImpl> workingSet = new LinkedList<IntervalImpl>(
                    unresolved);
            Collections.sort(workingSet);
            unresolved.clear();

            // loop over intervals being subtracted from 'w'
            int wIndex = 0; // used to efficiently calculate sublists
            for (IntervalImpl w : workingSet) {
                // if o is past w, move w to resolved
                if (w.end() < o.start()) {
                    resolved.add(w);
                    wIndex++;
                    continue; // continue the current 'o', move to next 'w'
                }

                // if the subtracted Interval is before the working set
                // Interval,
                // then move the rest of working set unresolved and move to the
                // next 'o'
                if (w.start() > o.end()) {
                    unresolved.addAll(workingSet.subList(wIndex,
                            workingSet.size()));
                    break;
                }

                // 'o' is not past or before 'w', so subtract them
                List<IntervalImpl> newIntervalList = w.sub(o);
                if (!newIntervalList.get(0).isNull()) {
                    unresolved.addAll(newIntervalList);
                }

                wIndex++; // move to next 'w'
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
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
