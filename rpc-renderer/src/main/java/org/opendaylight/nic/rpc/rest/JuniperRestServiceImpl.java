/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.rpc.exception.JuniperRPCException;
import org.opendaylight.nic.rpc.model.juniper.configuration.Configuration;
import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationFactory;
import org.opendaylight.nic.rpc.model.juniper.rpc.mapping.DeviceDetails;
import org.opendaylight.nic.rpc.utils.RPCRendererUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch.interfaces.status.SwitchInterfaceStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queues.EvpnDataflowQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.EvpnDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfoBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yrineu on 25/07/17.
 */
public class JuniperRestServiceImpl implements JuniperRestService {

    private interface TaskAction {
        Map<DeviceDetails, String> evaluateTask(final ConfigurationFactory configFactory);
    }

    private static final Logger LOG = LoggerFactory.getLogger(JuniperRestServiceImpl.class);

    private final static String POST_REQUEST = "POST";
    private final String BASE_URL = "http://$ip:$port/rpc$parameters";
    private final static String USER_AGENT = "Mozilla/5.0";
    private final static String CONTENT_TYPE = "application/xml";
    private final static String AUTHORIZATION = "Authorization";

    private final static String STOP_ON_ERROR = "?stop-on-error=1";
    private ConcurrentMap<String, ConfigurationFactory> configFactoryByQueueId;
    private ExecutorService executorService;
    private static final int FIXED_THREAD_POOL = 50;

    private final RPCRendererUtils rendererUtils;

    public JuniperRestServiceImpl(final DataBroker dataBroker) {
        this.rendererUtils = new RPCRendererUtils(dataBroker);
    }

    public void start() {
        executorService = Executors.newFixedThreadPool(FIXED_THREAD_POOL);
        configFactoryByQueueId = Maps.newConcurrentMap();
    }

    @Override
    public <T extends DataObject> void sendConfiguration(List<T> dataflowList, Boolean isADeleteSchema) {
        dataflowList.forEach((T dataFlow) -> {
            if (dataFlow instanceof EvpnDataflowQueue) {
                final EvpnDataflowQueue evpnQueue = (EvpnDataflowQueue) dataFlow;
                final String queueId = evpnQueue.getId();
                evpnQueue.getEvpnDataflows().forEach(evpnDataflow -> extractEvpnDataflow(queueId, evpnDataflow, isADeleteSchema));
                final RequestTask addConfigTask = new RequestTask(queueId, ConfigurationFactory::generateCompiledCommitByDevice);
                executorService.submit(addConfigTask);
            }
            if (dataFlow instanceof SwitchInterfaceStatus) {
                final SwitchInterfaceStatus switchInterfaceStatus = (SwitchInterfaceStatus) dataFlow;
                final String switchId = switchInterfaceStatus.getSwitchId();
                extractSwitchInterfaceDataflow(switchId, switchInterfaceStatus, isADeleteSchema);
                final RequestTask addInterfaceConfigs = new RequestTask(switchId, ConfigurationFactory::generateCompiledCommitByDevice);
                executorService.submit(addInterfaceConfigs);
            }
            if (dataFlow instanceof SwitchInfo) {
                final SwitchInfo switchInfo = (SwitchInfo) dataFlow;
                final String switchId = switchInfo.getName().getValue();
                extractSwitchInfo(switchInfo, isADeleteSchema);
                final RequestTask configSwitchInfos = new RequestTask(switchId, ConfigurationFactory::generateCommitByDevice);
                executorService.submit(configSwitchInfos);
            }
        });
    }

    private synchronized void extractEvpnDataflow(final String queueId,
                                                  final EvpnDataflow evpnDataflow,
                                                  final Boolean isADeleteSchema) {
        final ConfigurationFactory configFactory = getConfigFactory(queueId);

        final Map<String, Integer> vlanNameById = extractVlanNameById(evpnDataflow.getVlanInfo());
        final Set<Integer> vlanIds = Sets.newConcurrentHashSet();
        vlanIds.addAll(vlanNameById.values());

        final Configuration configuration = new Configuration();
        configuration.configureEvpn(vlanIds);
        configuration.configurePolicyCommunityAccept(vlanIds);

        final DeviceDetails deviceDetails = new DeviceDetails(
                evpnDataflow.getHttpIp(),
                evpnDataflow.getHttpPort(),
                evpnDataflow.getUserName(),
                evpnDataflow.getPassword());
        configFactory.orderConfigurationByDevice(configuration, deviceDetails, isADeleteSchema);
        configFactoryByQueueId.put(queueId, configFactory);
    }

