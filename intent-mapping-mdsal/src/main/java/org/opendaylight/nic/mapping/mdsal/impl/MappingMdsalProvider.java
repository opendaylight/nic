/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapping.mdsal.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.nic.api.IntentMappingService;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.Mappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.MappingsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.map.OuterMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.map.OuterMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.map.OuterMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.map.outer.map.InnerMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.map.outer.map.InnerMapBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingMdsalProvider implements IntentMappingService,
                                             BindingAwareProvider,
                                             DataChangeListener,
                                             AutoCloseable {

    protected ServiceRegistration<IntentMappingService> intentMappingServiceRegistration;
    private static final Logger LOG = LoggerFactory.getLogger(MappingMdsalProvider.class);
    private DataBroker dataBroker;
    private MdsalUtils mdsalUtils;
    // FIXME move to utils in MappingServiceIidFactory
    public static final InstanceIdentifier<Mappings> MAPPINGS_IID =
                                                         InstanceIdentifier
                                                             .builder(Mappings.class)
                                                             .build();

    public MappingMdsalProvider() {
    }

    public MappingMdsalProvider(DataBroker databroker) {
        this.dataBroker = databroker;
        this.mdsalUtils = new MdsalUtils(databroker);
    }

    @Override
    public void close() throws Exception {
        if (intentMappingServiceRegistration != null)
            intentMappingServiceRegistration.unregister();
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>,
                              DataObject> change) {
        // TODO folks might directly use the datastore to trigger datachanges
        // implement datachange would permit them to do so.
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        // Retrieve the data broker to create transactions
        dataBroker = session.getSALService(DataBroker.class);

        Mappings mappings = new MappingsBuilder().build();

        // Initialize default config data in MD-SAL data store
        initDatastore(LogicalDatastoreType.CONFIGURATION, MAPPINGS_IID, mappings);
    }

    private void initDatastore(LogicalDatastoreType store,
                               InstanceIdentifier<Mappings> iid,
                               Mappings mappings) {
        mdsalUtils.merge(store, iid, mappings);
        LOG.info("initDatastore: mappings data populated: {}", store, iid, mappings);
    }

    @Override
    public void add(String key, String obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addList(String key, List<String> objs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> retrieve(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns all the outkeys of the Mapping service.
     * @return keys of the OuterMap as a {@link Collection}
     */
    @Override
    public Collection<String> keys() {
        List<String> keys = new ArrayList<String>();
        // FIXME move to utils in MappingServiceIidFactory
        InstanceIdentifier<Mappings> fullMappings = InstanceIdentifier
                                                       .create(Mappings.class);
        Mappings mappings = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, fullMappings);
        for (OuterMap map: mappings.getOuterMap()) {
            keys.add(map.getKey().toString());
        }
        return keys;
    }

    @Override
    public String stringRepresentation(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(String key, Map<String, String> objs) {
        OuterMapKey mapKey = new OuterMapKey(key);
        // FIXME move to utils in MappingServiceIidFactory
        InstanceIdentifier<OuterMap> outerMapIid = InstanceIdentifier
                                                       .builder(Mappings.class)
                                                       .child(OuterMap.class, mapKey)
                                                       .build();
            List<InnerMap> innerMap = new ArrayList<>();
            for (String keyy : objs.keySet()) {
                String valuee = objs.get(keyy);
                InnerMap innerElement = new InnerMapBuilder()
                                                .setInnerKey(keyy)
                                                .setValue(valuee)
                                                .build();
                innerMap.add(innerElement);
            }
            OuterMap outerMap = new OuterMapBuilder()
                                        .setId(key)
                                        .setKey(mapKey)
                                        .setInnerMap(innerMap)
                                        .build();
            mdsalUtils.merge(LogicalDatastoreType.CONFIGURATION, outerMapIid, outerMap);
    }

    @Override
    public Map<String, String> get(String key) {
        Map<String, String> subjectMappings = new HashMap<>();
        // FIXME move to utils in MappingServiceIidFactory
        InstanceIdentifier<OuterMap> outerMapIid =
                                         InstanceIdentifier.builder(Mappings.class)
                                             .child(OuterMap.class,
                                                    new OuterMapKey(key))
                                             .build();
        List<InnerMap> listInnerMap = mdsalUtils.read(LogicalDatastoreType.CONFIGURATION,
                                                      outerMapIid).getInnerMap();
        for (InnerMap innerMap : listInnerMap) {
            subjectMappings.put(innerMap.getInnerKey(), innerMap.getValue());
        }
        return subjectMappings;
    }

    @Override
    public boolean delete(String key) {
        OuterMapKey mapKey = new OuterMapKey(key);
        InstanceIdentifier<OuterMap> outerMapIid = InstanceIdentifier
                                                       .builder(Mappings.class)
                                                       .child(OuterMap.class, mapKey)
                                                       .build();
        return mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, outerMapIid);
    }
}
