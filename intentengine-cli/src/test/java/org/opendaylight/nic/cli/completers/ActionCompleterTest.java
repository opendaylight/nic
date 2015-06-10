/*
 * Copyright (c) 2015 NEC Corporation!
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli.completers;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit test for {@link ActionCompleter}
 */
public class ActionCompleterTest {
    /**
     * Object for the class ActionCompleter
     */
    ActionCompleter actionCompleter = new ActionCompleter();

    /**
     * String Declaration for Beginning of the string.
     */
    private static final String buffer = "Beginning";

    /**
     * An Integer Declaration for Position of the Cursor.
     */
    private static final int cursor = 1;

    /**
     * Test case for {@link ActionCompleter#complete()}.
     */
    @Test
    public void testComplete() {
        /**
         * Inserting elements to the List candidates.
         */
        List<String> candidate = new ArrayList<String>();
        candidate.add("candidateOne");
        candidate.add("candidateTwo");

        // Positive case - Valid inputs.
        try {
            actionCompleter.complete(buffer, cursor, candidate);
        } catch(Exception e) {
            Assert.fail("Invalid Inputs!");
        }

        // Failure case - Buffer String as empty and null.
        actionCompleter.complete("", cursor, candidate);
        actionCompleter.complete(null, cursor, candidate);

        // Failure case - Invalid cursor value.
        actionCompleter.complete(buffer, 0, candidate);

        // Failure case - Invalid List elements.
        List<String> candidateFailure = new ArrayList<String>();
        candidateFailure.add("");
        candidateFailure.add(null);
        actionCompleter.complete(buffer, cursor, candidateFailure);
    }

    /**
     * Unused objects eligible for garbage collection.
     */
    @After
    public void afterSetUp() {
        actionCompleter = null;
    }
}
