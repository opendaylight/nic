//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.pm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.Domains;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.Domain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.Application;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.AppPolicy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.Policy;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifies registered listeners when changes occur in the OPERATIONAL state of
 * policies.
 *
 * @author Shaun Wackerly
 */
public class PolicyNotifier implements AutoCloseable, DataChangeListener {

    private static final Logger log = LoggerFactory
            .getLogger(PolicyNotifier.class);

    private final ListenerRegistration<DataChangeListener> dataChangeReg;
    private final Set<PolicyListener> listeners;

    /**
     * Constructs a policy manager using the given dependencies.
     *
     * @param dataBroker
     *            data broker for MD-SAL storage
     * @param rpc
     *            RPC registration
     * @param notify
     *            notification service
     */
    public PolicyNotifier(DataBroker dataBroker, RpcProviderRegistry rpc,
            NotificationProviderService notify) {
        log.debug("PolicyValidator() contructor");

        listeners = new HashSet<>(); // FIXME: Need concurrency safety

        InstanceIdentifier<Domain> path = InstanceIdentifier
                .builder(Domains.class).child(Domain.class).toInstance();
        dataChangeReg = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, path, this,
                AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    @Override
    public void close() throws Exception {
        log.debug("PolicyValidator.close()");
        dataChangeReg.close();
        // dataBroker.unregisterDataChangeListener() does not exist
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        log.debug("CHANGE: create=" + change.getCreatedData().size()
                + ", update=" + change.getUpdatedData().size() + ", remove="
                + change.getRemovedPaths().size());

        // Create an in-memory copy of the original data prior to this change.
        // Note that
        // this method is very inefficient (but is easy). It would be more
        // optimal to store
        // changes in a local cache as they occur over time.
        Map<UniqueId, Policy> original = new HashMap<>();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change
                .getOriginalData().entrySet()) {
            DataObject obj = entry.getValue();
            if (!(obj instanceof Domain))
                continue;

            Domain dom = (Domain) obj;
            for (Application app : dom.getApplication()) {
                for (AppPolicy pol : app.getAppPolicy()) {
                    UniqueId id = new UniqueId(dom.getDomainId(),
                            app.getAppId(), pol.getPolicyId());
                    original.put(id, pol.getPolicy());
                    log.debug("ORIGINAL: {}", id);
                }
            }
        }

        // Store the values we write to MD-SAL
        Set<Policy> created = new HashSet<>();
        Set<Policy> updated = new HashSet<>();
        Set<Policy> removed = new HashSet<>();

        // Handle created data
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change
                .getCreatedData().entrySet()) {
            DataObject obj = entry.getValue();
            if (!(obj instanceof Domain))
                continue;

            Domain dom = (Domain) obj;
            for (Application app : dom.getApplication()) {
                for (AppPolicy pol : app.getAppPolicy()) {
                    created.add(pol.getPolicy());
                }
            }
        }

        // Handle updated data
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change
                .getUpdatedData().entrySet()) {
            DataObject obj = entry.getValue();
            if (!(obj instanceof Domain))
                continue;

            Domain dom = (Domain) obj;
            for (Application app : dom.getApplication()) {
                for (AppPolicy pol : app.getAppPolicy()) {
                    if (pol.getPolicy() == null)
                        continue; // Deleted policies have 'null' in the updated
                                  // list

                    UniqueId id = new UniqueId(dom.getDomainId(),
                            app.getAppId(), pol.getPolicyId());
                    Policy orig = original.get(id);
                    if (orig == null) {
                        // There was originally no policy (just an ID) so this
                        // policy was created
                        created.add(pol.getPolicy());
                        continue;
                    }

                    if (pol.getPolicy().equals(orig)) {
                        log.debug(
                                "Policy {} marked as updated, but no change from original. Skipping update.",
                                id);
                        continue;
                    }

                    updated.add(pol.getPolicy());
                }
            }
        }

        // Handle removed data
        for (InstanceIdentifier<?> path : change.getRemovedPaths()) {
            if (path.getTargetType() != Policy.class) {
                log.debug("Skipping removal of target type {}",
                        path.getTargetType());
                continue;
            }

            Policy old = (Policy) change.getOriginalData().get(path);
            if (old == null) {
                log.error("Failed to find deleted policy at {}", path);
                continue;
            }

            removed.add(old);
        }

        // Get unmodifiable copies of each set
        Set<Policy> safeCreated = Collections.unmodifiableSet(created);
        Set<Policy> safeUpdated = Collections.unmodifiableSet(updated);
        Set<Policy> safeRemoved = Collections.unmodifiableSet(removed);

        // Notify listeners
        log.debug("NOTIFY_LISTENERS: create={} update={} remove={}",
                safeCreated.size(), safeUpdated.size(), safeRemoved.size());
        for (PolicyListener l : listeners) {
            l.notifyChange(safeCreated, safeUpdated, safeRemoved);
        }
    }

    /**
     * Registers the given listener to receive notifications when a change
     * occurs in the operational state of policies.
     */
    public void registerListener(PolicyListener l) {
        listeners.add(l);
    }

    /**
     * Unregisters the given listener so they will no longer receive
     * notifications when a change occurs in the operational state of policies.
     */
    public void unregisterListener(PolicyListener l) {
        listeners.remove(l);
    }

}
