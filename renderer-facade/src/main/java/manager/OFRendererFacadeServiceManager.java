/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package manager;

import common.RendererAction;
import common.RendererFlowModel;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.EdgeTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import service.OFRendererFacadeService;
import utils.RendererServiceRetrieverUtils;

import java.util.List;

/**
 * Created by yrineu on 30/03/16.
 */
public class OFRendererFacadeServiceManager implements OFRendererFacadeService {

    private OFRendererFlowService ofRendererFlowService;

    public OFRendererFacadeServiceManager() {
        ofRendererFlowService = RendererServiceRetrieverUtils.getOFRendererService();
    }

    @Override
    public void pushFlow(List<String> intentIds,
                         List<Edges> edges) {

        for(Edges edge : edges) {
            final RendererFlowModel flowModel = new RendererFlowModel();
            flowModel.setIntentId(intentIds);
            String srcMac = edge.getSrcNode();
            String dstMac = edge.getDstNode();

            EdgeTypes action = edge.getType();

            flowModel.setSrcMacAddress(MacAddress.getDefaultInstance(srcMac));
            flowModel.setDstMacAddress(MacAddress.getDefaultInstance(dstMac));
            flowModel.setAction(extractRendererAction(action));

            ofRendererFlowService.pushGraphFlow(flowModel);
        }
    }

    private RendererAction extractRendererAction(EdgeTypes action) {
        RendererAction result = RendererAction.ALLOW;
        switch (action) {
            case CanAllow:
            case MustAllow:
                result = RendererAction.ALLOW;
                break;
            case MustDeny:
                result = RendererAction.DENY;
                break;
        }
        return result;
    }
}
