package org.opendaylight.nic.impl;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapper.rev150105.GetValueInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapper.rev150105.GetValueOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapper.rev150105.GetValueOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapper.rev150105.MapperService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class GetValueImpl implements MapperService{

	@Override
	public Future<RpcResult<GetValueOutput>> getValue(GetValueInput input) {
        GetValueOutputBuilder builder = new GetValueOutputBuilder();
        builder.setResult("testValue");
        return RpcResultBuilder.success(builder.build()).buildFuture();
	}

}
