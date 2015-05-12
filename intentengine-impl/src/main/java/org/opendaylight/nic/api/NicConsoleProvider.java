//------------------------------------------------------------------------------
//  (c) Copyright 2015 Hewlett-Packard Development Company, L.P.
//
//  Confidential computer software. Valid license from HP required for
//  possession, use or copying.
//
//  Consistent with FAR 12.211 and 12.212, Commercial Computer Software,
//  Computer Software Documentation, and Technical Data for Commercial Items
//  are licensed to the U.S. Government under vendor's standard commercial
//  license.
//------------------------------------------------------------------------------
package org.opendaylight.nic.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

public interface NicConsoleProvider {

    public boolean addIntent(Intent intent);

    public boolean addIntents(Intents intents);

    public boolean removeIntent(Intent intent);

    public boolean removeIntents(Intents intents);

    public Intents listIntents();

    public Intent show(Uuid id);
}
