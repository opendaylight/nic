/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer.flow;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

public interface OFRendererFlowService {

    public void pushL2Flow(NodeId nodeId, List<String> endPointGroups,
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action action,
            FlowAction flowAction);
}
