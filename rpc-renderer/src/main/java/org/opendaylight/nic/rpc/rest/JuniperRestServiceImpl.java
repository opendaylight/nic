/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.rpc.exception.JuniperRPCException;
import org.opendaylight.nic.rpc.model.juniper.configuration.Configuration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.bgp.dataflow.rev170725.EvpnBgpDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.bgp.dataflow.rev170725.evpn.bgp.dataflows.EvpnBgpDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.bgp.dataflow.rev170725.evpn.bgp.dataflows.EvpnBgpDataflowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflows.EvpnDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.ospf.dataflow.rev170725.OspfDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.ospf.dataflow.rev170725.ospf.dataflows.OspfDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.ospf.dataflow.rev170725.ospf.dataflows.OspfDataflowKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
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

    private InstanceIdentifier<OspfDataflows> ospfIdentifier = InstanceIdentifier
            .create(OspfDataflows.class);
    private InstanceIdentifier<EvpnBgpDataflows> evpnBgpIdentifier = InstanceIdentifier
            .create(EvpnBgpDataflows.class);

    private final static String POST_REQUEST = "POST";
    private final String BASE_URL = "http://$ip:$port/rpc$parameters";
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String CONTENT_TYPE = "application/xml";
    private final static String AUTHORIZATION = "Authorization";

    private final static String STOP_ON_ERROR = "?stop-on-error=1";

    private DataBroker dataBroker;

    public JuniperRestServiceImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void sendConfiguration(final List<EvpnDataflow> evpnDataflows) {

        //TODO: Create a thread pool
        evpnDataflows.forEach(evpnDataflow -> {
            String URL = BASE_URL.replace("$ip", evpnDataflow.getHttpIp());
            URL = URL.replace("$port", evpnDataflow.getHttpPort());
            URL = URL.replace("$parameters", STOP_ON_ERROR);
            LOG.info("\nRequested POST URL: {}", URL);

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

            OspfDataflow ospfDataflow = null;
            EvpnBgpDataflow evpnBgpDataflow = null;

            if (evpnDataflow.getOspfId() != null) {
                ospfDataflow = retrieveOSPFDataflow(evpnDataflow.getOspfId());
                evpnBgpDataflow = retrieveEVPNBgpDataflow(evpnDataflow.getOspfId());
            }

            final Map<String, String> ospfInterfaceNameByType = Maps.newConcurrentMap();
            if (ospfDataflow != null) {
                ospfDataflow.getOspfInterfaces().forEach(ospfInterfaces -> {
                    final String ospfInterfaceName = ospfInterfaces.getInterfaceName();
                    final StringBuilder interfaceType = new StringBuilder();
                    if (ospfInterfaces.getInterfaceType() != null) {
                        interfaceType.append(ospfInterfaces.getInterfaceType().getName());
                    }
                    ospfInterfaceNameByType.put(
                            ospfInterfaceName,
                            interfaceType.toString());
                });
            }

            final Configuration configuration = new Configuration();
            if (!ospfInterfaceNameByType.isEmpty()) {
                if (ospfDataflow != null && evpnBgpDataflow != null) {

                    configuration.configureEvpnBgp(
                            evpnBgpDataflow.getGroupName(),
                            evpnBgpDataflow.getGroupType().name(),
                            evpnBgpDataflow.getLocalAddress(),
                            extractNeighbors(evpnBgpDataflow));
                    configuration.configureOSPF(ospfDataflow.getOspfName(), ospfInterfaceNameByType);
                }
            }
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
                final HttpURLConnection connection = retrieveBaseConnection(URL, createJuniperBasicAuth(userName, password));
                connection.setDoOutput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod(POST_REQUEST);
                connection.getOutputStream().write(configurationSchema
                        .getBytes(StandardCharsets.UTF_8));
                final int responseCode = connection.getResponseCode();

                LOG.info("\nResponse Info: {}", responseCode);
                LOG.info("\nResponse message: {}", connection.getResponseMessage());
                connection.disconnect();
            } catch (JuniperRPCException | IOException e) {
                LOG.info(e.getMessage());
            }
        });
    }

    private Set<String> extractNeighbors(final EvpnBgpDataflow evpnBgpDataflow) {
        final Set<String> neighbors = Sets.newHashSet();
        evpnBgpDataflow.getNeighbors().forEach(neighbor -> neighbors.add(neighbor.getNeighborIp()));
        return neighbors;
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

    private OspfDataflow retrieveOSPFDataflow(final String switchName) {
        OspfDataflow result = null;
        final InstanceIdentifier<OspfDataflow> identifier = ospfIdentifier
                .child(OspfDataflow.class, new OspfDataflowKey(switchName));
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<OspfDataflow> ospfDataflowOptional = transaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            if (ospfDataflowOptional.isPresent()) {
                result = ospfDataflowOptional.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private EvpnBgpDataflow retrieveEVPNBgpDataflow(final String switchName) {
        EvpnBgpDataflow result = null;
        final InstanceIdentifier<EvpnBgpDataflow> identifier = evpnBgpIdentifier
                .child(EvpnBgpDataflow.class, new EvpnBgpDataflowKey(switchName));
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<EvpnBgpDataflow> evpnBgpDataflowOptional = transaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            if (evpnBgpDataflowOptional.isPresent()) {
                result = evpnBgpDataflowOptional.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }
}
