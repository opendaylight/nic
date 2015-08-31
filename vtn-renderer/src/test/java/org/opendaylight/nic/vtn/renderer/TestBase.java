/*
 * Copyright (c) 2013-2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import org.junit.Assert;

/**
 * Base class for JUnit tests.
 */
public class TestBase extends Assert {

    /**
     * List of invalid source IPAddresses and MAC Addresses.
     */
    protected static final String[] INVALID_SRC_ADDRESS = { "10.0.50", "10.0",
            "1098", "6e:4f:f7:215:c9", "6e:4f:f7:27:15:c9" };

    /**
     * String value of encoded UUID.
     */
    protected static final String ENCODED_UUID = "hy7O5kn4QnqNJCf4Sj7L3A";
    /**
     * Boolean value true.
     */
    protected static final boolean BOOLEAN_TRUE = true;
    /**
     * Boolean value false.
     */
    protected static final boolean BOOLEAN_FALSE = false;
    /**
     * String value of Intent ID.
     */
    protected static final String INTENT_ID = "888ec35e-a93f-42ea-ae57-ffa1862677d0";

    /**
     * List of invalid destination IPAddresses and MAC Addresses.
     */
    protected static final String[] INVALID_DST_ADDRESS = { "10.0.40",
            "10.0..3", "10.0.2", "6e:4f:f7:2715:c9", "f7:27:15:c9",
            "4e:3f:f7:175:d8" };

    /**
     * List of valid source IPAddresses and MAC Addresses.
     */
    protected static final String[] VALID_SRC_ADDRESS = { "10.0.0.1",
            "10.0.0.50", "6e:4f:f7:27:15:c9", "10.0.0.3", "10.0.5.0", "0.0" };

    /**
     * List of valid source IPAddresses and MAC Addresses.
     */
    protected static final String[] VALID_DST_ADDRESS = { "10.0.0.2",
            "10.0.0.40", "4e:3f:f7:17:15:d8", "0.0" };

    /**
     * List of valid and Invalid Actions.
     */
    protected static final String[] ACTIONS = { "ALLOW", "BLOCK", "redirect",
            "PASS", "DROP" };

    /**
     * Default Tenant Name used.
     */
    protected static final String TENANT[] = { "vtnRenderer", "vtnRenderer2",
            "vtnRenderer3", "vtnRenderer4" };

    /**
     * List of bridge names used.
     */
    protected static final String[] BRIDGE = { "default", "NonDefault" };

    /**
     * List of flow direction.
     */
    protected static final String[] FLOW_DIRECTION = { "F", "R" };

    /**
     * List of Flowcondition names.
     */
    protected static final String[] FLOW_CONDITION_NAME = { "hy7O5kn4QnqNJCf4Sj7L3A_F_1", "hy7O5kn4QnqNJCf4Sj7L3A_R_2",
            "hy7O5kn4QnqNJCf4Sj7L3B_F_3", "match_any", "hy7O5kn4QnqNJCf4Sj7L3B_R_$" };

    /**
     * List of Flowcondition action.
     */
    protected static final String[] FLOW_CONDITION_ACTION = { "PASS", "DROP" };

    /**
     * List of Intent EntityDescriptions.
     */
    protected static final String[] INTENT_ENTITY_DESCRIPTION = {
            "Flow filter condition", "Flow filter condition2",
            "cond_1000110002", "cond_1000210001", "cond_1000110002" };

    /**
     * List of Intent EntityNames.
     */
    protected static final String[] INTENT_ENTITY_NAMES = { "FlowFilter",
            "Condition_123", "cond2" };

    /**
     * List of Intent List Id's.
     */
    protected static final String[] INTENT_LIST_ID = { "iId1", "id2", "iId2" };

    /**
     * Default name for Service.
     */
    protected static final String DEFAULT_SERVICE = "default";

    /**
     * List of Entity Descriptions.
     */
    protected static final String[] ENTITY_DESCRIPTION = { "cond_1000110002",
            "cond_1000210001", "INVALID_WRAPPER_DESCRIPTION",
            "Default_Description" };

    /**
     * List of Condition Names.
     */
    protected static final String[] CONDITION_NAME = { "hy7O5kn4QnqNJCf4Sj7L3A_F_1",
            "hy7O5kn4QnqNJCf4Sj7L3A_R_2" };
    /**
     * List of construct Condition Names.
     */
    protected static final String[] CONST_CONDITION_NAME = { "hy7O5kn4QnqNJCf4Sj7L3A_F",
            "hy7O5kn4QnqNJCf4Sj7L3A_R" };

    /**
     * List of Index values.
     */
    protected static final int[] INDEX = { 10, 11 };

    /**
     * String value of encoded UUID.
     */
    protected static final String ENCODED_UUID_FAILURE = "hy7O5kn4QnqNJCf4Sj";
}
