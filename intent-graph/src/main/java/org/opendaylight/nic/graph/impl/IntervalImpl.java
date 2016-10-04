/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.nic.graph.api.Interval;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class IntervalImpl implements Interval, Comparable<IntervalImpl> {
    private final int start;
    private final int end;

    public static final IntervalImpl INTERVAL_NULL = new IntervalImpl(
            Integer.MIN_VALUE, Integer.MIN_VALUE);

    @Override
    public int start() {
        return start;
    }

    @Override
    public int end() {
        return end;
    }

    /**
     * @param start start of instance
     * @param end end of instance
     * @return Interval
     */
    public static IntervalImpl getInstance(int start, int end) {
        if (start > end) {
            return INTERVAL_NULL;
        }
        return new IntervalImpl(start, end);
    }

    /**
     * @param value value of instance
     * @return Interval
     */
    public static IntervalImpl getInstance(int value) {
        return new IntervalImpl(value, value);
    }

    /**
     * @param start start of interval
     * @param end end of interval
     */
    protected IntervalImpl(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * is Null
     * @return boolean
     */
    public boolean isNull() {
        return this == INTERVAL_NULL;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Interval { start:");
        // String NEW_LINE = System.getProperty("line.separator");
        result.append(start);
        result.append(", end:");
        result.append(end);
        result.append(" }");
        return result.toString();
    }

    /**
     * is Null
     * @param obj Any object
     * @return boolean
     */
    public boolean notEquals(Object obj) {
        return !equals(obj);
    }

    /**
     * self "greater than" other: other must be a subset of self
     * @param other interval
     * @return boolean
     */
    public boolean greaterThan(IntervalImpl other) {
        if ((this.start < other.start) && (this.end >= other.end)) {
            return true;
        }
        if ((this.start <= other.start) && (this.end > other.end)) {
            return true;
        }
        return false;
    }

    // Removing the symbols from the comments to remove Javadoc errors.
    /**
     * self "less than" other: self must be a subset of other
     * @param other interval
     * @return T or F
     */
    public boolean lessThan(IntervalImpl other) {
        return other.greaterThan(this);
    }

    /**
     * self "greater than or equal to" other: other must be a subset or equal to self
     * @param other interval
     * @return T or F
     */
    public boolean greaterThanOrEqual(IntervalImpl other) {
        if ((this.start <= other.start) && (this.end >= other.end)) {
            return true;
        }
        return false;
    }

    /**
     * self "less than or equal to" other: self must be a subset or equal to other
     * @param other interval
     * @return T or F
     */
    public boolean lessThanOrEqual(IntervalImpl other) {
        return other.greaterThanOrEqual(this);
    }

    /**
     * self - other: this is the difference operator for Intervals.
     *
     * @param other interval
     * @return List of Intervals, since operator may result in 0 or more
     *         intervals
     */
    public List<IntervalImpl> sub(IntervalImpl other) {

        IntervalImpl overlap = and(other);
        if (overlap == INTERVAL_NULL) {
            // no overlap so nothing is subtracted
            return createList(this);
        }

        if (this.equals(overlap)) {
            // complete overlap so return list with null interval
            return List_NULL;
        } else if (start() == overlap.start()) {
            // overlap exists
            // Check if start or end of overlap is the same as this Interval.
            // If not then make 2 new Intervals.

            // overlap at start
            return createList(new IntervalImpl(overlap.end() + 1, end()));
        } else if (end() == overlap.end()) {
            // overlap at end
            return createList(new IntervalImpl(start(), overlap.start() - 1));
        } else {
            // overlap inside of this Interval
            IntervalImpl i1 = new IntervalImpl(start(), overlap.start() - 1);
            IntervalImpl i2 = new IntervalImpl(overlap.end() + 1, end());
            return createList(i1, i2);
        }
    }

    /**
     * <em>and</em> operator. Finds the overlap between two intervals.
     *
     * @param other Interval
     * @return an Interval containing the overlap
     */
    public IntervalImpl and(IntervalImpl other) {
        int start = Math.max(this.start, other.start);
        int end = Math.min(this.end, other.end);

        if (start <= end) {
            return new IntervalImpl(start, end);
        }
        return INTERVAL_NULL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
        return result;
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
        IntervalImpl other = (IntervalImpl) obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }

    // @Override
    @Override
    public int compareTo(IntervalImpl in) {
        if (this.start < in.start()) {
            return -1;
        }
        if (this.start > in.start()) {
            return 1;
        }
        return 0;
    }

    /**
     * This member function sorts and combines a list of Intervals.
     * <p>
     * Intervals which overlap or are adjacent are combined into single
     * intervals. The list is sorted by the start of each Interval.
     * @param intervals List of invertals
     */
    static void sortAndCombine(List<IntervalImpl> intervals) {

        if (intervals.isEmpty()) {
            return;
        }
        if (intervals.size() == 1) {
            if (intervals.get(0) == INTERVAL_NULL) {
                intervals.clear();
            }
            return;
        }

        Collections.sort(intervals);

        IntervalImpl i1 = INTERVAL_NULL;
        IntervalImpl i2 = INTERVAL_NULL;

        List<IntervalImpl> result = new LinkedList<IntervalImpl>();

        // loop through the intervals
        while (intervals.size() > 1) {
            i1 = intervals.remove(0); // remove current interval
            i2 = intervals.get(0); // get reference to next interval

            if (i1 == INTERVAL_NULL) {
                continue;
            }

            if (i1.end() >= i2.start()) {
                // overlap case, update i2 and dispose of i1
                i2 = getInstance(i1.start(), Math.max(i1.end(), i2.end()));
                intervals.set(0, i2);
            } else if (i1.end() == i2.start() - 1) {
                // adjacent case, update i2 and dispose of i1
                i2 = getInstance(i1.start(), i2.end());
                intervals.set(0, i2);
            } else {
                // no overlap or adjacency, add i1 to result
                result.add(i1);
            }
        }

        if (i2 != INTERVAL_NULL) {
            result.add(i2);
        }
        intervals.clear();
        intervals.addAll(result);
    }

    private static final List<IntervalImpl> List_NULL = new ArrayList<IntervalImpl>() {
        {
            add(INTERVAL_NULL);
        }
    };

    private static List<IntervalImpl> createList(IntervalImpl interval) {
        List<IntervalImpl> list = new ArrayList<IntervalImpl>();
        list.add(interval);
        return list;
    }

    private static List<IntervalImpl> createList(IntervalImpl i1,
                                                 IntervalImpl i2) {
        List<IntervalImpl> list = new ArrayList<IntervalImpl>();
        list.add(i1);
        list.add(i2);
        return list;
    }
}
