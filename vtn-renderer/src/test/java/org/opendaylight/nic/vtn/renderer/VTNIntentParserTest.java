/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;
import org.opendaylight.vtn.manager.flow.cond.EthernetMatch;
import org.opendaylight.vtn.manager.flow.cond.FlowCondition;
import org.opendaylight.vtn.manager.flow.cond.FlowMatch;
import org.opendaylight.vtn.manager.IVTNManager;
import org.opendaylight.vtn.manager.util.EtherAddress;
import org.opendaylight.vtn.manager.VBridge;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.vtn.manager.VTNException;

/**
 * JUnit test for {@link VTNIntentParser}.
 */
public class VTNIntentParserTest extends TestBase
{
    /**
     * Instance of VTNIntentParser to perform unit testing.
     */
    private VTNIntentParser intentParser;

    /**
     * Instance of Status to perform unit testing.
     */
    private Status status;

    /**
     * integer values used in mocking the functionality in unit testing.
     */
    private int hitCount, statusCodeIndex, flowCount;

    /**
     * Default instance of VTNIntentParser to perform unit testing.
     */
    private VTNIntentParser defaultParser = new VTNIntentParser();

    /**
     * It creates the required objects for every unit test cases.
     *
     * @throws Exception
     */
    @Before
    public void setup() throws Exception {
        /**
         * Mocking Status, IVTNManager objects for VTNIntentParser.
         */
        intentParser = new VTNIntentParser() {
            @Override
            public IVTNManager getVTNManager(String serviceName)
                    throws VTNException {
                IVTNManager ivtnManager = Mockito.mock(IVTNManager.class);
                status = new Status(StatusCode.CREATED) {
                    @Override
                    public boolean isSuccess() {
                        if (hitCount == 0) {
                            hitCount++;
                            return true;
                        }
                        else
                            return false;
                    }
                    @Override
                    public StatusCode getCode() {
                        if (statusCodeIndex != 3) {
                            statusCodeIndex++;
                            return StatusCode.CREATED;
                        } else if (statusCodeIndex == 3) {
                            return StatusCode.NOTFOUND;
                        } else if (statusCodeIndex == 4) {
                            return StatusCode.CONFLICT;
                        }
                        return StatusCode.CREATED;
                    }
                };
                List<FlowCondition> conditions = new ArrayList<FlowCondition>();
                EtherAddress addr = null;
                EthernetMatch ethernetMatch = new EthernetMatch(addr, addr, 2,
                        (short) 0, null);
                FlowMatch flowmatch = new FlowMatch(1, ethernetMatch, null,
                        null);
                List<FlowMatch> matchList = new ArrayList<FlowMatch>();
                matchList.add(flowmatch);
                FlowCondition condition = new FlowCondition(
                        FLOW_CONDITION_NAME[0], matchList);
                conditions.add(condition);
                conditions.add(null);
                Mockito.when(
                        ivtnManager.addTenant(Mockito.isA(VTenantPath.class),
                                (Mockito.isA(VTenantConfig.class))))
                        .thenReturn(status);
                Mockito.when(
                        ivtnManager.removeTenant(Mockito.isA(VTenantPath.class)))
                        .thenReturn(status);
                Mockito.when(
                        ivtnManager.removeFlowCondition(Mockito.anyString()))
                        .thenReturn(status);
                Mockito.when(ivtnManager.getFlowConditions()).thenReturn(
                        conditions);
                if(flowCount<3) {
                    flowCount++;
                    Mockito.when(ivtnManager.setFlowCondition(Mockito.anyString(), Mockito.isA(FlowCondition.class))).thenReturn(UpdateType.ADDED);
                    Mockito.when(ivtnManager.setFlowCondition(Mockito.anyString(), Mockito.isA(FlowCondition.class))).thenReturn(UpdateType.ADDED);
                }
                else if(flowCount<5) {
                    flowCount++;
                    Mockito.when(ivtnManager.setFlowCondition(Mockito.anyString(), Mockito.isA(FlowCondition.class))).thenReturn(null);
                }
                else
                    Mockito.when(ivtnManager.setFlowCondition(Mockito.anyString(), Mockito.isA(FlowCondition.class))).thenReturn(UpdateType.REMOVED);
                return ivtnManager;
            }
        };
    }

