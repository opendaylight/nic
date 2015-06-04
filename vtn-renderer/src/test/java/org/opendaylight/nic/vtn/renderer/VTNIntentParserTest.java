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
import org.opendaylight.vtn.manager.VTNException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * JUnit test for {@link VTNIntentParser}.
 */
public class VTNIntentParserTest extends TestBase

{
    private VTNIntentParser intentParser;
    private Status status;
    private int hitCount, statusCodeIndex;
    VTNIntentParser defaultParser = new VTNIntentParser();

    /**
     * It creates the required default object for every unit test cases.
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
        assertTrue(intentParser.deleteTenant(TENANT));
        assertFalse(intentParser.deleteTenant(TENANT));
        assertFalse(defaultParser.deleteTenant(TENANT));
        assertFalse(defaultParser.deleteTenant(TENANT));
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowCond()}
     */
    @Test
    public void testDeleteFlowCond() {
        assertTrue(intentParser.deleteFlowCond(FLOW_CONDITION_NAME[0]));
        assertTrue(intentParser.deleteFlowCond(FLOW_CONDITION_NAME[1]));
        assertFalse(defaultParser.deleteFlowCond(FLOW_CONDITION_NAME[2]));
    }

    /**
     * Test case for {@link VTNIntentParser#createTenant()}
     */
    @Test
    public void testCreateTenant() {
        assertTrue(intentParser.createTenant(TENANT));
        assertFalse(intentParser.createTenant(TENANT));
        assertFalse(intentParser.createTenant(TENANT));
        assertFalse(intentParser.createTenant(TENANT));
        assertFalse(defaultParser.createTenant(TENANT));
    }

    /**
     * Test case for {@link VTNIntentParser#deleteFlowFilter()}
     */
    @Test
    public void testDeleteFlowFilter() {
        assertTrue(intentParser.deleteFlowFilter(10));
        intentParser = new VTNIntentParser();
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
        VBridgePath path = new VBridgePath("", "");
        /**
         * Mocking VTN manager to get bridges.
         */
        Mockito.when(iVTNManager.getBridges(path)).thenReturn(list);
        Mockito.when(iVTNManager.getBridges(path)).thenReturn(list);
        Mockito.when(vBridge.getName()).thenReturn(BRIDGE[0]);
        Mockito.when(vBridge2.getName()).thenReturn(BRIDGE[1]);
        intentParser = new VTNIntentParser() {
            @Override
            public IVTNManager getVTNManager(String serviceName) {
                return iVTNManager;
            }
        };
        assertTrue(intentParser.isBridgeExist(BRIDGE[0], path));
        list.add(0, null);
        assertFalse(intentParser.isBridgeExist(BRIDGE[0], path));
        list.add(0, vBridge2);
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
        assertTrue(intentParser2.createBridge(TENANT, BRIDGE[0]));
    }

