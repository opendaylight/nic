/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.api;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

public interface NicConsoleProvider extends AutoCloseable {

    boolean addIntent(Intent intent);

    boolean addIntents(Intents intents);

    boolean removeIntent(Uuid id);

    boolean removeIntents(List<Uuid> intents);

    List<Intent> listIntents(boolean isConfigurationDatastore);

    Intent getIntent(Uuid id);

    String compile();
}
