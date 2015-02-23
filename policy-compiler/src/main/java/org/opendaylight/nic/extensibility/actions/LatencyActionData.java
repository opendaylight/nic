//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.actions;

/**
 * The auxiliary data used in translating a {@link LatencyActionType}.
 *
 * @author Shaun Wackerly
 */
@SuppressWarnings("serial")
public class LatencyActionData extends BaseAuxiliaryData {

    private final Integer dscp;
    public static final String DSCP_KEY = "dscp";

    public LatencyActionData(Integer dscp) {
        super();
        this.put(DSCP_KEY, dscp.toString());
        this.dscp = dscp;
    }

    /**
     * Returns the DSCP value to assign for latency.
     *
     * @return DSCP value
     */
    public Integer dscp() {
        return dscp;
    }

}
