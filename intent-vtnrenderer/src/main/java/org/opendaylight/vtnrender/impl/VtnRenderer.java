/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.vtnrender.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.vtn.manager.IVTNManager;
import org.opendaylight.vtn.manager.VBridgeConfig;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VTNException;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.vtn.manager.VlanMapConfig;
import org.opendaylight.vtn.manager.flow.cond.FlowCondition;
import org.opendaylight.vtn.manager.flow.cond.FlowMatch;
import org.opendaylight.vtn.manager.flow.cond.Inet4Match;
import org.opendaylight.vtn.manager.flow.cond.InetMatch;
import org.opendaylight.vtn.manager.flow.filter.FlowFilter;
import org.opendaylight.vtn.manager.flow.filter.FlowFilterId;
import org.opendaylight.vtn.manager.flow.filter.PassFilter;

public class VtnRenderer {

    private static final String TENANTNAME = "VTNRenderer";
    private static final String BRIDGENAME = "Default";
    int flow_index = 0;
    IVTNManager mgr;
    /**
     * Create the default tenant,vbridge and vlan mapping     
     */
    protected Status createDefault() {
        Status status = null;
        try {

            mgr = getVTNManager("default");
            status = createTenant(TENANTNAME, mgr);
            if (status.isSuccess()) {
                status = createBridge(TENANTNAME, BRIDGENAME, true, mgr);
                return status;
            }
            return status;

        } catch (Exception e) {
            e.printStackTrace();

        }

        return status;

    }

    /**
     * Creates the Intent after validation
     * @param IP1
     * @param IP2
     * @param action
     */
    public void rendering(final String IP1, final String IP2, String action) {
        try {
            if ((validateIP(IP1)) && (validateIP(IP2))) {
                if (validateSubnet(IP1, IP2)) {
                    Status status = createDefault();
                    if (status.isSuccess()) {
                        switch (action) {

                        case "allow":
                            createFlowCond(IP1, IP2, cond_name(IP1, IP2));
                            createFlowCond(IP2, IP1, cond_name(IP2, IP1));
                            createFlowFilter(TENANTNAME, BRIDGENAME, "PASS",
                                    cond_name(IP1, IP2));
                            createFlowFilter(TENANTNAME, BRIDGENAME, "PASS",
                                    cond_name(IP2, IP1));
                            break;
                        }

                    }
                }
            }
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }

    /**
     * Creates Flow condition name
     * @param IP1
     * @param IP2
     * @return
     */
    public String cond_name(final String IP1, final String IP2) {
        String cond_name = null;
        if (IP1 != null && IP2 != null) {
            cond_name = IP1.replace(".", "");
            cond_name = cond_name.concat(IP2.replace(".", ""));

        }
        return cond_name;
    }

    /**
     * To get the vtn instance.
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
     * Creates the Tenant.
     * @param tenantName
     * @param mgr
     * @return
     */
    public Status createTenant(String tenantName, IVTNManager mgr) {
        try {

            VTenantPath path = new VTenantPath(tenantName);
            VTenantConfig tconf = new VTenantConfig(tenantName + " "
                    + "created ");
            Status status = mgr.addTenant(path, tconf);
            return status;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return null;

    }

    /**
     * This method creates flow condition
     *
     * @param IP1
     * @param IP2
     */
    public boolean createFlowCond(final String IP1, final String IP2,
            String condName) {
        try {
            ArrayList<FlowMatch> matchList = new ArrayList<FlowMatch>();
            IVTNManager mgr = getVTNManager("default");
            InetMatch match = new Inet4Match(InetAddress.getByName(IP1), null,
                    InetAddress.getByName(IP2), null, null, null);

            FlowMatch flowmatch = new FlowMatch(null, match, null);
            matchList.add(flowmatch);
            FlowCondition fcond = new FlowCondition(condName, matchList);
            mgr.setFlowCondition(condName, fcond);
            if (mgr.getFlowCondition(condName) != null) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @param tenantName
     * @param bridgeName
     * @param type
     * @param cond_name
     * @throws Exception
     */
    public void createFlowFilter(String tenantName, String bridgeName,
            String type, String cond_name) throws Exception {
        boolean in = false;
        boolean out = true;
        int index = flow_index++;
        FlowFilter filter = null;
        IVTNManager mgr = getVTNManager("default");
        VBridgePath path = new VBridgePath(tenantName, bridgeName);

        if (type.equalsIgnoreCase("PASS")) {
            PassFilter passFilter = new PassFilter();
            filter = new FlowFilter(index, cond_name, passFilter, null);
        } else {
            return;
        }
        FlowFilterId fid = new FlowFilterId(path, in);
        mgr.setFlowFilter(fid, index, filter);

    }

    /**
     * @param tenantName
     * @param bridgeName
     * @param vlanMap
     *            which on true creates vlan mapping for the vbridge created
     * @return
     * @throws VTNException
     */
    public Status createBridge(String tenantName, String bridgeName,
            boolean vlanMap, IVTNManager mgr) throws VTNException {
        VBridgePath bridgePath = new VBridgePath(tenantName, bridgeName);
        VBridgeConfig bconf = new VBridgeConfig(bridgeName + " "
                + "description");
        Status status = mgr.addBridge(bridgePath, bconf);
        if (status.isSuccess()) {
            VlanMapConfig vlconf = new VlanMapConfig(null, (short) 0);
            mgr.addVlanMap(bridgePath, vlconf);
        }
        return status;
    }

    /**
     * Validates the Endpoint of the Intent
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