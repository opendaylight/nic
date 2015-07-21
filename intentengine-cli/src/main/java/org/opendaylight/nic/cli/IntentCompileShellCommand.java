/*
 * Copyright (c) 2015 Ciena and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;

@Command(name = "compile",
         scope = "intent",
         description = "[EXPERIMENTAL] Compile all intents and provide the results after conflict resolution")
public class IntentCompileShellCommand extends OsgiCommandSupport {

    protected NicConsoleProvider provider;

    @Option(name = "-g",
            aliases = { "--graph" },
            description = "Show graph view (optional).\n-g / --graph <ENTER>",
            required = false,
            multiValued = false)
    Boolean showGraph = false;

    public IntentCompileShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Object doExecute() throws Exception {
        return provider.compile(showGraph);
    }
}
