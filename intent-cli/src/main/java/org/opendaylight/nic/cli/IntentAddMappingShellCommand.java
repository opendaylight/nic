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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

import java.security.MessageDigest;

@Command(name = "map",
         scope = "intent",
         description = "List/Add/Delete current state from/to the mapping service."
                 + "\nExamples: --list, -l [ENTER], to retrieve all keys."
                 + "\n          --add-key <key> [ENTER], to add a new key with empty contents."
                 + "\n          --del-key <key> [ENTER], to remove a key with it's values."
                 + "\n          --add-key <key> --value [<value 1>, <value 2>, ...] [ENTER], to add a new key with some values (json format).")
public class IntentAddMappingShellCommand extends OsgiCommandSupport {

    protected IntentMappingService service = null;

    @Option(name = "-l",
            aliases = { "--list" },
            description = "List values associated with a particular key.\n-l / --filter <regular expression> [ENTER]",
            required = false,
            multiValued = false)
    String list = "";

    @Option(name = "--add-key",
            description = "Adds a new key to the mapping service.\n--add-key <key name> [ENTER]",
            required = false,
            multiValued = false)
    String addKey = "";

    @Option(name = "--del-key",
            description = "Deletes a key from the mapping service.\n--del-key <key name> [ENTER]",
            required = false,
            multiValued = false)
    String delKey = "";

    @Option(name = "--value",
            description = "Specifies which value should be added/delete from the mapping service.\n--value \"key => value\" ... --value \"key => value\" [ENTER]",
            required = false,
            multiValued = true)
    List<String> values = new ArrayList<String>();

    public IntentAddMappingShellCommand(IntentMappingService service) {
        this.service = service;
    }

    private String shaHash(String value){

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(value.getBytes("UTF-8"));
            byte[] digest = md.digest();

            return String.format("%064x", new java.math.BigInteger(1, digest));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    protected Object doExecute() throws Exception {

        StringBuilder builder = new StringBuilder();

        if (service == null) {
            return "Mapping service not available";
        }

        //for now we only support list all keys
        if(list.isEmpty())
            list = "*";
        else list = "*";

        if(!addKey.isEmpty()) {

            Map<String,String> map = new TreeMap<>();
            for(String s : values) {
                if (s == null)
                    continue;

                String [] mapValues = s.split("=>");

                String value = shaHash(mapValues[0]);

                if(mapValues != null && mapValues.length == 2)
                    map.put(mapValues[0].trim(), mapValues[1].trim());
            }

            service.add(addKey, map);
        }
        else if(!delKey.isEmpty()) {
            service.delete(delKey);
        }

        if (list.equalsIgnoreCase("*")) {
            for (String key : service.keys()) {
                buildOutput(builder, key);
            }
        } else {

            /* TODO: implement filters
            for (String key : service.keys()) {
                if (!key.matches(list)) {
                    continue;
                }
                buildOutput(builder, key);
            }
            */
        }

        return builder.toString();
    }

    private void buildOutput(StringBuilder builder, String key) {
        if (list.isEmpty()) {
            printKeys(builder, key);
        } else {
            printContents(builder, key);
        }
    }

    private void printKeys(StringBuilder builder, String key) {
        if (builder.toString().isEmpty()) {
            builder.append(key);
        } else {
            builder.append(", ")
                   .append(key);
        }
    }

    private void printContents(StringBuilder builder, String key) {
        Map<String, String> contents = service.get(key);

        builder.append(key)
               .append(" = [[ ")
               .append((contents != null) ? contents.toString() : "{}")
               .append(" ]]\n");
    }
}
