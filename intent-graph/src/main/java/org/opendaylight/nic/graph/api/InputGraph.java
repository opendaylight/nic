/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import java.util.Set;

public interface InputGraph {
    /* The input graph is created with MD-SAL Binding*/
    Nodes src();

    Nodes dst();

    Set<Edges> action();
}

