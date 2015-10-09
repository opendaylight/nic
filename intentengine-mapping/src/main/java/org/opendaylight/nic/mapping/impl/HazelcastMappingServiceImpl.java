/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapping.impl;

import org.opendaylight.nic.mapping.api.MappedObject;
import org.opendaylight.nic.mapping.api.IntentMappingService;

import java.util.List;
import java.util.Collection;

import org.opendaylight.nic.mapping.api.TypeHostname;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class HazelcastMappingServiceImpl implements IntentMappingService {

    private MultiMap<String, MappedObject> multiMap;

    protected ServiceRegistration<IntentMappingService> nicConsoleRegistration;

    @Override
    public void close() throws Exception {
        multiMap.clear();
        multiMap = null;
        nicConsoleRegistration.unregister();
    }

    public void init() {
        Config config = new Config();

        SerializationConfig serializationConfig = new SerializationConfig();

        SerializerConfig tsc = new SerializerConfig();
               // .setImplementation(new XmlSerializer()).
               //  setTypeClass(Object.class);

        //serializationConfig.ddSerializerConfig();

        //config.setSerializationConfig(serializationConfig);


        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        multiMap = instance.getMultiMap("Intent-Mapping-Service");

        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicConsoleRegistration = context.registerService(IntentMappingService.class, this, null);

        //default entry
        TypeHostname host = new TypeHostname();
        host.setHostname("bla");
        multiMap.put("host1", host);
        multiMap.put("host2", host);
        multiMap.put("user2", host);
        multiMap.put("raphael", host);
        multiMap.put("serverA", host);
    }

    private MultiMap<String, MappedObject> getMultiMap() {
        if (multiMap == null) {
            init();
        }
        return multiMap;
    }

    @Override
    public void add(String key, MappedObject obj) {
        getMultiMap().put(key, obj);
    }

    @Override
    public void addList(String key, List<MappedObject> objs) {
        for (MappedObject obj : objs) {
            getMultiMap().put(key, obj);
        }
    }

    @Override
    public Collection<MappedObject> retrieve(String key) {
        return getMultiMap().get(key);
    }

    @Override
    public Collection<String> keys() {
        return getMultiMap().keySet();
    }

    @Override
    public String stringRepresentation(String key) {
        StringBuilder builder = new StringBuilder();
        for (MappedObject obj : getMultiMap().get(key)) {
            builder.append("    " + obj.toString());
            builder.append("\n");
        }

        return builder.toString();
    }
}
