/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.nic.graph.api.TermLabel;

public class IpProtoTermType extends TermTypeBase {
    private static final int IP_PROTO_MIN = 0;
    private static final int IP_PROTO_MAX = 255;
    private static final IpProtoTermType INSTANCE = new IpProtoTermType();

    public static IpProtoTermType getInstance() {
        return INSTANCE;
    }

    private IpProtoTermType() {
        super(new TermLabel("IP_PROTO"), IP_PROTO_MIN, IP_PROTO_MAX);
    }
}
