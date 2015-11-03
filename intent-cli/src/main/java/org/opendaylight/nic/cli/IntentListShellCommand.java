/*
 * Copyright (c) 2015 Ciena and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import java.util.List;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

@Command(name = "list",
         scope = "intent",
         description = "Lists all intents in the controller.")
public class IntentListShellCommand extends OsgiCommandSupport {

    protected NicConsoleProvider provider;

    @Option(name = "-c",
            aliases = { "--config" },
            description = "List Configuration Data (optional).\n-c / --config <ENTER>",
            required = false,
            multiValued = false)
    Boolean isConfigurationData = false;

    public IntentListShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Object doExecute() throws Exception {

        List<Intent> listIntents = provider.listIntents(isConfigurationData);

        if (listIntents.size() > 0) {
            StringBuilder sb = new StringBuilder();
            Integer counter = 1;
            for (Intent intent : listIntents) {
                sb.append(String.format("#%d - id: %s\n", counter, intent.getId().getValue()));
                counter++;
            }
            return sb.toString();
        } else {
            return String.format("No intents found. Check the logs for more details.");
        }
    }
}
