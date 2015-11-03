/*
 * Copyright (c) 2015 NEC Corporation!
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli.completers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for {@link ActionCompleter}.
 */
public class ActionCompleterTest {
    /**
     * Object for the class ActionCompleter.
     */
    ActionCompleter actionCompleter;

    /**
     * String Declaration for Beginning of the string.
     */
    private static final String BUFFER = "buffer";

    /**
     * An Integer Declaration for Position of the Cursor.
     */

    private static final int CURSOR = 1;

    /**
     * A collection of valid candidates.
     */
    private static List<String> candidate;


    /**
     * A collection of invalid candidates.
     */
    private static List<String> candidateFailure;

    @Before
    public void beforeSetUp() {
        actionCompleter = new ActionCompleter();
    }

    /**
     * Test case for {@link ActionCompleter#complete()}.
     */
    @Test
    public void testComplete() {

        final int success = 0;
        final int failure = -1;
        candidate = new ArrayList<String>();
        candidateFailure = new ArrayList<String>();

        /**
         * Insertion of Valid Candidates.
         */
        candidate.add("candidateOne");
        candidate.add("candidateTwo");
        candidate.add(null);

        /**
         * Success Scenario - Valid inputs.
         */
        assertEquals(success, actionCompleter.complete(BUFFER, CURSOR, candidate));

        /**
         * Buffer String as empty and null.
         */
        assertEquals(success, actionCompleter.complete("", CURSOR, candidate));
        assertEquals(success, actionCompleter.complete(null, CURSOR, candidate));

        /**
         * Invalid cursor value.
         */
        assertEquals(success, actionCompleter.complete(BUFFER, 0, candidate));

        /**
         * Failure Scenario - Invalid List elements.
         */
        assertEquals("Empty Candidate List", failure, (actionCompleter.complete(BUFFER, CURSOR, candidateFailure)));
    }

    /**
     * Unused objects eligible for garbage collection.
     */
    @After
    public void cleanUp() {
        actionCompleter = null;
        candidate = null;
        candidateFailure = null;
    }
}
