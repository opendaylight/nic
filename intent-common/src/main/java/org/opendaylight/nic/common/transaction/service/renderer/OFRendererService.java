package org.opendaylight.nic.common.transaction.service.renderer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Created by yrineu on 25/04/17.
 */
public interface OFRendererService extends RendererService {

    /**
     * Create LLDP flows using the OpenFlow renderer
     * @param nodeId the {@link NodeId}
     */
    void evaluateLLDPFlow(NodeId nodeId);

    /**
     * Create ARP flows using the OpenFlow renderer
     * @param nodeId the {@link NodeId}
     */
    void evaluateArpFlows(NodeId nodeId);

    /**
     * Create flows based on a given {@link Intent}
     * @param intent the {@link Intent}
     */
    void applyIntent(Intent intent);

    /**
     * Remove flows based on a given {@link Intent}
     * @param intent the {@link Intent}
     */
    void removeIntent(Intent intent);
}
