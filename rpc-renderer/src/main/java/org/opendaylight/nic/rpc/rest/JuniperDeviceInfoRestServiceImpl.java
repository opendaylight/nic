/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.opendaylight.nic.rpc.model.juniper.information.device.SwitchInformation;
import org.opendaylight.nic.rpc.model.juniper.information.evpn.DatabaseInfo;
import org.opendaylight.nic.rpc.utils.RESTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by yrineu on 17/07/17.
 */
public class JuniperDeviceInfoRestServiceImpl extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(JuniperDeviceInfoRestServiceImpl.class);
    private static final long serialVersionUID = 1L;

    private final String BASE_URL = "http://$ip:3000/rpc$parameters";
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String CONTENT_TYPE = "application/xml";
    private final static String ACCEPT = "application/json";
    private final static String UTF_8 = "UTF-8";
    private final static String AUTHORIZATION = "Authorization";

    private final static String GET_REQUEST = "GET";

    private JuniperRestService juniperRestService;

    public JuniperDeviceInfoRestServiceImpl() throws ServletException {
        ServletContext context = super.getServletContext();
        LOG.info("\n### Rest initialized with success: {}!", context);
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
        final String targetMacAddress = req.getRequestURI().split("/")[2];
        final String switchInfosURLRequest = "http://localhost:8181/restconf/config/switch-group:switch-groups";

        final HttpURLConnection connection = retrieveConnectionBase(switchInfosURLRequest);
        connection.setRequestMethod(GET_REQUEST);
        String output;
        final StringBuffer switchInfos = new StringBuffer();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        while ((output = reader.readLine()) != null) {
            switchInfos.append(output);
        }
        reader.close();
        final JsonParser jsonParser = new JsonParser();
        final JsonElement element = jsonParser.parse(switchInfos.toString());

        final Set<SwitchInformation> switchInformationSet = SwitchInformation.extractSwitchInformation(element.getAsJsonObject());
        final ConcurrentMap<SwitchInformation, HttpURLConnection> connections = retrieveSwitchConnections(switchInformationSet);
        final ConcurrentMap<SwitchInformation, String> evpnInfoBySwitch = Maps.newConcurrentMap();

        connections.entrySet().forEach(entry -> {
            final StringBuffer evpnData = new StringBuffer();
            try {
                String line;
                final BufferedReader evpnDataReader = new BufferedReader(new InputStreamReader(entry.getValue().getInputStream()));
                while ((line = evpnDataReader.readLine()) != null) {
                    evpnData.append(line);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
            evpnInfoBySwitch.put(entry.getKey(), evpnData.toString());
        });

        final Set<DatabaseInfo> response = Sets.newConcurrentHashSet();
        evpnInfoBySwitch.entrySet().forEach(entry -> {
            final SwitchInformation switchInformation = entry.getKey();
            final DatabaseInfo databaseInfo = new DatabaseInfo(switchInformation.getSwitchName(), targetMacAddress);
            databaseInfo.extractEvpnInfoToJson(entry.getValue());
            response.add(databaseInfo);
        });
        final Gson jsonResponse = new Gson();
        createSuccessResponseWith(jsonResponse.toJson(response), resp);
    }

    private ConcurrentMap<SwitchInformation, HttpURLConnection> retrieveSwitchConnections(final Set<SwitchInformation> switchInformationSet) {
        final ConcurrentMap<SwitchInformation, HttpURLConnection> resultedConnections = Maps.newConcurrentMap();
        final String requestedRPC = "get-evpn-database-information";
        switchInformationSet.forEach(switchInformation -> {
            final String switchUrl = RESTUtils.buildDeviceURLRequest(
                    switchInformation.getHttpIp(), switchInformation.getHttpPort(), requestedRPC);
            final HttpURLConnection connection = retrieveConnectionBase(switchUrl);
            resultedConnections.put(switchInformation, connection);
        });
        return resultedConnections;
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