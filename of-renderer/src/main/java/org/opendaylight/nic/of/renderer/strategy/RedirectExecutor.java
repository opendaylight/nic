/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
*/
package org.opendaylight.nic.of.renderer.strategy;

import org.opendaylight.nic.of.renderer.impl.RedirectFlowManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

/**
 * @author victor. Created on 11/09/16.
 */
public class RedirectExecutor implements ActionStrategy {

    private RedirectFlowManager redirectFlowManager;

    public RedirectExecutor(RedirectFlowManager redirectFlowManager) {
        this.redirectFlowManager = redirectFlowManager;
    }

    @Override
    public void execute(Intent intent, FlowAction flowAction)
            throws IntentInvalidException {
        redirectFlowManager.redirectFlowConstruction(intent, flowAction);
    }

}
