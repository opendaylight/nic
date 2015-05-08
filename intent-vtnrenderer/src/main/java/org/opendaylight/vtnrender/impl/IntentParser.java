/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.vtnrender.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
/**
 * This class will parse and process the intents.
 */
public class IntentParser {

    /**
     * This method parse the intent and calls the VTN renderer
     * @param intents
     */
    public void IntentParserVtnRenderer(Intents intents) {
        String endPointSrc = "";
        String endPointDst = "";
        String action = "";
        for (Intent intent : intents.getIntent()) {
            if (intent.getStatus() != null) {
                if (intent.getSubjects().size() == 2) {
                    endPointSrc = intent.getSubjects().get(0).toString();
                    endPointDst = intent.getSubjects().get(1).toString();
                    if (intent.getActions() != null) {
                        action = intent.getActions().get(0).toString();
                        VtnRenderer vtnRenderer = new VtnRenderer();
                        vtnRenderer.rendering(endPointSrc, endPointDst, action);
                    }

                }
            }

        }
    }
}
