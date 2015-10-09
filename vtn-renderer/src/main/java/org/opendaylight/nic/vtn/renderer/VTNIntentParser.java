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
import java.util.List;

import javax.naming.ServiceUnavailableException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.packet.address.EthernetAddress;
import org.opendaylight.controller.sal.utils.ServiceHelper;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;
import org.opendaylight.vtn.manager.IVTNManager;
import org.opendaylight.vtn.manager.VBridge;
import org.opendaylight.vtn.manager.VBridgeConfig;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.vtn.manager.VTNException;
import org.opendaylight.vtn.manager.VlanMapConfig;
import org.opendaylight.vtn.manager.flow.cond.EthernetMatch;
import org.opendaylight.vtn.manager.flow.cond.FlowCondition;
import org.opendaylight.vtn.manager.flow.cond.FlowMatch;
import org.opendaylight.vtn.manager.flow.cond.Inet4Match;
import org.opendaylight.vtn.manager.flow.cond.InetMatch;
import org.opendaylight.vtn.manager.flow.filter.DropFilter;
import org.opendaylight.vtn.manager.flow.filter.FlowFilter;
import org.opendaylight.vtn.manager.flow.filter.FlowFilterId;
import org.opendaylight.vtn.manager.flow.filter.PassFilter;
import org.opendaylight.vtn.manager.util.EtherAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VTNIntentParser class creates a VTN objects based on the Intents received.
 */
public class VTNIntentParser {

    /**
     * Setting the name for virtual tenant created during first intent request.
     */
    private static final String TENANT_NAME = "vtnRenderer";

    /**
     * Setting the virtual Bridge name created during first intent request.
     */
    private static final String BRIDGE_NAME = "default";

    /**
     * The default Container name used in VTN Manager.
     */
    private static final String CONTAINER_NAME = "default";

    private static final String MATCH_ANY = "match_any";

    private static final String DELETE_MATCH_ANY = "match_any_65535";

    private static final String FORWARD_FLOWCOND = "F";

    private static final String REVERSE_FLOWCOND = "R";

    /**
     * Setting the lowest index value for match_any flow condition used by flow filter.
     */
    private static final int LOW_PRIORITY = 65535;

    /**
     * Setting the EtherType value to match only IPV4 packets, in flow condition.
     */
    private static final int ETHER_TYPE = 0x800;

    private static final Logger LOG = LoggerFactory.getLogger(VTNIntentParser.class);

    /**
     * Setting the index of flow filter.
     */
    private int flowFilterIndex = 1;

    /**
     * Setting the flow condition forward index.
     */
    private String flowCondFrdIndex;

    /**
     * Setting the flow condition revise index.
     */
    private String flowCondRevIndex;

    /**
     * Setting the flow condition name.
     */
    private String flowCondName;

    /**
     * Setting the index of flow condition.
     */
    private int flowCondIndex = 1;

    private DataBroker dataProvider;

    private IVTNManager mgr;

    private VTNRendererUtility vtnRendererUtility;

    public VTNIntentParser(DataBroker dataBroker) {
        this.dataProvider = dataBroker;
        this.vtnRendererUtility = new VTNRendererUtility(dataProvider);
    }
    /**
     * Creates a default Virtual Tenant and default bridge with Vlan mapping and flow condition
     *
     * @return  {@code true} is returned if the default configuration is created in VTN Manager.
     */
    private boolean isCreateDefault() {
        boolean status = createTenant(TENANT_NAME);
        if (!status) {
            LOG.error("Tenant creation failed");
            return false;
        }
        status = createBridge(TENANT_NAME, BRIDGE_NAME);
        if (!status) {
            LOG.error("Bridge creation failed");
            return false;
        }
        LOG.trace("Bridge creation status {}", status);
        /**
         * Creates a default flow condition
         */
        status = createFlowCond("0.0", "0.0", MATCH_ANY, "M");
        if (!status) {
            LOG.error("Flow condition creation failed");
            return false;
        }
        return true;
    }

