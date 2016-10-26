/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.util;

import org.opendaylight.nic.listeners.api.GraphEdgeAdded;
import org.opendaylight.nic.model.RendererAction;
import org.opendaylight.nic.model.RendererCommon;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.EdgeTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;

/**
 * Created by yrineu on 26/10/16.
 */
public class CommonUtils {

    public static RendererCommon convertEdge(final GraphEdgeAdded edgeAdded) {
        final Edges edges = edgeAdded.getEdge();
        final String src = edges.getSrcNode();
        final String dst = edges.getDstNode();
        final EdgeTypes edgeTypes = edges.getType();

        final MacAddress srcMacAddress = MacAddress.getDefaultInstance(src);
        final MacAddress dstMacAddress = MacAddress.getDefaultInstance(dst);
        return new RendererCommon(srcMacAddress, dstMacAddress, getRendererAction(edgeTypes));
    };

    private static RendererAction getRendererAction(final EdgeTypes edgeTypes) {
        RendererAction result = RendererAction.DENY;
        if(EdgeTypes.CanAllow.equals(edgeTypes)) {
            result = RendererAction.ALLOW;
        }
        return result;
    }
}
