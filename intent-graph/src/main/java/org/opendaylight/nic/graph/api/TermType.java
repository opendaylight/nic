/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

public interface TermType {
    /** identification of TermType
     *  @return Termlabel
     */
    public TermLabel label();

    /** minimum value of the TermType
     *  @return minimum
     */
    public int min();

    /** maximum value of the TermTYpe
     * @return maximum */
    public int max();

    /** determines if interval is legal for TermType
     * @return boolean*/
    public boolean isLegal(Interval interval);

    /** returns an interval with the maximum interval for the TermType
     * @return boolean */
    public boolean isMax(Interval interval);
}
