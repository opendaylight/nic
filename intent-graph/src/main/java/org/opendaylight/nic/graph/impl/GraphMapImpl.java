/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import com.google.gson.Gson;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to implement Label tree Map
 */

public class GraphMapImpl{

    /**
     * Labels incorporated: pga_label_tree
     *         parent
     *         children
     */
    private static final Logger LOG = LoggerFactory.getLogger(GraphMapImpl.class);
    protected IntentMappingService intentMappingService;

    public GraphMapImpl(IntentMappingService mappingSvc) {
        this.intentMappingService = mappingSvc;
    }

    /*
     * Override add method to correct LabelImpl's parent and children declaration
     * using Gson serializer to convert obj to string to be passed to intentmappingservice
     */
    public boolean addLabelChild(String key, String parent, String child) {
        LabelImpl label = new LabelImpl(parent, child, null);

        //use gson to convert label to json, then add to map
        Gson gson = new Gson();
        String object = gson.toJson(label); //convert to string (Json form) to be added to map
        Map<String, String> mapObject = new HashMap<>();
        mapObject.put(label.toString(), object);

        intentMappingService.add(key, mapObject); //add key/object to map

        //check parent's children to make sure new obj is part of parent
        Map<String, String> parentJson = intentMappingService.get(parent);
        boolean delete = intentMappingService.delete(parent); //remove parent objects since they will be replaced with updated forms
        if (parentJson != null && delete) {
            Map<String, String> mapParent = new HashMap<>();
            Integer index = 0;
            for (String parentIndex : parentJson.values()) {
                LabelImpl parentObj = gson.fromJson(parentIndex, LabelImpl.class);
                parentObj.addChild(key); //add children to parent (key)
                //add back to map
                String parent2Map = gson.toJson(parentObj);
                mapParent.put(index.toString(), parent2Map);
                index++;
            }
            intentMappingService.add(parent, mapParent);
        }
        return true;
    }

    public void addLabelChildren(String key, String parent, String[] children) {
        for (String child : children) {
            addLabelChild(key, parent, child);
        }
    }
}
