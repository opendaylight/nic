/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.opendaylight.nic.api.IntentMappingService;


import java.util.Collection;

/**
 * Class to implement Label tree Map*/

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
        LabelImpl label = new LabelImpl(parent, child);

        //use gson to convert label to json, then add to map
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String object = gson.toJson(label); //convert to string (Json form) to be added to map

        intentMappingService.add(key, object); //add key/object to map

        //check parent's children to make sure new obj is part of parent
        Collection<String> parentJson = intentMappingService.retrieve(parent);
        intentMappingService.remove(parent); //remove parent objects since they will be replaced with updated forms
        if (parentJson != null) {
            for (String s : parentJson) {
                LabelImpl parentObj = gson.fromJson(s, LabelImpl.class);
                parentObj.addChild(key); //add children to parent (key)
                //add back to map
                String object2 = gson.toJson(parentObj);
                intentMappingService.add(parent, object2);

            }
        }

    }

    public void addLabelChildren(String key, String parent, String[] children) {
        LabelImpl label = new LabelImpl(parent, children);
        //use gson to convert label to json, then add to map
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String object = gson.toJson(label); //convert to string (Json form) to be added to map

        intentMappingService.add(key, object); //add key/object to map

        //check parent's children to make sure new obj is part of parent
        Collection<String> parentJson = intentMappingService.retrieve(parent);
        intentMappingService.remove(parent); //remove parent objects since they will be replaced with updated forms
        if (parentJson != null) {
            for (String s : parentJson) {
                LabelImpl parentObj = gson.fromJson(s, LabelImpl.class);
                parentObj.addChild(key); //add children to parent (key)
                //add back to map
                String object2 = gson.toJson(parentObj);
                intentMappingService.add(parent, object2);

            }
        }
    }


}
