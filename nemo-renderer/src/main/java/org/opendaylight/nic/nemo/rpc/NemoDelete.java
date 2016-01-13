/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.opendaylight.nic.nemo.renderer.NEMORenderer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.OperationId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoDeleteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoDeleteInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.structure.style.nemo.delete.input.ObjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.structure.style.nemo.delete.input.OperationsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.objects.Connection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.operations.Operation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.User;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * @author gwu
 *
 */
public class NemoDelete implements NemoRpc {

    public NemoDelete() {
    }

    /**
     * This will prepare the input builder to delete all NEMO connections and operations with a "nic-" prefix in their
     * names
     *
     * @param user
     * @return
     */
    public static StructureStyleNemoDeleteInput buildInput(final User user) {

        final StructureStyleNemoDeleteInputBuilder builder = new StructureStyleNemoDeleteInputBuilder();

        // delete connections with a "nic-" prefix in the name
        if (user.getObjects() != null && user.getObjects().getConnection() != null) {
            List<ConnectionId> connections = new ArrayList<>();
            for (Connection connection : user.getObjects().getConnection()) {
                if (connection.getConnectionName().getValue().startsWith(NEMORenderer.NIC_PREFIX)) {
                    connections.add(connection.getConnectionId());
                }
            }
            builder.setObjects(new ObjectsBuilder().setConnection(connections).build());
        }

        // delete operations with a "nic-" prefix in the name
        if (user.getOperations() != null && user.getOperations().getOperation() != null) {
            List<OperationId> operations = new ArrayList<>();
            for (Operation operation : user.getOperations().getOperation()) {
                if (operation.getOperationName().getValue().startsWith(NEMORenderer.NIC_PREFIX)) {
                    operations.add(operation.getOperationId());
                }
            }

            builder.setOperations(new OperationsBuilder().setOperation(operations).build());
        }

        return builder.setUserId(user.getUserId()).build();
    }

    @Override
    public RpcResult<? extends CommonRpcResult> apply(NemoIntentService nemoEngine, User user)
            throws InterruptedException, ExecutionException {
        StructureStyleNemoDeleteInput deleteInput = buildInput(user);
        return nemoEngine.structureStyleNemoDelete(deleteInput).get();
    }

    @Override
    public boolean isInputValid() {
        return true;
    }
}
