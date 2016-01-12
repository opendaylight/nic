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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;

@PrepareForTest({LinkDeletedImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class LinkDeletedImplTest {

    private LinkDeletedImpl linkDeletedMock;

    @Before
    public void setUp() {
        linkDeletedMock = PowerMockito.spy(new LinkDeletedImpl());
    }

    @Test
    public void testTimestampNotNull() {
        assertNotNull(linkDeletedMock.getTimeStamp());
    }


}
