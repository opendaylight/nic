/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.model.ObjectGroupByMappingServices;
import org.opendaylight.nic.engine.service.MappingServiceTranslatorService;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.intent.definitions.ObjectGroups;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.intent.definitions.object.groups.MemberObjects;

import java.util.List;

public class MappingServiceTranslatorServiceImpl implements MappingServiceTranslatorService {

    private IntentMappingService mappingService;
    public MappingServiceTranslatorServiceImpl(final IntentMappingService mappingService) {
        this.mappingService = mappingService;
    }
    @Override
    public ObjectGroupByMappingServices fromObjectGroups(ObjectGroups objectGroups) {

        List<MemberObjects> memberObjectses = objectGroups.getMemberObjects();
        for(MemberObjects objects : memberObjectses) {
            //TODO: Retrieve and group retrieved services from Mapping service into ObjectGroupByMappingServices
        }
        return null;
    }
}
