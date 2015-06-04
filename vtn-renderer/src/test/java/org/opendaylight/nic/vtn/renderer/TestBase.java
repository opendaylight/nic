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
            "1098", "6e:4f:f7:215:c9", "6e:4f:f7:27:15:c9"};

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
            "10.0.0.50", "6e:4f:f7:27:15:c9", "10.0.0.3", "10.0.5.0", "0.0"};

    /**
     * List of valid source IPAddresses and MAC Addresses.
     */
    protected static final String[] VALID_DST_ADDRESS = { "10.0.0.2",
            "10.0.0.40", "4e:3f:f7:17:15:d8", "0.0"};

    /**
     * List of valid and Invalid Actions.
     */
    protected static final String[] ACTIONS = { "ALLOW", "BLOCK", "redirect" };

    /**
     * Default Tenant Name used.
     */
    protected static final String TENANT[] = { "vtnRenderer", "vtnRenderer2", "vtnRenderer3", "vtnRenderer4" };

    /**
     * List of bridge names used.
     */
    protected static final String[] BRIDGE = { "Default", "NonDefault" };

    /**
     * List of Flowcondition names.
     */
    protected static final String[] FLOW_CONDITION_NAME = { "cond_1", "cond_2",
            "cond_3", "match_any" };

    /**
     * List of Flowcondition action.
     */
    protected static final String[] FLOW_CONDITION_ACTION = { "PASS", "DROP" };

    /**
     * List of Intent EntityDescriptions.
     */
    protected static final String[] INTENT_ENTITY_DESCRIPTION={"Flow filter condition", "Flow filter condition2", "cond_1000110002", "cond_1000210001", "cond_1000110002"};

    /**
     * List of Intent EntityNames.
     */
    protected static final String[] INTENT_ENTITY_NAMES={"FlowFilter", "Condition_123", "cond2"};

    /**
     * List of Intent List Id's.
     */
    protected static final String[] INTENT_LIST_ID={"iId1", "id2", "iId2"};

    /**
     * Default name for Service.
     */
    protected static final String DEFAULT_SERVICE = "default";

    /**
     * Customized userfriendly messages.
     */
     protected static final String CUSTOM_MESSAGES[] ={"If Status is not indicating success, it should return false only",
            "If FlowCondition is not deleted, it should return false only",
            "If Tenant unable to created in VTNManager, it should return false only",
            "If FlowFilter is not deleted, it should return false only",
            "If unable to get Bridge status for given name, it should return false only",
            "If unable to create Bridge, it sould return false only",
            "If FlowFilter is not created, the Intent list should not updated",
            "If specified FlowCondition not exist, it should return false only",
            "If unable to delete default configuration, it should return false only",
            "If unable to delete specified Intent, Intent list should not updated",
            "For invalid IP, MAC addresses, it should return null instead of String",
            "If creation of defalult Tenant, Bridge, Vlan failed, it should return false only",
            "If unable to update VTN elements due to improper inputs, Intent list should not updated",
            "If unable to create FlowCondition due to improper inputs, it should return false only",
            "If unable to create VTN elements, Intent list should not updated"};
}
