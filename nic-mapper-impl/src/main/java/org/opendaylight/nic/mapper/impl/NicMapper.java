/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapper.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.nic.mapper.api.NicMapperProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.mapper.rev150831.NicMapperKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NicMapper implements NicMapperProvider {
    private static final Logger LOG = LoggerFactory.getLogger(NicMapper.class);
    private HashMap<String,List<String>> map;
    
    public NicMapper() {
        map = new HashMap<String,List<String>>();
        init();
    }

    public void init() {
        List<String> valueList = new LinkedList<String>();
        valueList.add("testValue");
        map.put("test", valueList);
    }
    @Override
    public void close() throws Exception {
        LOG.info("NIC mapper closed");
    }

    @Override
    public List<String> getMappedValues(NicMapperKey key) {
        return map.get(key.getValue());
    }   
}