    /**
     * Delete the default configuration in VTN Manager.
     *
     * @return  {@code true} is returned if the default configuration is deleted in VTN Manager.
     */
    private boolean isDeleteDefault() {
        if (deleteTenant(TENANT_NAME)) {
            return deleteFlowCond(DELETE_MATCH_ANY);
        }
        return false;
    }

    /**
     * Creates VTN elements based on the intent action
     *
     * @param adressSrc  Valid source IP or MAC Address.
     * @param adressDst  Valid destination IP or MAC Address.
     * @param action  Valid Actions.
     * @param encodeUUID the encodeUUID value of intent ID.
     * @param intent the intent instance.
     */
    public void rendering(final String adressSrc, final String adressDst,
            String action, String encodeUUID, Intent intent) {
        try {
            if ((vtnRendererUtility.validateIP(adressSrc))
                    && (vtnRendererUtility.validateIP(adressDst))
                    && (vtnRendererUtility.validateSubnet(adressSrc, adressDst))
                    || ((vtnRendererUtility.validateMacAddress(adressSrc)) && (vtnRendererUtility
                            .validateMacAddress(adressDst)))) {
                boolean status = isCreateDefault();
                if (!status) {
                    LOG.trace("Default VTN configuration creation failed");
                    return;
                }
                String condNameSrcDst = constructCondName(adressSrc, adressDst, encodeUUID, true);
                String condNameDstSrc = constructCondName(adressDst, adressSrc, encodeUUID, false);
                String whichAction = "allow".equalsIgnoreCase(action) ? "PASS" : "block"
                          .equalsIgnoreCase(action) ? "DROP" : "NA";
                if (whichAction.equalsIgnoreCase("NA")) {
                    LOG.error("Unsupported Action {}", whichAction);
                    return;
                }
                createFlowCond(adressSrc, adressDst, condNameSrcDst, FORWARD_FLOWCOND);
                createFlowCond(adressDst, adressSrc, condNameDstSrc, REVERSE_FLOWCOND);
                createFlowFilter(TENANT_NAME, BRIDGE_NAME, "DROP", MATCH_ANY);
                createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondFrdIndex);
                createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondRevIndex);

                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status isStatus = org.opendaylight
                        .yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status.CompletedSuccess;
                vtnRendererUtility.addIntent(intent, isStatus);
                LOG.info("VTN configuration is successfully updated for user Intents.");
            } else {
                LOG.error("Invalid Address");
            }
        } catch (Exception e) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status isStatus = org.opendaylight
                    .yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status.CompletedError;
            vtnRendererUtility.addIntent(intent, isStatus);
            LOG.error("Unable to create VTN Objects {}", e);
        }
    }

    /**
     * Updates VTN elements based on the intent action
     *
     * @param adressSrc  Valid source IP or MAC Address.
     * @param adressDst  Valid Destination IP or MAC Address.
     * @param action  Valid Action to update the intent.
     * @param intentID  ID of the updating intent.
     * @param encodeUUID ID of the encode UUID.
     * @param intent The intent instance.
     */
    public void updateRendering(final String adressSrc, final String adressDst,
            String action, String intentID, String encodeUUID, Intent intent) {
        String inSrcIP = null;
        String outSrcIP  = null;
        String inDstIP = null;
        String outDscIP  = null;
        String inSrcMAC = null;
        String outSrcMAC  = null;
        String inDscMAC  = null;
        String outDscMAC = null;
        try {
            if ((vtnRendererUtility.validateIP(adressSrc))
                    && (vtnRendererUtility.validateIP(adressDst))
                    && (vtnRendererUtility.validateSubnet(adressSrc, adressDst))
                    || ((vtnRendererUtility.validateMacAddress(adressSrc)) && (vtnRendererUtility
                            .validateMacAddress(adressDst)))) {
                boolean isvalidIP = vtnRendererUtility.validateIP(adressSrc);
                boolean isvalidMAC = vtnRendererUtility.validateMacAddress(adressSrc);
                boolean isIPMAC = false;
                boolean isMACIP = false;
                String condNameSrcDst = constructCondName(adressSrc, adressDst, encodeUUID, true);
                String condNameDstSrc = constructCondName(adressDst, adressSrc, encodeUUID, false);
                String whichAction = "allow".equalsIgnoreCase(action) ? "PASS" : "block"
                        .equalsIgnoreCase(action) ? "DROP" : "NA";
                if (whichAction.equalsIgnoreCase("NA")) {
                    LOG.error("Unsupported Action {}", whichAction);
                    return;
                }
                List<FlowMatch> flowMatch = listOfFlowMatch(encodeUUID);
                for (FlowMatch fm : flowMatch) {
                    if (isvalidIP) {
                        InetMatch inet = fm.getInetMatch();
                        if (inet == null) {
                            isMACIP = true;
                        } else {
                            InetAddress src = inet.getSourceAddress();
                            inSrcIP = src.toString();
                            InetAddress dst = inet.getDestinationAddress();
                            inDstIP = dst.toString();
                            outSrcIP = adressSrc.replace(".", "");
                            outDscIP = adressDst.replace(".", "");
                        }
                    } else if (isvalidMAC) {
                        EthernetMatch eth = fm.getEthernetMatch();
                        EthernetAddress esrc = eth.getSourceAddress();
                        if (esrc == null) {
                            isIPMAC = true;
                        } else {
                            inSrcMAC = esrc.toString();
                            EthernetAddress edst = eth.getDestinationAddress();
                            inDscMAC = edst.toString();
                            outSrcMAC = adressSrc.replace(":", "");
                            outDscMAC = adressDst.replace(":", "");
                        }
                    }
                    if (((!(inSrcIP == null) && (!inSrcIP.equals(outSrcIP))) || (!(inDstIP == null) && (!inDstIP
                               .equals(outDscIP))) || !(isMACIP))) {
                        delete(encodeUUID);
                        createFlowCond(adressSrc, adressDst, condNameSrcDst, FORWARD_FLOWCOND);
                        createFlowCond(adressDst, adressSrc, condNameDstSrc, REVERSE_FLOWCOND);
                        createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction,
                                 flowCondFrdIndex);
                        createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction,
                                 flowCondRevIndex);
                    } else if (((!(inSrcMAC == null) && (!inSrcMAC.equals(outSrcMAC))) || (!(inDscMAC == null) 
                        && (!inDscMAC.equals(outDscMAC))) || !(isIPMAC))) {
                        delete(encodeUUID);
                        createFlowCond(adressSrc, adressDst, condNameSrcDst, FORWARD_FLOWCOND);
                        createFlowCond(adressDst, adressSrc, condNameDstSrc, REVERSE_FLOWCOND);
                        createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondFrdIndex);
                        createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondRevIndex);
                    } else {
                        deleteAction(encodeUUID);
                        createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondFrdIndex);
                        createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondRevIndex);
                    }
                }
            } else {
                LOG.warn("Invalid address is specified in Intent configuration: {}",
                         intentID);
            }
        } catch (ServiceUnavailableException | VTNException e) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status isStatus = org.opendaylight
                    .yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status.CompletedError;
            vtnRendererUtility.addIntent(intent, isStatus);
            LOG.error("Unable to update VTN Objects {}", e);
        }
    }

    /**
     * Delete a particular intent
     *
     * @param intentID  ID of the Deleting intent.
     */
    public void delete(String intentID) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (FlowCondition flowCondition : mgr.getFlowConditions()) {
                String lclfc = flowCondition.getName();
                String encodeUUID = lclfc.split("_")[0];
                String flowfilterindex = lclfc.split("_")[2];
                Integer delflowfilterindex = Integer.valueOf(flowfilterindex);
                if (encodeUUID.equals(intentID)) {
                    deleteFlowCond(lclfc);
                    deleteFlowFilter(delflowfilterindex);
                }
            }
            LOG.info("Removed VTN configuration associated with the deleted Intent: {}", intentID);
            List<FlowCondition> flowConditionList =  mgr.getFlowConditions();
            if (flowConditionList.size() == 1) {
                boolean result = isDeleteDefault();
                if (result) {
                    LOG.info("Removed default VTN configuration.");
                }
            }
        } catch (Exception e) {
            LOG.error("{} : Unable to Delete, {}", intentID, e);
        }
    }

    /**
     * Delete a particular flow filter
     *
     * @param intentID  ID of the Deleting intent.
     */
    private void deleteAction(String intentID) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (FlowCondition flowCondition : mgr.getFlowConditions()) {
                String lclfc = flowCondition.getName();
                String encodeUUID = lclfc.split("_")[0];
                String flowfilterindex = lclfc.split("_")[2];
                Integer delflowfilterindex = Integer.valueOf(flowfilterindex);
                if (encodeUUID.equals(intentID)) {
                    deleteFlowFilter(delflowfilterindex);
                }
            }
        } catch (Exception e) {
            LOG.error("{} : Unable to Delete the flow filter, {}", intentID, e);
        }
    }
    /**
     * This method creates a flow condition name
     *
     * @param adressSrc  Valid source IP or MAC Address.
     * @param addressDst  Valid Destination IP or MAC Address.
     * @param encodeUUID encodeUUID value of intent ID.
     * @param isSrcCond choosing the flow condition name.
     * @return flow condition name on valid IP Address or MAC Address
     */
    private String constructCondName(final String adressSrc,
            final String addressDst, final String encodeUUID, boolean isSrcCond) {
        String condName = null;
        if (vtnRendererUtility.validateIP(adressSrc) && (vtnRendererUtility.validateIP(addressDst))) {
            if (isSrcCond) {
                condName = encodeUUID + "_" + FORWARD_FLOWCOND;
            } else {
                condName = encodeUUID + "_" + REVERSE_FLOWCOND;
            }
        } else if (vtnRendererUtility.validateMacAddress(adressSrc)
                && (vtnRendererUtility.validateMacAddress(addressDst))) {
            if (isSrcCond) {
                condName = encodeUUID + "_" + FORWARD_FLOWCOND;
            } else {
                condName = encodeUUID + "_" + REVERSE_FLOWCOND;
            }
        }
        return condName;
    }

    /**
     * To obtain VTN Manager Instance
     *
     * @param containerName  Name of the container.
     * @return  VTN Manager Instance
     * @throws ServiceUnavailableException  when VTN manager service is not available.
     */
    protected IVTNManager getVTNManager(String containerName) throws ServiceUnavailableException  {
        IVTNManager vtnManager = (IVTNManager) ServiceHelper.getInstance(
                IVTNManager.class, containerName, this);

        if (vtnManager == null) {
            throw new ServiceUnavailableException("VTN Manager Service unavailable");
        }

        return vtnManager;
    }

    /**
     * Create a new tenant in VTN Manager.
     *
     * @param tenantName  The name of the tenant to be created.
     * @return  {@code true} is returned if tenant is created in VTN Manager.
     */
    private boolean createTenant(String tenantName) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
        } catch (ServiceUnavailableException ex) {
            LOG.error("Failed to get the VTN Manager instance: {}", ex);
            return false;
        }
        Status status = mgr.addTenant(new VTenantPath(tenantName),
                                      new VTenantConfig(null));
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
    private boolean deleteTenant(String tenantName) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
        } catch (ServiceUnavailableException e) {
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
     * @param addressSrc  Valid source IP or MAC Address.
     * @param addressDst  Valid destination IP or MAC Address.
     * @param condName  Flow condition name that needs to be created.
     * @param flowDirection defining the flow direction as Forward and Reverse.
     * @return  {@code true} is returned if only flow condition is created in VTN Manager.
     */
    private boolean createFlowCond(final String addressSrc,
            final String addressDst, String condName, String flowDirection) {
        int index = 0;
        if (condName.equalsIgnoreCase(MATCH_ANY)) {
            index = LOW_PRIORITY;
        } else {
            index = flowFilterIndex++;
        }
        if (flowDirection.equalsIgnoreCase(FORWARD_FLOWCOND)) {
            flowCondFrdIndex = condName + "_" + index;
            flowCondName = flowCondFrdIndex;
        } else if (flowDirection.equalsIgnoreCase(REVERSE_FLOWCOND)) {
            flowCondRevIndex = condName + "_" + index;
            flowCondName = flowCondRevIndex;
        } else {
            flowCondName = condName + "_" + index;
        }
        try {
            if (!isFlowCondExist(flowCondName)) {
                List<FlowMatch> matchList = new ArrayList<FlowMatch>();
                mgr = getVTNManager(CONTAINER_NAME);
                FlowCondition fcond;
                if (addressSrc.equalsIgnoreCase("0.0")) {
                    EtherAddress addr = null;
                    EthernetMatch ethernetMatch = new EthernetMatch(addr, addr,
                            ETHER_TYPE, (short) 0, null);
                    FlowMatch flowmatch = new FlowMatch(flowCondIndex++,
                            ethernetMatch, null, null);
                    matchList.add(flowmatch);
                } else if ((vtnRendererUtility.validateIP(addressSrc))
                        && (vtnRendererUtility.validateIP(addressDst))
                        || ((vtnRendererUtility.validateMacAddress(addressSrc)) && (vtnRendererUtility
                                .validateMacAddress(addressDst)))) {
                    if (vtnRendererUtility.validateIP(addressSrc)) {
                        InetMatch match = new Inet4Match(
                                InetAddress.getByName(addressSrc), null,
                                InetAddress.getByName(addressDst), null,
                                (short) 1, null);
                        EtherAddress addr = null;
                        EthernetMatch ethernetMatch = new EthernetMatch(addr,
                                addr, ETHER_TYPE, (short) 0, null);
                        FlowMatch flowmatch = new FlowMatch(flowCondIndex++,
                                ethernetMatch, match, null);
                        matchList.add(flowmatch);
                    } else {
                        MacAddress macAddress = new MacAddress(addressSrc);
                        EtherAddress src = new EtherAddress(macAddress);
                        macAddress = new MacAddress(addressDst);
                        EtherAddress dst = new EtherAddress(macAddress);
                        EthernetMatch ethernetMatch = new EthernetMatch(src,
                                dst, null, (short) 0, null);
                        FlowMatch flowmatch = new FlowMatch(flowCondIndex++,
                                ethernetMatch, null, null);
                        matchList.add(flowmatch);
                    }
                } else {
                    return false;
                }
                fcond = new FlowCondition(flowCondName, matchList);
                UpdateType result = mgr.setFlowCondition(flowCondName, fcond);
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
        } catch (ServiceUnavailableException | VTNException ex) {
            LOG.error("Unable to create Flow Condition {}", ex);
            return false;
        } catch (UnknownHostException e) {
            LOG.error("Unable to create Flow Condition {}", e);
            return false;
        }
        return true;
    }

    /**
     * Delete the given flow condition
     *
     * @param condName  Flow condition name that needs to be deleted.
     * @return  {@code true} on successful deletion
     */
    private boolean deleteFlowCond(String condName) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            if (isFlowCondExist(condName)) {
                Status status = mgr.removeFlowCondition(condName);
                return status.isSuccess();
            }
        } catch (ServiceUnavailableException e) {
            LOG.error("Unable to delete Flow Condition {}", e);
            return false;
        }
        return true;
    }

    /**
     * This method will return if the bridge already exists, if tenant with same
     * name already exists false is returned else return true.
     *
     * @param condName  Flow condition name, that needs to be verified if already exists.
     * @return  {@code false} is returned if flow condition is not present in VTN Manager.
     */
    private boolean isFlowCondExist(String condName) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (FlowCondition condition : mgr.getFlowConditions()) {
                if (condition.getName().equals(condName)) {
                    return true;
                }
            }
        } catch (ServiceUnavailableException | VTNException e) {
            LOG.error("Unable to check Flow Condition {}", e);
        }
        return false;
    }

    /**
     * Creates flow filter for the given flow condition
     *
     * @param tenantName  The VTN tenant name to create the FlowFilter.
     * @param bridgeName  The VTN bridge name to create the FlowFilter.
     * @param type  The type of flow filter that needs to be created.
     * @param condName  The flow condition name to create the Flow Filter.
     * @throws Exception
     */
    private void createFlowFilter(String tenantName, String bridgeName,
            String type, String condName) throws ServiceUnavailableException, VTNException {
        boolean in = false;
        int index = 0;
        if (condName.equalsIgnoreCase(MATCH_ANY)) {
            index = LOW_PRIORITY;
            condName = condName + "_" + index;
        } else {
            String flowfilterindex = condName.split("_")[2];
            index = Integer.valueOf(flowfilterindex);
        }
        FlowFilter filter = null;
        mgr = getVTNManager(CONTAINER_NAME);
        VBridgePath path = new VBridgePath(tenantName, bridgeName);
        if (type.equalsIgnoreCase("PASS")) {
            PassFilter passFilter = new PassFilter();
            filter = new FlowFilter(index, condName, passFilter, null);
        } else if (type.equalsIgnoreCase("DROP")) {
            DropFilter dropFilter = new DropFilter();
            filter = new FlowFilter(index, condName, dropFilter, null);
        } else {
            return;
        }
        FlowFilterId fid = new FlowFilterId(path, in);
        mgr.setFlowFilter(fid, index, filter);
    }

    /**
     * Delete an Existing flow filter with received ID
     *
     * @param index  Flow Filter index number to be deleted.
     * @return  {@code true} flow filter is deleted in VTN Manager.
     */
    private boolean deleteFlowFilter(int index) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            VBridgePath path = new VBridgePath(TENANT_NAME, BRIDGE_NAME);
            FlowFilterId fid = new FlowFilterId(path, false);
            mgr.removeFlowFilter(fid, index);
        } catch (ServiceUnavailableException e) {
            LOG.error("Unable to delete Flow Filter {}", e);
            return false;
        }
        return true;
    }

    /**
     * Create a virtual bridge which on true creates vlanmapping for
     * the vbridge created.
     * @param tenantName  The VTN tenant name under which the VTN Bridge should be created.
     * @param bridgeName  Valid VTN bridge name to be created.
     * @return {@code true} only bridge is created in VTN Manager.
     */
    private boolean createBridge(String tenantName, String bridgeName) {
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
        } catch (ServiceUnavailableException | VTNException exception) {
            LOG.error("Failed to create Bridge {}", exception);
        }
        return false;
    }

    /**
     * This method will return true if the bridge already exists else return false.
     *
     * @param bridgeName  A valid Bridge name.
     * @param path  An reference for VBridgePath.
     * @return  {@code false} only bridge is not present in VTN Manager.
     */
    private boolean isBridgeExist(String bridgeName, VBridgePath path) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (VBridge bridge : mgr.getBridges(path)) {
                if (bridge.getName().equals(bridgeName)) {
                    return true;
                }
            }
        } catch (ServiceUnavailableException | VTNException e) {
            LOG.error("Unable to get bridge status {}", e);
        }
        return false;
    }

    /**
     * Iterates over all List until a specified condition name is found that has
     * the same name as specified in search
     *
     * @param  search Passing a parameter as encode UUID.
     * @return  {@code false} only if search string is not present in the list
     */
    public boolean containsIntentID(final String search) {
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (FlowCondition fc : mgr.getFlowConditions()) {
                String lclfc = fc.getName();
                String intentID = lclfc.split("_")[0];
                if (intentID.equals(search)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to get flow condition name {}", e);
        }
        return false;
    }
    
    /**
     * Iterates over all List until a specified condition name is found that has
     * the same name as specified in search
     *
     * @param  search Passing a parameter as encode UUID.
     * @return  list of flow match based on flow condition name.
     */
    private List<FlowMatch> listOfFlowMatch(final String search) {
        List<FlowMatch> flow = null;
        try {
            mgr = getVTNManager(CONTAINER_NAME);
            for (FlowCondition fc : mgr.getFlowConditions()) {
                String lclfc = fc.getName();
                String intentID = lclfc.split("_")[0];
                if (intentID.equals(search)) {
                    flow = fc.getMatches();
                    return flow;
                }
            }
        } catch (Exception e) {
            LOG.error("Unable to get flow condition name {}", e);
        }
        return flow;
    }
}