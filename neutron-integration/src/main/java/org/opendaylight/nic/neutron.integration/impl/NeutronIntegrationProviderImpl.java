/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.neutron.integration.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.listeners.api.EventType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Provider implementation for integrating Neutron sec groups
 */
public class NeutronIntegrationProviderImpl implements AutoCloseable {

    private final DataBroker db;

    private EventRegistryService serviceRegistry = null;

    /**
     * Provider constructor set all needed final parameters
     *
     * @param db - dataBroker
     */
    public NeutronIntegrationProviderImpl(final DataBroker db) {
        Preconditions.checkNotNull(db);
        this.db = db;
    }

    public void start() {
        // Retrieve reference for Event Registry service
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(EventRegistryService.class);
        serviceRegistry = (EventRegistryService) context.
                getService(serviceReference);

        // Neutron Security group and rules event listeners
        SecGroupNotificationSubscriberImpl secGroupNotificationSubscriber =
                new SecGroupNotificationSubscriberImpl();
        // There is only one service associated with Sec group Added, Deleted and Modified
        // So registering the subscriber with just Added type will associate it with the correct supplier
        // for Deleted and Modified
        serviceRegistry.registerEventListener(EventType.SECURITY_GROUP_ADDED, secGroupNotificationSubscriber);
        SecRuleNotificationSubscriberImpl secRuleNotificationSubscriber =
                new SecRuleNotificationSubscriberImpl();
        serviceRegistry.registerEventListener(EventType.SECURITY_RULE_ADDED, secRuleNotificationSubscriber);
    }

    @Override
    public void close() throws Exception {
    }
}

