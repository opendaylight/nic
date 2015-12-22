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

import java.util.Collection;

/**
 * Class to implement Label tree Map
 */

public class GraphMapImpl{

    /**
     * Labels incorporated: pga_label_tree
     *         parent
     *         children
     */
    protected IntentMappingService intentMappingService;

    public GraphMapImpl(IntentMappingService mappingSvc) {
        this.intentMappingService = mappingSvc;
    }

    //override add method to correct LabelImpl's parent and children declaration
    //using Gson serializer to convert obj to string to be passed to intentmappingservice
    public void addLabelChild(String key, String parent, String child) {
        LabelImpl label = new LabelImpl(parent, child, null);

        //use gson to convert label to json, then add to map
        Gson gson = new Gson();
        String object = gson.toJson(label); //convert to string (Json form) to be added to map

        intentMappingService.add(key, object); //add key/object to map

        //check parent's children to make sure new obj is part of parent
        Collection<String> parentJson = intentMappingService.retrieve(parent);
        boolean delete = intentMappingService.delete(parent); //remove parent objects since they will be replaced with updated forms
        if (parentJson != null && delete) {
            for (String parentIndex : parentJson) {
                LabelImpl parentObj = gson.fromJson(parentIndex, LabelImpl.class);
                parentObj.addChild(key); //add children to parent (key)
                //add back to map
                String parent2Map = gson.toJson(parentObj);
                intentMappingService.add(parent, parent2Map);
            }
        }

    }

    public void addLabelChildren(String key, String parent, String[] children) {
        LabelImpl label = new LabelImpl(parent, children, null);
        //use gson to convert label to json, then add to map
        Gson gson = new Gson();
        String object = gson.toJson(label); //convert to string (Json form) to be added to map

        intentMappingService.add(key, object); //add key/object to map

        //check parent's children to make sure new obj is part of parent
        Collection<String> parentJson = intentMappingService.retrieve(parent);
        boolean delete = intentMappingService.delete(parent); //remove parent objects since they will be replaced with updated forms
        if (parentJson != null && delete) {
            for (String parentIndex : parentJson) {
                LabelImpl parentObj = gson.fromJson(parentIndex, LabelImpl.class);
                parentObj.addChild(key); //add children to parent (key)
                //add back to map
                String parent2Map = gson.toJson(parentObj);
                intentMappingService.add(parent, parent2Map);

            }
        }
    }
}
