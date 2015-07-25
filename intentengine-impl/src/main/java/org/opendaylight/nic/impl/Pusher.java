//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

;

public class Pusher {

    private Collection<Policy> policyList = new LinkedList<Policy>();

    public Pusher(Collection<Policy> policyList) {
        this.policyList = policyList;
    }

    public void Push() {

        try {

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post =
                    new HttpPost(
                            "http://127.0.0.1:8181/restconf/config/opendaylight-inventory:nodes/");
            // JSONObject json = new JSONObject();

            String data = "";
            StringEntity input = new StringEntity(data);
            input.setContentType("application/json");
            post.setEntity(input);

            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() != 201) {
                throw new RuntimeException("Failed : HTTP Error code :"
                        + response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {

        }
    }
}