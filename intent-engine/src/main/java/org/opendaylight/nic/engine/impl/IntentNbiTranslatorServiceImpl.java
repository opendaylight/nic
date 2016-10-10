/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.service.TranslatorService;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentDefinitions;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentNbiData;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.intent.definitions.Associations;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.intent.definitions.ObjectGroups;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.intent.definitions.object.groups.MemberObjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.util.List;
import java.util.UUID;

public class IntentNbiTranslatorServiceImpl implements TranslatorService {

    private IntentMappingService mappingService;

    public IntentNbiTranslatorServiceImpl(final IntentMappingService mappingService) {
        this.mappingService = mappingService;
    }

    @Override
    public Intent fromIntentNbi(IntentNbiData intentNbiData) {
        final IntentDefinitions definitions = intentNbiData.getIntentDefinitions();
        final List<Associations> associationsList = definitions.getAssociations();
        final List<ObjectGroups> objectGroupsList = definitions.getObjectGroups();
        final UUID uuid = UUID.randomUUID();

        for(Associations associations : associationsList) {
        }

        for(ObjectGroups objectGroups : objectGroupsList) {
            final List<MemberObjects> memberObjectGroupses = objectGroups.getMemberObjects();
        }

        IntentBuilder intentBuilder = new IntentBuilder();
        intentBuilder.setId(new Uuid(uuid.toString()));
        return null;
    }

    @Override
    public IntentNbiData fromIntentNic(Intent intent) {
        return null;
    }

}