    public synchronized void extractSwitchInterfaceDataflow(final String switchId,
                                                            final SwitchInterfaceStatus interfacesStatus,
                                                            final Boolean isADeleteSchema) {
        Preconditions.checkNotNull(interfacesStatus);
        final Map<String, Map<String, Set<VlanInfo>>> vlanNamesByInterfaceAndSwitch = Maps.newConcurrentMap();

        interfacesStatus.getSwitchInterfaceDetails().forEach(interfaceDetails -> {
            vlanNamesByInterfaceAndSwitch.put(switchId, Maps.newConcurrentMap());
            final String interfaceName = interfaceDetails.getSwitchInterfaceName();
            vlanNamesByInterfaceAndSwitch.get(switchId).put(interfaceName, Sets.newConcurrentHashSet());
            interfaceDetails.getAggregatedVlans().forEach(aggregatedVlans -> {
                final VlanInfoBuilder vlanInfoBuilder = new VlanInfoBuilder();
                vlanInfoBuilder.setVlanName(aggregatedVlans.getVlanName());
                vlanInfoBuilder.setVlanId(aggregatedVlans.getVlanId());
                vlanNamesByInterfaceAndSwitch.get(switchId).get(interfaceName).add(vlanInfoBuilder.build());
            });
        });

        vlanNamesByInterfaceAndSwitch.forEach((switchName, vlansByInterface) ->
                vlansByInterface.forEach((switchInterface, vlans) -> {
                    final SwitchInfo switchInfo = rendererUtils.retrieveSwitchInfo(switchName);
                    final DeviceDetails deviceDetails = extractDeviceDetails(switchInfo);
                    final Configuration configuration = new Configuration();

                    final Map<String, Integer> vlanIdByName = Maps.newConcurrentMap();
                    vlans.forEach(vlanInfo -> vlanIdByName.put(vlanInfo.getVlanName(), vlanInfo.getVlanId().intValue()));
                    configuration.configureInterfaceVlans(switchInterface, vlanIdByName.keySet());
                    configuration.configureVxlans(vlanIdByName);

                    final ConfigurationFactory configFactory = getConfigFactory(switchId);
                    configFactory.orderConfigurationByDevice(configuration, deviceDetails, isADeleteSchema);
                    configFactoryByQueueId.put(switchId, configFactory);
                }));
    }

    private void extractSwitchInfo(final SwitchInfo switchInfo,
                                   final Boolean isADeleteSchema) {
        Preconditions.checkNotNull(switchInfo);

        final DeviceDetails deviceDetails = extractDeviceDetails(switchInfo);
        final Configuration configuration = new Configuration();

        configuration.configureEvpnBasics();
        configuration.configureSwitchOptions(switchInfo.getLoopbackIp());
        final ConfigurationFactory configFactory = getConfigFactory(switchInfo.getName().getValue());

        configFactory.orderSingleConfigurationByDevice(configuration, deviceDetails, isADeleteSchema);
        configFactoryByQueueId.put(switchInfo.getName().getValue(), configFactory);
    }

    private DeviceDetails extractDeviceDetails(final SwitchInfo switchInfo) {
        final DeviceDetails deviceDetails = new DeviceDetails(
                switchInfo.getHttpIp(),
                switchInfo.getHttpPort(),
                switchInfo.getHttpUser(),
                switchInfo.getHttpPassword());
        return deviceDetails;
    }

    private ConfigurationFactory getConfigFactory(final String queueId) {
        ConfigurationFactory configFactory = configFactoryByQueueId.get(queueId);
        if (configFactory == null) {
            configFactory = new ConfigurationFactory();
        } else {
            LOG.debug("\nConfigFactoryQueue found for ID: {}", queueId);
        }
        return configFactory;
    }

    private HttpURLConnection retrieveHttpPostConnection(final String httpIp,
                                                         final String httpPort,
                                                         final String user,
                                                         final String password) {
        HttpURLConnection connection = null;
        try {
            LOG.debug("\nHttpConnection not found, creating a new one.");
            String URL = BASE_URL.replace("$ip", httpIp);
            URL = URL.replace("$port", httpPort);
            URL = URL.replace("$parameters", STOP_ON_ERROR);
            connection = retrieveBaseConnection(URL, createJuniperBasicAuth(user, password));
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(POST_REQUEST);
        } catch (ProtocolException | JuniperRPCException e) {
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

    //TODO: Change to use callable
    private class RequestTask extends Thread {

        final String queueId;
        final TaskAction taskAction;

        RequestTask(final String queueId,
                    final TaskAction taskAction) {
            this.queueId = queueId;
            this.taskAction = taskAction;
        }

        @Override
        public void run() {
            final ConfigurationFactory configFactory = configFactoryByQueueId.remove(queueId);
            if (configFactory != null) {
                final Map<DeviceDetails, String> commitByDevice = taskAction.evaluateTask(configFactory);
                commitByDevice.keySet().forEach(deviceDetails -> {
                    final String commit = commitByDevice.get(deviceDetails);
                    final HttpURLConnection connection = retrieveHttpPostConnection(
                            deviceDetails.getHttpIp(),
                            deviceDetails.getHttpPort(),
                            deviceDetails.getUserName(),
                            deviceDetails.getPassword());
                    try {
                        connection.getOutputStream().write(commit.getBytes(StandardCharsets.UTF_8));
                        connection.getOutputStream().close();
                        LOG.debug("\nResponse Info: {}", connection.getResponseCode());
                        LOG.info("\nResponse message: {}", connection.getResponseMessage());
                    } catch (IOException e) {
                        LOG.error(e.getMessage());
                    }
                });
            } else {
                LOG.error("Could not retrieve commits for Queue with ID: {}", queueId);
            }
        }
    }

    private synchronized Map<String, Integer> extractVlanNameById(final List<VlanInfo> vlanInfos) {
        final Map<String, Integer> vlanNameById = Maps.newConcurrentMap();
        vlanInfos.forEach(vlanInfo -> {
            final Integer vlanId = vlanInfo.getVlanId().intValue();
            final String vlanName = vlanInfo.getVlanName();
            vlanNameById.put(vlanName, vlanId);
        });
        return vlanNameById;
    }

    public void stop() {
        executorService.shutdown();
        configFactoryByQueueId.clear();
    }
}
