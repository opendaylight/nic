/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.nic.graph.api.Interval;
import org.opendaylight.nic.graph.api.TermLabel;
import org.opendaylight.nic.graph.api.TermType;

/**
 * Base class for creating {@link TermType}s which are registered for use in
 * {@link ClassifierImpl}s.
 *
 */
public abstract class TermTypeBase implements TermType {
    private final TermLabel label;
    private final int min;
    private final int max;

    @Override
    public int min() {
        return min;
    }

    @Override
    public int max() {
        return max;
    }

    public TermTypeBase(TermLabel label, int min, int max) {
        super();
        this.label = new TermLabel(label.toString());
        this.min = min;
        this.max = max;
    }

    @Override
    public TermLabel label() {
        return new TermLabel(label.toString());
    }

    @Override
    public boolean isLegal(Interval interval) {
        return ((interval.start() >= min()) && (interval.end() <= max()));
    }

    @Override
    public boolean isMax(Interval interval) {
        return ((interval.start() == min()) && (interval.end() == max()));
    }

    @Override
    public String toString() {
        return "TermType { label=" + label + "}";
    }
}
