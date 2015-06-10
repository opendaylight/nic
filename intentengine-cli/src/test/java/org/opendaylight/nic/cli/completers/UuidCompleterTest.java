/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli.completers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
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
     * create mock object for NicConsoleProvider class
     */
    @Mock private NicConsoleProvider provider;

    /**
     * create mock object for Intent class
     */
    @Mock private Intent intent;

    /**
     * create mock object for Uuid class
     */
    @Mock private Uuid uuid;

    /**
     * create object for UuidCompleter class
     */
    private UuidCompleter uuidCompleter;

    /**
     * An Integer declaration for position of the cursor.
     */
    private static final int CURSOR = 1;

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
        String id = UUID.randomUUID().toString();
        List<Intent> intentlist = new ArrayList<Intent>();
        intentlist.add(intent);
        Mockito.when(provider.listIntents(true)).thenReturn(intentlist);
        Mockito.when(uuid.getValue()).thenReturn(id);
        Mockito.when(intent.getId()).thenReturn(uuid);
        List<String> candidateList = new ArrayList<String>();
        candidateList.add("CandidateFirst");
        candidateList.add("CandidateSecond");
        uuidCompleter.complete(id, CURSOR, candidateList);
        uuidCompleter.complete("dummy", CURSOR, candidateList);
        uuidCompleter.complete(null, 0, candidateList);
        uuidCompleter.complete("", 0, candidateList);

        List<String> candidateFailureList = new ArrayList<String>();
        candidateFailureList.add("");
        candidateFailureList.add("null");
        uuidCompleter.complete(id, CURSOR, candidateFailureList);
    }

    /**
     * Unused objects eligible for garbage collection.
     */
    @After
    public void cleanUp() {
        uuidCompleter = null;
    }
}

