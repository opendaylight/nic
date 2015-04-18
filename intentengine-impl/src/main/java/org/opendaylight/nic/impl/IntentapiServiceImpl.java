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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.GetIntentStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.GetIntentStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.GetIntentStatusOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class IntentapiServiceImpl implements IntentapiService {

    @Override
    public Future<RpcResult<GetIntentStatusOutput>> getIntentStatus(GetIntentStatusInput input) {
        GetIntentStatusOutputBuilder outputBuilder = new GetIntentStatusOutputBuilder();
        outputBuilder.setAccepted(true);
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }
}

