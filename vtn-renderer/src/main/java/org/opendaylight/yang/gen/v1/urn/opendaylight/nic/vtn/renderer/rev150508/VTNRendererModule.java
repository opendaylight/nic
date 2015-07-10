/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.nic.vtn.renderer.VTNRenderer;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;

public class VTNRendererModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508.AbstractVTNRendererModule {
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
        DataBroker broker = getDataBrokerDependency();

        LogicalDatastoreType store = LogicalDatastoreType.CONFIGURATION;
        InstanceIdentifier<Intents> path = InstanceIdentifier.builder(Intents.class).build();
        VTNRenderer renderer = new VTNRenderer();
        DataChangeScope scope = DataChangeScope.SUBTREE;
        broker.registerDataChangeListener(store, path, renderer, scope);

        return renderer;
    }
}
