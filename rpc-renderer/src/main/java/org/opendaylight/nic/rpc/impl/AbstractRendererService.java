/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.nic.rpc.api.RPCRendererService;
import org.opendaylight.nic.rpc.rest.JuniperRestService;
import org.opendaylight.nic.rpc.utils.RPCRendererUtils;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Created by yrineu on 28/08/17.
 */
public abstract class AbstractRendererService <T extends DataObject> implements RPCRendererService<T>{

    private final DataBroker dataBroker;
    private final JuniperRestService juniperRestService;
    private final RPCRendererUtils rendererUtils;

    private ListenerRegistration registration;

    AbstractRendererService(final DataBroker dataBroker,
                            final JuniperRestService juniperRestService) {
        this.dataBroker = dataBroker;
        this.juniperRestService = juniperRestService;
        this.rendererUtils = new RPCRendererUtils(dataBroker);
    }

    void registerListener(final DataTreeIdentifier<T> treeIdentifier) {
        this.registration = dataBroker.registerDataTreeChangeListener(treeIdentifier, this);
    }

    void close() {
        this.registration.close();
    }

    public JuniperRestService getJuniperRestService() {
        return juniperRestService;
    }
}
