/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 02/03/16.
 */
public class MappingServiceUtils {

    private static final String NO_KEY_FOUND_MESSAGE = "No key found in IntentMappingService for EndPointGroup with ID: ";

    private MappingServiceUtils() {}

    /**
     * Creates a hashmap of the mapping information map for every subject
     * @param intent intent with endpoint group
     * @param intentMappingService Mapping service interface
     * @return nested hashmap with mapping details of subjects
     */
    public static Map<String, Map<String, String>> extractSubjectDetails(final Intent intent,
                                                                         IntentMappingService intentMappingService)
    throws IntentElementNotFoundException {
        List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        Map<String, Map<String, String>> subjectsMapping = new HashMap<String, Map<String, String>>();
        for (String id : endPointGroups) {
            Map<String, String> values = intentMappingService.get(id);
            if( values != null && values.size() >0 ) {
                subjectsMapping.put(id, values);
            } else {
                throw new IntentElementNotFoundException(NO_KEY_FOUND_MESSAGE + id);
            }
        }
        return subjectsMapping;
    }
}
