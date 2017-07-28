/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.utils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.bgp.utils.Utils;
import org.opendaylight.nic.common.transaction.exception.RemoveDataflowException;
import org.opendaylight.nic.common.transaction.exception.RemoveDelayconfigException;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.nic.utils.exceptions.PushDataflowException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.intent.evpns.IntentEvpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.intent.evpns.IntentEvpnKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.IntentIspPrefixes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.intent.isp.prefixes.IntentIspPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfoKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.ethernet.service.rev170613.EthernetServices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.ethernet.service.rev170613.ethernet.services.EthernetService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.ethernet.service.rev170613.ethernet.services.EthernetServiceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.router.group.rev700101.RouterGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.router.group.rev700101.router.groups.RouterGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.router.group.rev700101.router.groups.RouterGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.router.info.rev170613.RouterInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.router.info.rev170613.router.infos.RouterInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.router.info.rev170613.router.infos.RouterInfoKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflow.AsNumbers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflow.AsNumbersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflows.BgpDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflows.BgpDataflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.Dataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.DataflowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.DelayConfigs;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.DelayConfigsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.delay.configs.DelayConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.delay.configs.DelayConfigKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.EvpnDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.EvpnDataflowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflows.EvpnDataflow;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CommonUtils.class);
    private final InstanceIdentifier<Dataflows> DATAFLOW_IID = InstanceIdentifier.create(Dataflows.class);
    private final InstanceIdentifier<DelayConfigs> DELAY_CONFIGS_IID = InstanceIdentifier.create(DelayConfigs.class);
    private final InstanceIdentifier<EthernetServices> ETHERNET_SERVICES_IID = InstanceIdentifier.create(EthernetServices.class);
    private final InstanceIdentifier<RouterGroups> ROUTER_GROUPS_IID = InstanceIdentifier.create(RouterGroups.class);
    private final InstanceIdentifier<RouterInfos> ROUTER_INFO_IID = InstanceIdentifier.create(RouterInfos.class);
    private DataBroker dataBroker;

    public CommonUtils(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public List<Intent> retrieveIntents() {
        List<Intent> listOfIntents = Lists.newArrayList();
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<Intents> intents = tx.read(LogicalDatastoreType.CONFIGURATION,
                    IntentUtils.INTENTS_IID).checkedGet();

            if (intents.isPresent()) {
                listOfIntents = intents.get().getIntent();
            } else {
                LOG.debug("Intent tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("ListIntents: failed: {}", e.getMessage(), e);
        }
        LOG.debug("ListIntentsConfiguration: list of intents retrieved successfully");
        return listOfIntents;
    }

    public List<IntentLimiter> retrieveIntentLimiters() {
        List<IntentLimiter> listOfIntents = Lists.newArrayList();
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<IntentsLimiter> intents = tx.read(LogicalDatastoreType.CONFIGURATION,
                    IntentUtils.INTENTS_LIMITER_IDD).checkedGet();

            if (intents.isPresent()) {
                listOfIntents = intents.get().getIntentLimiter();
            } else {
                LOG.debug("\nIntentLimiter tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("\nListIntents: failed: {}", e.getMessage(), e);
        }
        LOG.info("\nListIntentsConfiguration: list of intents retrieved successfully");
        return listOfIntents;
    }

    public IntentLimiter retrieveIntentLimiter(final String id) {
        final List<IntentLimiter> intentLimiters = retrieveIntentLimiters();
        List<IntentLimiter> result = Lists.newArrayList();
        intentLimiters.forEach(intentLimiter -> {
            if (intentLimiter.getId().getValue().equals(id)) {
                result.add(intentLimiter);
            }
        });
        return result.iterator().next();
    }

    public List<IntentIspPrefix> retrieveIntentIspPrefixes() {
        List<IntentIspPrefix> listOfIntents = Lists.newArrayList();
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<IntentIspPrefixes> intents = tx.read(LogicalDatastoreType.CONFIGURATION,
                    IntentUtils.INTENT_ISP_PREFIX_IID).checkedGet();
            if (intents.isPresent()) {
                listOfIntents = intents.get().getIntentIspPrefix();
            } else {
                LOG.error("\nIntentISPPrefixes was empty!");
            }
        } catch (Exception e) {
            LOG.error("\nList IntentIspPrefixes failed: {}", e.getMessage());
        }
        return listOfIntents;
    }

    public IntentIspPrefix retrieveIntentIspPrefix(final String id) {
        final List<IntentIspPrefix> intentIspPrefixes = retrieveIntentIspPrefixes();
        List<IntentIspPrefix> result = Lists.newArrayList();
        intentIspPrefixes.forEach(intentIspPrefix -> {
            if (intentIspPrefix.getId().getValue().equals(id)) {
                result.add(intentIspPrefix);
            }
        });
        return result.iterator().next();
    }

    public List<Dataflow> retrieveDataflowList() {
        final List<Dataflow> result = Lists.newArrayList();
        Dataflows dataflows = retrieveDataflows();
        if (null != dataflows) {
            result.addAll(dataflows.getDataflow());
        }
        return result;
    }

    public Dataflows retrieveDataflows() {
        Dataflows result = null;
        try {
            ReadOnlyTransaction read = dataBroker.newReadOnlyTransaction();
            Optional<Dataflows> dataflows = read.read(LogicalDatastoreType.CONFIGURATION,
                    DATAFLOW_IID).checkedGet();
            if (dataflows.isPresent()) {
                result = dataflows.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("\nError when try to retrieve Dataflows. {}", e.getMessage());
        }
        return result;
    }

    public DelayConfigs retrieveDelayConfigs() {
        DelayConfigs result = null;
        try {
            ReadOnlyTransaction read = dataBroker.newReadOnlyTransaction();
            Optional<DelayConfigs> configs = read.read(LogicalDatastoreType.CONFIGURATION,
                    DELAY_CONFIGS_IID).checkedGet();
            if (configs.isPresent()) {
                result = configs.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("\nError when try to retrieve DelayConfigs. {}", e.getMessage());
        }
        return result;
    }

    public DelayConfig retrieveDelayConfig(final String id) {
        DelayConfigs configs = retrieveDelayConfigs();
        final Set<DelayConfig> delayConfigs = new HashSet<>();
        configs.getDelayConfig().forEach(delayConfig -> {
            if (delayConfig.getId().getValue().equals(id)) {
                delayConfigs.add(delayConfig);
            }
        });
        return delayConfigs.iterator().next();
    }

    public Dataflow retrieveDataflow(final String intentLimiterId) {
        Dataflow result = null;
        final List<Dataflow> dataflows = retrieveDataflowList();
        for (Dataflow dataflow : dataflows) {
            if (dataflow.getId().getValue().equals(intentLimiterId)) {
                result = dataflow;
                break;
            }
        }
        return result;
    }

    public void saveDataflow(final Dataflow dataflow) throws PushDataflowException {
        List<Dataflow> dataflows = retrieveDataflowList();
        final DataflowsBuilder dataflowsBuilder = new DataflowsBuilder();
        dataflows.add(dataflow);
        dataflowsBuilder.setDataflow(dataflows);
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, DATAFLOW_IID, dataflowsBuilder.build());
        CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = writeTransaction.submit();
        try {
            checkedFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
            throw new PushDataflowException(e.getMessage());
        }
    }

    public void saveDelayConfig(final DelayConfig delayConfig) {
        final DelayConfigs delayConfigs = retrieveDelayConfigs();
        final DelayConfigsBuilder delayConfigsBuilder = (delayConfigs != null ? new DelayConfigsBuilder(delayConfigs) : new DelayConfigsBuilder());
        final List<DelayConfig> delayConfigList = delayConfigsBuilder.getDelayConfig();
        if (null != delayConfigList) {
            delayConfigList.add(delayConfig);
            delayConfigsBuilder.setDelayConfig(delayConfigList);
        } else {
            delayConfigsBuilder.setDelayConfig(Lists.newArrayList(delayConfig));
        }
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, DELAY_CONFIGS_IID, delayConfigsBuilder.build());
        writeTransaction.submit();
    }

    public void removeDelayConfig(final String delayConfigId) throws RemoveDelayconfigException {
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        final InstanceIdentifier<DelayConfig> identifier = InstanceIdentifier.create(DelayConfigs.class).
                child(DelayConfig.class, new DelayConfigKey(Uuid.getDefaultInstance(delayConfigId)));
        writeTransaction.delete(LogicalDatastoreType.CONFIGURATION, identifier);
        CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = writeTransaction.submit();
        try {
            checkedFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            throw new RemoveDelayconfigException(e.getMessage());
        }
    }

    public void removeDataFlow(final String dataflowId) throws RemoveDataflowException {
        final InstanceIdentifier<Dataflow> identifier = InstanceIdentifier.create(Dataflows.class).
                child(Dataflow.class, new DataflowKey(Uuid.getDefaultInstance(dataflowId)));
        final WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.delete(LogicalDatastoreType.CONFIGURATION, identifier);
        CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = transaction.submit();
        try {
            checkedFuture.checkedGet();
        } catch (TransactionCommitFailedException e) {
            throw new RemoveDataflowException(e.getMessage());
        }
    }

    public Dataflow createFlowData(final IntentLimiter intent, final MeterId meterId) throws IntentInvalidException {
        final Ipv4Prefix sourceIp = intent.getSourceIp();
        DataflowBuilder dataflowBuilder = new DataflowBuilder();
        dataflowBuilder.setCreationTime(String.valueOf(System.currentTimeMillis()));
        dataflowBuilder.setIsFlowMeter(true);
        dataflowBuilder.setId(intent.getId());
        dataflowBuilder.setTimeout(intent.getDuration());
        dataflowBuilder.setDataflowMeterBandType(org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow
                .rev170309.Dataflow.DataflowMeterBandType.OFMBTDROP);
        dataflowBuilder.setMeterFlags(Dataflow.MeterFlags.METERKBPS);
        dataflowBuilder.setSourceIpAddress(sourceIp);
        dataflowBuilder.setRendererAction(Dataflow.RendererAction.ADD);
        dataflowBuilder.setBandwidthRate(intent.getBandwidthLimit());
        dataflowBuilder.setFlowType(Dataflow.FlowType.L3);
        dataflowBuilder.setMeterId(meterId.getValue().shortValue());
        dataflowBuilder.setStatus(Dataflow.Status.PROCESSING);
        dataflowBuilder.setIsRefreshable(true);
        return dataflowBuilder.build();
    }

    public Map<Ipv4Address, BgpDataflow> createBGPDataFlow(final IntentIspPrefix intent) throws IntentInvalidException {
        final EthernetService ethernetService = retrieveEthernetServiceBy(intent.getIspName());
        final Map<Ipv4Address, BgpDataflow> bgpDataflowByPeerIp = Maps.newConcurrentMap();

        retrieveRouterInfosByRouterGroup(ethernetService.getRouterGroupId().getValue()).forEach(routerInfo -> {
            BgpDataflowBuilder dataflowBuilder = new BgpDataflowBuilder();
            dataflowBuilder.setId(intent.getId());
            dataflowBuilder.setOriginatorIp(Ipv4Address.getDefaultInstance(routerInfo.getServicePeerIp()));
            dataflowBuilder.setPrefix(intent.getPrefix());
            dataflowBuilder.setGlobalIp(Ipv4Address.getDefaultInstance(routerInfo.getServicePeerIp()));
            dataflowBuilder.setPathId(routerInfo.getPeerPathId());
            final List<AsNumbers> asNumbers = Lists.newArrayList();
            final AsNumbersBuilder asNumbersBuilder = new AsNumbersBuilder();
            asNumbersBuilder.setAsNumber(AsNumber.getDefaultInstance(routerInfo.getAsn().toString()));
            asNumbers.add(asNumbersBuilder.build());
            dataflowBuilder.setAsNumbers(asNumbers);

            bgpDataflowByPeerIp.put(Ipv4Address.getDefaultInstance(
                    routerInfo.getServicePeerIp()), dataflowBuilder.build());
        });
        return bgpDataflowByPeerIp;
    }

    public BgpDataflows retrieveBgpDataflowTree() {
        BgpDataflows result = null;
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            Optional<BgpDataflows> bgpDataflowsOptional = transaction.read(
                    LogicalDatastoreType.CONFIGURATION,
                    Utils.BGP_DATAFLOW_IDENTIFIER).checkedGet();
            if (bgpDataflowsOptional.isPresent()) {
                result = bgpDataflowsOptional.get();
            }
            if (result == null) {
                final BgpDataflowsBuilder bgpDataflowsBuilder = new BgpDataflowsBuilder();
                bgpDataflowsBuilder.setBgpDataflow(Lists.newArrayList());
                result = bgpDataflowsBuilder.build();
            }
        } catch (ReadFailedException e) {
            LOG.error("\nError when try to retrieve BGP DataFlow Tree: {}", e.getMessage());
        }
        return result;
    }

    public void pushBgpDataflow(final BgpDataflow bgpDataflow) {
        try {
            final BgpDataflows bgpDataflowTree = retrieveBgpDataflowTree();
            bgpDataflowTree.getBgpDataflow().add(bgpDataflow);
            final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
            writeTransaction.put(LogicalDatastoreType.CONFIGURATION, Utils.BGP_DATAFLOW_IDENTIFIER, bgpDataflowTree);
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("\nError when try to push BGP dataflow: {}", e.getMessage());
        }
    }

    public EthernetService retrieveEthernetServiceBy(final String name) {
        EthernetService result = null;
        final InstanceIdentifier<EthernetService> identifier = ETHERNET_SERVICES_IID
                .child(EthernetService.class, new EthernetServiceKey(name));
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            final Optional<EthernetService> ethernetServiceOptional = transaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            if (ethernetServiceOptional.isPresent()) {
                result = ethernetServiceOptional.get();
            }
        } catch (ReadFailedException e) {
            LOG.error("\nError when try to retrieve Ethernet Services by Name {}", e.getMessage());
        }
        return result;
    }

    public List<EthernetService> retrieveEthernetServices() {
        final List<EthernetService> result = Lists.newArrayList();
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        try {
            Optional<EthernetServices> optional = transaction.read(
                    LogicalDatastoreType.CONFIGURATION,
                    ETHERNET_SERVICES_IID).checkedGet();
            if (optional.isPresent()) {
                result.addAll(optional.get().getEthernetService());
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public List<RouterInfo> retrieveRouterInfosByRouterGroup(final String id) {
        List<RouterInfo> result = Lists.newArrayList();
        final InstanceIdentifier<RouterGroup> identifier = ROUTER_GROUPS_IID
                .child(RouterGroup.class, new RouterGroupKey(Uuid.getDefaultInstance(id)));
        ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<RouterGroup> routerGroupOptional = transaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            final RouterGroup routerGroup = routerGroupOptional.get();
            routerGroup.getRoutersId().forEach(routerId ->
                    result.add(retrieveRouterInfoBy(routerId.getRouterId().getValue())));
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private RouterInfo retrieveRouterInfoBy(final String id) {
        RouterInfo result = null;
        final InstanceIdentifier<RouterInfo> identifier = ROUTER_INFO_IID
                .child(RouterInfo.class, new RouterInfoKey(Uuid.getDefaultInstance(id)));
        ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<RouterInfo> routerInfoOptional = transaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            result = routerInfoOptional.get();
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public IntentEvpn retrieveIntentVlans(final String id) {
        IntentEvpn result = null;
        final InstanceIdentifier<IntentEvpn> identifier = InstanceIdentifierUtils.INTENT_EVPN_IDENTIFIER
                .child(IntentEvpn.class, new IntentEvpnKey(id));
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();

        try {
            final Optional<IntentEvpn> intentVlanOptional = transaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            result = intentVlanOptional.get();
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public HostInfos retrieveHostInfos() {
        HostInfos result = null;
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<HostInfos> hostInfosOptional = transaction.read(
                    LogicalDatastoreType.CONFIGURATION,
                    InstanceIdentifierUtils.HOST_INFOS_IDENTIFIER).checkedGet();
            if (hostInfosOptional.isPresent()) {
                result = hostInfosOptional.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public SwitchInfo retrieveSwitchInfo(final String id) {
        SwitchInfo result = null;
        final InstanceIdentifier<SwitchInfo> identifier = InstanceIdentifierUtils.SWITCH_INFOS_IDENTIFIER
                .child(SwitchInfo.class, new SwitchInfoKey(new SwitchName(id)));
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<SwitchInfo> switchInfoOptional = transaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            result = switchInfoOptional.get();
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public EvpnDataflows retrieveEvpnDataflowsTree() {
        EvpnDataflows result = null;
        final InstanceIdentifier<EvpnDataflows> identifier = InstanceIdentifierUtils.EVPN_DATAFLOWS_IDENTIFIER;
        final ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<EvpnDataflows> evpnDataflowsOptional = readOnlyTransaction
                    .read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
            if (evpnDataflowsOptional.isPresent()) {
                result = evpnDataflowsOptional.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public void pushEvpnDataflow(final EvpnDataflow evpnDataflow) {
        EvpnDataflows evpnDataflows = retrieveEvpnDataflowsTree();
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        if (evpnDataflows == null) {
            final EvpnDataflowsBuilder evpnDataflowsBuilder = new EvpnDataflowsBuilder();
            evpnDataflowsBuilder.setEvpnDataflow(Lists.newArrayList(evpnDataflow));
            evpnDataflows = evpnDataflowsBuilder.build();
        } else {
            evpnDataflows.getEvpnDataflow().add(evpnDataflow);
        }
        writeTransaction.put(
                LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifierUtils.EVPN_DATAFLOWS_IDENTIFIER,
                evpnDataflows);
        try {
            writeTransaction.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error(e.getMessage());
        }
    }

    public static void waitUnlock() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }
}
