/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.webservice;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.opendaylight.nic.pubsub.util.JsonFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 26/05/17.
 */
public class WebServiceSubscriberOperation extends AbstractOperation implements WebServiceOperationService {
    private static final Logger LOG = LoggerFactory.getLogger(WebServiceSubscriberOperation.class);

    private final String urlToReceiveNotifications;
    private final String nodeIp;

    public WebServiceSubscriberOperation(final String url,
                                         final String urlToReceiveNotifications,
                                         final String nodeIp) {
        super(url);
        this.urlToReceiveNotifications = urlToReceiveNotifications;
        this.nodeIp = nodeIp;
    }
    @Override
    public void run() {
        evaluatePOSTOperation(getWebResource());
    }

    @Override
    public ClientResponse evaluatePOSTOperation(WebResource.Builder resource) {
        ClientResponse clientResponse = null;
        try {
            clientResponse = resource.post(ClientResponse.class, JsonFormatUtils.createJsonBy(nodeIp, urlToReceiveNotifications));
            LOG.info("\n### Notifying mitigated action for NodeIP: {} using URL: {}", nodeIp, urlToReceiveNotifications);
        } catch (ClientHandlerException e) {
            LOG.error(e.getMessage());
        } finally {
            if (clientResponse != null) {
                finalizeOperation(clientResponse);
            }
        }
        return clientResponse;
    }
}
