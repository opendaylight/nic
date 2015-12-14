/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapping.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.opendaylight.nic.api.IntentMappingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class HazelcastMappingServiceImpl implements IntentMappingService {

    private MultiMap<String, String> multiMap;

    protected ServiceRegistration<IntentMappingService> nicConsoleRegistration;

    @Override
    public void close() throws Exception {
        multiMap.clear();
        multiMap = null;
        nicConsoleRegistration.unregister();
    }

    protected BundleContext getBundleCtx() {
        return FrameworkUtil.getBundle(this.getClass()).getBundleContext();
    }

    public void init() {
        Config config = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        multiMap = instance.getMultiMap("Intent-Mapping-Service");

        BundleContext context = getBundleCtx();
        nicConsoleRegistration = context.registerService(IntentMappingService.class, this, null);
    }

    private MultiMap<String, String> getMultiMap() {
        if (multiMap == null) {
            init();
        }
        return multiMap;
    }

    @Override
    public void add(String key, String obj) {
        getMultiMap().put(key, obj);
    }

    @Override
    public void addList(String key, List<String> objs) {
        for (String obj : objs) {
            getMultiMap().put(key, obj);
        }
    }

    @Override
    public Collection<String> retrieve(String key) {
        return getMultiMap().get(key);
    }

    @Override
    public Collection<String> keys() {
        return getMultiMap().keySet();
    }

    @Override
    public String stringRepresentation(String key) {
        StringBuilder builder = new StringBuilder();
        Collection<String> list = getMultiMap().get(key);

        if (list == null) {
            return "";
        }

        for (String obj : list) {
            builder.append("    ");
            builder.append(obj.toString());
            builder.append("\n");
        }

        return builder.toString();
    }

    @Override
    public void add(String key, Map<String, String> objs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> get(String outerKey) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete(String outerKey) {
        throw new UnsupportedOperationException();
    }
}
