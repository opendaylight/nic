/*
 * Copyright (c) 2015 Inocybe Technologies, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511;

import org.opendaylight.nic.gbp.renderer.impl.GBPRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBPRendererModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511.AbstractGBPRendererModule {

    private static final Logger LOG = LoggerFactory.getLogger(GBPRendererModule.class);

    public GBPRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public GBPRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511.GBPRendererModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("GBP Renderer createInstance()");
        final GBPRenderer renderer = new GBPRenderer(getDataBrokerDependency());
        renderer.init();

        final class CloseResources implements AutoCloseable {
            @Override
            public void close() throws Exception {
                if (renderer != null) {
                    renderer.close();
                }
                LOG.info("GBP Renderer (instance {}) torn down.", this);
            }
        }
        return new CloseResources();
    }
}
