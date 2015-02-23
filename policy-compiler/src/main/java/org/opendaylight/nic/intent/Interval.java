//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;


/**
 * An interval is the inclusive range of values denoted by its start and end values.
 * The start value is always guaranteed to be lower/less than or equal to the end
 * value. An interval where the start value is equal to the end value indicates
 * an interval which consists of a single value.
 *
 * @author Duane Mentze
 */
public interface Interval {

    /**
     * Returns the inclusive start value of this interval. The start value
     * is guaranteed to be equal to or less than the end value.
     *
     * @return start value
     */
	int start();

	/**
	 * Returns the inclusive end value of this interval. The end value is
	 * guaranteed to be equal to or greater than the start value.
	 *
	 * @return end value
	 */
	int end();

}
