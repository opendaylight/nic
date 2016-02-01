/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapped;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.opendaylight.nic.mapping.api.IntentMappingService;

import java.util.Hashtable;
import java.util.Map;

public class MappedObject {
    protected String type;
    protected String key;
    protected Map<String,String> properties = new Hashtable<>();

    protected IntentMappingService mappingService;
    // FIXME this will always give a Unknown JSON format
    public MappedObject(){
        //this.type = mappingService.get()
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

    public static <T extends MappedObject> T extractMappedObjectByIndex(Map<String, String> mappedObject, Class<T> type, Integer index) throws JsonSyntaxException {
        Gson gson = new Gson();
        T object =  null;
        Integer currentIndex = 0;

        for (String value : mappedObject.values()) {
            if(currentIndex++ != index)
                continue;
            object =  gson.fromJson(value, type);
            break;
        }

        return type.cast(T.fromMappedObject(object));
    }

    public static <T extends MappedObject> T extractMappedObjectByKey(Map<String, String> mappedObject, Class<T> type, String key) throws JsonSyntaxException {
        Gson gson = new Gson();
        T object =  null;

        String json = mappedObject.get(key);
        if(json != null) {
            object = gson.fromJson(json, type);
            return type.cast(T.fromMappedObject(object));
        }

        return object;
    }

    public static <T extends MappedObject> T extractFirstMappedObject(Map<String, String> mappedObject, Class<T> type) throws JsonSyntaxException {
        Gson gson = new Gson();
        T object =  null;
        T obj = null;

        for (String value : mappedObject.values()) {
            object = gson.fromJson(value, type);
            break;
        }

        return type.cast(T.fromMappedObject(object));
    }

}