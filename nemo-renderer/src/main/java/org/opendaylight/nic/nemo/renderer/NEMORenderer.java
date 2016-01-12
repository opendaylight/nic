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
import org.opendaylight.nic.nemo.rpc.NemoDelete;
import org.opendaylight.nic.nemo.rpc.NemoRpc;
import org.opendaylight.nic.nemo.rpc.NemoUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult.ResultCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
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
    private final NemoIntentService nemoEngine;
    private ListenerRegistration<DataChangeListener> listenerRegistration;

    public NEMORenderer(DataBroker dataBroker0, RpcProviderRegistry rpcProviderRegistry0) {
        this.dataBroker = dataBroker0;
        this.rpcProviderRegistry = rpcProviderRegistry0;
        this.nemoEngine = rpcProviderRegistry.getRpcService(NemoIntentService.class);
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

    private boolean executeNemoRpc(UserId userId, NemoRpc nemoRpc) throws InterruptedException, ExecutionException {

        if (!nemoRpc.isInputValid()) {
            LOG.info("NemoRpc {} input is invalid", nemoRpc.getClass().getSimpleName());
            return false;
        }

        final Optional<User> userOpt = readUser(userId);
        if (!userOpt.isPresent()) {
            LOG.info("UserId {} not found", userId);
            return false;
        }

        final User user = userOpt.get();

        // run all three operations even if there were already failures

        boolean r1 = beginTransaction(userId);

        RpcResult<? extends CommonRpcResult> rpcResult = null;
        try {
            rpcResult = nemoRpc.apply(nemoEngine, user);
        } catch (Exception e) {
            LOG.warn("NemoRpc failed: ", e);
        }
        boolean r2 = isSuccessful(rpcResult);
        if (!r2) {
            LOG.warn("NemoRpc {} failed: {}", nemoRpc.getClass().getSimpleName(), rpcResult);
        }

        boolean r3 = endTransaction(userId);

        return r1 && r2 && r3;
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
        return executeNemoRpc(getUserId(intent.getKey()), new NemoUpdate(intent));
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
        return executeNemoRpc(getUserId(intentKey), new NemoDelete());
    }

    private static boolean isSuccessful(RpcResult<? extends CommonRpcResult> rpcResult) {
        return rpcResult != null && rpcResult.isSuccessful() && rpcResult.getResult().getResultCode() == ResultCode.Ok;
    }

    private boolean beginTransaction(UserId userId) throws InterruptedException, ExecutionException {
        BeginTransactionInput input = new BeginTransactionInputBuilder().setUserId(userId).build();
        RpcResult<BeginTransactionOutput> r1 = nemoEngine.beginTransaction(input).get();
        boolean success = isSuccessful(r1);
        if (!success) {
            LOG.warn("NemoEngine beginTransaction failed: " + r1.getResult());
        }
        return success;
    }

    private boolean endTransaction(UserId userId) throws InterruptedException, ExecutionException {
        EndTransactionInput input = new EndTransactionInputBuilder().setUserId(userId).build();
        RpcResult<? extends CommonRpcResult> r3 = nemoEngine.endTransaction(input).get();
        boolean success = isSuccessful(r3);
        if (!success) {
            LOG.warn("NemoEngine endTransaction failed: " + r3.getResult());
        }
        return success;
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
    private static UserId getUserId(IntentKey intentKey) {
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
