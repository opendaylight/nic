/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.graph.api.GraphProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.util.List;

public class GraphManager implements GraphProviderService {
    /*
     * Categorizes the input intents based on label graph
     */

     protected NicConsoleProvider provider;

     Boolean isConfigurationData = false;

     public void createGraph(Intent intent) {
          /* Obtain the most recent list of intents to create graph */
          List<Intent> listIntents = provider.listIntents(isConfigurationData);

          if (listIntents.size() > 0) {
               /* create graph*/
          } else {
               String result = String.format("No intents found. Check the logs for more details.");
               /* keyword "result" to be used as a return message */
          }
     }
}
