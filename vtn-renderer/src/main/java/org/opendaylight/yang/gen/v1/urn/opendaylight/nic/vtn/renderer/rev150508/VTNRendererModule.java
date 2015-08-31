/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508;

import org.opendaylight.nic.vtn.renderer.VTNRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VTNRendererModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508.AbstractVTNRendererModule {

    private static final Logger LOG = LoggerFactory.getLogger(VTNRendererModule.class);

    public VTNRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public VTNRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508.VTNRendererModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("VTN Renderer createInstance()");
        final VTNRenderer renderer = new VTNRenderer(getDataBrokerDependency());

        final class CloseResources implements AutoCloseable {
            @Override
            public void close() throws Exception {
                if (renderer != null) {
                    renderer.close();
                }
                LOG.info("VTN Renderer (instance {}) torn down.", this);
            }
        }
        return new CloseResources();
    }
}
