/*
 * aoeu and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class NicProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NicProvider.class);

    @Override public Collection<? extends RpcService> getImplementations() {
        return null;
    }

    @Override public Collection<? extends ProviderFunctionality> getFunctionality() {
        return null;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("NicProvider Session Initiated");
    }

    @Override public void onSessionInitialized(
            BindingAwareBroker.ConsumerContext session) {

    }

    @Override
    public void close() throws Exception {
        LOG.info("NicProvider Closed");
    }

}
