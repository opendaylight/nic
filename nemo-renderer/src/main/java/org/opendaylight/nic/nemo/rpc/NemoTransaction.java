/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.rpc;

import java.util.concurrent.ExecutionException;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult.ResultCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 *
 * @author gwu
 *
 */
public class NemoTransaction {

    private NemoTransaction() {
    }

    public static boolean begin(NemoIntentService nemoEngine, UserId userId) throws InterruptedException,
            ExecutionException {
        RpcResult<BeginTransactionOutput> r1 = nemoEngine.beginTransaction(
                new BeginTransactionInputBuilder().setUserId(userId).build()).get();
        return r1.isSuccessful() && r1.getResult().getResultCode() == ResultCode.Ok;
    }

}
