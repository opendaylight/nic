/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.model;

import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.intent.definitions.ObjectGroups;

import java.util.Map;

public class ObjectGroupByMappingServices {

    private ObjectGroups objectGroups;
    private Map<String, String> mappedServices;

    public ObjectGroupByMappingServices(final ObjectGroups objectGroups, final Map<String, String> mappedServices) {
        this.objectGroups = objectGroups;
        this.mappedServices = mappedServices;
    }

    public Map<String, String> getMappedServices() {
        return mappedServices;
    }

    public ObjectGroups getObjectGroups() {
        return objectGroups;
    }
}
