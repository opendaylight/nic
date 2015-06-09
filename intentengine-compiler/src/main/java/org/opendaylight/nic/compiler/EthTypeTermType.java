//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.TermLabel;

/**
 * Matches traffic with a particular ethernet type.
 */
public class EthTypeTermType extends TermTypeBase {

    private final static int ETH_TYPE_MIN = 0;
    private final static int ETH_TYPE_MAX = 65535;
    private final static EthTypeTermType INSTANCE = new EthTypeTermType();

    public static EthTypeTermType getInstance() {
        return INSTANCE;
    }

    private EthTypeTermType() {
        super(new TermLabel("ETH_TYPE"), ETH_TYPE_MIN, ETH_TYPE_MAX);
    }

}
