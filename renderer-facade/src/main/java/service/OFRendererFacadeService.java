/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package service;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;

import java.util.List;

/**
 * Created by yrineu on 30/03/16.
 */
public interface OFRendererFacadeService {

    /**
     * Service to handle Edges after compiled Intents
     * @param intentIds
     * @param edges
     */
    void pushFlow(List<String> intentIds,
                  List<Edges> edges);

}
