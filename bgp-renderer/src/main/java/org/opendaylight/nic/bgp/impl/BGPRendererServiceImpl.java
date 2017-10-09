/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.impl;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.bgp.api.BGPRendererService;
import org.opendaylight.nic.bgp.service.rest.BgpPrefixRESTServices;
import org.opendaylight.nic.bgp.service.rest.RESTService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev150305.ipv4.routes.Ipv4Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.ApplicationRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.ApplicationRibId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.ApplicationRibKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.Tables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev130925.rib.TablesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.Ipv4AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev130919.UnicastSubsequentAddressFamily;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        final RESTService bgpPrefixRESTServices = new BgpPrefixRESTServices();
        bgpPrefixRESTServices.POST(bgpDataflow);
    }

    private InstanceIdentifier<Ipv4Routes> retrieveIpv4Identifier() {
        final ApplicationRibKey applicationRibKey = new ApplicationRibKey(new ApplicationRibId(APPLICATION_RIB_ID));
        final TablesKey tablesKey = new TablesKey(Ipv4AddressFamily.class, UnicastSubsequentAddressFamily.class);
        final InstanceIdentifier tableIdentifier = KeyedInstanceIdentifier.builder(ApplicationRib.class, applicationRibKey)
                .child(Tables.class, tablesKey).build();
        return tableIdentifier.child(Ipv4Routes.class);
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