/*
 * Copyright 2015, Inocybe Technologies
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer;

import java.util.Map;
import java.util.Map.Entry;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBPRenderer implements DataChangeListener,
    AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(GBPRenderer.class);


    @Override
    public void close() throws Exception {

    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        //Pull the list of new items
        Map<InstanceIdentifier<?>, DataObject> createdObjects = changes.getCreatedData();

        for (Entry<InstanceIdentifier<?>, DataObject> created : createdObjects.entrySet()) {
            if (created.getValue() != null
                    && created.getValue() instanceof Intent) {

            }
        }
    }

}
