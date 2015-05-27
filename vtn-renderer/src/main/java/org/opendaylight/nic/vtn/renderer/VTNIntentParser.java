/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.vtn.manager.flow.cond.EthernetMatch;
import org.opendaylight.vtn.manager.flow.cond.FlowCondition;
import org.opendaylight.vtn.manager.flow.cond.FlowMatch;
import org.opendaylight.vtn.manager.flow.cond.Inet4Match;
import org.opendaylight.vtn.manager.flow.cond.InetMatch;
import org.opendaylight.vtn.manager.flow.filter.DropFilter;
import org.opendaylight.vtn.manager.flow.filter.FlowFilter;
import org.opendaylight.vtn.manager.flow.filter.FlowFilterId;
import org.opendaylight.vtn.manager.flow.filter.PassFilter;
import org.opendaylight.vtn.manager.IVTNManager;
import org.opendaylight.vtn.manager.VBridge;
import org.opendaylight.vtn.manager.VBridgeConfig;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VlanMapConfig;
import org.opendaylight.vtn.manager.VTenant;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.vtn.manager.util.EtherAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

/**
 * The VTNIntentParser class creates a VTN objects based on the Intents received.
 */
public class VTNIntentParser {

    private static final String TENANT_NAME = "vtnRenderer";
    private static final String BRIDGE_NAME = "default";
    private static final String CONTAINER_NAME = "default";
    private static final Logger LOG = LoggerFactory.getLogger(VTNIntentParser.class);
    private int flow_index = 1;
    private int flowcond_index = 1;
    private IVTNManager mgr;
    private VTNRendererUtility utility = new VTNRendererUtility();

    /**
     * Creates a default Virtual Tenant and default bridge with Vlan mapping
     */
    public void createDefault() {

        try {
            boolean status = createTenant(TENANT_NAME);

            if (status != true) {
                LOG.error("Tenant creation failed");
                return;
            }

            status = createBridge(TENANT_NAME, BRIDGE_NAME, true);

            if (status != true) {
                LOG.error("Bridge creation failed");
                return;
            }

            LOG.trace("Bridge creation status {}", status);
        } catch (Exception e) {
            LOG.error("Exception occurred in VTNIntentParser {}", e);
        }
    }

    /**
     * Delete the default configuration in VTN Manager.
     *
     * @return  {@code true} is returned if the default configuration is deleted in VTN Manager.
     */
    public boolean deleteDefault() {
        return deleteTenant(TENANT_NAME);
    }

    /**
     * Creates VTN elements based on the intent action
     *
     * @param adressSrc
     * @param adressDst
     * @param action
     */
    public void rendering(final String adressSrc, final String adressDst,
            String action, List<IntentWrapper> intentList) {
        try {
            if ((utility.validateIP(adressSrc))
                    && (utility.validateIP(adressDst))
                    && (utility.validateSubnet(adressSrc, adressDst))
                    || ((utility.validateMacAddress(adressSrc)) && (utility
                            .validateMacAddress(adressDst)))) {

                createDefault();

                String condNameSrcDst = constructCondName(adressSrc, adressDst);
                String condNameDstSrc = constructCondName(adressDst, adressSrc);

                action = action.equalsIgnoreCase("allow") ? "PASS" : action
                        .equalsIgnoreCase("block") ? "DROP" : "NA";
                if (action.equalsIgnoreCase("NA")) {
                    LOG.error("Unsupported Action {}", action);
                    return;
                }

                createFlowCond(adressSrc, adressDst, condNameSrcDst);
                createFlowCond(adressDst, adressSrc, condNameDstSrc);
                createFlowCond("0.0", "0.0", "match_any");

                createFlowFilter(TENANT_NAME, BRIDGE_NAME, "DROP",
                    "match_any", false, intentList);
                createFlowFilter(TENANT_NAME, BRIDGE_NAME, action,
                        condNameSrcDst, true, intentList);
                createFlowFilter(TENANT_NAME, BRIDGE_NAME, action,
                        condNameDstSrc, true, intentList);

                LOG.info("VTN configuration is successfully updated for user Intents.");
            } else {
                LOG.error("Invalid Address");
            }

        } catch (Exception e) {
            LOG.error("Unable to create VTN Objects {}", e);
        }
    }

    /**
     * Delete a particular intent
     *
     * @param intentID
     */
    public void delete(String intentID) {
        try {
            List<IntentWrapper> list = VTNRendererUtility.hashMapIntentUtil
                    .get(intentID);
            LOG.trace("Intent wrapper arraylist : {}", list);

            for (IntentWrapper intentWrapper : list) {
                if (intentWrapper.getEntityName()
                        .equalsIgnoreCase("FlowFilter")) {
                    deleteFlowCond(intentWrapper.getEntityDescription());
                    deleteFlowFilter(intentWrapper.getEntityValue());
                }
            }
            LOG.info("Removed VTN configuration associated with the deleted Intent: {}", intentID);

            VTNRendererUtility.hashMapIntentUtil.remove(intentID);
            if (VTNRendererUtility.hashMapIntentUtil.isEmpty()) {
                boolean result = deleteDefault();
                if (result) {
                    LOG.info("Removed default VTN configuration.");
                }
            }

        } catch (Exception e) {
            LOG.error("{} : Unable to Delete, {}", intentID, e);
        }
    }

