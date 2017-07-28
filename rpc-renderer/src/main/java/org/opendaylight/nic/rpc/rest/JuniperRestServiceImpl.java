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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.rpc.exception.JuniperRPCException;
import org.opendaylight.nic.rpc.model.juniper.configuration.Configuration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.bgp.dataflow.rev170725.evpn.bgp.dataflows.EvpnBgpDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflows.EvpnDataflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yrineu on 25/07/17.
 */
public class JuniperRestServiceImpl implements JuniperRestService {
    private static final Logger LOG = LoggerFactory.getLogger(JuniperRestServiceImpl.class);

    private final static String POST_REQUEST = "POST";
    private final String BASE_URL = "http://$ip:$port/rpc$parameters";
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String CONTENT_TYPE = "application/xml";
    private final static String AUTHORIZATION = "Authorization";

    private final static String STOP_ON_ERROR = "?stop-on-error=1";
    private Map<String, HttpURLConnection> deviceIpByHttpPostConnectionCache;

    public JuniperRestServiceImpl() {}

    public void start() {
        deviceIpByHttpPostConnectionCache = Maps.newConcurrentMap();
    }

    @Override
    public void sendConfiguration(final List<EvpnDataflow> evpnDataflows) {

        //TODO: Create a thread pool
        evpnDataflows.forEach(evpnDataflow -> {
            final String httpIp = evpnDataflow.getHttpIp();
            final String httpPort = evpnDataflow.getHttpPort();
            final String userName = evpnDataflow.getUserName();
            final String password = evpnDataflow.getPassword();
            final String interfaceName = evpnDataflow.getInterfaceName();

            final Set<Integer> vlanIds = Sets.newConcurrentHashSet();
            final Set<String> vlanNames = Sets.newConcurrentHashSet();

            final Map<String, Integer> vlanNameById = Maps.newConcurrentMap();
            evpnDataflow.getVlanInfos().forEach(vlanInfo -> {
                final Integer vlanId = vlanInfo.getVlanId().intValue();
                final String vlanName = vlanInfo.getVlanName();

                vlanIds.add(vlanId);
                vlanNames.add(vlanName);

                vlanNameById.put(vlanName, vlanId);
            });

            final Configuration configuration = new Configuration();
            configuration.configureSwitchOptions(evpnDataflow.getLoopbackIp());
            configuration.configureEvpn(vlanIds);
            configuration.configureInterfaceVlans(interfaceName, vlanNames);
            configuration.configurePolicyCommunityAccept(evpnDataflow.getId(), vlanIds);
            configuration.configureVxlans(vlanNameById);

            final String configurationSchema = configuration.generateRPCStructure();
            LOG.info("\n#####");
            LOG.info(configurationSchema);
            LOG.info("\n#####");
            try {
                final HttpURLConnection connection = retrieveHttpPostConnection(httpIp, httpPort, userName, password);
                connection.getOutputStream().write(configurationSchema
                        .getBytes(StandardCharsets.UTF_8));
                final int responseCode = connection.getResponseCode();

                LOG.info("\nResponse Info: {}", responseCode);
                LOG.info("\nResponse message: {}", connection.getResponseMessage());
                connection.disconnect();
            } catch (IOException e) {
                LOG.info(e.getMessage());
            }
        });
    }

    private HttpURLConnection retrieveHttpPostConnection(final String httpIp,
                                                         final String httpPort,
                                                         final String user,
                                                         final String password) {
        HttpURLConnection connection = deviceIpByHttpPostConnectionCache.get(httpIp);
        if (connection == null) {
            LOG.info("\n#### HttpConnection not found, creating a new one.");
            String URL = BASE_URL.replace("$ip", httpIp);
            URL = URL.replace("$port", httpPort);
            URL = URL.replace("$parameters", STOP_ON_ERROR);
            LOG.info("\nRequested POST URL: {}", URL);
            try {
                connection = retrieveBaseConnection(URL, createJuniperBasicAuth(user, password));
            } catch (JuniperRPCException e) {
                LOG.error(e.getMessage());
            }
        } else {
            LOG.info("\n#### HttpConnection already exists in cache, reusing.");
        }
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        try {
            connection.setRequestMethod(POST_REQUEST);
        } catch (ProtocolException e) {
            LOG.error(e.getMessage());
        }
        return connection;
    }

    private HttpURLConnection retrieveBaseConnection(final String requestedUrl,
                                                     final String authentication) throws JuniperRPCException {
        HttpURLConnection connection;
        try {
            final URL url = new URL(requestedUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty(AUTHORIZATION, authentication);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("Accept", CONTENT_TYPE);
        } catch (IOException e) {
            throw new JuniperRPCException(e.getMessage());
        }
        return connection;
    }

    private String createJuniperBasicAuth(final String username,
                                          final String password) {
        final String USER_CREDENTIALS = username + ":" + password;
        final String basicAuth = "Basic " + new String(Base64.getEncoder().encode(USER_CREDENTIALS.getBytes()));
        return basicAuth;
    }

    public void stop() {
        deviceIpByHttpPostConnectionCache.entrySet().forEach(connection -> {
            connection.getValue().disconnect();
        });
        deviceIpByHttpPostConnectionCache.clear();
    }
}
