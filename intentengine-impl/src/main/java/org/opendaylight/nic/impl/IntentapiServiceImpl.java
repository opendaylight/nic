//------------------------------------------------------------------------------
//  (c) Copyright 2015 Hewlett-Packard Development Company, L.P.
//
//  Confidential computer software. Valid license from HP required for 
//  possession, use or copying. 
//
//  Consistent with FAR 12.211 and 12.212, Commercial Computer Software,
//  Computer Software Documentation, and Technical Data for Commercial Items
//  are licensed to the U.S. Government under vendor's standard commercial 
//  license.
//------------------------------------------------------------------------------
package org.opendaylight.nic.impl;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.IntentapiService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.AddIntentInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.AddIntentOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.AddIntentOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class IntentapiServiceImpl implements IntentapiService {

    @Override
    public Future<RpcResult<AddIntentOutput>> addIntent(AddIntentInput input) {
        AddIntentOutputBuilder outputBuilder = new AddIntentOutputBuilder();
        outputBuilder.setAccepted(true);
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }
}