    /**
     * This method creates a flow condition name
     *
     * @param adressSrc
     * @param addressDst
     * @return flow condition name on valid IP Address or Mac Address
     */
    public String constructCondName(final String adressSrc,
            final String addressDst) {
        String cond_name = null;

        if (utility.validateIP(adressSrc) && (utility.validateIP(addressDst))) {
            cond_name = adressSrc.replace(".", "");
            cond_name = cond_name.concat(addressDst.replace(".", ""));
            cond_name = "cond_" + cond_name;

        } else if (utility.validateMacAddress(adressSrc)
                && (utility.validateMacAddress(addressDst))) {
            cond_name = adressSrc.replace(":", "");
            cond_name = cond_name.concat(addressDst.replace(":", ""));
            cond_name = "cond_" + cond_name;
        }

        return cond_name;
    }

    /**
     * To obtain VTN Manager Instance
     *
     * @param containerName
     * @return  VTN Manager Instance
     * @throws Exception
     */
    protected IVTNManager getVTNManager(String containerName) throws Exception {
        IVTNManager mgr = (IVTNManager) ServiceHelper.getInstance(
                IVTNManager.class, containerName, this);

        if (mgr == null) {
            throw new Exception("VTN Manager Service unavailable");
        }

        return mgr;
    }

    /**
     * Create a new tenant in VTN Manager.
     *
     * @param tenantName  The name of the tenant to be created.
     * @return  {@code true} is returned if tenant is created in VTN Manager.
     */
    public boolean createTenant(String tenantName) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
        } catch (Exception e) {
            LOG.error("Failed to get the VTN Manager instance: {}", e);
            return false;
        }

        Status status = mgr.addTenant(new VTenantPath(tenantName), null);
        if (status.isSuccess()) {
            return true;
        }

        if (status.getCode().equals(StatusCode.CONFLICT)) {
            LOG.debug("The specified tenant has been already created: Tenant={}",
                      tenantName);
            return true;
        }


        LOG.error("Failed to create the tenant: Tenant={}: {}",
                  tenantName, status);
        return false;
    }

    /**
     * Delete the specified tenant in VTN Manager.
     *
     * @param tenantName  The name of the tenant to be deleted.
     * @return  {@code true} is returned if the tenant is deleted.
     */
    public boolean deleteTenant(String tenantName) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
        } catch (Exception e) {
            LOG.error("Failed to get the VTN Manager instance: {}", e);
            return false;
        }

        Status status = mgr.removeTenant(new VTenantPath(tenantName));

        if (status.isSuccess()) {
            return true;
        }

        if (status.getCode().equals(StatusCode.NOTFOUND)) {
            LOG.debug("The specified tenant has been already deleted: Tenant={}",
                      tenantName);
            return true;
        }

        LOG.error("Failed to delete the tenant: Tenant={}: {}",
                  tenantName, status);
        return false;
    }

    /**
     * This method creates flow condition
     *
     * @param addressSrc
     * @param addressDst
     * @return  {@code true} is returned if only flow condition is created in VTN Manager.
     */
    public boolean createFlowCond(final String addressSrc,
            final String addressDst, String condName) {

        try {
            if (!isFlowCondExist(condName)) {
                List<FlowMatch> matchList = new ArrayList<FlowMatch>();
                mgr = getVTNManager(CONTAINER_NAME);
                FlowCondition fcond;

                if (addressSrc.equalsIgnoreCase("0.0")) {
                    EtherAddress addr = null;
                    EthernetMatch ethernetMatch = new EthernetMatch(addr, addr,
                            2048, (short) 0, null);
                    FlowMatch flowmatch = new FlowMatch(flowcond_index++,
                            ethernetMatch, null, null);
                    matchList.add(flowmatch);
                } else if ((utility.validateIP(addressSrc))
                        && (utility.validateIP(addressDst))
                        || ((utility.validateMacAddress(addressSrc)) && (utility
                                .validateMacAddress(addressDst)))) {
                    if (utility.validateIP(addressSrc)) {
                        InetMatch match = new Inet4Match(
                                InetAddress.getByName(addressSrc), null,
                                InetAddress.getByName(addressDst), null,
                                (short) 1, null);
                        EtherAddress addr = null;
                        EthernetMatch ethernetMatch = new EthernetMatch(addr,
                                addr, 2048, (short) 0, null);
                        FlowMatch flowmatch = new FlowMatch(flowcond_index++,
                                ethernetMatch, match, null);
                        matchList.add(flowmatch);
                    } else {
                        MacAddress macAddress = new MacAddress(addressSrc);
                        EtherAddress src = new EtherAddress(macAddress);
                        macAddress = new MacAddress(addressDst);
                        EtherAddress dst = new EtherAddress(macAddress);
                        EthernetMatch ethernetMatch = new EthernetMatch(src,
                                dst, null, (short) 0, null);

                        FlowMatch flowmatch = new FlowMatch(flowcond_index++,
                                ethernetMatch, null, null);
                        matchList.add(flowmatch);
                    }

                } else {
                    return false;
                }

                fcond = new FlowCondition(condName, matchList);
                UpdateType result = mgr.setFlowCondition(condName, fcond);

                if (result == null) {
                    LOG.trace("A Flow Condition has been already created.");
                    return true;
                } else if (result.equals(UpdateType.ADDED)) {
                    LOG.trace("A Flow Condition is newly created.");
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            LOG.error("Unable to create Flow Condition {}", ex);
            return false;
        }

        return true;
    }

    /**
     * Delete the given flow condition
     *
     * @param condName
     * @return  {@code true} on successful deletion
     */
    public boolean deleteFlowCond(String condName) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            if (isFlowCondExist(condName)) {
                Status status = mgr.removeFlowCondition(condName);

                return status.isSuccess();
            }
        } catch (Exception e) {
            LOG.error("Unable to delete Flow Condition {}", e);
            return false;
        }

        return true;
    }

    /**
     * This method will return if the bridge already exists, if tenant with same
     * name already exists false is returned else return true.
     *
     * @param condName
     * @return  {@code false} is returned if flow condition is not present in VTN Manager.
     */
    public boolean isFlowCondExist(String condName) {

        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (FlowCondition condition : mgr.getFlowConditions()) {
                if (condition.getName().equalsIgnoreCase(condName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to check Flow Condition {}", e);
        }

        return false;
    }

    /**
     * Creates flow filter for the given flow condition
     *
     * @param tenantName
     * @param bridgeName
     * @param type
     * @param cond_name
     * @throws Exception
     */
    public void createFlowFilter(String tenantName, String bridgeName,
            String type, String cond_name, boolean canAdd,
            List<IntentWrapper> intentList) throws Exception {
        boolean in = false;
        int index = 0;

        if (cond_name.equalsIgnoreCase("match_any")) {
            index = 65535;
        } else {
            index = flow_index++;
        }

        FlowFilter filter = null;
        mgr = getVTNManager(CONTAINER_NAME);
        VBridgePath path = new VBridgePath(tenantName, bridgeName);

        if (type.equalsIgnoreCase("PASS")) {
            PassFilter passFilter = new PassFilter();
            filter = new FlowFilter(index, cond_name, passFilter, null);
        } else if (type.equalsIgnoreCase("DROP")) {
            DropFilter dropFilter = new DropFilter();
            filter = new FlowFilter(index, cond_name, dropFilter, null);

        } else {
            return;
        }

        FlowFilterId fid = new FlowFilterId(path, in);
        mgr.setFlowFilter(fid, index, filter);

        if (canAdd) {
            IntentWrapper intentWrapper = new IntentWrapper();
            intentWrapper.setEntityValue(index);
            intentWrapper.setEntityDescription(cond_name);
            intentWrapper.setEntityName("FlowFilter");
            intentList.add(intentWrapper);
        }
    }

    /**
     * Delete an Existing flow filter with received ID
     *
     * @param index
     * @return  {@code = true} flow filter is deleted in VTN Manager.
     */
    public boolean deleteFlowFilter(int index) {

        try {
            mgr = getVTNManager(CONTAINER_NAME);
            VBridgePath path = new VBridgePath(TENANT_NAME, BRIDGE_NAME);
            FlowFilterId fid = new FlowFilterId(path, false);
            mgr.removeFlowFilter(fid, index);
        } catch (Exception e) {
            LOG.error("Unable to delete Flow Filter {}", e);
            return false;
        }

        return true;
    }

    /**
     * Create a virtual bridge which on true creates vlanmapping for
     * the vbridge created.
     * @param tenantName
     * @param bridgeName
     * @param vlanMap
     * @return {@code = true} only bridge is created in VTN Manager.
     * @throws Exception
     */
    public boolean createBridge(String tenantName, String bridgeName,
            boolean vlanMap) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            VBridgePath bridgePath = new VBridgePath(tenantName, bridgeName);
            VBridgeConfig bconf = new VBridgeConfig(bridgeName + " description");

            if (!isBridgeExist(bridgeName, bridgePath)) {
                Status status = mgr.addBridge(bridgePath, bconf);
                VlanMapConfig vlconf = new VlanMapConfig(null, (short) 0);
                mgr.addVlanMap(bridgePath, vlconf);

                return status.isSuccess();
            } else {
                // Already Bridge exists
                return true;
            }

        } catch (Exception exception) {
            LOG.error("Failed to create Bridge {}", exception);
        }

        return false;
    }

    /**
     * This method will return true if the bridge already exists else return false.
     *
     * @param bridgeName
     * @return  {@code = false} only bridge is not present in VTN Manager.
     */
    public boolean isBridgeExist(String bridgeName, VBridgePath path) {

        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (VBridge vBridge : mgr.getBridges(path)) {
                if (vBridge.getName().equalsIgnoreCase(bridgeName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to get bridge status {}", e);
        }

        return false;
    }

}
