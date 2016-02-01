/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapped;

import com.google.gson.Gson;

import java.util.Hashtable;
import java.util.Map;

public class MappedObject {
    protected String type;
    protected String key;
    protected Map<String,String> properties = new Hashtable<>();

    public MappedObject(){

    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public void setType(String type) {
        this.type = type;
    }


    public String type() {return type;};
    public String key() {return key;};
    public String getProperty(String name) { return properties.get(name);}
    public static Object fromMappedObject(MappedObject obj) { return obj;};

    public static <T extends MappedObject> T extractMappedObjectByIndex(Map<String, String> mappedConstraint, Class<T> type, Integer index) {
        Gson gson = new Gson();
        T object =  null;
        Integer currentIndex = 0;

        for (String value : mappedConstraint.values()) {
            if(currentIndex++ != index)
                continue;
            object =  gson.fromJson(value, type);
            break;
        }

        return type.cast(T.fromMappedObject(object));
    }

    public static <T extends MappedObject> T extractMappedObjectByKey(Map<String, String> mappedConstraint, Class<T> type, String key) {
        Gson gson = new Gson();
        T object =  null;

        String json = mappedConstraint.get(key);
        if(json != null) {
            object = gson.fromJson(json, type);
            return type.cast(T.fromMappedObject(object));
        }

        return object;
    }

    public static <T extends MappedObject> T extractFirstMappedObject(Map<String, String> mappedConstraint, Class<T> type) {
        Gson gson = new Gson();
        T object =  null;

        for (String value : mappedConstraint.values()) {
            object =  gson.fromJson(value, type);
            break;
        }

        return type.cast(T.fromMappedObject(object));
    }

}