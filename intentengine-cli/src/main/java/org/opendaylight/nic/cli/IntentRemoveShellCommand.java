/*
 * Copyright (c) 2015 Ciena and others.  All rights reserved.
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

@Command(name = "remove", scope = "intent", description = "Removes an intent from the controller.")
public class IntentRemoveShellCommand extends OsgiCommandSupport {

    protected NicConsoleProvider provider;

    @Argument(index = 0, name = "id", description = "Intent Id", required = true, multiValued = false)
    String id;

    public IntentRemoveShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Object doExecute() throws Exception {

        Uuid uuid = Uuid.getDefaultInstance(id);
        if (provider.removeIntent(uuid))
            return String.format("Intent sucessfully removed (id: %s)", uuid.toString());
        else
            return String.format("Error removing intent (id: %s)", uuid.toString());
    }
}
