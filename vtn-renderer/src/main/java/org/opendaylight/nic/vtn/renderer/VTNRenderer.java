/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;

import org.opendaylight.vtn.manager.IVTNManager;
import org.opendaylight.vtn.manager.VTenant;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VTNRenderer implements AutoCloseable, DataChangeListener {
    /**
     * Logger instance.
     */
    private static final Logger  LOG =
        LoggerFactory.getLogger(VTNRenderer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> ev) {
        // TODO: Change the log level to TRACE.
        LOG.info("Intent configuration changed.");

        IVTNManager mgr = getVTNManager();

        /**
         *  Delete the following lines, and implements the renderer.
         */
        try {
            List<VTenant> vtns = mgr.getTenants();
            for (VTenant vtn: vtns) {
                LOG.info("{}", vtn);
            }
        } catch (Exception e) {
            LOG.info("Failed to get tenants: {}", e);
        }
    }

    /**
     * Return the VTN manager service.
     *
     * @return  The VTN manager service associated with the specified
     *          container.
     */
    protected IVTNManager getVTNManager() {
        IVTNManager mgr = (IVTNManager)ServiceHelper.
            getInstance(IVTNManager.class, "default", this);
        return mgr;
    }
}
