/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest({FlowUtils.class})
@RunWith(PowerMockRunner.class)
public class FlowUtilsTest {

    @Before
    public void setUp() {
        PowerMockito.mockStatic(FlowUtils.class, Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void callPrivateConstructorsForCodeCoverage() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] classesToConstruct = {FlowUtils.class};
        for(Class<?> clazz : classesToConstruct)
        {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }
    }

    @Test
    public void testCreateSetFieldDestinationMacAddressAction() throws Exception {
        ActionBuilder ab = mock(ActionBuilder.class);
        PowerMockito.whenNew(ActionBuilder.class).withNoArguments().thenReturn(ab);

        EthernetMatchBuilder ethernetMatchBuilder = mock(EthernetMatchBuilder.class);
        PowerMockito.whenNew(EthernetMatchBuilder.class).withNoArguments().thenReturn(ethernetMatchBuilder);

        when(ethernetMatchBuilder.setEthernetDestination(any(EthernetDestination.class))).thenReturn(ethernetMatchBuilder);

        when(ab.setOrder(any(Integer.class))).thenReturn(ab);
        PowerMockito.whenNew(ActionKey.class).withAnyArguments().thenReturn(mock(ActionKey.class));
        when(ab.withKey(any(ActionKey.class))).thenReturn(ab);

        SetFieldCaseBuilder setFieldCaseBuilder = mock(SetFieldCaseBuilder.class);
        PowerMockito.whenNew(SetFieldCaseBuilder.class).withNoArguments().thenReturn(setFieldCaseBuilder);
        when(setFieldCaseBuilder.setSetField(any(SetField.class))).thenReturn(setFieldCaseBuilder);
        when(ab.setAction(any(SetFieldCase.class))).thenReturn(ab);
        SetFieldBuilder setFieldBuilder = mock(SetFieldBuilder.class);
        PowerMockito.whenNew(SetFieldBuilder.class).withNoArguments().thenReturn(setFieldBuilder);
        when(setFieldBuilder.setProtocolMatchFields(any(ProtocolMatchFields.class))).thenReturn(setFieldBuilder);
        when(ethernetMatchBuilder.build()).thenReturn(mock(EthernetMatch.class));
        when(setFieldBuilder.build()).thenReturn(mock(SetField.class));
        when(setFieldCaseBuilder.build()).thenReturn(mock(SetFieldCase.class));

        Action action = mock(Action.class);
        when(ab.build()).thenReturn(action);
        assertEquals("Failed to return correct Action object", action, FlowUtils.createSetFieldDestinationMacAddress(0, "d2:00:1f:e5:8b:e4"));
    }
}