    /**
     * This will make unwanted object eligible for garbage collection.
     */
    @After
    public void tearDown() {
        intentParser = null;
    }

    /**
     * Test case for {@link VTNIntentParser#deleteTenant()}
     */
    @Test
    public void testDeleteTenant() {
        /**
         * Verifying that deletion of specified Tenant in VTN Manager.
         * Here testing valid scenario if Tenant delted then deleteTenant() must return true.
         */
        assertTrue(intentParser.deleteTenant(TENANT[0]));

        /**
         * Verifying that deletion of specified TENANT in VTN Manager.
         * Here testing invalid scenarios if Tenant not delted then deleteTenant() must return false.
         */
        assertFalse(intentParser.deleteTenant(TENANT[1]));
        assertFalse(defaultParser.deleteTenant(TENANT[2]));
        assertFalse(defaultParser.deleteTenant(TENANT[3]));
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowCond()}
     */
    @Test
    public void testDeleteFlowCond() {
        /**
         * Verifying that deletion of specified FlowCondition in VTN Manager.
         * Here testing valid scenarios by passing a parameter as valid FlowConditions and deleteFlowCond() must return true.
         */
        assertTrue(intentParser.deleteFlowCond(FLOW_CONDITION_NAME[0]));
        assertTrue(intentParser.deleteFlowCond(FLOW_CONDITION_NAME[1]));

        /**
         * Verifying that deletion of the specified FlowCondition in VTN Manager.
         * Here testing invalid scenario by passing a parameter as invalid FlowCondition and deleteFlowCond() must return false.
         */
        assertFalse(defaultParser.deleteFlowCond(FLOW_CONDITION_NAME[2]));
    }

    /**
     * Test case for {@link VTNIntentParser#createTenant()}
     */
    @Test
    public void testCreateTenant() {
        /**
         * Verifying that creation a new tenant in VTN Manager in VTN Manager.
         * Here testing valid scenario if specified tenant created and createTenant() must return true.
         */
        assertTrue(intentParser.createTenant(TENANT[0]));

        /**
         * Verifying that creation a new tenant in VTN Manager in VTN Manager.
         * Here testing invalid scenario if specified tenant not created and createTenant() must return false.
         */
        assertFalse(intentParser.createTenant(TENANT[1]));
        assertFalse(intentParser.createTenant(TENANT[2]));
        assertFalse(intentParser.createTenant(TENANT[3]));
        assertFalse(defaultParser.createTenant(TENANT[0]));
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowFilter()}
     */
    @Test
    public void testDeleteFlowFilter() {
        /**
         * Verifying that deletion of specified FlowFilter in VTN Manager.
         * Here testing valid scenario by passing valid index and deleteFlowFilter() must return true.
         */
        assertTrue(intentParser.deleteFlowFilter(10));
        intentParser = new VTNIntentParser();

        /**
         * Verifying that deletion of specified FlowFilter in VTN Manager.
         * Here testing invalid scenario by passing invalid index and deleteFlowFilter() must return false.
         */
        assertFalse(intentParser.deleteFlowFilter(11));
    }

