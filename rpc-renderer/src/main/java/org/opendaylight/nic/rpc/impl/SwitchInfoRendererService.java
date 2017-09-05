/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.rpc.rest.JuniperRestService;
import org.opendaylight.nic.rpc.utils.InstanceIdentifierUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 28/08/17.
 */
public class SwitchInfoRendererService extends AbstractRendererService<SwitchInfos> {
    private static final Logger LOG = LoggerFactory.getLogger(SwitchInfoRendererService.class);

    public SwitchInfoRendererService(final DataBroker dataBroker, final JuniperRestService juniperRestService) {
        super(dataBroker, juniperRestService);
    }

    @Override
    public void start() {
        LOG.info("\nSwitchInfos listener started with success!");
        final DataTreeIdentifier identifier = new DataTreeIdentifier(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.SWITCH_INFOS_IDENTIFIER);
        super.registerListener(identifier);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<SwitchInfos>> collection) {
        collection.forEach(tree -> {
            final DataObjectModification<SwitchInfos> modification = tree.getRootNode();
            switch (modification.getModificationType()) {
                case WRITE:
                    final SwitchInfos switchInfos = modification.getDataAfter();
                    super.getJuniperRestService().sendConfiguration(switchInfos.getSwitchInfo(), false);
                    break;
                case SUBTREE_MODIFIED:
                    final SwitchInfos before = modification.getDataBefore();
                    final SwitchInfos after = modification.getDataAfter();

                    before.getSwitchInfo().removeAll(after.getSwitchInfo());
                    super.getJuniperRestService().sendConfiguration(before.getSwitchInfo(), false);
                    break;
                case DELETE:
                    //TODO: Provide a default behavior when the Tree were removed
                    break;
            }
        });
    }

    @Override
    public void stop() {
        super.close();
    }
}
