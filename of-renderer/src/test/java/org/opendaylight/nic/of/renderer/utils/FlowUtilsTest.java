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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
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
    public void testCreateMPLSAction() throws Exception {
        ActionBuilder ab = mock(ActionBuilder.class);
        PowerMockito.whenNew(ActionBuilder.class).withNoArguments().thenReturn(ab);
        Action action = mock(Action.class);
        when(ab.build()).thenReturn(action);
        when(ab.setOrder(any(Integer.class))).thenReturn(ab);
        PowerMockito.whenNew(ActionKey.class).withAnyArguments().thenReturn(mock(ActionKey.class));
        when(ab.setKey(any(ActionKey.class))).thenReturn(ab);

        //popLabel true case
        PopMplsActionBuilder popMplsActionBuilder = mock(PopMplsActionBuilder.class);
        when(popMplsActionBuilder.setEthernetType(any(Integer.class))).thenReturn(popMplsActionBuilder);
        PopMplsActionCaseBuilder popMplsActionCaseBuilder = mock(PopMplsActionCaseBuilder.class);
        PowerMockito.whenNew(PopMplsActionCaseBuilder.class).withNoArguments().thenReturn(popMplsActionCaseBuilder);
        when(popMplsActionCaseBuilder.setPopMplsAction(any(PopMplsAction.class))).thenReturn(popMplsActionCaseBuilder);
        when(popMplsActionBuilder.build()).thenReturn(mock(PopMplsAction.class));
        when(popMplsActionCaseBuilder.build()).thenReturn(mock(PopMplsActionCase.class));
        when(ab.setAction(any(PopMplsActionCase.class))).thenReturn(ab);
        assertEquals("Failed to return correct Action object", action, FlowUtils.createMPLSAction(0, true));

        // popLabel false case
        PushMplsActionBuilder pushMplsActionBuilder = mock(PushMplsActionBuilder.class);
        when(pushMplsActionBuilder.setEthernetType(any(Integer.class))).thenReturn(pushMplsActionBuilder);
        PushMplsActionCaseBuilder pushMplsActionCaseBuilder = mock(PushMplsActionCaseBuilder.class);
        PowerMockito.whenNew(PushMplsActionCaseBuilder.class).withNoArguments().thenReturn(pushMplsActionCaseBuilder);
        when(pushMplsActionCaseBuilder.setPushMplsAction(any(PushMplsAction.class))).thenReturn(pushMplsActionCaseBuilder);
        when(pushMplsActionBuilder.build()).thenReturn(mock(PushMplsAction.class));
        when(pushMplsActionCaseBuilder.build()).thenReturn(mock(PushMplsActionCase.class));
        when(ab.setAction(any(PushMplsActionCase.class))).thenReturn(ab);
        assertEquals("Failed to return correct Action object", action, FlowUtils.createMPLSAction(0, false));
    }

    @Test
    public void testCreateSetFieldMPLSLabelAction() throws Exception {
        ActionBuilder ab = mock(ActionBuilder.class);
        PowerMockito.whenNew(ActionBuilder.class).withNoArguments().thenReturn(ab);

        ProtocolMatchFieldsBuilder matchFieldsBuilder = mock(ProtocolMatchFieldsBuilder.class);
        PowerMockito.whenNew(ProtocolMatchFieldsBuilder.class).withNoArguments().thenReturn(matchFieldsBuilder);

        when(matchFieldsBuilder.setMplsLabel(any(Long.class))).thenReturn(matchFieldsBuilder);
        when(matchFieldsBuilder.setMplsBos(any(Short.class))).thenReturn(matchFieldsBuilder);

        when(ab.setOrder(any(Integer.class))).thenReturn(ab);
        PowerMockito.whenNew(ActionKey.class).withAnyArguments().thenReturn(mock(ActionKey.class));
        when(ab.setKey(any(ActionKey.class))).thenReturn(ab);

        SetFieldCaseBuilder setFieldCaseBuilder = mock(SetFieldCaseBuilder.class);
        PowerMockito.whenNew(SetFieldCaseBuilder.class).withNoArguments().thenReturn(setFieldCaseBuilder);
        when(setFieldCaseBuilder.setSetField(any(SetField.class))).thenReturn(setFieldCaseBuilder);
        when(ab.setAction(any(SetFieldCase.class))).thenReturn(ab);
        SetFieldBuilder setFieldBuilder = mock(SetFieldBuilder.class);
        PowerMockito.whenNew(SetFieldBuilder.class).withNoArguments().thenReturn(setFieldBuilder);
        when(setFieldBuilder.setProtocolMatchFields(any(ProtocolMatchFields.class))).thenReturn(setFieldBuilder);
        when(matchFieldsBuilder.build()).thenReturn(mock(ProtocolMatchFields.class));
        when(setFieldBuilder.build()).thenReturn(mock(SetField.class));
        when(setFieldCaseBuilder.build()).thenReturn(mock(SetFieldCase.class));

        Action action = mock(Action.class);
        when(ab.build()).thenReturn(action);
        assertEquals("Failed to return correct Action object", action, FlowUtils.createSetFieldMPLSLabelAction(0, new Long(28999), new Short((short) 1)));
    }

    @Test
    public void testCreateOutputToPort() throws Exception {
        ActionBuilder ab = mock(ActionBuilder.class);
        PowerMockito.whenNew(ActionBuilder.class).withNoArguments().thenReturn(ab);
        when(ab.setOrder(any(Integer.class))).thenReturn(ab);
        PowerMockito.whenNew(ActionKey.class).withAnyArguments().thenReturn(mock(ActionKey.class));
        when(ab.setKey(any(ActionKey.class))).thenReturn(ab);

        OutputActionCaseBuilder outputActionCaseBuilder = mock(OutputActionCaseBuilder.class);
        when(outputActionCaseBuilder.setOutputAction(any(OutputAction.class))).thenReturn(outputActionCaseBuilder);
        when(ab.setAction(any(OutputActionCase.class))).thenReturn(ab);
        OutputActionBuilder outputActionBuilder = mock(OutputActionBuilder.class);
        when(outputActionBuilder.setMaxLength(any(Integer.class))).thenReturn(outputActionBuilder);
        when(outputActionBuilder.setOutputNodeConnector(any(Uri.class))).thenReturn(outputActionBuilder);
        PowerMockito.whenNew(Uri.class).withArguments(anyString()).thenReturn(mock(Uri.class));
        when(outputActionBuilder.build()).thenReturn(mock(OutputAction.class));
        when(outputActionCaseBuilder.build()).thenReturn(mock(OutputActionCase.class));
        Action action = mock(Action.class);
        when(ab.build()).thenReturn(action);
        assertEquals("Failed to return correct Action object", action, FlowUtils.createOutputToPort(0, "2"));
    }

    /**
     * Test case for {@link FlowUtils#createQosNormal(int, Dscp)}.
     */
    @Test
    public void testCreateQosNormal() throws Exception {
        ActionBuilder ab = mock(ActionBuilder.class);
        PowerMockito.whenNew(ActionBuilder.class).withNoArguments().thenReturn(ab);
        when(ab.setOrder(any(Integer.class))).thenReturn(ab);
        PowerMockito.whenNew(ActionKey.class).withAnyArguments().thenReturn(mock(ActionKey.class));
        when(ab.setKey(any(ActionKey.class))).thenReturn(ab);

        SetNwTosActionCaseBuilder setNwTosActionCaseBuilder = mock(SetNwTosActionCaseBuilder.class);
        when(setNwTosActionCaseBuilder.setSetNwTosAction(any(SetNwTosAction.class))).thenReturn(setNwTosActionCaseBuilder);
        when(ab.setAction(any(SetNwTosActionCase.class))).thenReturn(ab);
        SetNwTosActionBuilder setNwTosActionBuilder = mock(SetNwTosActionBuilder.class);
        when(setNwTosActionBuilder.setTos(any(Integer.class))).thenReturn(setNwTosActionBuilder);
        when(setNwTosActionBuilder.build()).thenReturn(mock(SetNwTosAction.class));
        when(setNwTosActionCaseBuilder.build()).thenReturn(mock(SetNwTosActionCase.class));
        Action action = mock(Action.class);
        when(ab.build()).thenReturn(action);
        Dscp dscp = new Dscp((short) 7);
        assertEquals("Failed to return correct Action object", action, FlowUtils.createQosNormal(0, dscp));
    }

    /**
     * Test case for {@link FlowUtils#dscpToTos(short)}.
     */
    @Test
    public void testDscpToTos() {
        for (short dscp = 0; dscp <= 63; dscp++) {
            int tos = FlowUtils.dscpToTos(dscp);
            assertEquals((int)dscp << 2, tos);
        }
        assertEquals(0xfc, FlowUtils.dscpToTos((short)0xffff));
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
        when(ab.setKey(any(ActionKey.class))).thenReturn(ab);

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
