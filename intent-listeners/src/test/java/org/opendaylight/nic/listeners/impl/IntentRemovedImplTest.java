/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@PrepareForTest({IntentRemovedImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 11/01/16.
 */
public class IntentRemovedImplTest {

    private IntentRemovedImpl intentRemovedImplMock;

    @Mock
    private Intent intentMock;

    @Mock
    private Timestamp timestampMock;

    @Before
    public void setUp() throws Exception {
        intentRemovedImplMock = PowerMockito.spy(new IntentRemovedImpl(intentMock));
    }

    @Test
    public void testVerifyIntentRemovedTime() {
        Timestamp timestamp = intentRemovedImplMock.getTimeStamp();
        assertNotNull(timestamp);
    }

    @Test
    public void testIntentShouldBeEqualsToConstructorParameter() {
        Intent intent = intentRemovedImplMock.getIntent();
        assertEquals(intent, intentMock);
    }
}
