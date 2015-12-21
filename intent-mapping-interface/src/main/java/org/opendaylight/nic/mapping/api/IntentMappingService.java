/*
 * Copyright (c) 2015 Hewlett Packard Entreprise, Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.mapping.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IntentMappingService extends AutoCloseable {

    /**
     * Index a map of objects based on a key.
     *
     * @param key
     *            OuterKey
     * @param objs
     *            A map of inner elements
     */
    public void add(String key, Map<String, String> objs);

    /**
     * Returns a map based on an indexed key.
     *
     * @param outerKey
     *            indexed key
     * @return A map of inner elements
     */
    public Map<String, String> get(String outerKey);

    /**
     * @return Return a collection with all the outerKeys
     */
    public Collection<String> keys();

    /**
     * @param outerKey
     *            indexed key
     * @return Return true if succeed, otherwise false
     */
    public  boolean delete(String outerKey);
}
