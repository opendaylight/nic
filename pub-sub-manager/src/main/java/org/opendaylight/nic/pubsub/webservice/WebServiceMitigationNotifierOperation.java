/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.webservice;

import com.sun.jersey.api.client.*;
import org.opendaylight.nic.pubsub.util.JsonFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 25/05/17.
 */
public class WebServiceMitigationNotifierOperation extends AbstractOperation implements WebServiceOperationService {
    private static final Logger LOG = LoggerFactory.getLogger(WebServiceMitigationNotifierOperation.class);

    private final String nodeIp;

    public WebServiceMitigationNotifierOperation(final String url,
                                                 final String nodeIp) {
        super(url);
        this.nodeIp = nodeIp;
    }

    @Override
    public void run() {
        evaluatePOSTOperation(getWebResource());
    }

    @Override
    public ClientResponse evaluatePOSTOperation(final WebResource.Builder resource) {
        ClientResponse clientResponse = null;
        try {
            LOG.info("\n### Notify external app for mitigated action for DeviceIP: {}", nodeIp);
            clientResponse = resource.post(ClientResponse.class, JsonFormatUtils.createJsonBy(nodeIp));
        } catch (ClientHandlerException e) {
            LOG.error(e.getMessage());
        } finally {
            if (clientResponse != null) {
                finalizeOperation(clientResponse);
            } else {
                LOG.info("\nNo client response available.");
            }
        }
        return clientResponse;
    }
}
