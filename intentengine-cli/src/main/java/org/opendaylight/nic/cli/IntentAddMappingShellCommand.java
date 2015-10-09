/*
 * Copyright (c) 2015 Hewlett-Packard and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.mapping.api.IntentMappingService;

@Command(name = "map",
         scope = "intent",
         description = "List current state of the mapping service."
                 + "\nExamples: --list [ENTER], to retrieve all keys"
                 + "\n          <KEY> [ENTER], to retrieve contents")
public class IntentAddMappingShellCommand extends OsgiCommandSupport {

    protected IntentMappingService service = null;

    @Option(name = "-l",
            aliases = { "--list" },
            description = "List Mapping Service.\n-l / --list <regular expression> [ENTER]",
            required = true,
            multiValued = false)
    String list = "";

    @Option(name = "-p",
            aliases = { "--print" },
            description = "Print Mapping Service.\n-p / --print [ENTER]",
            required = false,
            multiValued = false)
    boolean print = false;

    public IntentAddMappingShellCommand(IntentMappingService service) {
        this.service = service;
    }

    @Override
    protected Object doExecute() throws Exception {

        StringBuilder builder = new StringBuilder();

        if (service == null) {
            return "Mapping service not available";
        }

        if (list.isEmpty()) {
            list = "*";
        }

        if (list.equalsIgnoreCase("*")) {
            for (String key : service.keys()) {
                buildOutput(builder, key);
            }
        } else {
            for (String key : service.keys()) {
                if (!key.matches(list)) {
                    continue;
                }
                buildOutput(builder, key);
            }
        }

        return builder;
    }

    private void buildOutput(StringBuilder builder, String key) {
        if (!print) {
            printKeys(builder, key);
        } else {
            printContents(builder, key);
        }
    }

    private void printKeys(StringBuilder builder, String key) {
        if (builder.toString().isEmpty()) {
            builder.append(key);
        } else {
            builder.append(", " + key);
        }
    }

    private void printContents(StringBuilder builder, String key) {
        builder.append(key + " = {\n");
        builder.append(service.stringRepresentation(key));
        builder.append("}\n");
    }
}
