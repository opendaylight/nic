//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.Interval;
import org.opendaylight.nic.compiler.api.TermLabel;
import org.opendaylight.nic.compiler.api.TermType;

/*
 * An enumeration of common term types, including the maximum and minimum values
 * for each type.
 *
 */
public enum TermTypeImpl implements TermType {

    /** VLAN */
    VLAN(0, 4095),

    /** Ethernet type */
    ETH_TYPE(0, 65535),

    /** IP Protocol. */
    IP_PROTO(0, 255),

    /** L4 source port. */
    L4_SRC(0, 65535),

    /** L4 destination port. */
    L4_DST(0, 65535),

    /** UDP source port. */
    UDP_SRC(0, 65535),

    /** UDP destination port. */
    UDP_DST(0, 65535),

    /** TCP source port. */
    TCP_SRC(0, 65535),

    /** TCP destination port. */
    TCP_DST(0, 65535);

    private final IntervalImpl interval;

    private TermTypeImpl(int min, int max) {
        interval = IntervalImpl.getInstance(min, max);
    }

    @Override
    public int min() {
        return interval.start();
    }

    @Override
    public int max() {
        return interval.end();
    }

    @Override
    public TermLabel label() {
        return new TermLabel(toString());
    }

    @Override
    public boolean isLegal(Interval interval) {
        return ((interval.start() >= min()) && (interval.end() <= max()));
    }

    @Override
    public boolean isMax(Interval interval) {
        return ((interval.start() == min()) && (interval.end() == max()));
    }

}
