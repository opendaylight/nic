/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.bgp.api.BGPRendererService;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev150305.ipv4.routes.Ipv4Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev150305.ipv4.routes.ipv4.routes.Ipv4Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev150305.ipv4.routes.ipv4.routes.Ipv4RouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev150305.ipv4.routes.ipv4.routes.Ipv4RouteKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.PathId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.path.attributes.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.path.attributes.AttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.path.attributes.attributes.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev130919.path.attributes.attributes.as.path.SegmentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.ApplicationRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.ApplicationRibId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.ApplicationRibKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.TablesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.BgpOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.Ipv4AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.UnicastSubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.next.hop.c.next.hop.Ipv4NextHopCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.next.hop.c.next.hop.ipv4.next.hop._case.Ipv4NextHopBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yrineu on 17/05/17.
 */
public class BGPRendererServiceImpl implements BGPRendererService {
    private static final Logger LOG = LoggerFactory.getLogger(BGPRendererServiceImpl.class);

    private DataBroker dataBroker;
    private static final String APPLICATION_RIB_ID = "example-app-rib";

    public BGPRendererServiceImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void advertiseRoute(BgpDataflow bgpDataflow) {

        final Ipv4Prefix prefix = bgpDataflow.getPrefix();
        final Long pathId = bgpDataflow.getPathId();
        final Ipv4Address globalIp = bgpDataflow.getGlobalIp();
        final Ipv4Address originatorIp = bgpDataflow.getOriginatorIp();
        final List<AsNumber> asNumbers = Lists.newArrayList();
        bgpDataflow.getAsNumbers().forEach(asNumber -> asNumbers.add(asNumber.getAsNumber()));

        final Ipv4RouteBuilder ipv4RouteBuilder = new Ipv4RouteBuilder();
        final PathId path = new PathId(pathId);
        final Ipv4RouteKey ipv4RouteKey = new Ipv4RouteKey(path, prefix);

        ipv4RouteBuilder.setKey(ipv4RouteKey);
        ipv4RouteBuilder.setPathId(path);
        ipv4RouteBuilder.setPrefix(prefix);
        ipv4RouteBuilder.setAttributes(generateAttributes(globalIp, originatorIp, asNumbers));

        publishRoute(ipv4RouteKey, ipv4RouteBuilder.build());
    }

    private Attributes generateAttributes(final Ipv4Address globalIp,
                                          final Ipv4Address originatorIp,
                                          final List<AsNumber> asNumbers) {
        final AttributesBuilder attributesBuilder = new AttributesBuilder();
        final Long MULTI_EXIT_PREF = 100L;
        final Long MED = 0L;

        final Ipv4NextHopCaseBuilder nextHopCaseBuilder = new Ipv4NextHopCaseBuilder();
        final Ipv4NextHopBuilder nextHopBuilder = new Ipv4NextHopBuilder();
        final MultiExitDiscBuilder multiExitDiscBuilder = new MultiExitDiscBuilder();
        final LocalPrefBuilder localPrefBuilder = new LocalPrefBuilder();
        final OriginatorIdBuilder originatorIdBuilder = new OriginatorIdBuilder();
        final OriginBuilder originBuilder = new OriginBuilder();
        final SegmentsBuilder segmentsBuilder = new SegmentsBuilder();
        final AsPathBuilder asPathBuilder = new AsPathBuilder();

        segmentsBuilder.setAsSequence(asNumbers);
        asPathBuilder.setSegments(Lists.newArrayList(segmentsBuilder.build()));

        nextHopBuilder.setGlobal(globalIp);
        nextHopCaseBuilder.setIpv4NextHop(nextHopBuilder.build());
        multiExitDiscBuilder.setMed(MED);
        localPrefBuilder.setPref(MULTI_EXIT_PREF);
        originatorIdBuilder.setOriginator(originatorIp);
        originBuilder.setValue(BgpOrigin.Igp);

        attributesBuilder.setAsPath(asPathBuilder.build());
        attributesBuilder.setMultiExitDisc(multiExitDiscBuilder.build());
        attributesBuilder.setCNextHop(nextHopCaseBuilder.build());
        attributesBuilder.setOriginatorId(originatorIdBuilder.build());
        attributesBuilder.setOrigin(originBuilder.build());

        return attributesBuilder.build();
    }

    private InstanceIdentifier<Ipv4Routes> retrieveIpv4Identifier() {
        final ApplicationRibKey applicationRibKey = new ApplicationRibKey(new ApplicationRibId(APPLICATION_RIB_ID));
        final TablesKey tablesKey = new TablesKey(Ipv4AddressFamily.class, UnicastSubsequentAddressFamily.class);
        final InstanceIdentifier tableIdentifier = KeyedInstanceIdentifier.builder(ApplicationRib.class, applicationRibKey)
                .child(Tables.class, tablesKey).build();
        return tableIdentifier.child(Ipv4Routes.class);
    }

    private void publishRoute(final Ipv4RouteKey ipv4RouteKey, final Ipv4Route route) {
        MdsalUtils mdsalUtils = new MdsalUtils(dataBroker);
        mdsalUtils.put(LogicalDatastoreType.CONFIGURATION,
                retrieveIpv4Identifier().child(Ipv4Route.class,
                        ipv4RouteKey), route);
    }

    private void evaluateCleanup() {
        final InstanceIdentifier<Ipv4Routes> ipv4RouteIdentifier = retrieveIpv4Identifier();
        final ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        try {
            final Optional<Ipv4Routes> result = readOnlyTransaction.read(
                    LogicalDatastoreType.CONFIGURATION, ipv4RouteIdentifier).checkedGet();
            if (result.isPresent()) {
                dataBroker.newWriteOnlyTransaction().delete(LogicalDatastoreType.CONFIGURATION, ipv4RouteIdentifier);
            }
        } catch (ReadFailedException e) {
            LOG.error("\nUnable to evaluate cleanup at shutdown. {}", e.getMessage());
        }
    }

    @Override
    public void stop() {
        evaluateCleanup();
    }
}