    /**
     * Test case for {@link VTNIntentParser#isBridgeExist()}
     *
     * @throws Exception
     */
    @Test
    public void testIsBridgeExist() throws Exception {
        final IVTNManager iVTNManager = Mockito.mock(IVTNManager.class);
        VBridge vBridge = Mockito.mock(VBridge.class);
        VBridge vBridge2 = Mockito.mock(VBridge.class);
        List<VBridge> list = new ArrayList<VBridge>();
        list.add(vBridge);
        VBridgePath path = new VBridgePath(" ", " ");
        /**
         * Mocking VTN manager to get bridges.
         */
        Mockito.when(iVTNManager.getBridges(path)).thenReturn(list);
        Mockito.when(vBridge.getName()).thenReturn(BRIDGE[0]);
        Mockito.when(vBridge2.getName()).thenReturn(BRIDGE[1]);
        intentParser = new VTNIntentParser() {
            @Override
            public IVTNManager getVTNManager(String serviceName) {
                return iVTNManager;
            }
        };
        /**
         * Verifying that specified Bridge exist in VTN Manager.
         * Here testing valid scenario by passing valid Bridge name, path and isBridgeExist() must return true.
         */
        assertTrue(intentParser.isBridgeExist(BRIDGE[0], path));

        /**
         * Verifying that specified Bridge exist in VTN Manager.
         * Here testing invalid scenarios by passing invalid Bridge names, paths and isBridgeExist() must return false.
         */
        assertFalse(intentParser.isBridgeExist(BRIDGE[1], path));
        list.clear();
        list.add(null);
        assertFalse(intentParser.isBridgeExist(BRIDGE[0], path));
        assertFalse(intentParser.isBridgeExist(BRIDGE[1], path));
        list.clear();
        list.add(vBridge2);
        assertFalse(intentParser.isBridgeExist(BRIDGE[0], path));
        /**
         * Verifying that specified Bridge exist in VTN Manager.
         * Here testing valid scenario by passing valid Bridge name, path and isBridgeExist() must return true.
         */
        assertTrue(intentParser.isBridgeExist(BRIDGE[1], path));
    }

    /**
     * Test case for {@link VTNIntentParser#createBridge()}
     */
    @Test
    public void testCreateBridge() {
        VTNIntentParser intentParser2 = new VTNIntentParser() {
            @Override
            public boolean isBridgeExist(String bridgeName, VBridgePath path) {
                return true;
            }
            @Override
            public IVTNManager getVTNManager(String serviceName) {
                return Mockito.mock(IVTNManager.class);
            }
        };
        /**
         * Verifying that specified Bridge created in VTN Manager.
         * Here testing valid scenario by passing valid Tenant, Bridge name and createBridge() must return true.
         */
        assertTrue(intentParser2.createBridge(TENANT[0], BRIDGE[0]));

        /**
         * Verifying that specified Bridge created in VTN Manager.
         * Here testing invalid scenario by passing invalid Tenant, Bridge name and createBridge() must return false.
         */
        assertFalse(new VTNIntentParser().createBridge(null, null));
    }

    /**
     * Test case for {@link VTNIntentParser#createFlowFilter()}
     *
     * @throws Exception
     */
    @Test
    public void testCreateFlowFilter() throws Exception {
        List list = new ArrayList();
        int initialSize;
        int newSize;
        /**
         * Verifying that specified FlowFilter created in VTN Manager.
         * Here testing invalid scenario by passing a parameter as invalid action, flow Condition.
         */
        initialSize = list.size();
        intentParser.createFlowFilter(TENANT[0], BRIDGE[0], " ",FLOW_CONDITION_NAME[3], false, list);
        newSize = list.size();
        assertEquals(newSize, initialSize);

        initialSize = list.size();
        intentParser.createFlowFilter(TENANT[0], BRIDGE[0],
                FLOW_CONDITION_ACTION[0], FLOW_CONDITION_NAME[3], false, list);
        newSize = list.size();
        assertEquals(newSize, initialSize);

        initialSize = list.size();
        intentParser.createFlowFilter(TENANT[0], BRIDGE[0],
                FLOW_CONDITION_ACTION[1], FLOW_CONDITION_NAME[3], false, list);
        newSize = list.size();
        assertEquals(newSize, initialSize);

        initialSize = list.size();
        intentParser.createFlowFilter(TENANT[0], BRIDGE[0],
                FLOW_CONDITION_ACTION[1], " ", false, list);
        newSize = list.size();
        assertEquals(newSize, initialSize);

        initialSize = list.size();
        intentParser.createFlowFilter(TENANT[0], BRIDGE[0],
                FLOW_CONDITION_ACTION[1], " ", false, list);
        newSize = list.size();
        assertEquals(newSize, initialSize);

        /**
         * Verifying that specified FlowFilter created in VTN Manager.
         * Here testing valid scenario by passing a parameter as valid Tenant, Bridge, Action, Condition.
         */
        initialSize = list.size();
        intentParser.createFlowFilter(TENANT[0], BRIDGE[0],
                FLOW_CONDITION_ACTION[1], FLOW_CONDITION_NAME[3], true,
                list);
        newSize = list.size();
        assertEquals(newSize, initialSize+1);
    }

