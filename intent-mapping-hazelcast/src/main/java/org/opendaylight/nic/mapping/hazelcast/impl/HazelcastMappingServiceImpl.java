/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapping.hazelcast.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

import org.opendaylight.nic.mapping.api.IntentMappingService;

public class HazelcastMappingServiceImpl implements IntentMappingService {

    private MultiMap<String, String> multiMap;

    @Override
    public void close() throws Exception {
        multiMap.clear();
        multiMap = null;
    }

    public void init() {
        Config config = new Config();
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        multiMap = instance.getMultiMap("Intent-Mapping-Service");
    }

    private MultiMap<String, String> getMultiMap() {
        if (multiMap == null) {
            init();
        }

        return multiMap;
    }

    @Override
    public Collection<String> keys() {
        return getMultiMap().keySet();
    }

    @Override
    public void add(String key, Map<String, String> objs) {

        delete(key);

        if (objs != null) {
            for (String s : objs.values()) {
                getMultiMap().put(key, s);
            }
        }
    }

    @Override
    public Map<String, String> get(String outerKey) {
        Map<String, String> result = new HashMap<>();

        Integer index = 0;
        for (String s : getMultiMap().get(outerKey)) {
            result.put(index.toString(), s);
            index++;
        }

        return result;
    }

    @Override
    public boolean delete(String outerKey) {

        if(outerKey != null)
            return false;

        if (outerKey.isEmpty()) {
            return true;
        }

        if (getMultiMap().get(outerKey) != null) {
            getMultiMap().clear();
        }

        return true;
    }
}
