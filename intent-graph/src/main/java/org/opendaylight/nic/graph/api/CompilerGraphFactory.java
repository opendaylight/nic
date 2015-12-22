/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

import org.opendaylight.nic.graph.impl.CompilerGraphImpl;
import org.opendaylight.nic.mapping.api.IntentMappingService;

public final class CompilerGraphFactory {

    protected static IntentMappingService intentMappingService;

    private CompilerGraphFactory() {
    }

    public static CompilerGraph createGraphCompiler() {
        return new CompilerGraphImpl(intentMappingService);
    }
}
