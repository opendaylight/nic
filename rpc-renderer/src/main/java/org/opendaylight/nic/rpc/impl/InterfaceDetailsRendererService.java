/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.impl;

import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.rpc.api.RPCRendererService;
import org.opendaylight.nic.rpc.rest.JuniperRestService;
import org.opendaylight.nic.rpc.utils.InstanceIdentifierUtils;
import org.opendaylight.nic.rpc.utils.RPCRendererUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811.SwitchInterfaceDetail;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811.SwitchInterfacesStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch._interface.status.SwitchInterfaceDetails;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 25/08/17.
 */
public class InterfaceDetailsRendererService implements RPCRendererService<SwitchInterfacesStatus> {

    private static final Logger LOG = LoggerFactory.getLogger(InterfaceDetailsRendererService.class);
    private final DataBroker dataBroker;
    private final JuniperRestService juniperRestService;
    private final RPCRendererUtils rendererUtils;

    private ListenerRegistration registration;

    public InterfaceDetailsRendererService(final DataBroker dataBroker,
                                           final JuniperRestService juniperRestService) {
        this.dataBroker = dataBroker;
        this.juniperRestService = juniperRestService;
        this.rendererUtils = new RPCRendererUtils(dataBroker);
    }

    @Override
    public void start() {
        LOG.info("\nInterfaceDetailsRendererService initialized with success");
        final DataTreeIdentifier dataTreeIdentifier = new DataTreeIdentifier(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.SWITCH_INTERFACES_STATUS_IDENTIFIER);
        registration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<SwitchInterfacesStatus>> collection) {
        collection.forEach(tree -> {
            final DataObjectModification<SwitchInterfacesStatus> objectModification = tree.getRootNode();
            switch (objectModification.getModificationType()) {
                case WRITE:
                    final SwitchInterfacesStatus interfacesStaus = objectModification.getDataAfter();
                    juniperRestService.sendConfiguration(interfacesStaus.getSwitchInterfaceStatus(), false);
//                    LOG.info("\n### Subtree write: {}", objectModification.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    final SwitchInterfacesStatus before = objectModification.getDataBefore();
                    final SwitchInterfacesStatus after = objectModification.getDataAfter();

                    before.getSwitchInterfaceStatus().addAll(after.getSwitchInterfaceStatus());
                    juniperRestService.sendConfiguration(before.getSwitchInterfaceStatus(), false);
//                    LOG.info("\n### Subtree modified - Before: {}, After {}", objectModification.getDataBefore(), objectModification.getDataAfter());
                    break;
                case DELETE:
                    break;
            }
        });
    }

    @Override
    public void stop() {
        registration.close();
    }
}
