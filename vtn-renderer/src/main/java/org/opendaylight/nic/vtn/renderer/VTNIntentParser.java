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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.vtn.manager.IVTNManager;
import org.opendaylight.vtn.manager.VBridge;
import org.opendaylight.vtn.manager.VBridgeConfig;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VTenant;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.vtn.manager.VlanMapConfig;
import org.opendaylight.vtn.manager.flow.cond.FlowCondition;
import org.opendaylight.vtn.manager.flow.cond.FlowMatch;
import org.opendaylight.vtn.manager.flow.cond.EthernetMatch;
import org.opendaylight.vtn.manager.flow.cond.Inet4Match;
import org.opendaylight.vtn.manager.flow.cond.InetMatch;
import org.opendaylight.vtn.manager.flow.filter.DropFilter;
import org.opendaylight.vtn.manager.flow.filter.FlowFilter;
import org.opendaylight.vtn.manager.flow.filter.FlowFilterId;
import org.opendaylight.vtn.manager.flow.filter.PassFilter;
import org.opendaylight.vtn.manager.util.EtherAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

public class VTNIntentParser {

    private static final String TENANTNAME = "VTNRenderer";
    private static final String BRIDGENAME = "default";
    private static final Logger LOG = LoggerFactory
            .getLogger(VTNIntentParser.class);
    int flow_index = 1;
    int flowcond_index = 1;
    IVTNManager mgr;