    /**
     * Test case for {@link VTNIntentParser#isFlowCondExist()}
     *
     * @throws Exception
     */
    @Test
    public void testIsFlowCondExist() throws Exception {
        VTNIntentParser intentParser2 = new VTNIntentParser() {
            @Override
            public IVTNManager getVTNManager(String serviceName)
                    throws Exception {
                IVTNManager iVTNManager = Mockito.mock(IVTNManager.class);
                List<FlowCondition> list = new ArrayList<FlowCondition>();
                list.add(new FlowCondition(FLOW_CONDITION_NAME[0],
                        new ArrayList()));
                list.add(new FlowCondition(FLOW_CONDITION_NAME[0],
                        new ArrayList()));
                list.add(new FlowCondition(FLOW_CONDITION_NAME[0],
                        new ArrayList()));
                list.add(new FlowCondition(FLOW_CONDITION_NAME[3],
                        new ArrayList()));
                Mockito.when(iVTNManager.getFlowConditions()).thenReturn(list);
                return iVTNManager;
            }
        };
        VTNIntentParser intentParser3 = new VTNIntentParser() {
            @Override
            public IVTNManager getVTNManager(String serviceName)
                    throws Exception {
                IVTNManager iVTNManager = Mockito.mock(IVTNManager.class);
                if (serviceName.equals(defaultService))
                    throw new Exception();
                return iVTNManager;
            }
        };
        /**
         * Verifying that specified FlowCondition exist in VTN Manager.
         * Here testing valid scenario by passing a parameter as valid FlowContion and isFlowCondExist() must return true.
         */
        assertTrue(intentParser2.isFlowCondExist(FLOW_CONDITION_NAME[0]));
        assertTrue(intentParser2.isFlowCondExist(FLOW_CONDITION_NAME[3]));

        /**
         * Verifying that specified FlowCondition exist in VTN Manager.
         * Here testing invalid scenario by passing a parameter as invalid FlowContion and isFlowCondExist() must return false.
         */
        assertFalse(intentParser3.isFlowCondExist(FLOW_CONDITION_NAME[2]));
    }

    /**
     * Test case for {@link VTNIntentParser#deleteDefault()()}
     */
    @Test
    public void testDeleteDefault() {
        /**
         * Verifying that default configuration deleted in VTN Manager.
         * Here if default configuration already exist then deleteDefault() must delete return true.
         */
        assertTrue(intentParser.deleteDefault());

        /**
         * Verifying that default configuration deleted in VTN Manager.
         * Here if default configuration not exists then deleteDefault() must return false.
         */
        assertFalse(new VTNIntentParser().deleteDefault());
    }

