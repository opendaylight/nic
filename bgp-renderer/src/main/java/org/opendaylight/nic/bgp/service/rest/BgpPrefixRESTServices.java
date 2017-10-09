/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service.rest;

import org.opendaylight.nic.bgp.exception.BgpRestOperationException;
import org.opendaylight.nic.bgp.service.parser.BgpDataflowParser;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 * Created by yrineu on 10/07/17.
 */
public class BgpPrefixRESTServices implements RESTService<BgpDataflow> {

    private final static Logger LOG = LoggerFactory.getLogger(BgpPrefixRESTServices.class);
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String ACCEPT_LANGUAGE = "en-US,en;q=0.5";
    private final static String CONTENT_TYPE = "application/json";

    private final static String POST = "POST";
    private final static String GET = "GET";

    private static int SUCCESS = 200;

    public BgpPrefixRESTServices() {
        authenticator();
    }

    protected void authenticator() {
        final String admin = "admin";
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(admin, admin.toCharArray());
            }

        });
    }

    @Override
    public String GET() {
        final String URL = "http://localhost:8181/restconf/operational/bgp-rib:bgp-rib/rib/bgp-example/loc-rib/" +
                "tables/bgp-types:ipv4-address-family/bgp-types:unicast-subsequent-address-family/ipv4-routes";
        try {
            final HttpURLConnection connection = retrieveConnectionBase(URL);
            connection.setRequestMethod(GET);
            int responseCode = connection.getResponseCode();
            if (responseCode == SUCCESS) {
                String output;
                final StringBuffer buffer = new StringBuffer();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                while ((output = reader.readLine()) != null) {
                    buffer.append(output);
                }
                reader.close();
                System.out.println(buffer.toString());
            }

        } catch (IOException e) {
            LOG.info("\nError: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void POST(BgpDataflow dataFlow) {
        try {
            String PATH = "http://localhost:8181/restconf/config/bgp-rib:application-rib/%s/tables/" +
                    "bgp-types:ipv4-address-family/bgp-types:unicast-subsequent-address-family";
            PATH = PATH.replace("%s", dataFlow.getGlobalIp().getValue());
            final HttpURLConnection connection = retrieveConnectionBase(PATH);
            connection.setRequestMethod(POST);

            final String postJsonData = BgpDataflowParser.fromBgpDataFlow(dataFlow);

            connection.setDoOutput(true);
            final DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(postJsonData);
            outputStream.flush();
            outputStream.close();

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String output;
            final StringBuffer response = new StringBuffer();

            while ((output = bufferedReader.readLine()) != null) {
                response.append(output);
            }

            bufferedReader.close();
            int responseCode = connection.getResponseCode();
            if (responseCode == SUCCESS) {
                LOG.info("\n BGP prefix advertised with success.");
            }
        } catch (Exception e) {
            LOG.error("\nError: {}", e.getMessage());
        }
    }

    @Override
    public void DELETE(BgpDataflow dataFlow) {
        //TODO: Implement the delete REST call.
    }

    @Override
    public BgpDataflow PUT(BgpDataflow dataFlow) {
        //TODO: Implement update
        return null;
    }

    private HttpURLConnection retrieveConnectionBase(final String requestedUrl) {
        HttpURLConnection connection;
        try {
            final URL url = new URL(requestedUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE);
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        } catch (IOException e) {
            throw new BgpRestOperationException(e.getMessage());
        }
        return connection;
    }
}
