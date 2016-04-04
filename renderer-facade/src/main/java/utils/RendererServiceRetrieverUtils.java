/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package utils;

import manager.OFRendererFacadeServiceManager;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Created by yrineu on 30/03/16.
 */
public class RendererServiceRetrieverUtils {

    private static final BundleContext context = FrameworkUtil
            .getBundle(OFRendererFacadeServiceManager.class).getBundleContext();
    private static final ServiceReference<?> ofServiceReference = context.
            getServiceReference(OFRendererFlowService.class);

    public static OFRendererFlowService getOFRendererService() {
        OFRendererFlowService flowService = (OFRendererFlowService) context.
                getService(ofServiceReference);
        return flowService;
    }
}
