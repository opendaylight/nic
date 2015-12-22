/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

public interface TermType {
    /** identification of TermType */
    public TermLabel label();

    /** minimum value of the TermType */
    public int min();

    /** maximum value of the TermTYpe */
    public int max();

    /** determines if interval is legal for TermType */
    public boolean isLegal(Interval interval);

    /** returns an interval with the maximum interval for the TermTYpe */
    public boolean isMax(Interval interval);
}
