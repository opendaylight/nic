/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import com.hazelcast.core.MultiMap;
import org.opendaylight.nic.mapping.impl.HazelcastMappingServiceImpl;

import java.util.Collection;

/**
 * Class to implement Label tree Map*/

public class GraphMapImpl extends HazelcastMappingServiceImpl{

    /**
     * Labels incorporated: pga_label_tree
     *         parent
     *         children
     */
    private MultiMap<String, LabelImpl> multiMap;

    //override add method to correct LabelImpl's parent and children declaration
    public void add(String key, String parent, String child) {
        LabelImpl obj = new LabelImpl(parent, child);
        getMultiMap().put(key, obj);
        //when adding a child, make sure it's parent has the child declared as a child
        Collection<LabelImpl> parentLabels = getMultiMap().get(parent);
        if (parentLabels != null)
            for (LabelImpl label : parentLabels)
                label.addChild(key);
    }

    public void add(String key, String parent, String[] children) {
        LabelImpl obj = new LabelImpl(parent, children);
        getMultiMap().put(key, obj);
        //when adding a child, make sure it's parent has the child declared as a child
        Collection<LabelImpl> parentLabels = getMultiMap().get(parent);
        if (parentLabels != null)
            for (LabelImpl label : parentLabels)
                label.addChild(key);

    }

    protected MultiMap<String, LabelImpl> getMultiMap() {
        if (multiMap == null) {
            init();
        }
        return multiMap;
    }
    //TODO: implement deletion of nodes
//    public void remove(String key) {
//        delete(getMultiMap().get(key));
//    }
}
