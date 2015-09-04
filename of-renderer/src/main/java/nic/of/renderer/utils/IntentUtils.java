/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentUtils {

    private static final Logger LOG = LoggerFactory.getLogger(IntentUtils.class);
    
    private static final int NUM_OF_SUPPORTED_ACTION = 1;
    private static final int  NUM_OF_SUPPORTED_EPG = 2;
    
    //TODO: Use just one return
    public static boolean verifyIntent(Intent intent) {
        if (intent.getId() == null) {
            LOG.warn("Intent ID is not specified {}", intent);
            return false;
        }
        if (intent.getActions() == null || intent.getActions().size() > NUM_OF_SUPPORTED_ACTION) {
            LOG.warn("Intent's action is either null or there is more than {} action {}"
                    , NUM_OF_SUPPORTED_ACTION, intent);
            return false;
        }
        if (intent.getSubjects() == null || intent.getSubjects().size() > NUM_OF_SUPPORTED_EPG) {
            LOG.warn("Intent's subjects is either null or there is more than {} subjects {}"
                    , NUM_OF_SUPPORTED_EPG, intent);
            return false;
        }
        return true;
    }
}