    /**
     * Test case for {@link VTNIntentParser#delete()}
     */
    @Test
    public void testDelete() {
        int initialSize, newSize;
        IntentWrapper intentWrapper = new IntentWrapper();
        intentWrapper.setEntityDescription(INTENT_ENTITY_DESCRIPTION[0]);
        intentWrapper.setEntityName(INTENT_ENTITY_NAMES[1]);
        intentWrapper.setEntityValue(15);
        List<IntentWrapper> intentList = new ArrayList<IntentWrapper>();
        intentList.add(intentWrapper);
        Map<String, List<IntentWrapper>> hashmapIntent = new HashMap<String, List<IntentWrapper>>();
        hashmapIntent.put(INTENT_LIST_ID[0], intentList);
        VTNRendererUtility.storeIntentDetail(hashmapIntent);
        intentParser = Mockito.spy(intentParser);
        Mockito.when(intentParser.deleteFlowCond(Mockito.anyString())).thenReturn(null);
        Mockito.when(intentParser.deleteFlowFilter(Mockito.anyInt())).thenReturn(null);
        /**
         * Verifying that particular intent deleted in VTN Manager.
         * Here testing valid scenario by passing valid Intent ID and that Intent must be deleted and Intent list size must be decreased.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        intentParser.delete(INTENT_LIST_ID[0]);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        assertEquals(newSize, initialSize-1);
        assertEquals(VTNRendererUtility.hashMapIntentUtil.get(INTENT_LIST_ID[0]), null);
        Mockito.verify(intentParser).deleteFlowFilter(0);
        intentList.add(null);
        /**
         * Verifying that particular intent deleted in VTN Manager.
         * Here testing invalid scenario by passing invalid Intent ID and that Intent list size must be same.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        intentParser.delete(INTENT_LIST_ID[1]);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        assertEquals(newSize, initialSize);

        intentList.clear();
        IntentWrapper intentWrapper3 = new IntentWrapper();
        intentWrapper3.setEntityDescription(INTENT_ENTITY_DESCRIPTION[0]);
        intentWrapper3.setEntityName(INTENT_ENTITY_NAMES[1]);
        intentWrapper3.setEntityValue(1);
        List<IntentWrapper> intentList3 = new ArrayList<IntentWrapper>();
        intentList3.add(intentWrapper3);
        Map<String, List<IntentWrapper>> hashmapIntent3 = new HashMap<String, List<IntentWrapper>>();
        VTNRendererUtility.hashMapIntentUtil.clear();
        hashmapIntent3.put(INTENT_LIST_ID[1], intentList3);
        VTNRendererUtility.storeIntentDetail(hashmapIntent3);
        /**
         * Verifying that particular intent deleted in VTN Manager.
         * Here testing valid scenario by passing valid Intent ID and that Intent must be deleted and Intent list size must be decreased.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        defaultParser.delete(INTENT_LIST_ID[1]);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        assertEquals(newSize, initialSize-1);

        VTNRendererUtility.storeIntentDetail(hashmapIntent3);
        VTNIntentParser parser = new VTNIntentParser() {
            @Override
            public boolean deleteDefault() {
                return true;
            }
        };
        parser = Mockito.spy(parser);
        /**
         * Verifying that particular intent deleted in VTN Manager.
         * Here testing valid scenario by passing valid Intent ID and that Intent must be deleted and
         * Intent list size must be decreased.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        parser.delete(INTENT_LIST_ID[1]);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        assertEquals(newSize, initialSize-1);
        assertEquals(VTNRendererUtility.hashMapIntentUtil.get(INTENT_LIST_ID[1]), null);
        Mockito.verify(parser).deleteDefault();
    }

    /**
     * Test case for {@link VTNIntentParser#constructCondName()}
     */
    @Test
    public void testConstructCondName() {
        /**
         * Verifying that creation of a FlowCondition name if we pass valid IP or MAC addresses.
         * Here testing valid scenario by passing valid IP, MAC addresses and that constructCondName must return String object.
         */
        assertTrue(intentParser.constructCondName(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0]) instanceof String);
        assertTrue(intentParser.constructCondName(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2]) instanceof String);

