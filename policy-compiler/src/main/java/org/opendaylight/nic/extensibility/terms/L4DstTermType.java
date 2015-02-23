//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.terms;

import org.opendaylight.nic.extensibility.TermLabel;

/**
 * Matches traffic with a particular L4 destination port value.
 *
 * @author Duane Mentze
 * @author Shaun Wackerly
 */
public class L4DstTermType extends TermTypeBase {

    private final static int L4_DST_MIN = 0;
    private final static int L4_DST_MAX = 65535;
    private final static L4DstTermType INSTANCE = new L4DstTermType();

    public static L4DstTermType getInstance() {
        return INSTANCE;
    }

    private L4DstTermType() {
        super(new TermLabel("L4_DST"), L4_DST_MIN, L4_DST_MAX);
    }

}
