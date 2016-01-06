/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.nic.graph.api.TermLabel;

public class L4SrcTermType extends TermTypeBase {
    private static final int L4_SRC_MIN = 0;
    private static final int L4_SRC_MAX = 65535;
    private static final L4SrcTermType INSTANCE = new L4SrcTermType();

    public static L4SrcTermType getInstance() {
        return INSTANCE;
    }

    private L4SrcTermType() {
        super(new TermLabel("L4_SRC"), L4_SRC_MIN, L4_SRC_MAX);
    }
}
