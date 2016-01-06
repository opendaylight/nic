/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

/**
 * Shell command to perform disable of Intents.
 */
@Command(name = "disable",
        scope = "intent",
        description = "Disable an intent to be handled by the Intent State Machine."
                + "\nExample: intent:disable [INTENT ID].")
public class IntentDisableShellCommand extends OsgiCommandSupport {

    protected NicConsoleProvider provider;

    @Argument(index = 0, name = "id", description = "Intent Id", required = true, multiValued = false)
    String id;

    public IntentDisableShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Object doExecute() throws Exception {
        String result;
        Uuid uuid = Uuid.getDefaultInstance(id);
        if(provider.disableIntent(uuid)) {
            result = String.format("Try to disable intent with ID: %s with success.!", id);
        } else {
            result = String.format("Error when try to disable intent with ID: %s.!", id);
        }
        return result;
    }
}
