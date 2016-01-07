/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.nemo.intent.IntentResolverUtils;
import org.opendaylight.nic.nemo.renderer.NEMOIntentParser.BandwidthOnDemandParameters;
import org.opendaylight.nic.nemo.rpc.NemoDelete;
import org.opendaylight.nic.nemo.rpc.NemoUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult.ResultCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoDeleteInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.Users;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.User;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.UserKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

/**
 *
 * @author gwu
 *
 */
public class NEMORenderer implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(NEMORenderer.class);

    public static final String NIC_PREFIX = "nic-";

    public static final InstanceIdentifier<Intent> INTENT_IID = InstanceIdentifier.builder(Intents.class)
            .child(Intent.class).build();

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private ListenerRegistration<DataChangeListener> listenerRegistration;

    public NEMORenderer(DataBroker dataBroker0, RpcProviderRegistry rpcProviderRegistry0) {
        this.dataBroker = dataBroker0;
        this.rpcProviderRegistry = rpcProviderRegistry0;
    }

    public void init() {
        LOG.info("Initializing NEMORenderer");

        listenerRegistration = dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, INTENT_IID,
                this, DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        // TODO: replace this when operational data initialization is finalized
        IntentResolverUtils.copyPhysicalNetworkConfigToOperational(dataBroker);

        create(changes.getCreatedData());
        update(changes.getUpdatedData());
        delete(changes);
    }

    private void create(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> created : changes.entrySet()) {
            if (created.getValue() instanceof Intent) {
                LOG.info("Created Intent {}.", created);

                try {
                    createOrUpdateIntent((Intent) created.getValue());
                } catch (InterruptedException | ExecutionException e) {
                    // the call is synchronous, so this should never occur
                    LOG.error("Unexpected exception", e);
                }
            }
        }
    }

    private void update(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes.entrySet()) {
            if (updated.getValue() instanceof Intent) {
                LOG.info("Updated Intent {}.", updated);

                try {
                    createOrUpdateIntent((Intent) updated.getValue());
                } catch (InterruptedException | ExecutionException e) {
                    // the call is synchronous, so this should never occur
                    LOG.error("Unexpected exception", e);
                }
            }
        }
    }

    private void delete(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (InstanceIdentifier<?> deleted : changes.getRemovedPaths()) {
            if (deleted.getTargetType().equals(Intent.class)) {
                IntentKey intentKey = deleted.firstKeyOf(Intent.class);
                LOG.info("Deleting IntentKey {}.", intentKey);

                try {
                    deleteIntent(intentKey);
                } catch (InterruptedException | ExecutionException e) {
                    // the call is synchronous, so this should never occur
                    LOG.error("Unexpected exception", e);
                }
            } else {
                LOG.trace("Skipping NIC Intent {} deletion", deleted);
            }
        }
    }

    /**
     *
     * @param intent
     * @return true if the intent create/update was successful
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @VisibleForTesting
    boolean createOrUpdateIntent(Intent intent) throws InterruptedException, ExecutionException {

        final UserId userId = getUserId(intent.getKey());

        StructureStyleNemoUpdateInputBuilder builder = null;
        try {
            BandwidthOnDemandParameters params = NEMOIntentParser.parseBandwidthOnDemand(intent);

            // find existing NEMO nodes by name
            Optional<User> userOpt = readUser(userId);
            if (userOpt.isPresent()) {
                builder = NemoUpdate.prepareInputBuilder(params, userOpt.get());
            }

        } catch (Exception e) {
            LOG.error("Unable to process BoD Intent", e);
        }

        if (builder != null) {

            // make call to NEMO via MD-SAL RPC
            NemoIntentService nemoEngine = rpcProviderRegistry.getRpcService(NemoIntentService.class);

            boolean result = beginTransaction(nemoEngine, userId);

            RpcResult<? extends CommonRpcResult> r2 = nemoEngine.structureStyleNemoUpdate(
                    builder.setUserId(userId).build()).get();
            if (!r2.isSuccessful() || r2.getResult().getResultCode() != ResultCode.Ok) {
                LOG.warn("NemoEngine structureStyleNemoUpdate failed: " + r2.getResult());
                result = false;
            }

            // run endTransaction even if there were already failures
            result = endTransaction(nemoEngine, userId) && result;

            return result;
        } else {
            return false;
        }
    }

    /**
     *
     * @param intent
     * @return true if the intent create/update was successful
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @VisibleForTesting
    private boolean deleteIntent(IntentKey intentKey) throws InterruptedException, ExecutionException {
        LOG.info("Deleting Existing intent with UUID of {}", intentKey.getId());

        final UserId userId = getUserId(intentKey);

        StructureStyleNemoDeleteInputBuilder builder = null;

        Optional<User> userOpt = readUser(userId);
        if (userOpt.isPresent()) {
            builder = NemoDelete.prepareInputBuilder(userOpt.get());
        }

        if (builder != null) {

            // make call to NEMO via MD-SAL RPC
            NemoIntentService nemoEngine = rpcProviderRegistry.getRpcService(NemoIntentService.class);

            boolean result = beginTransaction(nemoEngine, userId);

            RpcResult<? extends CommonRpcResult> r2 = nemoEngine.structureStyleNemoDelete(
                    builder.setUserId(userId).build()).get();
            if (!r2.isSuccessful() || r2.getResult().getResultCode() != ResultCode.Ok) {
                LOG.warn("NemoEngine structureStyleNemoDelete failed: " + r2.getResult());
                result = false;
            }

            // run endTransaction even if there were already failures
            result = endTransaction(nemoEngine, userId) && result;

            return result;
        } else {
            return false;
        }
    }

    private boolean beginTransaction(NemoIntentService nemoEngine, UserId userId) throws InterruptedException,
            ExecutionException {
        RpcResult<BeginTransactionOutput> r1 = nemoEngine.beginTransaction(
                new BeginTransactionInputBuilder().setUserId(userId).build()).get();
        if (!r1.isSuccessful() || r1.getResult().getResultCode() != ResultCode.Ok) {
            LOG.warn("NemoEngine beginTransaction failed: " + r1.getResult());
            return false;
        } else {
            return true;
        }
    }

    private boolean endTransaction(NemoIntentService nemoEngine, UserId userId) throws InterruptedException,
            ExecutionException {
        RpcResult<? extends CommonRpcResult> r3 = nemoEngine.endTransaction(
                new EndTransactionInputBuilder().setUserId(userId).build()).get();
        if (!r3.isSuccessful() || r3.getResult().getResultCode() != ResultCode.Ok) {
            LOG.warn("NemoEngine endTransaction failed: " + r3.getResult());
            return false;
        } else {
            return true;
        }
    }

    private Optional<User> readUser(UserId userId) throws InterruptedException, ExecutionException {
        try (ReadOnlyTransaction txn = dataBroker.newReadOnlyTransaction()) {
            InstanceIdentifier<User> userPath = InstanceIdentifier.builder(Users.class)
                    .child(User.class, new UserKey(userId)).build();
            return txn.read(LogicalDatastoreType.CONFIGURATION, userPath).get();
        }
    }

    /**
     *
     * @param intentKey
     * @return the user that created this intent
     */
    private UserId getUserId(IntentKey intentKey) {
        // for this release, assume userId to be the same as the NIC intent ID
        return new UserId(intentKey.getId().getValue());
    }

    @Override
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

}
