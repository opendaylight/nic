//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.pm;

import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.AppId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.DomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.Domains;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.PolicyId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.Domain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.DomainKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.Application;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.ApplicationKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.AppPolicy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.AppPolicyKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.Policy;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * Validates policies that have been added to the CONFIGURATION tree and
 * once validated moves them to the OPERATIONAL tree.
 *
 * @author Shaun Wackerly
 */
public class PolicyValidator implements AutoCloseable, DataChangeListener {

	private static final Logger log = LoggerFactory.getLogger(PolicyValidator.class);

	private final DataBroker dataBroker;
	private final RpcProviderRegistry rpc;
	private final NotificationProviderService notify;
	private final ListenerRegistration<DataChangeListener> dataChangeReg;

	/**
	 * Constructs a policy manager using the given dependencies.
	 *
	 * @param dataBroker data broker for MD-SAL storage
	 * @param rpc RPC registration
	 * @param notify notification service
	 */
	public PolicyValidator(DataBroker dataBroker, RpcProviderRegistry rpc,
	                     NotificationProviderService notify) {
        log.debug("PolicyValidator() contructor");
		this.dataBroker = dataBroker;
		this.rpc = rpc;
		this.notify = notify;

		InstanceIdentifier<Domain> path = InstanceIdentifier.builder(Domains.class).child(Domain.class).toInstance();
		dataChangeReg = dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, path, this, AsyncDataBroker.DataChangeScope.SUBTREE);
	}

	@Override
	public void close() throws Exception {
	    log.debug("PolicyValidator.close()");
        dataChangeReg.close();
	}

	/**
	 * Gets an instance identifier which uniquely identifies the stored policy
	 * which has the given ID. Note that the presence of an identifier does
	 * not necessarily indicate the presence of the stored policy. An instance
	 * identifier is merely a reference.
	 *
	 * @param id policy ID
	 * @return instance identifier
	 */
	private InstanceIdentifier<Policy> getInstanceId(DomainId dId, AppId aId, PolicyId pId) {
        return InstanceIdentifier.builder(Domains.class)
                .child(Domain.class, new DomainKey(dId))
                .child(Application.class, new ApplicationKey(aId))
                .child(AppPolicy.class, new AppPolicyKey(pId))
                .child(Policy.class)
                .toInstance();
	}

	/**
	 * Reads the MD-SAL and returns the policy which corresponds to the given
	 * ID, if it exists. If no policy is found, null is returned.
	 *
	 * @param pId policy ID
	 * @return policy, or null
	 */
	private Policy readPolicy(DomainId dId, AppId aId, PolicyId pId) {
        log.debug("READ: dom={} app={} pol={}", dId, aId, pId);

        // Get the path which uniquely identifies (via a reference) the new policy we'll add
        final InstanceIdentifier<Policy> path = getInstanceId(dId, aId, pId);

        // Create a read-only transaction to query the MD-SAL
        ReadOnlyTransaction readTrans = dataBroker.newReadOnlyTransaction();
        final CheckedFuture<Optional<Policy>, ReadFailedException> readFuture = readTrans.read(LogicalDatastoreType.OPERATIONAL, path);

        // Retrieve the optional result. If the result was found, return it.
        try {
            Optional<Policy> sp = readFuture.get();
            if (sp.isPresent())
                return sp.get();
        } catch (Exception e) {
            log.error("Failed to read policy {}", path, e);
        }

        // No policy was found
        return null;
	}

	/**
	 * Writes the MD-SAL with the given policy.
	 *
	 * @param pId policy Id
	 * @param policy policy to write
	 */
	private void writePolicy(DomainId dId, AppId aId, PolicyId pId, Policy policy) {
        log.debug("WRITE: dom={} app={} pol={} policy={}", dId, aId, pId, policy);
        if (policy == null) {
           log.error("Skipping update for null policy");
           return;
        }

        // Get the path which uniquely identifies (via a reference) the policy we'll write
        final InstanceIdentifier<Policy> path = getInstanceId(dId, aId, pId);

        // Create a write transaction which will automatically create parent
        // nodes in the tree if they do not exist.
        WriteTransaction writeTrans = dataBroker.newWriteOnlyTransaction();
        writeTrans.merge(LogicalDatastoreType.OPERATIONAL, path, policy, true);
        try {
            // Write the policy to MD-SAL
            writeTrans.submit();
        } catch (Exception e) {
            log.error("FAILED to write policy {}: {}", path, e);
        }
	}

    /**
     * Removes the given policy from MD-SAL.
     *
     * @param path policy ID
     * @param policy policy to write
     */
    private void removePolicy(InstanceIdentifier<Policy> path) {
        log.debug("REMOVE: path={}", path);

        // Create a write transaction which will automatically create parent
        // nodes in the tree if they do not exist.
        WriteTransaction writeTrans = dataBroker.newWriteOnlyTransaction();
        writeTrans.delete(LogicalDatastoreType.OPERATIONAL, path);
        try {
            // Write the policy to MD-SAL
            writeTrans.submit();
        } catch (Exception e) {
            log.error("FAILED to remove policy {}: {}", path, e);
        }
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        log.debug("CHANGE: create="+change.getCreatedData().size() + ", update=" + change.getUpdatedData().size() + ", remove=" + change.getRemovedPaths().size());

        // Handle created data
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getCreatedData().entrySet()) {
            DataObject obj = entry.getValue();
            if (!(obj instanceof Domain))
                continue;

            Domain dom = (Domain)obj;
            for (Application app : dom.getApplication()) {
                for (AppPolicy pol : app.getAppPolicy()) {
                    writePolicy(dom.getDomainId(), app.getAppId(), pol.getPolicyId(), pol.getPolicy());
                }
            }
        }

        // Handle updated data
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : change.getUpdatedData().entrySet()) {
            DataObject obj = entry.getValue();
            if (!(obj instanceof Domain)) {
                log.debug("Skipping update of class {}", obj.getClass());
                continue;
            }

            Domain dom = (Domain)obj;
            for (Application app : dom.getApplication()) {
                for (AppPolicy pol : app.getAppPolicy()) {
                    if (pol.getPolicy() == null)
                        continue;  // Deleted policies have 'null' in the updated list

                    writePolicy(dom.getDomainId(), app.getAppId(), pol.getPolicyId(), pol.getPolicy());
                }
            }
        }

        // Handle removed data
        for (InstanceIdentifier<?> path : change.getRemovedPaths()) {
            if (path.getTargetType() != Policy.class) {
                log.debug("Skipping removal of target type {}", path.getTargetType());
                continue;
            }

            removePolicy((InstanceIdentifier<Policy>)path);
        }
    }

}