        /**
         * Verifying that creates a FlowCondition name if we pass valid IP or MAC addresses
         * Here testing invalid scenario by passing invalid IP, MAC addresses and that constructCondName must return null.
         */
        assertFalse(intentParser.constructCondName(VALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[2]) instanceof String);
        assertFalse(intentParser.constructCondName(INVALID_SRC_ADDRESS[0],
                INVALID_SRC_ADDRESS[0]) instanceof String);
        assertFalse(intentParser.constructCondName(INVALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2]) instanceof String);
        assertFalse(intentParser.constructCondName(VALID_SRC_ADDRESS[2],
                INVALID_DST_ADDRESS[3]) instanceof String);
    }

    /**
     * Test case for {@link VTNIntentParser#createDefault()}
     */
    @Test
    public void testCreateDefault() {
        VTNIntentParser intentParserobj = new VTNIntentParser() {
            @Override
            public boolean createTenant(String tenantName) {
                return true;
            }
            @Override
            public boolean createBridge(String tenantName, String bridgeName) {
                return true;
            }
        };
        VTNIntentParser intentParserobj2 = new VTNIntentParser() {
            @Override
            public boolean createTenant(String tenantName) {
                return true;
            }
            @Override
            public boolean createBridge(String tenantName, String bridgeName) {
                return true;
            }
            @Override
            public boolean createFlowCond(final String addressSrc,
                    final String addressDst, String condName) {
                return true;
            }
        };
        /**
         * Verifying that create a default Virtual Tenant and default bridge with Vlan mapping and flow condition
         * Here testing invalid scenario if any thing among those are not created then createDefault() must return false.
         */
        assertFalse(intentParser.createDefault());
        assertFalse(intentParserobj.createDefault());

        /**
         * Verifying that create a default Virtual Tenant and default bridge with Vlan mapping and flow condition
         * Here testing valid scenario if all created then createDefault() must return true.
         */
        assertTrue(intentParserobj2.createDefault());
    }

    /**
     * Test case for {@link VTNIntentParser#updateRendering()}
     */
    @Test
    public void testUpdateRendering() {
        IntentWrapper intentWrapper = new IntentWrapper();
        intentWrapper.setEntityDescription(INTENT_ENTITY_DESCRIPTION[2]);
        intentWrapper.setEntityName(INTENT_ENTITY_NAMES[0]);
        intentWrapper.setEntityValue(15);
        intentWrapper.setAction(FLOW_CONDITION_ACTION[0]);
        IntentWrapper intentWrapper2 = new IntentWrapper();
        intentWrapper2.setEntityDescription(INTENT_ENTITY_DESCRIPTION[2]);
        intentWrapper2.setEntityName(INTENT_ENTITY_NAMES[0]);
        intentWrapper2.setEntityValue(10);
        intentWrapper2.setAction(FLOW_CONDITION_ACTION[0]);
        IntentWrapper intentWrapper3 = new IntentWrapper();
        intentWrapper3.setEntityDescription(INTENT_ENTITY_DESCRIPTION[3]);
        intentWrapper3.setEntityName(INTENT_ENTITY_NAMES[0]);
        intentWrapper3.setEntityValue(10);
        intentWrapper3.setAction(FLOW_CONDITION_ACTION[0]);
        List<IntentWrapper> intentList = new ArrayList<IntentWrapper>();
        List<IntentWrapper> intentListObj2 = new ArrayList<IntentWrapper>();
        List<IntentWrapper> intentListObj = new ArrayList<IntentWrapper>();
        List<IntentWrapper> intentListObj1 = new ArrayList<IntentWrapper>();
        intentList.add(intentWrapper);
        intentList.add(intentWrapper2);
        intentListObj2.add(intentWrapper3);
        intentListObj2.add(intentWrapper);
        Map<String, List<IntentWrapper>> hashmapIntent = new HashMap<String, List<IntentWrapper>>();
        hashmapIntent.put(INTENT_LIST_ID[0], intentList);
        hashmapIntent.put(INTENT_LIST_ID[2], intentListObj2);
        VTNRendererUtility.storeIntentDetail(hashmapIntent);

        intentParser = Mockito.spy(intentParser);
        int initialSize, newSize;
        String initialContent, newContent;
        /**
         * Verifying that whether VTN elements updated based on the intent action
         * Here testing valid scenario, passing valid addresses, action, Intent list, Intent ID and list must be updated.
         */
        initialContent = intentListObj.toString();
        initialSize = intentListObj.size();
        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], intentListObj, INTENT_LIST_ID[0]);
        newSize = intentListObj.size();
        newContent = intentListObj.toString();
        assertEquals(initialSize+1, newSize);
        assertNotEquals(initialContent, newContent);
        Mockito.verify(intentParser).constructCondName(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);

        initialSize = intentListObj1.size();
        initialContent = intentListObj1.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[1], intentListObj1, INTENT_LIST_ID[0]);
        newSize = intentListObj1.size();
        newContent = intentListObj1.toString();
        assertEquals(initialSize+1, newSize);
        assertNotEquals(initialContent, newContent);
        Mockito.verify(intentParser, Mockito.times(2)).constructCondName(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize+1, newSize);
        assertNotEquals(initialContent, newContent);
        Mockito.verify(intentParser, Mockito.times(3)).constructCondName(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);

        /**
         * Verifying that whether VTN elements updated based on the intent action
         * Here testing invalid scenario, passing invalid addresses, action, Intent list, Intent ID and list must be same.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], intentList, INTENT_LIST_ID[2]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(newSize, initialSize);
        assertEquals(initialContent, newContent);
        Mockito.verify(intentParser, Mockito.times(4)).constructCondName(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[1],
                VALID_DST_ADDRESS[1], ACTIONS[2], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(newSize, initialSize);
        assertEquals(initialContent, newContent);
        Mockito.verify(intentParser, Mockito.times(1)).constructCondName(VALID_SRC_ADDRESS[1], VALID_DST_ADDRESS[1]);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(INVALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[1], ACTIONS[2], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(newSize, initialSize);
        assertEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(INVALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[1], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize,  newSize);
        assertEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[4], VALID_DST_ADDRESS[1],
                ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(INVALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[0], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);
        /**
         * Verifying that whether VTN elements updated based on the intent action
         * Here testing valid scenario, passing valid addresses, action, Intent list, Intent ID and list must be updated.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        Mockito.verify(intentParser).constructCondName(VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2]);
        assertNotEquals(initialContent, newContent);
        /**
         * Verifying that whether VTN elements updated based on the intent action
         * Here testing invalid scenario, passing invalid addresses, action, Intent list, Intent ID and list must be same.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(INVALID_SRC_ADDRESS[3],
                VALID_DST_ADDRESS[2], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[2],
                INVALID_DST_ADDRESS[5], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.updateRendering(VALID_SRC_ADDRESS[2],
                INVALID_DST_ADDRESS[5], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);
    }

    /**
     * Test case for {@link VTNIntentParser#createFlowCond()}
     */
    @Test
    public void testcreateFlowCond() {
        /**
         * Verifying that specified FlowCondition created in VTN Manager.
         * Here testing valid scenario by passing valid addresses, FlowCondition names and  createFlowCond() must return true.
         */
        assertTrue(intentParser.createFlowCond(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]));
        assertTrue(intentParser.createFlowCond(VALID_SRC_ADDRESS[5], VALID_DST_ADDRESS[3], INTENT_ENTITY_NAMES[2]));
        assertTrue(intentParser.createFlowCond(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2], FLOW_CONDITION_NAME[2]));
        /**
         * Verifying that specified FlowCondition created in VTN Manager.
         * Here testing invalid scenario by passing invalid addresses, FlowCondition names and createFlowCond() must return false.
         */
        assertFalse(intentParser.createFlowCond(null, null, null));
        assertFalse(intentParser.createFlowCond(INVALID_SRC_ADDRESS[1],
                VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[2]));
        assertFalse(intentParser.createFlowCond(VALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[2], FLOW_CONDITION_NAME[2]));
        assertFalse(intentParser.createFlowCond(VALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[3], FLOW_CONDITION_NAME[2]));
    }
    /**
     * Test case for {@link VTNIntentParser#rendering()}
     */
    @Test
    public void testRendering() {
        List<IntentWrapper> intentList = new ArrayList<IntentWrapper>();
        VTNIntentParser intentParser = new VTNIntentParser() {
            @Override
            public boolean createDefault() {
                return true;
            }
            @Override
            public void createFlowFilter(String tenantName, String bridgeName,
                    String type, String cond_name, boolean canAdd,
                    List<IntentWrapper> intentList) {
                IntentWrapper intentWrapper = new IntentWrapper();
                intentWrapper.setEntityDescription(cond_name);
                intentWrapper.setAction(type);
                intentWrapper.setEntityName(INTENT_ENTITY_NAMES[0]);
                intentList.add(intentWrapper);
            }
        };
        intentParser = Mockito.spy(intentParser);
        int initialSize, newSize;
        String initialContent, newContent;
        /**
         * Verifying based on Intent action that specified VTN elements created in VTN Manager.
         * Here testing valid scenario by passing valid addresses, action, Intent list and  list must be updated.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[0], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertNotEquals(initialSize, newSize);
        assertNotEquals(initialContent, newContent);
        Mockito.verify(intentParser).constructCondName(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0]);
        Mockito.verify(intentParser).constructCondName(VALID_DST_ADDRESS[0], VALID_SRC_ADDRESS[0]);
        /**
         * Verifying based on Intent action that specified VTN elements created in VTN Manager.
         * Here testing invalid scenario by passing invalid addresses, action, Intent list and  list must be same.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(INVALID_SRC_ADDRESS[0], INVALID_DST_ADDRESS[0],
                ACTIONS[0], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        /**
         * Verifying based on Intent action that specified VTN elements created in VTN Manager.
         * Here testing valid scenario by passing valid addresses, action, Intent list and  list must be updated.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
        Mockito.verify(intentParser, Mockito.times(2)).createDefault();
        newSize = intentList.size();
        newContent = intentList.toString();
        assertNotEquals(initialSize, newSize);
        assertNotEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[1], VALID_DST_ADDRESS[1],
                ACTIONS[0], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertNotEquals(initialSize, newSize);
        assertNotEquals(initialContent, newContent);
        Mockito.verify(intentParser).constructCondName(VALID_SRC_ADDRESS[1], VALID_DST_ADDRESS[1]);
        Mockito.verify(intentParser).constructCondName(VALID_DST_ADDRESS[1], VALID_SRC_ADDRESS[1]);
        /**
         * Verifying based on Intent action that specified VTN elements created in VTN Manager.
         * Here testing invalid scenario by passing invalid addresses, action, Intent list and  list must be same.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[1], VALID_DST_ADDRESS[1],
                ACTIONS[2], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);
        Mockito.verify(intentParser, Mockito.times(4)).createDefault();

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(INVALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[1],
                ACTIONS[2], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(INVALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[1],
                ACTIONS[0], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[4], VALID_DST_ADDRESS[1], ACTIONS[1],
                intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);
        Mockito.verify(intentParser, Mockito.times(4)).createDefault();

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[1], INVALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);
        /**
         * Verifying based on Intent action that specified VTN elements created in VTN Manager.
         * Here testing valid scenario by passing valid addresses, action, Intent list and  list must be updated.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2],
                ACTIONS[0], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertNotEquals(initialSize, newSize);
        assertNotEquals(initialContent, newContent);
        Mockito.verify(intentParser, Mockito.times(5)).createDefault();
        /**
         * Verifying based on Intent action that specified VTN elements created in VTN Manager.
         * Here testing invalid scenario by passing invalid addresses, action, Intent list and  list must be same.
         */
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser.rendering(VALID_SRC_ADDRESS[2], INVALID_DST_ADDRESS[5],
                ACTIONS[1], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        VTNIntentParser intentParser2 = new VTNIntentParser() {
            @Override
            public boolean createDefault() {
                return true;
            }
            @Override
            public void createFlowFilter(String tenantName, String bridgeName,
                    String type, String cond_name, boolean canAdd,
                    List<IntentWrapper> intentList) throws Exception {
                if (canAdd == false)
                    throw new Exception();
            }
        };
        intentParser2 = Mockito.spy(intentParser2);
        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser2.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);

        Mockito.verify(intentParser2).createDefault();
        VTNIntentParser intentParser3 = Mockito.spy(new VTNIntentParser());

        initialSize = intentList.size();
        initialContent = intentList.toString();
        intentParser3.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
        newSize = intentList.size();
        newContent = intentList.toString();
        assertEquals(initialSize, newSize);
        assertEquals(initialContent, newContent);
        Mockito.verify(intentParser3).createDefault();
    }
}
