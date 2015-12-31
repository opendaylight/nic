/*
 * Copyright (c) 2016 NEC Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.flow.cond.config.VtnFlowMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.flow.conditions.VtnFlowCondition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnEtherMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeUpdateMode;

/**
 * JUnit test for {@link VTNIntentParser}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ VTNIntentParser.class, InetAddress.class, MacAddress.class, VtnEtherMatch.class, VTNManagerService.class, VtnFlowMatch.class})
public class VTNIntentParserTest extends TestBase {
    /**
     * Mock instance of VTNIntentParser to perform unit testing.
     */
    private VTNIntentParser spyVTNIntentParser;
    /**
     * create a mock object for DataBroker class.
     */
    private DataBroker dataBroker;
    /**
     * create a mock object for VTNRendererUtility class.
     */
    private VTNManagerService mockVtnManagerService;
    /**
     * It creates the required objects for every unit test cases.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        dataBroker = Mockito.mock(DataBroker.class);
        mockVtnManagerService = PowerMockito.mock(VTNManagerService.class);
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
    }

    /**
     * Test case for {@link VTNIntentParser#isCreateDefault()}
     */
    @Test
    public void testIsCreateDefault() throws Exception {
        boolean actualResult, expectedResult;
        /**
         * Here checking invalid scenario, if unable to create Tenant in the
         * default configuration of VTN Manager, this isCreateDefault() should
         * return false only.
         */
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "createTenant", TENANT[0]);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isCreateDefault");
        Assert.assertEquals("Should return false, when unable to create Tenant.", expectedResult, actualResult);
        /**
         * Here checking invalid scenario, if unable to create Bridge in the
         * default configuration of VTN Manager, this isCreateDefault() should
         * return false only.
         */
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "createTenant", TENANT[0]);
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "createBridge", TENANT[0], BRIDGE[0]);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isCreateDefault");
        Assert.assertEquals("Should return false, when unable to create Bridge.", expectedResult, actualResult);
        /**
         * Here checking invalid scenario, if unable to create FlowCondition in
         * the default configuration of VTN Manager, this isCreateDefault()
         * should return false only.
         */
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "createBridge", TENANT[0], BRIDGE[0]);
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "createFlowCond",
                VALID_SRC_ADDRESS[5], VALID_DST_ADDRESS[3], FLOW_CONDITION_NAME[4],FLOW_DIRECTION[0]);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isCreateDefault");
        Assert.assertEquals("Should return false, when unable to create Flow Condition.", expectedResult, actualResult);
        /**
         * Here checking valid scenario, if the default configuration is created
         * in VTN Manager successfully, this isCreateDefault() should return
         * true only.
         */
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "createTenant", Matchers.any(String.class));
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "createBridge", Matchers.any(String.class), Matchers.any(String.class));
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "createFlowCond",
                Matchers.any(String.class), Matchers.any(String.class), Matchers.any(String.class), Matchers.any(String.class));
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isCreateDefault");
        Assert.assertEquals("Should return true, when created Tenant, Bridge and Flow Condition.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#isDeleteDefault()}
     */
    @Test
    public void testIsDeleteDefault() throws Exception {
        boolean actualResult, expectedResult;
        /**
         * Verifying that default configuration deleted in VTN Manager. Here
         * checking invalid scenario, if default configuration already exist
         * then isDeleteDefault() must delete return false only.
         */
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "deleteTenant", TENANT[0]);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isDeleteDefault");
        Assert.assertEquals("Should return false, when unable to delete Tenant.", expectedResult, actualResult);
        /**
         * Verifying that default configuration deleted in VTN Manager. Here
         * checking valid scenario, if default configuration already exist then
         * isDeleteDefault() must delete return true only.
         */
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteTenant", Matchers.any(String.class));
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteFlowCond", Matchers.any(String.class));
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isDeleteDefault");
        Assert.assertEquals("Should true, when Tenant and Flow Condition deleted.",expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#rendering()}
     */
    @Test
    public void testRendering() throws Exception {
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing invalid scenario by passing invalid
         * addresses.
         */
        spyVTNIntentParser.rendering(INVALID_SRC_ADDRESS[0], INVALID_DST_ADDRESS[0], ACTIONS[0], ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing invalid scenario if default VTN
         * configuration creation failed.
         */
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "isCreateDefault");
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ACTIONS[0], ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing invalid scenario by passing invalid
         * action.
         */
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isCreateDefault");
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ACTIONS[2], ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing valid scenario by passing valid
         * addresses, actions.
         */
        PowerMockito.doNothing().when(spyVTNIntentParser, "createFlowFilter",
                Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(String.class), Matchers.any(String.class));
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[1], ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class));
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ACTIONS[0], ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(6))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class));
    }

    /**
     * Test case for {@link VTNIntentParser#updateRendering()}
     */
    @Test
    public void testUpdateRendering() throws Exception {
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        MacAddress mockMacAddress = PowerMockito.mock(MacAddress.class);
        VtnEtherMatch mockEthernetMatch = PowerMockito.mock(VtnEtherMatch.class);
        VtnFlowMatch mockFlowMatch = PowerMockito.mock(VtnFlowMatch.class);
        List<VtnFlowMatch> mockFlowMatchList = new ArrayList<VtnFlowMatch>();
        mockFlowMatchList.add(mockFlowMatch);
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing invalid scenario, passing invalid addresses,
         * actions.
         */
        spyVTNIntentParser.updateRendering(INVALID_SRC_ADDRESS[0], INVALID_DST_ADDRESS[0], ACTIONS[0],
                INTENT_ID, ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("constructCondName", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ENCODED_UUID, BOOLEAN_TRUE);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("constructCondName", VALID_DST_ADDRESS[0], VALID_SRC_ADDRESS[0], ENCODED_UUID, BOOLEAN_FALSE);
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing invalid scenario, passing invalid addresses,
         * actions.
         */
        PowerMockito.doReturn(mockFlowMatchList).when(spyVTNIntentParser, "listOfFlowMatch", Matchers.any(String.class));
        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], INTENT_ID, ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("constructCondName", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ENCODED_UUID, BOOLEAN_TRUE);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("constructCondName", VALID_DST_ADDRESS[0], VALID_SRC_ADDRESS[0], ENCODED_UUID, BOOLEAN_FALSE);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("delete", Matchers.any(String.class));
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing valid scenario, passing valid addresses, action
         * and if flow condition already exist.
         */
        PowerMockito.doReturn(mockFlowMatchList).when(spyVTNIntentParser, "listOfFlowMatch", Matchers.any(String.class));
        PowerMockito.doReturn(mockEthernetMatch).when(mockFlowMatch, "getVtnEtherMatch");
        PowerMockito.doReturn(mockMacAddress).when(mockEthernetMatch, "getSourceAddress");
        PowerMockito.doReturn(mockMacAddress).when(mockEthernetMatch, "getDestinationAddress");
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isCreateDefault");
        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2], ACTIONS[0], INTENT_ID, ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("createFlowCond", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("delete", Matchers.any(String.class));

        PowerMockito.doReturn(mockFlowMatchList).when(spyVTNIntentParser, "listOfFlowMatch", Matchers.any(String.class));
        PowerMockito.doReturn(mockEthernetMatch).when(mockFlowMatch, "getVtnEtherMatch");
        PowerMockito.doReturn(null).when(mockEthernetMatch, "getSourceAddress");
        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2], ACTIONS[0], INTENT_ID, ENCODED_UUID, mock(Intent.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("delete", Matchers.any(String.class));

        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[2], INTENT_ID, ENCODED_UUID, mock(Intent.class));
    }

    /**
     * Test case for {@link VTNIntentParser#delete()}
     */
    @Test
    public void testDelete() throws Exception {
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        VtnFlowCondition mockFlowcondition = PowerMockito.mock(VtnFlowCondition.class);
        VnodeName vnode = PowerMockito.mock(VnodeName.class);
        PowerMockito.doReturn(FLOW_CONDITION_NAME[0]).when(vnode,
                "getValue");
        PowerMockito.doReturn(vnode).when(mockFlowcondition,
                "getName");
        List<VtnFlowCondition> mockFlowConditionList = new ArrayList<VtnFlowCondition>();
        mockFlowConditionList.add(mockFlowcondition);
        PowerMockito.doReturn(mockFlowConditionList).when(spyVTNIntentParser, "readFlowConditions");
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteFlowCond", Matchers.any(String.class));
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * valid scenario by passing valid Intent ID.
         */
        spyVTNIntentParser.delete(ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * invalid scenario if any exception raised during this deletion
         * process.
         */
        spyVTNIntentParser.delete(ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * invalid scenario by passing invalid Intent Id.
         */
        spyVTNIntentParser.delete(ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));

        spyVTNIntentParser.delete(ENCODED_UUID_FAILURE);
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * valid scenario by passing valid Intent Id and if all intents deleted
         * from list then default configurations also must delete.
         */
        List<VtnFlowCondition> mockFlowConditionListEmpty = new ArrayList<VtnFlowCondition>();
        PowerMockito.doReturn(mockFlowConditionListEmpty).when(spyVTNIntentParser, "readFlowConditions");
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "isDeleteDefault");
        spyVTNIntentParser.delete(ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));

        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isDeleteDefault");
        spyVTNIntentParser.delete(ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
    }

    /**
     * Test case for {@link VTNIntentParser#deleteAction()}
     */
    @Test
    public void testDeleteAction() throws Exception {
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        VtnFlowCondition mockFlowcondition = PowerMockito.mock(VtnFlowCondition.class);
        VnodeName vnode = PowerMockito.mock(VnodeName.class);
        PowerMockito.doReturn(FLOW_CONDITION_NAME[0]).when(vnode,
                "getValue");
        PowerMockito.doReturn(vnode).when(mockFlowcondition,
                "getName");
        List<VtnFlowCondition> mockFlowConditionList = new ArrayList<VtnFlowCondition>();
        mockFlowConditionList.add(mockFlowcondition);
        PowerMockito.doReturn(mockFlowConditionList).when(spyVTNIntentParser, "readFlowConditions");
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isDeleteDefault");
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * valid scenario by passing valid Intent ID.
         */
        Whitebox.invokeMethod(spyVTNIntentParser, "deleteAction", ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * invalid scenario if any exception raised during this deletion
         * process.
         */
        Whitebox.invokeMethod(spyVTNIntentParser, "deleteAction", ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * invalid scenario by passing invalid Intent Id.
         */
        Whitebox.invokeMethod(spyVTNIntentParser, "deleteAction", ENCODED_UUID);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        Whitebox.invokeMethod(spyVTNIntentParser, "deleteAction", ENCODED_UUID_FAILURE);
    }

    /**
     * Test case for {@link VTNIntentParser#constructCondName()}
     */
    @Test
    public void testConstructCondName() throws Exception {
        String actualResult, expectedResult;
        /**
         * Verifying that creation of a FlowCondition name if we pass valid IP
         * or MAC addresses. Here testing valid scenario by passing valid IP,
         * MAC addresses and that constructCondName must return specific format
         * String object.
         */
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName",
                        VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],ENCODED_UUID, BOOLEAN_TRUE );
        expectedResult = CONST_CONDITION_NAME[0];
        Assert.assertEquals("Should return specified condition only.", expectedResult, actualResult);
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName",
                        VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2], ENCODED_UUID, BOOLEAN_FALSE );
        expectedResult = CONST_CONDITION_NAME[1];
        Assert.assertEquals("Should return specified condition only.",expectedResult, actualResult);
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName",
                        VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2], ENCODED_UUID, BOOLEAN_TRUE );
        expectedResult = CONST_CONDITION_NAME[0];
        Assert.assertEquals("Should return specified condition only.",expectedResult, actualResult);
        /**
         * Verifying that creates a FlowCondition name if we pass valid IP or
         * MAC addresses Here testing invalid scenario by passing invalid IP,
         * MAC addresses and that constructCondName must return null.
         */
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "constructCondName", VALID_SRC_ADDRESS[2], INVALID_DST_ADDRESS[4], ENCODED_UUID, BOOLEAN_FALSE );
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.",expectedResult, actualResult);

        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName", VALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[2], ENCODED_UUID, BOOLEAN_FALSE );
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.", expectedResult, actualResult);

        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName", INVALID_SRC_ADDRESS[3],
                INVALID_DST_ADDRESS[3], ENCODED_UUID, BOOLEAN_FALSE );
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.", expectedResult, actualResult);

        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "constructCondName", INVALID_SRC_ADDRESS[1],
                INVALID_DST_ADDRESS[2], ENCODED_UUID, BOOLEAN_FALSE );
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#createTenant()}
     */
    @Test
    public void testCreateTenant() throws Exception {
        boolean actualResult, expectedResult;
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        PowerMockito.doReturn(true).when(mockVtnManagerService, "updateTenant",
                Matchers.any(String.class),
                 Matchers.any(VnodeUpdateMode.class));
        /**
         * Verifying that creation a new tenant in VTN Manager in VTN Manager.
         * Here testing valid scenario if specified tenant created and
         * createTenant() must return true.
         */
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createTenant", TENANT[0]);
        expectedResult = true;
        Assert.assertEquals("Should return ture, when Tenant created.", expectedResult, actualResult);

        /**
         * Verifying that creation a new tenant in VTN Manager in VTN Manager.
         * Here testing invalid scenario if specified tenant not created and
         * createTenant() must return false.
         */
        String nullObj = null;
        PowerMockito.doReturn(false).when(mockVtnManagerService, "updateTenant",
                Matchers.any(String.class),
                Matchers.any(VnodeUpdateMode.class));
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createTenant", nullObj);
        Assert.assertEquals("Should return false, when Tenant not created.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#deleteTenant()}
     */
    @Test
    public void testDeleteTenant() throws Exception {
        boolean actual, expected;
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        /*
         * Verifying that deletion of specified TENANT in VTN Manager. Here
         * testing invalid scenarios if Tenant not deleted then deleteTenant()
         * must return false.
         */

        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteTenant", TENANT[0]);
        expected = false;
        Assert.assertEquals("Should return false, when Tenant not deleted.", expected, actual);
        PowerMockito.doReturn(true).when(mockVtnManagerService, "removeTenant",
                Matchers.any(String.class));
        /*
         * Verifying that deletion of specified Tenant in VTN Manager. Here
         * testing valid scenario if Tenant deleted then deleteTenant() must
         * return true.
         */
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteTenant", TENANT[0]);
        expected = true;
        Assert.assertEquals("Should return true, when Tenant deleted.", expected, actual);
        /**
         * Verifying that deletion of specified TENANT in VTN Manager. Here
         * testing invalid scenarios if Tenant not deleted then deleteTenant()
         * must return false.
         */
        PowerMockito.doReturn(false).when(mockVtnManagerService, "removeTenant",
                Matchers.any(String.class));
        expected = false;
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteTenant", TENANT[0]);
        Assert.assertEquals("Should return false, when Tenant not deleted.", expected, actual);
    }

    /**
     * Test case for {@link VTNIntentParser#createFlowCond()}
     */
    @Test
    public void testCreateFlowCond() throws Exception {
        boolean actualResult, expectedResult;
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isFlowCondExist",
                Matchers.any(String.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0], FLOW_DIRECTION[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        PowerMockito.doReturn(true).when(spyVTNIntentParser,
                "isFlowCondExist", Matchers.any(String.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[1], FLOW_DIRECTION[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2], FLOW_CONDITION_NAME[2], FLOW_DIRECTION[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[5], VALID_DST_ADDRESS[3], FLOW_CONDITION_NAME[3], FLOW_DIRECTION[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing invalid scenario by passing invalid addresses, FlowCondition
         * names and createFlowCond() must return false.
         */
        expectedResult = false;
        PowerMockito.doReturn(false).when(spyVTNIntentParser,
                "isFlowCondExist", Matchers.any(String.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createFlowCond", INVALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[4], FLOW_DIRECTION[0]);
        Assert.assertEquals("Should return false, when Flow Condition not created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        PowerMockito.doReturn(true).when(mockVtnManagerService,
                "setFlowCond", Matchers.any(SetFlowConditionInput.class));
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0], FLOW_DIRECTION[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing invalid scenario if unable to update flow condition in VTN
         * Manager then createFlowCond() must return false.
         */
        PowerMockito.doReturn(false).when(mockVtnManagerService,
                "setFlowCond",  Matchers.any(SetFlowConditionInput.class));
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0], FLOW_DIRECTION[0]);
        Assert.assertEquals("Should return false, when Flow Condition not created.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowCond()}
     */
    @Test
    public void testDeleteFlowCond() throws Exception {
        boolean actual, expected;
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        /*
         * Verifying that deletion of the specified FlowCondition in VTN
         * Manager. Here testing invalid scenario if any exception is raised
         * during deletion process then deleteFlowCond() must return false.
         */
        actual = Whitebox.invokeMethod(PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService)),
                "deleteFlowCond", FLOW_CONDITION_NAME[0]);
        expected = false;
        Assert.assertEquals("Should return false, when Flow Condition not deleted.", expected, actual);
        /**
         * Verifying that deletion of specified FlowCondition in VTN Manager.
         * Here testing valid scenarios by passing a parameter as valid
         * FlowConditions and deleteFlowCond() must return true.
         */
        PowerMockito.doReturn(true).when(mockVtnManagerService, "unsetFlowCond", Matchers.any(String.class));
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteFlowCond", FLOW_CONDITION_NAME[0]);
        expected = true;
        Assert.assertEquals("Should return true, when Flow Condition deleted.", expected, actual);
    }

    /**
     * Test case for {@link VTNIntentParser#isFlowCondExist()}
     */
    @Test
    public void testIsFlowCondExist() throws Exception {
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        VtnFlowCondition mockFlowCondition = PowerMockito.mock(VtnFlowCondition.class);
        VnodeName vnode = PowerMockito.mock(VnodeName.class);
        PowerMockito.doReturn(FLOW_CONDITION_NAME[0]).when(vnode,
                "getValue");
        PowerMockito.doReturn(vnode).when(mockFlowCondition,
                "getName");
        List<VtnFlowCondition> listOfFlowConditions = new ArrayList<VtnFlowCondition>();
        listOfFlowConditions.add(mockFlowCondition);
        PowerMockito.doReturn(listOfFlowConditions).when(spyVTNIntentParser, "readFlowConditions");

        /**
         * Verifying that specified FlowCondition exist in VTN Manager. Here
         * testing valid scenario by passing a parameter as valid FlowContion
         * and isFlowCondExist() must return true.
         */
        boolean actualResult, expectedResult;
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isFlowCondExist", FLOW_CONDITION_NAME[0]);
        Assert.assertEquals("Should return true, when Flow Condition available.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition exist in VTN Manager. Here
         * testing invalid scenario by passing a parameter as invalid
         * FlowContion and isFlowCondExist() must return false.
         */
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isFlowCondExist", FLOW_CONDITION_NAME[1]);
        Assert.assertEquals("Should return false, when Flow Condition not available.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#createFlowFilter()}
     */
    @Test
    public void testCreateFlowFilter() throws Exception {
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        /**
         * Verifying that specified FlowFilter created in VTN Manager. Here
         * testing creation of flow filter for the given flow condition and if
         * required flow filter needs to be added in the Intent List.
         */
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[3], FLOW_CONDITION_NAME[0]);
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[3], FLOW_CONDITION_NAME[0]);
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[4], FLOW_CONDITION_NAME[0]);
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[2], FLOW_CONDITION_NAME[3]);
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowFilter()}
     */
    @Test
    public void testDeleteFlowFilter() throws Exception{
        boolean actualResult, expectedResult;
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        PowerMockito.doReturn(true).when(mockVtnManagerService, "unSetFlowFilter", Matchers.any(RemoveFlowFilterInput.class));
        /**
         * Verifying that deletion of specified FlowFilter in VTN Manager. Here
         * testing valid scenario by passing valid index and deleteFlowFilter()
         * must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "deleteFlowFilter", INDEX[0]);
        Assert.assertEquals("Should return true, when flow filter deleted.", expectedResult, actualResult);
        /**
         * Verifying that deletion of specified FlowFilter in VTN Manager. Here
         * testing invalid scenario by passing invalid index and
         * deleteFlowFilter() must return false.
         */
        expectedResult = false;
        PowerMockito.doReturn(false).when(mockVtnManagerService, "unSetFlowFilter", Matchers.any(RemoveFlowFilterInput.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "deleteFlowFilter", INDEX[1]);
        Assert.assertEquals("Should return false, when unable to deleted flow filter.",
                expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#createBridge()}
     */
    @Test
    public void testCreateBridge() throws Exception {
        boolean actualResult, expectedResult;
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        /**
         * Verifying that specified Bridge created in VTN Manager. Here testing
         * invalid scenario by passing  Tenant, Bridge name and
         * createBridge() must return false.
         */
        expectedResult = false;
        PowerMockito.doReturn(false).when(mockVtnManagerService, "updateBridge",
                Matchers.any(String.class), Matchers.any(String.class),Matchers.any(String.class), Matchers.any(VnodeUpdateMode.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createBridge", TENANT[0], BRIDGE[0]);
        Assert.assertEquals("Should return true, when Bridge created.", expectedResult, actualResult);

        /**
         * Verifying that specified Bridge created in VTN Manager. Here testing
         * valid scenario by passing valid Tenant, Bridge name and
         * createBridge() must return true.
         */
        expectedResult = true;
        PowerMockito.doReturn(true).when(mockVtnManagerService, "updateBridge",
                Matchers.any(String.class), Matchers.any(String.class),Matchers.any(String.class),Matchers.any(VnodeUpdateMode.class));
        PowerMockito.doReturn(true).when(mockVtnManagerService, "setVlanMap",Matchers.any(AddVlanMapInput.class) );
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createBridge", TENANT[0], BRIDGE[0]);
        Assert.assertEquals("Should return true, when Bridge created.", expectedResult, actualResult);

    }

    /**
     * Test case for {@link VTNIntentParser#containsIntentID()}
     */
    @Test
    public void testContainsIntentID() throws Exception {
        boolean actualResult, expectedResult;
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        VtnFlowCondition mockFlowcondition = PowerMockito.mock(VtnFlowCondition.class);
        VnodeName vnode = PowerMockito.mock(VnodeName.class);
        PowerMockito.doReturn(FLOW_CONDITION_NAME[0]).when(vnode,
                "getValue");
        PowerMockito.doReturn(vnode).when(mockFlowcondition,
                "getName");
        List<VtnFlowCondition> mockFlowConditionList = new ArrayList<VtnFlowCondition>();
        mockFlowConditionList.add(mockFlowcondition);
        PowerMockito.doReturn(mockFlowConditionList).when(spyVTNIntentParser, "readFlowConditions");

        expectedResult = true;
        actualResult = spyVTNIntentParser.containsIntentID(ENCODED_UUID);
        Assert.assertEquals("Should return true, when UUID is  available.", expectedResult, actualResult);

        spyVTNIntentParser.containsIntentID(ENCODED_UUID_FAILURE);

        PowerMockito.doThrow(new NullPointerException ("Raised for UT"))
                .when(spyVTNIntentParser, "readFlowConditions");
        spyVTNIntentParser.containsIntentID(ENCODED_UUID);
    }

    /**
     * Test case for {@link VTNIntentParser#listOfFlowMatch()}
     */
    @Test
    public void testListofFlowMatch() throws Exception {
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker, mockVtnManagerService));
        VtnFlowCondition mockFlowcondition = PowerMockito.mock(VtnFlowCondition.class);
        VnodeName vnode = PowerMockito.mock(VnodeName.class);
        PowerMockito.doReturn(FLOW_CONDITION_NAME[0]).when(vnode,
                "getValue");
        PowerMockito.doReturn(vnode).when(mockFlowcondition,
                "getName");
        List<VtnFlowCondition> mockFlowConditionList = new ArrayList<VtnFlowCondition>();
        mockFlowConditionList.add(mockFlowcondition);
        PowerMockito.doReturn(mockFlowConditionList).when(spyVTNIntentParser, "readFlowConditions");

        Whitebox.invokeMethod(spyVTNIntentParser, "listOfFlowMatch", ENCODED_UUID);
        Whitebox.invokeMethod(spyVTNIntentParser, "listOfFlowMatch", ENCODED_UUID_FAILURE);
    }
}
