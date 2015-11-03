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
import org.opendaylight.nic.impl.NicProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

@Command(name = "show", scope = "intent", description = "Shows detailed information about an intent.")
public class IntentShowShellCommand extends OsgiCommandSupport {

    protected NicConsoleProvider provider;

    private static final String VALUE = "   Value: %s\n";


    @Argument(index = 0, name = "id", description = "Intent Id", required = true, multiValued = false)
    String id;

    public IntentShowShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Object doExecute() throws Exception {
        StringBuilder sb = new StringBuilder();
        Uuid uuid = Uuid.getDefaultInstance(id);
        Intent intent = provider.getIntent(uuid);

        if (intent != null) {
            sb.append(String.format("Intent Id: <%s>\n", intent.getId().getValue()));
            sb.append(String.format("Subjects: \n"));
            for (Subjects subjects : intent.getSubjects()) {
                EndPointGroup endPointGroup = (EndPointGroup) subjects.getSubject();
                sb.append(String.format("   Order: %d\n", subjects.getOrder()));
                sb.append(String.format(VALUE , endPointGroup.getEndPointGroup().getName()));
                sb.append("\n");
            }

            sb.append(String.format("Actions: \n"));
            for (Actions actions : intent.getActions()) {
                Action action = actions.getAction();
                sb.append(String.format("   Order: %d\n", actions.getOrder()));
                if (action instanceof Allow) {
                    sb.append(String.format(VALUE , NicProvider.ACTION_ALLOW));
                } else if (action instanceof Block) {
                    sb.append(String.format(VALUE , NicProvider.ACTION_BLOCK));
                } else {
                    sb.append(String.format(VALUE , "UNKNOWN"));
                }
            }

            return sb.toString();

        } else {
            return String.format("No intent found. Check the logs for more details.");
        }
    }
}
