/*
 * Copyright (c) 2015 Hewlett Packard Entreprise, Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IntentMappingService extends AutoCloseable {
    void add(String key, String obj);

    /**
     * Index a map of objects based on a key.
     *
     * @param key
     *            OuterKey
     * @param objs
     *            A map of inner elements
     */
    void add(String key, Map<String, String> objs);

    void addList(String key, List<String> objs);

    Collection<String> retrieve(String key);

    /**
     * Returns a map based on an indexed key.
     *
     * @param outerKey
     *            indexed key
     * @return A map of inner elements
     */
    Map<String, String> get(String outerKey);

    Collection<String> keys();

    String stringRepresentation(String key);

    /**
     * @param outerKey
     *            indexed key
     * @return Return true if succeed, otherwise false
     */
    boolean delete(String outerKey);

    /** Removes all objects based on a key.
     *
     * @param key
     *          key
     * @return collection of values that existed under the key, before removal
     */

    Collection<String> remove(String key);

}
