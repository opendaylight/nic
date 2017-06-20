/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.webservice;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 26/05/17.
 */
public abstract class AbstractOperation {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOperation.class);

    private static final String APPLICTION_JSON = "application/json";
    private static final int HTTP_SUCCESS = 200;
    private String HTTP_ERROR_MSG;

    private final String url;

    protected AbstractOperation(final String url) {
        this.url = url;
        this.HTTP_ERROR_MSG = "Error when try to evaluate HTTP call for URL: " + url;
    }

    protected WebResource.Builder getWebResource() {
        final ClientConfig clientConfig = new DefaultClientConfig();
        final Client client = Client.create(clientConfig);
        LOG.info("\n### Creating web resource for URL: {}", url);

        return client.resource(url).type(APPLICTION_JSON);
    }

    protected void finalizeOperation(final ClientResponse clientResponse) {
        if (clientResponse != null && clientResponse.getStatus() != HTTP_SUCCESS) {
            throw new UniformInterfaceException(HTTP_ERROR_MSG, clientResponse);
        }
        clientResponse.close();
    }
}
