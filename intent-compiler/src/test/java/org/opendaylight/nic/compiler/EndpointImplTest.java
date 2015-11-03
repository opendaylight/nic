/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test class for {@link EndpointImpl}.
 */
public class EndpointImplTest {

    /**
     * Object for the class EndpointImpl.
     */
    private EndpointImpl endPointImpl, endPointImpl2, endPointImpl3, endPointImpl4,
            endPointTest;

    private final String IP_ADDRESS = "10.12.12.3";

    @Before
    public void setUp() throws Exception {
        endPointImpl = new EndpointImpl(InetAddress.getByName(IP_ADDRESS));
    }

    /**
     * Test to verify if getIpAddress() method work
     * for the EndpointImpl object.
     * @throws Exception
     */
    @Test
    public void testGetIpAddress() throws Exception {
        String expectedValue = "/10.12.12.3";
        String actualValue = endPointImpl.getIpAddress().toString();
        assertEquals("It should return IPAddress what we passed at the time of object creation in specified format",
                expectedValue, actualValue);
    }

    /**
     * Test to verify if equals() method work
     * for the EndpointImpl object.
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {
        endPointImpl2 = new EndpointImpl(null);
        endPointImpl3 = new EndpointImpl(InetAddress.getByName(IP_ADDRESS));
        endPointImpl4 = new EndpointImpl(null);
        assertTrue("EndPointImpl instances should be True, if both are having same content",
                endPointImpl.equals(endPointImpl));
        assertFalse("EndPointImpl instance should be False, if comparing with null", 
                endPointImpl.equals(null));
        assertEquals("EndPointImpl instances should be equal, if both are having same content",
                true, endPointImpl.equals(endPointImpl));
        assertEquals("EndPointImpl instances should be equal, if both are having same content",
                true, endPointImpl2.equals(endPointImpl4));
        assertEquals("EndPointImpl instance should not be equal, if comparing with null",
                false, endPointImpl.equals(null));
        assertEquals("EndPointImpl instance should not be equal, if comparing with other object type",
                false, endPointImpl.equals(new Object()));
        assertEquals("EndPointImpl instances should  not be equal, if both are having different content",
                false, endPointImpl2.equals(endPointImpl3));
        assertEquals("EndPointImpl instances should be equal, if both are having same content",
                true, endPointImpl.equals(endPointImpl3));
        assertEquals("EndPointImpl instances should  not be equal, if both are having different content",
                false, endPointImpl.equals(endPointImpl2));
    }

    /**
     * Test to verify if hashCode() method work
     * for the EndpointImpl object.
     */
    @Test
    public void testHashCode() {
        endPointTest = new EndpointImpl(null);
        endPointImpl.hashCode();
        assertEquals("If IPAddress is null it should return Zero", 0, endPointTest.hashCode());
    }

    /**
     * Test to verify if toString()  method work
     * for the EndpointImpl object.
     */
    @Test
    public void testToString() {
        String expectedString = "10.12.12.3";
        String actualString = endPointImpl.toString();
        assertEquals("It should return Host name what we passed at the time of object creation.",
                expectedString, actualString);
    }
}