    /**
     * Creates a default Virtual Tenant and default bridge with vlan mapping
     */
    public void createDefault() {
        try {

            mgr = getVTNManager("default");
            boolean status = createTenant(TENANTNAME, mgr);
            status = createBridge(TENANTNAME, BRIDGENAME, true);
            LOG.info(" Bridge creation status", status);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * Creates VTN elements based on the intent action
     *
     * @param IP1
     * @param IP2
     * @param action
     */
    public void rendering(final String adressSrc, final String adressDst,
            String action, ArrayList<IntentWrapper> intentList) {
        try {
            if ((validateIP(adressSrc))
                    && (validateIP(adressDst))
                    && (validateSubnet(adressSrc, adressDst))
                    || ((validateMacAddress(adressSrc)) && (validateMacAddress(adressDst)))) {

                createDefault();

                action = action.toLowerCase();
                String condNameSrcDst = constructCondName(adressSrc, adressDst);
                String condNameDstSrc = constructCondName(adressDst, adressSrc);
                switch (action) {

                case "allow":

                    boolean condStatus = createFlowCond(adressSrc, adressDst,
                            condNameSrcDst);

                    createFlowCond(adressDst, adressSrc, condNameDstSrc);
                    createFlowCond("0.0", "0.0", "match_any");
                    createFlowFilter(TENANTNAME, BRIDGENAME, "DROP",
                            "match_any", false, intentList);
                    createFlowFilter(TENANTNAME, BRIDGENAME, "PASS",
                            condNameSrcDst, true, intentList);
                    createFlowFilter(TENANTNAME, BRIDGENAME, "PASS",
                            condNameDstSrc, true, intentList);

                    break;
                case "block":
                    createFlowCond(adressSrc, adressDst, condNameSrcDst);
                    createFlowCond(adressDst, adressSrc, condNameDstSrc);
                    createFlowCond("0.0", "0.0", "match_any");
                    createFlowFilter(TENANTNAME, BRIDGENAME, "DROP",
                            "match_any", false, intentList);
                    createFlowFilter(TENANTNAME, BRIDGENAME, "DROP",
                            condNameSrcDst, true, intentList);
                    createFlowFilter(TENANTNAME, BRIDGENAME, "DROP",
                            condNameDstSrc, true, intentList);

                    break;
                default:
                    LOG.info("{} Operation NOT SUPPORTED", action);

                }

            } else {
                LOG.error("Invalid Address");
            }
        } catch (Exception e) {
            LOG.error("Unable to create VTN Objects", e);
        }
    }

    /**
     * Delete a particular intent
     *
     * @param intentID
     */
    public void Delete(String intentID) {
        try {
            ArrayList<IntentWrapper> list = VTNRendererUtility.hashMapIntentUtil
                    .get(intentID);
            for (IntentWrapper intentWrapper : list) {
                if (intentWrapper.getEntityDescription().equalsIgnoreCase(
                        "FlowFilter")) {
                    deleteFlowCond(intentWrapper.entityName);
                    deleteFlowFilter(intentWrapper.getEntityValue());

                }
            }
        } catch (Exception e) {
            LOG.error(intentID + ":Unable to Delete", e);
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
        if (validateIP(adressSrc) && (validateIP(addressDst))) {
            cond_name = adressSrc.replace(".", "");
            cond_name = cond_name.concat(addressDst.replace(".", ""));
            cond_name = "cond_" + cond_name;

        } else if (validateMacAddress(adressSrc)
                && (validateMacAddress(addressDst))) {
            cond_name = adressSrc.replace(":", "");
            cond_name = cond_name.concat(addressDst.replace(":", ""));
            cond_name = "cond_" + cond_name;
        }
        return cond_name;
    }

    /**
     * To obtain VTN manager Instance
     *
     * @param containerName
     * @return
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
     * @param tenantName
     * @param mgr
     * @return
     */
    public boolean createTenant(String tenantName, IVTNManager mgr) {
        boolean created = false;
        try {

            VTenantPath path = new VTenantPath(tenantName);
            VTenantConfig tconf = new VTenantConfig(tenantName + " "
                    + "created ");

            if (!checkTenantStatus(tenantName)) {
                Status status = mgr.addTenant(path, tconf);
                if (status.isSuccess()) {
                    created = true;

                } else {
                    created = false;
                }
            } else {
                created = true;
            }
        } catch (Exception ex) {
            LOG.error("Tenant creation Error", ex);
        }

        return created;
    }

    /**
     * This method will return if the tenant with the same name is already
     * created, if tenant with same name already exists false is returned else
     * return true.
     *
     * @param tenantName
     * @return
     */
    public boolean checkTenantStatus(String tenantName) {
        boolean status = false;
        try {
            mgr = getVTNManager("default");

            for (VTenant vTenant : mgr.getTenants()) {
                if (vTenant.getName().equalsIgnoreCase(tenantName)) {
                    status = true;
                }
            }
        } catch (Exception e) {
            status = true;
            LOG.error("Unable to check the tenant status", e);
        }
        return status;
    }

    /**
     * This method creates flow condition
     *
     * @param IP1
     * @param IP2
     */
    public boolean createFlowCond(final String addressSrc,
            final String addressDst, String condName) {
        boolean created = false;
        try {

            if (!checkFlowCondCreation(condName)) {
                ArrayList<FlowMatch> matchList = new ArrayList<FlowMatch>();
                mgr = getVTNManager("default");
                FlowCondition fcond;

                if (addressSrc.equalsIgnoreCase("0.0")) {
                    //matchList = null;
                    EtherAddress addr = null;
                    EthernetMatch ethernetMatch = new EthernetMatch(addr,
                            addr, 2048, (short) 0, null);
                    FlowMatch flowmatch = new FlowMatch(flowcond_index++,
                            ethernetMatch, null, null);
                    matchList.add(flowmatch);

                } else if ((validateIP(addressSrc))
                        && (validateIP(addressDst))
                        || ((validateMacAddress(addressSrc)) && (validateMacAddress(addressDst)))) {
                    if (validateIP(addressSrc)) {
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

                UpdateType status = mgr.setFlowCondition(condName, fcond);

                if (status.getName().equalsIgnoreCase("added")) {
                    LOG.info("Flow Condition created:");
                    created = true;

                } else {
                    created = false;
                }

            } else {
                created = true;
                return created;
            }
        } catch (Exception ex) {
            created = false;
            LOG.error("Unable to create Flow Condition", ex);
        }

        return created;
    }

    /**
     * Delete the given flow condition
     *
     * @param condName
     *            returns true on successfully deletion
     * @return
     */
    public boolean deleteFlowCond(String condName) {
        try {
            mgr = getVTNManager("default");
            if (checkFlowCondCreation(condName)) {
                Status status = mgr.removeFlowCondition(condName);
                if (status.isSuccess()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            LOG.error("Unable to delete Flow Condition", e);
        }
        return false;

    }

    /**
     * This method will return if the bridge already exists, if tenant with same
     * name already exists false is returned else return true.
     *
     * @param bridgeName
     * @return
     */
    public boolean checkFlowCondCreation(String condName) {
        boolean status = false;
        try {
            mgr = getVTNManager("default");
            for (FlowCondition condition : mgr.getFlowConditions()) {
                if (condition.getName().equalsIgnoreCase(condName)) {
                    status = true;
                }
            }
        } catch (Exception e) {
             status = true;
             LOG.error("Unable to check Flow Condition Creation", e);
        }
        return status;
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
            String type, String cond_name, boolean add,
            ArrayList<IntentWrapper> arrayList) throws Exception {
        boolean in = false;
        boolean out = true;
        int index = 0;
        if (cond_name.equalsIgnoreCase("match_any")) {
            index = 65535;
        } else {
            index = flow_index++;
        }

        FlowFilter filter = null;
        mgr = getVTNManager("default");
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
        if (add) {
            IntentWrapper intentWrapper = new IntentWrapper();
            intentWrapper.setEntityValue(index);
            intentWrapper.setEntityDescription(cond_name);
            intentWrapper.setEntityName("FlowFilter");
            arrayList.add(intentWrapper);
        }

    }

    /**
     * Delete an Existing flow filter with received ID
     *
     * @param index
     * @return
     */
    public boolean deleteFlowFilter(int index) {
        try {
            mgr = getVTNManager("default");
            VBridgePath path = new VBridgePath(TENANTNAME, BRIDGENAME);
            FlowFilterId fid = new FlowFilterId(path, false);
            mgr.removeFlowFilter(fid, index);
        } catch (Exception e) {
            LOG.error("Unable to delete Flow Filter ", e);

        }

        return false;
    }

    /**
     * Create a virtual bridge.
     *
     * @param tenantName
     * @param bridgeName
     * @param vlanMap
     *            which on true creates vlanmapping for the vbridge created
     * @return
     * @throws Exception
     */
    public boolean createBridge(String tenantName, String bridgeName,
            boolean vlanMap) {
        boolean bridgeStatus = false;
        try {
            mgr = getVTNManager("default");
            VBridgePath bridgePath = new VBridgePath(tenantName, bridgeName);
            VBridgeConfig bconf = new VBridgeConfig(bridgeName + " "
                    + "description");
            if (!checkBridgeCreation(bridgeName, bridgePath)) {
                Status status = mgr.addBridge(bridgePath, bconf);
                VlanMapConfig vlconf = new VlanMapConfig(null, (short) 0);
                mgr.addVlanMap(bridgePath, vlconf);
                if (status.isSuccess()) {
                    bridgeStatus = true;
                } else {
                    bridgeStatus = false;
                }
            } else {
                bridgeStatus = true;
            }

        } catch (Exception exception) {
            bridgeStatus = false;
            LOG.error("Failed to create Bridge.", exception);
        }
        return bridgeStatus;
    }

    /**
     * This method will return if the bridge already exists, if tenant with same
     * name already exists false is returned else return true.
     *
     * @param bridgeName
     * @return
     */
    public boolean checkBridgeCreation(String bridgeName, VBridgePath path) {
        boolean status = false;
        try {
            mgr = getVTNManager("default");
            for (VBridge vBridge : mgr.getBridges(path)) {
                if (vBridge.getName().equalsIgnoreCase(bridgeName)) {
                    status = true;
                }
            }
        } catch (Exception e) {

            status = true;
            LOG.error("Unable to get bridge status.", e);

        }
        return status;
    }

    /**
     * Validates the received IP address
     *
     * @param ip
     * @return true on success
     */
    private boolean validateIP(final String ip) {
        String ipAddressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        Pattern pattern = Pattern.compile(ipAddressPattern);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    /**
     * Validate the given Mac address
     *
     * @param macAddress
     * @return true on valid MAC address
     */
    private boolean validateMacAddress(final String macAddress) {

        String macAdrressPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        Pattern pattern = Pattern.compile(macAdrressPattern);
        Matcher matcher = pattern.matcher(macAddress);
        return matcher.matches();

    }

    /**
     * This method verifies if both the ip exists on the same subnet
     *
     * @param IP1
     * @param IP2
     * @return
     */
    private boolean validateSubnet(String IP1, String IP2) {
        boolean result = false;
        if (!IP1.equalsIgnoreCase(IP2)) {
            result = IP1.substring(0, IP1.lastIndexOf('.')).equalsIgnoreCase(
                    IP2.substring(0, IP2.lastIndexOf('.')));
        }
        return result;

    }

}
