/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.nic.rpc.exception.JuniperRestException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by yrineu on 19/07/17.
 */
@PrepareForTest(RestValidations.class)
@RunWith(PowerMockRunner.class)
public class RestValidationsTest {

    @Test (expected = JuniperRestException.class)
    public void testNullRequest() {
        RestValidations.validateReceivedRequest(null);
    }

    @Test (expected = JuniperRestException.class)
    public void testInvalidRequest() {
        RestValidations.validateReceivedRequest("/192.1681111123::");
    }

    @Test (expected = JuniperRestException.class)
    public void testRequestWithInvalidIp() {
        RestValidations.validateReceivedRequest("/nic/192.166666");
    }

    @Test
    public void testWithValidRequest() {
        RestValidations.validateReceivedRequest("/nic/192.168.1.1");
    }
}
