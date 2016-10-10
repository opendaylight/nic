/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.service;

import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentNbiData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public interface TranslatorService {

    Intent fromIntentNbi(IntentNbiData intentNbiData);

    IntentNbiData fromIntentNic(Intent intent);
}
