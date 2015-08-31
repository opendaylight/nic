/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.ServiceUnavailableException;

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
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;
import org.opendaylight.vtn.manager.flow.cond.FlowCondition;
import org.opendaylight.vtn.manager.IVTNManager;
import org.opendaylight.vtn.manager.VBridge;
import org.opendaylight.vtn.manager.VBridgeConfig;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.vtn.manager.VTNException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.vtn.renderer.intent.IntentWrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.vtnintents.VtnRendererIntent;

/**
 * JUnit test for {@link VTNIntentParser}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ VTNIntentParser.class, FlowCondition.class,
        InetAddress.class, ServiceHelper.class })
public class VTNIntentParserTest extends TestBase {
    /**
     * Mock instance of VTNIntentParser to perform unit testing.
     */
    private VTNIntentParser spyVTNIntentParser;
    /**
     * Mock instance of IVTNManager to perform unit testing.
     */
    private IVTNManager mockIVTNManager;
    /**
     * create a mock object for DataBroker class.
     */
    private DataBroker dataBroker;

    private VTNRendererUtility spyVtnRendererUtility;
    /**
     * It creates the required objects for every unit test cases.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        dataBroker = Mockito.mock(DataBroker.class);
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker));
        mockIVTNManager = PowerMockito.mock(IVTNManager.class);
        PowerMockito.doReturn(mockIVTNManager).when(spyVTNIntentParser,
                "getVTNManager", DEFAULT_SERVICE);
        spyVtnRendererUtility =PowerMockito.spy(new VTNRendererUtility(dataBroker));
        Whitebox.setInternalState(spyVTNIntentParser, "vtnRendererUtility", spyVtnRendererUtility);
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
                VALID_SRC_ADDRESS[5], VALID_DST_ADDRESS[3], FLOW_CONDITION_NAME[3]);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isCreateDefault");
        Assert.assertEquals("Should return false, when unable to create Flow Condition.", expectedResult, actualResult);
        /**
         * Here checking valid scenario, if the default configuration is created
         * in VTN Manager successfully, this isCreateDefault() should return
         * true only.
         */
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "createFlowCond",
                VALID_SRC_ADDRESS[5], VALID_DST_ADDRESS[3], FLOW_CONDITION_NAME[3]);
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
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteTenant", TENANT[0]);
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteFlowCond", FLOW_CONDITION_NAME[3]);
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isDeleteDefault");
        Assert.assertEquals("Should true, when Tenant and Flow Condition deleted.",expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#rendering()}
     */
    @Test
    public void testRendering() throws Exception {

        List<IntentWrapper> mockIntentList = PowerMockito.mock(List.class);
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing invalid scenario by passing invalid
         * addresses.
         */
        spyVTNIntentParser.rendering(INVALID_SRC_ADDRESS[0], INVALID_DST_ADDRESS[0], ACTIONS[0], mockIntentList, INTENT_LIST_ID[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing invalid scenario if default VTN
         * configuration creation failed.
         */
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "isCreateDefault");
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ACTIONS[0], mockIntentList,
        INTENT_LIST_ID[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing invalid scenario by passing invalid
         * action.
         */
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isCreateDefault");
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ACTIONS[2], mockIntentList,
        INTENT_LIST_ID[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing invalid scenario if any exception is
         * raised during rendering.
         */
        PowerMockito.doThrow(new ServiceUnavailableException()).when(spyVTNIntentParser, "createFlowFilter",
                Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ACTIONS[0], mockIntentList,
        INTENT_LIST_ID[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        /**
         * Verifying based on Intent action that specified VTN elements created
         * in VTN Manager. Here testing valid scenario by passing valid
         * addresses, actions.
         */
        PowerMockito.doNothing().when(spyVTNIntentParser, "createFlowFilter",
                Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(String.class), Matchers.any(String.class),
                Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[1], mockIntentList, INTENT_LIST_ID[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(4))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        spyVTNIntentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], ACTIONS[0], mockIntentList,
        INTENT_LIST_ID[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(7))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
    }

    /**
     * Test case for {@link VTNIntentParser#updateRendering()}
     */
    @Test
    public void testUpdateRendering() throws Exception {
        List<IntentWrapper> listOfMockIntentWrappers = new ArrayList<IntentWrapper>();
        List<IntentWrapper> listOfMockIntentWrappersTwo = new ArrayList<IntentWrapper>();
        List<VtnRendererIntent> listOfMockVtnRendererIntent = new ArrayList<VtnRendererIntent>();
        IntentWrapper mockIntentWrapper = PowerMockito.mock(IntentWrapper.class);
        PowerMockito.doReturn(" ").when(mockIntentWrapper).getEntityDescription();
        listOfMockIntentWrappers.add(mockIntentWrapper);
        listOfMockIntentWrappersTwo.add(mockIntentWrapper);
        PowerMockito.doReturn(listOfMockIntentWrappers).when(spyVtnRendererUtility).listIntentWrapper(
        Matchers.any(String.class), Matchers.any(List.class));
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing invalid scenario, passing invalid addresses,
         * actions.
         */
        spyVTNIntentParser.updateRendering(INVALID_SRC_ADDRESS[0], INVALID_DST_ADDRESS[0], ACTIONS[0],
                listOfMockIntentWrappersTwo, INTENT_LIST_ID[0], listOfMockVtnRendererIntent);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("constructCondName", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("constructCondName", VALID_DST_ADDRESS[0], VALID_SRC_ADDRESS[0]);
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing invalid scenario, passing invalid addresses,
         * actions.
         */
        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[2], listOfMockIntentWrappersTwo, INTENT_LIST_ID[0], 
                listOfMockVtnRendererIntent);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("constructCondName", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("constructCondName", VALID_DST_ADDRESS[0], VALID_SRC_ADDRESS[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing valid scenario, passing valid addresses, action
         * and if flow condition already exist.
         */
        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], listOfMockIntentWrappersTwo, UUID_LIST_ID[0], 
                listOfMockVtnRendererIntent);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("constructCondName", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("constructCondName", VALID_DST_ADDRESS[0], VALID_SRC_ADDRESS[0]);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class)); 
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing valid scenario, passing valid addresses, action
         * and if flow filter already exist.
         */
        IntentWrapper mockIntentWrapperTwo = PowerMockito.mock(IntentWrapper.class);
        PowerMockito.doReturn(ENTITY_DESCRIPTION[0]).when(mockIntentWrapperTwo).getEntityDescription();
        PowerMockito.doReturn(ACTIONS[0]).when(mockIntentWrapperTwo).getAction();
        IntentWrapper mockIntentWrapperThree = PowerMockito.mock(IntentWrapper.class);
        PowerMockito.doReturn(ENTITY_DESCRIPTION[1]).when(mockIntentWrapperThree).getEntityDescription();
        PowerMockito.doReturn(ACTIONS[0]).when(mockIntentWrapperThree).getAction();
        listOfMockIntentWrappers.clear();
        listOfMockIntentWrappers.add(mockIntentWrapperTwo);
        listOfMockIntentWrappers.add(mockIntentWrapperThree);
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing valid scenario, passing valid addresses, action,
         * Intent list, Intent ID.
         */
        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], listOfMockIntentWrappersTwo, UUID_LIST_ID[0], 
                listOfMockVtnRendererIntent);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(4))
                .invoke("createFlowFilter", Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        /**
         * Verifying that whether VTN elements updated based on the intent
         * action Here testing invalid scenario, if any exception raised during
         * updation process.
         */
        spyVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBroker));
        PowerMockito.doThrow(new ServiceUnavailableException("Raised for UT"))
                .when(spyVTNIntentParser, "createFlowFilter",
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
        spyVTNIntentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], listOfMockIntentWrappersTwo, INTENT_LIST_ID[0],
                listOfMockVtnRendererIntent);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(0))
                .invoke("createFlowFilter", Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(String.class), Matchers.any(String.class),
                        Matchers.any(Boolean.class), Matchers.any(List.class), Matchers.any(String.class));
    }

    /**
     * Test case for {@link VTNIntentParser#delete()}
     */
    @Test
    public void testDelete() throws Exception {
        List<IntentWrapper> listOfMockIntentWrappers = new ArrayList<IntentWrapper>();
        List<VtnRendererIntent> listOfVtnRendererIntent = new ArrayList<VtnRendererIntent>();
        IntentWrapper mockIntentWrapper = PowerMockito.mock(IntentWrapper.class);
        Uuid uuid  = Mockito.mock(Uuid.class);
        listOfMockIntentWrappers.add(mockIntentWrapper);
        PowerMockito.doReturn(listOfMockIntentWrappers).when(spyVtnRendererUtility).listIntentWrapper(
        Matchers.any(String.class), Matchers.any(List.class));
        PowerMockito.doReturn("FlowFilter").when(mockIntentWrapper, "getEntityName");
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isDeleteDefault");
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteFlowCond", Matchers.any(String.class));
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * valid scenario by passing valid Intent ID.
         */
        spyVTNIntentParser.delete("iId1", listOfVtnRendererIntent, uuid);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1)).invoke("isDeleteDefault");
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(1))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * invalid scenario if any exception raised during this deletion
         * process.
         */
        PowerMockito.doThrow(new RuntimeException("Raised for UT")).when(spyVTNIntentParser, "isDeleteDefault");
        spyVTNIntentParser.delete("iId1", listOfVtnRendererIntent, uuid);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2)).invoke("isDeleteDefault");
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * invalid scenario by passing invalid Intent Id.
         */
        PowerMockito.doReturn("Invalid_FlowFilter").when(mockIntentWrapper, "getEntityName");
        spyVTNIntentParser.delete("iId1", listOfVtnRendererIntent, uuid);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3)).invoke("isDeleteDefault");
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(2))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
        /**
         * Verifying that particular intent deleted in VTN Manager. Here testing
         * valid scenario by passing valid Intent Id and if all intents deleted
         * from list then default configurations also must delete.
         */
        PowerMockito.doReturn("FlowFilter").when(mockIntentWrapper, "getEntityName");
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "isDeleteDefault");
        spyVTNIntentParser.delete("iId1", listOfVtnRendererIntent, uuid);
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(4)).invoke("isDeleteDefault");
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowCond", Matchers.any(String.class));
        PowerMockito.verifyPrivate(spyVTNIntentParser, Mockito.times(3))
                .invoke("deleteFlowFilter", Matchers.any(Integer.class));
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
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName",
                        VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);
        expectedResult = CONDITION_NAME[0];
        Assert.assertEquals("Should return specified condition only.", expectedResult, actualResult);
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName",
                        VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2]);
        expectedResult = CONDITION_NAME[1];
        Assert.assertEquals("Should return specified condition only.",expectedResult, actualResult);
        /**
         * Verifying that creates a FlowCondition name if we pass valid IP or
         * MAC addresses Here testing invalid scenario by passing invalid IP,
         * MAC addresses and that constructCondName must return null.
         */
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "constructCondName", VALID_SRC_ADDRESS[2], INVALID_DST_ADDRESS[4]);
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.",expectedResult, actualResult);

        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName", VALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[2]);
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.", expectedResult, actualResult);

        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "constructCondName", INVALID_SRC_ADDRESS[3],
                INVALID_DST_ADDRESS[3]);
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.", expectedResult, actualResult);

        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "constructCondName", INVALID_SRC_ADDRESS[1],
                INVALID_DST_ADDRESS[2]);
        expectedResult = null;
        Assert.assertEquals("Should return null only, if addresses are invalid.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#getVTNManager()}
     */
    @Test
    public void testGetVTNManager() throws Exception {
        PowerMockito.mockStatic(ServiceHelper.class);
        PowerMockito.when(ServiceHelper.getInstance(Mockito.eq(IVTNManager.class),
                        Matchers.any(String.class),
                        Matchers.any(VTNIntentParser.class))).thenReturn(
                mockIVTNManager);
        /**
         * Here testing valid scenario by passing valid service name and that
         * getVTNManager() must return IVTNManager object.
         */
        IVTNManager actualIVTNIvtnManager, expectedIvtnManager;
        actualIVTNIvtnManager = mockIVTNManager;
        expectedIvtnManager = new VTNIntentParser(dataBroker).getVTNManager(DEFAULT_SERVICE);
        Assert.assertEquals("Should return required VTNManager.", expectedIvtnManager, actualIVTNIvtnManager);
        /**
         * Here testing invalid scenario if service is not available then
         * getVTNManager() must throw ServiceUnavailableException.
         */
        try {
            PowerMockito.when(ServiceHelper.getInstance(Mockito.eq(IVTNManager.class),
                            Matchers.any(String.class),
                            Matchers.any(VTNIntentParser.class))).thenReturn(null);
            new VTNIntentParser(dataBroker).getVTNManager(DEFAULT_SERVICE);
            Assert.fail("This statement should not be reached");
        } catch (Exception exception) {
            Assert.assertTrue("Exception should be ServiceUnavailableException only.", exception instanceof ServiceUnavailableException);
        }
    }

    /**
     * Test case for {@link VTNIntentParser#createTenant()}
     */
    @Test
    public void testCreateTenant() throws Exception {
        boolean actualResult, expectedResult;
        Status status = PowerMockito.mock(Status.class);
        PowerMockito.doReturn(status).when(mockIVTNManager, "addTenant",
                Matchers.any(VTenantPath.class),
                Matchers.any(VTenantConfig.class));
        PowerMockito.doReturn(true).when(status, "isSuccess");
        /**
         * Verifying that creation a new tenant in VTN Manager in VTN Manager.
         * Here testing valid scenario if specified tenant created and
         * createTenant() must return true.
         */
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createTenant", TENANT[0]);
        expectedResult = true;
        Assert.assertEquals("Should return true, when Tenant created.", expectedResult, actualResult);
        PowerMockito.doReturn(false).when(status, "isSuccess");
        PowerMockito.doReturn(StatusCode.CONFLICT).when(status, "getCode");
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createTenant", TENANT[0]);
        Assert.assertEquals("Should return true, when Tenant already created.",expectedResult, actualResult);
        PowerMockito.doReturn(false).when(status, "isSuccess");
        PowerMockito.doReturn(StatusCode.FORBIDDEN).when(status, "getCode");
        expectedResult = false;
        /**
         * Verifying that creation a new tenant in VTN Manager in VTN Manager.
         * Here testing invalid scenario if specified tenant not created and
         * createTenant() must return false.
         */
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createTenant", TENANT[0]);
        Assert.assertEquals("Should return false, when Tenant not created.", expectedResult, actualResult);
        /**
         * Verifying that creation a new tenant in VTN Manager in VTN Manager.
         * Here testing invalid scenario if any exception raised during tenant
         * creation then createTenant() must return false.
         */
        PowerMockito.doThrow(new ServiceUnavailableException("Raised for UT"))
                .when(spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createTenant", TENANT[0]);
        expectedResult = false;
        Assert.assertEquals("Should return false, when Tenant not created.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#deleteTenant()}
     */
    @Test
    public void testDeleteTenant() throws Exception {
        boolean actual, expected;
        Status status = PowerMockito.mock(Status.class);
        /**
         * Verifying that deletion of specified TENANT in VTN Manager. Here
         * testing invalid scenarios if Tenant not deleted then deleteTenant()
         * must return false.
         */
        PowerMockito.doThrow(new ServiceUnavailableException("Raised for UT"))
                .when(spyVTNIntentParser, "getVTNManager",
                        Matchers.any(String.class));
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteTenant",
                TENANT[0]);
        expected = false;
        Assert.assertEquals("Should return false, when Tenant not deleted.", expected, actual);
        PowerMockito.doReturn(mockIVTNManager).when(spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        PowerMockito.doReturn(status).when(mockIVTNManager, "removeTenant",
                Matchers.any(VTenantPath.class));
        PowerMockito.doReturn(true).when(status, "isSuccess");
        /**
         * Verifying that deletion of specified Tenant in VTN Manager. Here
         * testing valid scenario if Tenant deleted then deleteTenant() must
         * return true.
         */
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteTenant", TENANT[0]);
        expected = true;
        Assert.assertEquals("Should return true, when Tenant deleted.", expected, actual);
        PowerMockito.doReturn(false).when(status, "isSuccess");
        PowerMockito.doReturn(StatusCode.NOTFOUND).when(status, "getCode");
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteTenant", TENANT[0]);
        Assert.assertEquals("Should return true, when Tenant already deleted.", expected, actual);
        /**
         * Verifying that deletion of specified TENANT in VTN Manager. Here
         * testing invalid scenarios if Tenant not deleted then deleteTenant()
         * must return false.
         */
        PowerMockito.doReturn(false).when(status, "isSuccess");
        PowerMockito.doReturn(StatusCode.FORBIDDEN).when(status, "getCode");
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
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isFlowCondExist",
                Matchers.any(String.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        PowerMockito.doReturn(false).when(spyVTNIntentParser,
                "isFlowCondExist", Matchers.any(String.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2], FLOW_CONDITION_NAME[2]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[5], VALID_DST_ADDRESS[3], FLOW_CONDITION_NAME[4]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing invalid scenario by passing invalid addresses, FlowCondition
         * names and createFlowCond() must return false.
         */
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createFlowCond", INVALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[4]);
        Assert.assertEquals("Should return false, when Flow Condition not created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing invalid scenario if any exception raised during creation then
         * createFlowCond() must return false.
         */
        PowerMockito.doThrow(new VTNException("Raised for UT")).when(mockIVTNManager, "setFlowCondition",
                Matchers.any(String.class), Matchers.any(FlowCondition.class));
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]);
        Assert.assertEquals("Should return false, when Flow Condition not created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing valid scenario by passing valid addresses, FlowCondition
         * names and createFlowCond() must return true.
         */
        PowerMockito.doReturn(UpdateType.ADDED).when(mockIVTNManager,
                "setFlowCondition", Matchers.any(String.class), Matchers.any(FlowCondition.class));
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]);
        Assert.assertEquals("Should return true, when Flow Condition created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing invalid scenario if unable to update flow condition in VTN
         * Manager then createFlowCond() must return false.
         */
        PowerMockito.doReturn(UpdateType.CHANGED).when(mockIVTNManager,
                "setFlowCondition", Matchers.any(String.class),
                Matchers.any(FlowCondition.class));
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]);
        Assert.assertEquals("Should return false, when Flow Condition not created.", expectedResult, actualResult);
        /**
         * Verifying that specified FlowCondition created in VTN Manager. Here
         * testing invalid scenario if any exception raised because of hosts are
         * unavailable then createFlowCond() must return false.
         */
        PowerMockito.mockStatic(InetAddress.class);
        Mockito.when(InetAddress.getByName(Matchers.any(String.class)))
                .thenThrow(new UnknownHostException("Raised for UT"));
        expectedResult = false;
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "isFlowCondExist", Matchers.any(String.class));
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "createFlowCond", VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]);
        Assert.assertEquals("Should return false, when Flow Condition not created.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowCond()}
     */
    @Test
    public void testDeleteFlowCond() throws Exception {
        boolean actual, expected;
        Status status = PowerMockito.mock(Status.class);
        /**
         * Verifying that deletion of the specified FlowCondition in VTN
         * Manager. Here testing invalid scenario if any exception is raised
         * during deletion process then deleteFlowCond() must return false.
         */
        actual = Whitebox.invokeMethod(PowerMockito.spy(new VTNIntentParser(dataBroker)),
                "deleteFlowCond", FLOW_CONDITION_NAME[0]);
        expected = false;
        Assert.assertEquals("Should return false, when Flow Condition not deleted.", expected, actual);
        PowerMockito.doReturn(status).when(mockIVTNManager, "removeFlowCondition", Matchers.any(String.class));
        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isFlowCondExist", FLOW_CONDITION_NAME[0]);
        PowerMockito.doReturn(true).when(status, "isSuccess");
        /**
         * Verifying that deletion of specified FlowCondition in VTN Manager.
         * Here testing valid scenarios by passing a parameter as valid
         * FlowConditions and deleteFlowCond() must return true.
         */
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteFlowCond", FLOW_CONDITION_NAME[0]);
        expected = true;
        Assert.assertEquals("Should return true, when Flow Condition deleted.", expected, actual);
        /**
         * Verifying that deletion of specified FlowCondition in VTN Manager.
         * Here testing valid scenarios by passing a parameter as valid
         * FlowConditions and deleteFlowCond() must return true.
         */
        PowerMockito.doReturn(false).when(spyVTNIntentParser,
                "isFlowCondExist", FLOW_CONDITION_NAME[0]);
        actual = Whitebox.invokeMethod(spyVTNIntentParser, "deleteFlowCond", FLOW_CONDITION_NAME[0]);
        expected = true;
        Assert.assertEquals("Should return true, when Flow Condition already deleted.", expected, actual);
    }

    /**
     * Test case for {@link VTNIntentParser#isFlowCondExist()}
     */
    @Test
    public void testIsFlowCondExist() throws Exception {
        FlowCondition mockFlowCondition = PowerMockito.mock(FlowCondition.class);
        PowerMockito.doReturn(FLOW_CONDITION_NAME[0]).when(mockFlowCondition, "getName");
        List<FlowCondition> listOfFlowConditions = new ArrayList<FlowCondition>();
        listOfFlowConditions.add(mockFlowCondition);
        PowerMockito.doReturn(listOfFlowConditions).when(mockIVTNManager, "getFlowConditions");
        PowerMockito.doReturn(mockIVTNManager).when(spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
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
        /**
         * Verifying that specified FlowCondition exist in VTN Manager. Here
         * testing invalid scenario if any exception is raised when reading
         * object of VTN Manager then isFlowCondExist() must return false.
         */
        PowerMockito.doThrow(new ServiceUnavailableException()).when(
                spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "isFlowCondExist", FLOW_CONDITION_NAME[1]);
        Assert.assertEquals("Should return false, when any exception raised.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#createFlowFilter()}
     */
    @Test
    public void testCreateFlowFilter() throws Exception {
        List list = new ArrayList();
        int initialSize, newSize;
        initialSize = list.size();
        /**
         * Verifying that specified FlowFilter created in VTN Manager. Here
         * testing creation of flow filter for the given flow condition and if
         * required flow filter needs to be added in the Intent List.
         */
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[3], FLOW_CONDITION_NAME[0], true, list, UUID_LIST_ID[0]);
        newSize = list.size();
        Assert.assertEquals(initialSize + 1, newSize);
        initialSize = list.size();
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[3], FLOW_CONDITION_NAME[0], false, list, UUID_LIST_ID[0]);
        newSize = list.size();
        Assert.assertEquals("Intent list should not be updated.", initialSize, newSize);
        initialSize = list.size();
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[4], FLOW_CONDITION_NAME[0], true, list, UUID_LIST_ID[0]);
        newSize = list.size();
        Assert.assertEquals("Intent list should be updated.", initialSize + 1, newSize);
        initialSize = list.size();
        Whitebox.invokeMethod(spyVTNIntentParser, "createFlowFilter",
                TENANT[0], BRIDGE[0], ACTIONS[2], FLOW_CONDITION_NAME[3], true, list, UUID_LIST_ID[0]);
        newSize = list.size();
        Assert.assertEquals("Intent list should not be updated.", initialSize, newSize);
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowFilter()}
     */
    @Test
    public void testDeleteFlowFilter() throws Exception {
        boolean actualResult, expectedResult;
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
        PowerMockito.doThrow(new ServiceUnavailableException()).when(
                spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "deleteFlowFilter", INDEX[1]);
        Assert.assertEquals("Should return false, when unable to deleted flow filter because of ServiceUnavailableException.",
                expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#createBridge()}
     */
    @Test
    public void testCreateBridge() throws Exception {
        boolean actualResult, expectedResult;
        Status mockStatus = PowerMockito.mock(Status.class);
        PowerMockito.doReturn(true).when(mockStatus, "isSuccess");
        /**
         * Verifying that specified Bridge created in VTN Manager. Here testing
         * invalid scenario by passing invalid Tenant, Bridge name and
         * createBridge() must return false.
         */
        IVTNManager mockIVTNManager = PowerMockito.mock(IVTNManager.class);
        PowerMockito.doReturn(mockStatus).when(mockIVTNManager, "addBridge",
                Matchers.any(String.class), Matchers.any(VBridgeConfig.class));
        PowerMockito.doReturn(mockIVTNManager).when(spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        PowerMockito.doReturn(false).when(spyVTNIntentParser, "isBridgeExist",
                Matchers.any(String.class), Matchers.any(VBridgePath.class));
        /**
         * Verifying that specified Bridge created in VTN Manager. Here testing
         * valid scenario by passing valid Tenant, Bridge name and
         * createBridge() must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createBridge", TENANT[0], BRIDGE[0]);
        Assert.assertEquals("Should return true, when Bridge created.", expectedResult, actualResult);

        PowerMockito.doReturn(true).when(spyVTNIntentParser, "isBridgeExist",
                Matchers.any(String.class), Matchers.any(VBridgePath.class));
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createBridge", TENANT[0], BRIDGE[0]);
        Assert.assertEquals("Should return true, when Bridge created.", expectedResult, actualResult);
        /**
         * Verifying that specified Bridge created in VTN Manager. Here testing
         * invalid scenario if any exception raised during bridge creation then
         * createBridge() must return false.
         */
        PowerMockito.doThrow(new ServiceUnavailableException()).when(
                spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "createBridge", TENANT[0], BRIDGE[0]);
        Assert.assertEquals("Should return false, when Bridge not created because of ServiceUnavailableException.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#isBridgeExist()}
     */
    @Test
    public void testIsBridgeExist() throws Exception {
        boolean actualResult, expectedResult;
        VBridge mockVBridge = PowerMockito.mock(VBridge.class);
        List<VBridge> listOfVBridges = new ArrayList<VBridge>();
        listOfVBridges.add(mockVBridge);
        VBridgePath mockVBridgePath = PowerMockito.mock(VBridgePath.class);
        IVTNManager mockIVTNManager = PowerMockito.mock(IVTNManager.class);
        PowerMockito.doReturn(listOfVBridges).when(mockIVTNManager, "getBridges", Matchers.any(VBridgePath.class));
        PowerMockito.doReturn(mockIVTNManager).when(spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        PowerMockito.doReturn(BRIDGE[0]).when(mockVBridge, "getName");
        /**
         * Verifying that specified Bridge exist in VTN Manager. Here testing
         * valid scenario by passing valid Bridge name, path and isBridgeExist()
         * must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isBridgeExist", BRIDGE[0], mockVBridgePath);
        Assert.assertEquals("Should return true, when Bridge is  available.", expectedResult, actualResult);
        /**
         * Verifying that specified Bridge exist in VTN Manager. Here testing
         * invalid scenarios by passing invalid Bridge names, paths and
         * isBridgeExist() must return false.
         */
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isBridgeExist", BRIDGE[1], mockVBridgePath);
        Assert.assertEquals("Should return false, when Bridge is not available.", expectedResult, actualResult);
        /**
         * Verifying that specified Bridge exist in VTN Manager. Here testing
         * invalid scenarios if any exception is raised when reading VTN Manager
         * object then isBridgeExist() must return false.
         */
        PowerMockito.doThrow(new ServiceUnavailableException()).when(spyVTNIntentParser, "getVTNManager", DEFAULT_SERVICE);
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser, "isBridgeExist", BRIDGE[0], mockVBridgePath);
        Assert.assertEquals("Should return false, when Bridge is not available.", expectedResult, actualResult);
    }

    /**
     * Test case for {@link VTNIntentParser#containsName()}
     */
    @Test
    public void testContainsName() throws Exception {
        boolean actualResult, expectedResult;
        IntentWrapper mockIntentWrapper = PowerMockito.mock(IntentWrapper.class);
        PowerMockito.doReturn(ENTITY_DESCRIPTION[3]).when(mockIntentWrapper, "getEntityDescription");
        List<IntentWrapper> listOfIntentWrappers = new ArrayList<IntentWrapper>();
        listOfIntentWrappers.add(mockIntentWrapper);
        /**
         * Verifying that specified condition exist in given list. Here testing
         * valid scenarios by passing list of intent wrappers and valid
         * condition then containsName() must return true.
         */
        expectedResult = true;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "containsName", listOfIntentWrappers, ENTITY_DESCRIPTION[3]);
        Assert.assertEquals("Should return true, when entity description is available in given list of intent wrappers",
                expectedResult, actualResult);
        /**
         * Verifying that specified condition exist in given list. Here testing
         * invalid scenarios by passing list of intent wrappers and invalid
         * condition then containsName() must return false.
         */
        expectedResult = false;
        actualResult = Whitebox.invokeMethod(spyVTNIntentParser,
                "containsName", listOfIntentWrappers, ENTITY_DESCRIPTION[2]);
        Assert.assertEquals("Should return false, when entity description is not available in given list of intent wrappers",
                expectedResult, actualResult);
    }
}
