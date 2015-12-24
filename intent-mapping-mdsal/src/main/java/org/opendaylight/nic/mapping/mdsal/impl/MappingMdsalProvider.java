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
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.rev151111.Mappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.rev151111.MappingsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.rev151111.multimap.OuterMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.rev151111.multimap.OuterMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.rev151111.multimap.OuterMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.rev151111.multimap.outer.map.InnerMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.rev151111.multimap.outer.map.InnerMapBuilder;
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

    //FIXME extract this constant to the Utils bundle
    public static final InstanceIdentifier<Mappings> MAPPINGS_IID =
                                                        InstanceIdentifier
                                                            .builder(Mappings.class)
                                                            .build();

    public MappingMdsalProvider() {
    }

    @Override
    public void close() throws Exception {
        if (intentMappingServiceRegistration != null)
            intentMappingServiceRegistration.unregister();
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        // Retrieve the data broker to create transactions
        dataBroker = session.getSALService(DataBroker.class);
        this.mdsalUtils = new MdsalUtils(dataBroker);

        Mappings mappings = new MappingsBuilder().build();

        // Initialize default config data in MD-SAL data store
        initDatastore(LogicalDatastoreType.CONFIGURATION, MAPPINGS_IID, mappings);
        // Initialize the Operational datastore so that we can copy data to it
        // in the future.
        initDatastore(LogicalDatastoreType.OPERATIONAL, MAPPINGS_IID, mappings);
    }

    private void initDatastore(LogicalDatastoreType store,
                               InstanceIdentifier<Mappings> iid,
                               Mappings mappings) {
        mdsalUtils.put(store, iid, mappings);
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

    @Override
    public Collection<String> keys() {
        Collection<String> keys = new ArrayList<String>();
        Mappings mappings = mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, MAPPINGS_IID);
        List<OuterMap> outerMap = mappings.getOuterMap();
        if (outerMap != null && !outerMap.isEmpty()) {
            for (OuterMap map: outerMap) {
                keys.add(map.getKey().getId());
            }
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
        InstanceIdentifier<OuterMap> outerMapIid = InstanceIdentifier
                                                        .builder(Mappings.class)
                                                       .child(OuterMap.class, mapKey)
                                                       .build();
        List<InnerMap> innerMap = new ArrayList<>();
        for (String keyy : objs.keySet()) {
            String valuee = objs.get(keyy);
            InnerMap innerElement = new InnerMapBuilder().setInnerKey(keyy).setValue(valuee).build();
            innerMap.add(innerElement);
        }
        OuterMap outerMap = new OuterMapBuilder().setId(key).setKey(mapKey).setInnerMap(innerMap).build();
        // FIXME Copy data from config to oper on the datachange
        mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, outerMapIid, outerMap);
    }

    @Override
    public Map<String, String> get(String key) {
        Map<String, String> subjectMappings = new HashMap<>();
        InstanceIdentifier<OuterMap> outerMapIid = InstanceIdentifier
                                                       .builder(Mappings.class)
                                                       .child(OuterMap.class,
                                                              new OuterMapKey(key))
                                                       .build();
        // We want the Operational data
        //FIXME use datachange to sync config datachange to oper
        OuterMap outerMap = mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, outerMapIid);

        if (outerMap != null && outerMap.getInnerMap() != null && !outerMap.getInnerMap().isEmpty()) {
            for (InnerMap innerMap : outerMap.getInnerMap()) {
                subjectMappings.put(innerMap.getInnerKey(), innerMap.getValue());
            }
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
