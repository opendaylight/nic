/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapping.api;

import java.util.List;
import java.util.Collection;

public interface IntentMappingService extends AutoCloseable {
    void add(String key, MappedObject obj);
    void addList(String key, List<MappedObject> objs);
    Collection<MappedObject> retrieve(String key);
    Collection<String> keys();
    String stringRepresentation(String key);
}
