/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.rpc;

import java.util.concurrent.ExecutionException;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.User;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 *
 * @author gwu
 *
 */
public interface NemoRpc {

    /**
     * This method is called by NEMORenderer to execute the NEMO RPC operation against the specified Nemo engine using
     * the passed user's tenant context. This operation will be executed between a beginTransaction/endTransaction pair.
     *
     * @param nemoEngine
     * @param user
     * @return the result of the RPC execution
     * @throws InterruptedException
     * @throws ExecutionException
     */
    RpcResult<? extends CommonRpcResult> apply(NemoIntentService nemoEngine, User user) throws InterruptedException,
            ExecutionException;

    /**
     * Returns whether this Rpc object was created with valid input paramaters. If this methods returns false, the
     * entire RPC transactions will be skipped.
     *
     * @return true if the input parameters were valid; false if not.
     */
    boolean isInputValid();

}
