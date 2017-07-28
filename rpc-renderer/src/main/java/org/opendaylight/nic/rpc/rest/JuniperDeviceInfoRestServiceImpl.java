/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.rpc.exception.JuniperRPCException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.group.rev170724.SwitchGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.group.rev170724._switch.group.HostByInterfaceList;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by yrineu on 17/07/17.
 */
public class JuniperDeviceInfoRestServiceImpl extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final String BASE_URL = "http://$ip:3000/rpc$parameters";
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String CONTENT_TYPE = "application/xml";
    private final static String ACCEPT = "application/json";
    private final static String UTF_8 = "UTF-8";
    private final static String AUTHORIZATION = "Authorization";

    private final static String GET_REQUEST = "GET";

    private static final Logger LOG = LoggerFactory.getLogger(JuniperDeviceInfoRestServiceImpl.class);

    private InstanceIdentifier<SwitchGroups> SWITCH_GROUPS_IDENTIFIER = InstanceIdentifier.builder(SwitchGroups.class).build();

    private DataBroker dataBroker;

    public JuniperDeviceInfoRestServiceImpl() {
        authenticator();
    }

    /**
     * Stats a new servlet with the following URL:
     * http://"odl-ip":8181/nic
     *
     * @throws ServletException when NIC can't init the servlet.
     */
    @Override
    public void init() throws ServletException {
        LOG.info("\nJuniper device info REST Service initialized.");
        super.init();
    }
//

    /**
     * GET request used to retrieve Interface names for a given Juniper device
     * the requests must have the following pattern:
     * http://"odl-ip":"odl-port"/nic/"device-ipv4"
     *
     * @param req  the {@link HttpServletRequest} containing the IPv4 Address of the Juniper device.
     * @param resp the {@link HttpServletResponse} containing the output.
     * @throws ServletException when the request doesn't follows the pattern or when face some issue
     *                          when try to collect device information.
     * @throws IOException      when try to read device information results.
     */
    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
//        RestValidations.validateReceivedRequest(req.getRequestURI());
        final String hostName = req.getRequestURI().split("/")[2];
        final String requestedUrl = "http://localhost:8181/restconf/config/switch-group:switch-groups";

        final HttpURLConnection connection = retrieveConnectionBase(requestedUrl);
        connection.setRequestMethod(GET_REQUEST);
        String output;
        final StringBuffer buffer = new StringBuffer();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        while ((output = reader.readLine()) != null) {
            buffer.append(output);
        }
        reader.close();
        Gson gson = new Gson();
        final String response = gson.toJson(buffer.toString());
        createSuccessResponseWith(response, resp);
    }

    private HttpServletResponse createSuccessResponseWith(final String output,
                                                          final HttpServletResponse resp) throws IOException {
        addBasicHeadersToResponse(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(output);
        return resp;
    }

    private void addBasicHeadersToResponse(final HttpServletResponse resp) {
        resp.setContentType(ACCEPT);
        resp.setCharacterEncoding(UTF_8);
    }


    protected void authenticator() {
        final String admin = "admin";
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(admin, admin.toCharArray());
            }

        });
    }

    private HttpURLConnection retrieveConnectionBase(final String requestedUrl) {
        HttpURLConnection connection = null;
        try {
            final URL url = new URL(requestedUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept-Language", ACCEPT);
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return connection;
    }
}