    /**
     * Test case for {@link VTNIntentParser#createFlowFilter()}
     *
     * @throws Exception
     */
    @Test
    public void testCreateFlowFilter() throws Exception {
        intentParser.createFlowFilter(TENANT, BRIDGE[0], " ",
                FLOW_CONDITION_NAME[3], false, null);
        intentParser.createFlowFilter(TENANT, BRIDGE[0],
                FLOW_CONDITION_ACTION[0], FLOW_CONDITION_NAME[3], false, null);
        intentParser.createFlowFilter(TENANT, BRIDGE[0],
                FLOW_CONDITION_ACTION[1], FLOW_CONDITION_NAME[3], false, null);
        intentParser.createFlowFilter(TENANT, BRIDGE[0],
                FLOW_CONDITION_ACTION[1], FLOW_CONDITION_NAME[3], true,
                new ArrayList());
        intentParser.createFlowFilter(TENANT, BRIDGE[0],
                FLOW_CONDITION_ACTION[1], "", false, null);
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
                if (serviceName.equals("default"))
                    throw new Exception();
                return iVTNManager;
            }
        };
        assertTrue(intentParser2.isFlowCondExist(FLOW_CONDITION_NAME[0]));
        assertTrue(intentParser2.isFlowCondExist(FLOW_CONDITION_NAME[3]));
        assertFalse(intentParser3.isFlowCondExist(FLOW_CONDITION_NAME[2]));
    }

    /**
     * Test case for {@link VTNIntentParser#deleteDefault()()}
     */
    @Test
    public void testDeleteDefault() {
        assertTrue(intentParser.deleteDefault());
        assertFalse(new VTNIntentParser().deleteDefault());
    }

    /**
     * Test case for {@link VTNIntentParser#delete()}
     */
    @Test
    public void testDelete() {
        IntentWrapper intentWrapper = new IntentWrapper();
        intentWrapper.setEntityDescription(INTENT_ENTITY_DESCRIPTION[0]);
        intentWrapper.setEntityName(INTENT_ENTITY_NAMES[1]);
        intentWrapper.setEntityValue(15);

        IntentWrapper intentWrapper2 = new IntentWrapper();
        intentWrapper.setEntityDescription(INTENT_ENTITY_DESCRIPTION[1]);
        intentWrapper.setEntityName(INTENT_ENTITY_NAMES[0]);
        intentWrapper.setEntityValue(10);
        List<IntentWrapper> intentList = new ArrayList<IntentWrapper>();
        intentList.add(intentWrapper);
        intentList.add(intentWrapper2);
        Map<String, List<IntentWrapper>> hashmapIntent = new HashMap<String, List<IntentWrapper>>();
        hashmapIntent.put(INTENT_LIST_ID[0], intentList);
        VTNRendererUtility.storeIntentDetail(hashmapIntent);
        intentParser.delete(INTENT_LIST_ID[0]);
        intentList.remove(1);
        intentList.remove(0);
        intentParser.delete(INTENT_LIST_ID[0]);

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
        new VTNIntentParser().delete(INTENT_LIST_ID[1]);
        VTNRendererUtility.storeIntentDetail(hashmapIntent3);
        VTNIntentParser parser = new VTNIntentParser() {
            @Override
            public boolean deleteDefault() {
                return true;
            }
        };
        parser.delete(INTENT_LIST_ID[1]);
    }

    /**
     * Test case for {@link VTNIntentParser#constructCondName()}
     */
    @Test
    public void testConstructCondName() {
        assertTrue(intentParser.constructCondName(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0]) instanceof String);
        assertTrue(intentParser.constructCondName(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2]) instanceof String);
        assertNull(intentParser.constructCondName(VALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[2]));
        assertNull(intentParser.constructCondName(INVALID_SRC_ADDRESS[0],
                INVALID_SRC_ADDRESS[0]));
        assertNull(intentParser.constructCondName(INVALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2]));
        assertNull(intentParser.constructCondName(VALID_SRC_ADDRESS[2],
                INVALID_DST_ADDRESS[3]));
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
        assertFalse(intentParser.createDefault());
        assertFalse(intentParserobj.createDefault());
        assertTrue(intentParserobj2.createDefault());
    }

    /**
     * Test case for {@link VTNIntentParser#updateRendering()}
     */
    @Test
    public void testUpdateRendering() {
        IntentWrapper intentWrapper = new IntentWrapper();
        intentWrapper.setEntityDescription("cond_1000110002");
        intentWrapper.setEntityName(INTENT_ENTITY_NAMES[0]);
        intentWrapper.setEntityValue(15);
        intentWrapper.setAction(FLOW_CONDITION_ACTION[0]);

        IntentWrapper intentWrapper2 = new IntentWrapper();
        intentWrapper2.setEntityDescription("cond_1000210001");
        intentWrapper2.setEntityName(INTENT_ENTITY_NAMES[0]);
        intentWrapper2.setEntityValue(10);
        intentWrapper2.setAction(FLOW_CONDITION_ACTION[0]);
        IntentWrapper intentWrapper3 = new IntentWrapper();
        intentWrapper3.setEntityDescription("cond_1000210004");
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
        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], intentListObj, INTENT_LIST_ID[0]);
        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[1], intentListObj1, INTENT_LIST_ID[0]);

        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], ACTIONS[0], intentList, INTENT_LIST_ID[2]);
        intentParser.updateRendering(VALID_SRC_ADDRESS[1],
                VALID_DST_ADDRESS[1], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(VALID_SRC_ADDRESS[1],
                VALID_DST_ADDRESS[1], ACTIONS[2], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(INVALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[1], ACTIONS[2], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(INVALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[1], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering("10.0.5.0", VALID_DST_ADDRESS[1],
                ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(INVALID_SRC_ADDRESS[0],
                INVALID_DST_ADDRESS[0], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(INVALID_SRC_ADDRESS[3],
                VALID_DST_ADDRESS[2], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
        intentParser.updateRendering(VALID_SRC_ADDRESS[2],
                INVALID_DST_ADDRESS[5], ACTIONS[0], intentList, INTENT_LIST_ID[0]);
    }

    /**
     * Test case for {@link VTNIntentParser#createFlowCond()}
     */
    @Test
    public void testcreateFlowCond() {
        assertTrue(intentParser.createFlowCond(VALID_SRC_ADDRESS[0],
                VALID_DST_ADDRESS[0], FLOW_CONDITION_NAME[0]));
        assertTrue(intentParser.createFlowCond("0.0", "0.0", "cond2"));
        assertTrue(intentParser.createFlowCond(VALID_SRC_ADDRESS[2],
                VALID_DST_ADDRESS[2], FLOW_CONDITION_NAME[2]));
        /**
         * To test Negative scenarios to verify flow condition creation.
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

            }
        };
        intentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[0], intentList);
        intentParser.rendering(INVALID_SRC_ADDRESS[0], INVALID_DST_ADDRESS[0],
                ACTIONS[0], intentList);
        intentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[0], intentList);
        intentParser.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
        intentParser.rendering(VALID_SRC_ADDRESS[1], VALID_DST_ADDRESS[1],
                ACTIONS[0], intentList);
        intentParser.rendering(VALID_SRC_ADDRESS[1], VALID_DST_ADDRESS[1],
                ACTIONS[2], intentList);
        intentParser.rendering(INVALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[1],
                ACTIONS[2], intentList);
        intentParser.rendering(INVALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[1],
                ACTIONS[0], intentList);
        intentParser.rendering("10.0.5.0", VALID_DST_ADDRESS[1], ACTIONS[1],
                intentList);
        intentParser.rendering(VALID_SRC_ADDRESS[1], INVALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
        intentParser.rendering(VALID_SRC_ADDRESS[2], VALID_DST_ADDRESS[2],
                ACTIONS[0], intentList);
        intentParser.rendering(VALID_SRC_ADDRESS[2], INVALID_DST_ADDRESS[5],
                ACTIONS[1], intentList);
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
        intentParser2.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
        VTNIntentParser intentParser3 = new VTNIntentParser();
        intentParser3.rendering(VALID_SRC_ADDRESS[0], VALID_DST_ADDRESS[0],
                ACTIONS[1], intentList);
    }
}
