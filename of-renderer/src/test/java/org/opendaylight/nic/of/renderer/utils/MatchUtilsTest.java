/*
 * Copyright © 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertNotNull;

public class MatchUtilsTest {

    private MatchBuilder result = null;

    @Mock
    private MatchBuilder matchBuilderMock;

    @Mock
    private MacAddress macAddressMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void callPrivateConstructorsForCodeCoverage() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] classesToConstruct = {MatchUtils.class};
        for(Class<?> clazz : classesToConstruct)
        {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }
    }

    @Test
    public void testCreateEthMatch() {
        result = MatchUtils.createEthMatch(matchBuilderMock, macAddressMock, macAddressMock);
        Assert.assertNotNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void testThrowNullPointerException() {

        MatchUtils.createEthMatch(null, macAddressMock, macAddressMock);
        MatchUtils.createEthMatch(matchBuilderMock, null, macAddressMock);
        MatchUtils.createEthMatch(matchBuilderMock, macAddressMock, null);
        MatchUtils.createEthMatch(null, null, null);
    }
}