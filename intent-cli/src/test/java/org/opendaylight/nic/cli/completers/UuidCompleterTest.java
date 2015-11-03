/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli.completers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

/**
 * JUnit test for {@link UuidCompleter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UuidCompleterTest {
    /**
     * create mock object for NicConsoleProvider class.
     */
    @Mock private NicConsoleProvider provider;

    /**
     * create mock object for Intent class.
     */
    @Mock private Intent intent;

    /**
     * create mock object for Uuid class.
     */
    @Mock private Uuid uuid;

    /**
     * create object for UuidCompleter class.
     */
    private UuidCompleter uuidCompleter;

    @Before
    public void init() throws Exception {
        uuidCompleter = new UuidCompleter(provider);
    }

    /**
     * Test method for {@link UuidCompleter#complete()}.
     *
     * @throws Exception  An error occurred
     */
    @Test
    public final void testComplete() throws Exception {
        final int success = 0;
        final int failure = -1;
        final int cursor = 1;
        final String validId = UUID.randomUUID().toString();
        final String invalidId = "c20bec-c874-4f31-bf9e-a88e56b05";
        final String invalidUUID = "Invalid uuid";
        final List<String> candidateList = new ArrayList<String>();
        final List<Intent> intentList = new ArrayList<Intent>();
        intentList.add(intent);

        when(provider.listIntents(true)).thenReturn(intentList);
        when(intent.getId()).thenReturn(uuid);
        when(uuid.getValue()).thenReturn(validId);

        /**
         * Failure case - passing invalid uuid.
         */
        assertEquals(invalidUUID, failure, uuidCompleter.complete(invalidId, cursor, candidateList));

        /**
         * Success case - passing valid uuid.
         */
        assertEquals(success, uuidCompleter.complete(validId, cursor, candidateList));

        /**
         * passing buffer string as null and empty.
         */
        assertEquals(success, uuidCompleter.complete(null, cursor, candidateList));
        assertEquals(success, uuidCompleter.complete("", cursor, candidateList));
    }

    /**
     * Unused objects eligible for garbage collection.
     */
    @After
    public void cleanUp() {
        uuidCompleter = null;
    }
}
