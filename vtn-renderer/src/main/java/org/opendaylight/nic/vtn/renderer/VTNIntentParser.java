/**
 * Copyright (c) 2015, 2016 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.nic.vtn.renderer.VTNRendererUtility.ActionTypeEnum;
import org.opendaylight.vtn.manager.util.IpNetwork;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.RemoveFlowConditionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.RemoveFlowConditionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.VtnFlowConditions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.flow.cond.config.VtnFlowMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.flow.cond.config.VtnFlowMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.flow.conditions.VtnFlowCondition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnEtherMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnEtherMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnInetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnInetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.list.VtnFlowFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.list.VtnFlowFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.type.fields.vtn.flow.filter.type.VtnDropFilterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.type.fields.vtn.flow.filter.type.VtnDropFilterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.type.fields.vtn.flow.filter.type.VtnPassFilterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.type.fields.vtn.flow.filter.type.VtnPassFilterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.type.fields.vtn.flow.filter.type.vtn.drop.filter._case.VtnDropFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.vtn.flow.filter.type.fields.vtn.flow.filter.type.vtn.pass.filter._case.VtnPassFilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeUpdateMode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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

    /**
     * Adding DataBroker instance to store data.
     */
    private DataBroker dataProvider;

    /**
     * Instance created for VTNManagerService
     */
    private VTNManagerService vtnManangerService;

    /**
     * Instance created for VTNRendererUtility
     */
    private VTNRendererUtility vtnRendererUtility;

    private static final String FLOW_COND_ER_MESSAGE = "Unable to get flow condition name {}";

    public VTNIntentParser(DataBroker dataBroker, VTNManagerService vtn) {
        this.dataProvider = dataBroker;
        this.vtnRendererUtility = new VTNRendererUtility(dataProvider);
        this.vtnManangerService = vtn;
    }
    /**
     * Creates a default Virtual Tenant and default bridge with Vlan mapping and flow condition
     *
     * @return  {@code true} is returned if the default configuration is created in VTN Manager.
     */
    private boolean isVTNCreated(final boolean isIp, final boolean isMac) {
        boolean status = createTenant(TENANT_NAME);
        if (!status) {
            LOG.error("Tenant creation failed");
            return false;
        }
        status = setupDefaultBridge(TENANT_NAME, BRIDGE_NAME);
        if (!status) {
            LOG.error("Vbridge creation failed");
            return false;
        }
        /**
         * Creates a default flow condition
         */
        status = createFlowCond("0.0", "0.0", MATCH_ANY, "M", isIp, isMac);
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
     * @return CompletedSuccess is returned if VTN elements are successfully created.
     */
    public Status rendering(final String adressSrc, final String adressDst,
            String action, String encodeUUID, Intent intent) {
        try {
            boolean isValidIp = vtnRendererUtility.validateSrcDstIP(adressSrc,adressDst);
            boolean isValidMac = false;
            if (!isValidIp) {
                isValidMac = vtnRendererUtility.validateSrcDstMac(adressSrc, adressDst);
            }
            if (isValidIp || isValidMac) {
                if (!isVTNCreated(isValidIp, isValidMac)) {
                    LOG.error("Default VTN configuration creation failed");
                    return Status.CompletedError;
                }
                String condNameSrcDst = constructCondName(adressSrc, adressDst, encodeUUID, true);
                String condNameDstSrc = constructCondName(adressDst, adressSrc, encodeUUID, false);
                String whichAction = ActionTypeEnum.fromActionType(action).getLabel();
                createFlowCond(adressSrc, adressDst, condNameSrcDst, FORWARD_FLOWCOND, isValidIp, isValidMac);
                createFlowCond(adressDst, adressSrc, condNameDstSrc, REVERSE_FLOWCOND, isValidIp, isValidMac);

                createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondFrdIndex);
                createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondRevIndex);

                createFlowFilter(TENANT_NAME, BRIDGE_NAME, "DROP", MATCH_ANY);
                LOG.trace("VTN configuration is successfully updated for user Intents.");
                return Status.CompletedSuccess;
            } else {
                LOG.warn("Invalid address is specified in Intent configuration: {}",
                         intent.getId());
                return Status.CompletedError;
            }
        } catch (Exception e) {
            LOG.error("Unable to create VTN Objects {}", e);
            return Status.CompletedError;
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
     * @return CompletedSuccess is returned if VTN elements are successfully updated.
     */
    public Status updateRendering(final String adressSrc, final String adressDst,
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
            boolean isValidIp = vtnRendererUtility.validateSrcDstIP(adressSrc,adressDst);
            boolean isValidMac = false;
            if (!isValidIp) {
                isValidMac = vtnRendererUtility.validateSrcDstMac(adressSrc, adressDst);
            }
            if (isValidIp || isValidMac) {
                String condNameSrcDst = constructCondName(adressSrc, adressDst, encodeUUID, true);
                String condNameDstSrc = constructCondName(adressDst, adressSrc, encodeUUID, false);
                String whichAction = ActionTypeEnum.fromActionType(action).getLabel();
                List<VtnFlowMatch> flowMatch = listOfFlowMatch(encodeUUID);
                for (VtnFlowMatch fm : flowMatch) {
                    if (isValidIp) {
                        VtnInetMatch inet = fm.getVtnInetMatch();
                        if (inet != null) {
                            IpPrefix src = inet.getSourceNetwork();
                            inSrcIP = src.toString();
                            IpPrefix dst = inet.getDestinationNetwork();
                            inDstIP = dst.toString();
                            outSrcIP = adressSrc.replace(".", "");
                            outDscIP = adressDst.replace(".", "");
                        }
                    } else if (isValidMac) {
                        VtnEtherMatch eth = fm.getVtnEtherMatch();
                        MacAddress esrc = eth.getSourceAddress();
                        if (esrc != null) {
                            inSrcMAC = esrc.toString();
                            MacAddress edst = eth.getDestinationAddress();
                            inDscMAC = edst.toString();
                            outSrcMAC = adressSrc.replace(":", "");
                            outDscMAC = adressDst.replace(":", "");
                        }
                    }
                    if (validateSrcDstInputs(inSrcIP,outSrcIP,inDstIP,outDscIP)
                        || validateSrcDstInputs(inSrcMAC,outSrcMAC,inDscMAC,outDscMAC) ) {
                        delFlowCondFilter(encodeUUID); // Deleting the flow condition
                        if (isVTNCreated(isValidIp,isValidMac)) {
                            createFlowCond(adressSrc, adressDst, condNameSrcDst,
                                 FORWARD_FLOWCOND, isValidIp, isValidMac);
                            createFlowCond(adressDst, adressSrc, condNameDstSrc,
                                 REVERSE_FLOWCOND, isValidIp, isValidMac);
                        }
                    } else {
                        delFlowFilterIndex(encodeUUID); // Deleting the flow filter
                    }
                    createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondFrdIndex);
                    createFlowFilter(TENANT_NAME, BRIDGE_NAME, whichAction, flowCondRevIndex);
                }
                return Status.CompletedSuccess;
            } else {
                LOG.warn("Invalid address is specified in Intent configuration: {}",
                         intentID);
                return Status.CompletedError;
            }
        } catch (Exception e) {
            LOG.error("Unable to update VTN Objects {}", e);
            return Status.CompletedError;
        }
    }

    /**
     * Delete the flow condition and flow filter associated with a particular intent.
     *
     * @param intentID  ID of the Deleting intent.
     */
    public void delFlowCondFilter(String intentID) {
        try {
            for (VtnFlowCondition flowCondition : readFlowConditions()) {
                String lclfc = flowCondition.getName().getValue();
                String encodeUUID = lclfc.split("_")[0];
                String flowfilterindex = lclfc.split("_")[2];
                Integer delflowfilterindex = Integer.valueOf(flowfilterindex);
                if (encodeUUID.equals(intentID)) {
                    deleteFlowCond(lclfc);
                    deleteFlowFilter(delflowfilterindex);
                }
            }
            LOG.trace("Removed VTN configuration associated with the deleted Intent: {}", intentID);
            List<VtnFlowCondition> flowConditionList = readFlowConditions();
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
     * To get and delete the flow filter index associated with a particular intent.
     *
     * @param intentID  ID of the Deleting intent.
     */
    private void delFlowFilterIndex(String intentID) {
        try {
            for (VtnFlowCondition flowCondition : readFlowConditions()) {
                String lclfc = flowCondition.getName().getValue();
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
        if (isSrcCond) {
            condName = encodeUUID + "_" + FORWARD_FLOWCOND;
        } else {
            condName = encodeUUID + "_" + REVERSE_FLOWCOND;
        }
        return condName;
    }

    /**
     * Create a new tenant in VTN Manager.
     *
     * @param tenantName  The name of the tenant to be created.
     * @return  {@code true} is returned if tenant is created in VTN Manager.
     */
    private boolean createTenant(String tenantName) {
        return vtnManangerService.updateTenant(tenantName,
                                      VnodeUpdateMode.UPDATE);
    }

    /**
     * Delete the specified tenant in VTN Manager.
     *
     * @param tenantName  The name of the tenant to be deleted.
     * @return  {@code true} is returned if the tenant is deleted.
     */
    private boolean deleteTenant(String tenantName) {
        return vtnManangerService.removeTenant(tenantName);
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
            final String addressDst, String condName, String flowDirection, boolean isValidIp, boolean isValidMac) {
        int index = 0;
        if (isFlowCondExist(condName)) {
            return true ;
        }
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
                List<VtnFlowMatch> matchList = new ArrayList<VtnFlowMatch>();
                VlanId vlanId = new VlanId(0);
                if (addressSrc.equalsIgnoreCase("0.0")) {
                    MacAddress macAddress = null;
                    VtnEtherMatch ethernetMatch = new VtnEtherMatchBuilder()
                        .setDestinationAddress(macAddress)
                        .setSourceAddress(macAddress)
                        .setVlanId(vlanId)
                        .setVlanPcp(null)
                        .setEtherType(new EtherType(Long.valueOf(ETHER_TYPE)))
                        .build();
                    VtnFlowMatch flowMatch = new VtnFlowMatchBuilder()
                        .setIndex(flowCondIndex++)
                        .setVtnEtherMatch(ethernetMatch)
                        .build();
                    matchList.add(flowMatch);
                } else if (isValidIp ) {
                    matchList = createFlowMatchForIP(addressSrc, addressDst);
                } else if (isValidMac) {
                    matchList = createFlowMatchForMAC(addressSrc, addressDst);
                } else {
                    return false;
                }
                VnodeName vnodeName = new VnodeName(flowCondName);
                SetFlowConditionInput fcond = new SetFlowConditionInputBuilder()
                    .setName(vnodeName)
                    .setVtnFlowMatch(matchList)
                    .build();
                return vtnManangerService.setFlowCond(fcond);
            }
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
        RemoveFlowConditionInput input = new RemoveFlowConditionInputBuilder()
            .setName(condName)
            .build();
        return vtnManangerService.unsetFlowCond(input);
    }

    /**
     * This method will return if the bridge already exists, if tenant with same
     * name already exists false is returned else return true.
     *
     * @param condName  Flow condition name, that needs to be verified if already exists.
     * @return  {@code false} is returned if flow condition is not present in VTN Manager.
     */
    private boolean isFlowCondExist(String condName) {
        for (VtnFlowCondition condition : readFlowConditions()) {
            if (condition.getName().getValue().equals(condName)) {
                return true;
            }
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
     */
    private void createFlowFilter(String tenantName, String bridgeName,
            String type, String condName) {
        String flowFilterCondName;
        int index = 0;
        if (condName.equalsIgnoreCase(MATCH_ANY)) {
            index = LOW_PRIORITY;
            flowFilterCondName = condName + "_" + index;
        } else {
            flowFilterCondName = condName;
            String flowfilterindex = flowFilterCondName.split("_")[2];
            index = Integer.valueOf(flowfilterindex);
        }
        if ("PASS".equalsIgnoreCase(type)) {
            VtnPassFilterCase pass = new VtnPassFilterCaseBuilder()
                .setVtnPassFilter(new VtnPassFilterBuilder().build())
                .build();
            List<VtnFlowFilter> vtnFlowFilterList = new ArrayList<VtnFlowFilter>();
            VnodeName vnodeName = new VnodeName(flowFilterCondName);
            VtnFlowFilter filter = new VtnFlowFilterBuilder()
                .setIndex(index)
                .setCondition(vnodeName)
                .setVtnFlowFilterType(pass)
                .build();
            vtnFlowFilterList.add(filter);
            SetFlowFilterInput input = new SetFlowFilterInputBuilder()
                .setTenantName(tenantName)
                .setBridgeName(bridgeName)
                .setVtnFlowFilter(vtnFlowFilterList)
                .build();
            vtnManangerService.setFlowFilter(input);
        } else if ("DROP".equalsIgnoreCase(type)) {
            VtnDropFilterCase drop = new VtnDropFilterCaseBuilder()
                .setVtnDropFilter(new VtnDropFilterBuilder().build())
                .build();
            List<VtnFlowFilter> vtnFlowFilterList = new ArrayList<VtnFlowFilter>();
            VnodeName vnodeName = new VnodeName(flowFilterCondName);
            VtnFlowFilter filter = new VtnFlowFilterBuilder()
                .setIndex(index)
                .setCondition(vnodeName)
                .setVtnFlowFilterType(drop)
                .build();
            vtnFlowFilterList.add(filter);
            SetFlowFilterInput input = new SetFlowFilterInputBuilder()
                .setTenantName(tenantName)
                .setBridgeName(bridgeName)
                .setVtnFlowFilter(vtnFlowFilterList)
                .build();
            vtnManangerService.setFlowFilter(input);

        } else {
            return;
        }
    }

    /**
     * Delete an Existing flow filter with received ID
     *
     * @param index  Flow Filter index number to be deleted.
     * @return  {@code true} flow filter is deleted in VTN Manager.
     */
    private boolean deleteFlowFilter(int index) {
        final List<Integer> indexList = new ArrayList<Integer>();
        indexList.add(index);
        RemoveFlowFilterInput input = new RemoveFlowFilterInputBuilder()
            .setTenantName(TENANT_NAME)
            .setBridgeName(BRIDGE_NAME)
            .setIndices(indexList)
            .build();
        return vtnManangerService.unSetFlowFilter(input);
    }

    /**
     * Create a virtual bridge which on true creates vlanmapping for
     * the vbridge created.
     *
     * @param tenantName  The VTN tenant name under which the VTN Bridge should be created.
     * @param bridgeName  Valid VTN bridge name to be created.
     * @return {@code true} only bridge is created in VTN Manager.
     */
    private boolean setupDefaultBridge(String tenantName, String bridgeName) {
        VlanId vlanId = new VlanId(0);
        boolean status = vtnManangerService.updateBridge(tenantName, bridgeName, bridgeName + " description",
                            VnodeUpdateMode.UPDATE);
        if (status) {
            AddVlanMapInput input = new AddVlanMapInputBuilder()
                .setBridgeName(bridgeName)
                .setTenantName(tenantName)
                .setVlanId(vlanId)
                .build();
            status = vtnManangerService.setVlanMap(input);
        }
        return status;
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
            for (VtnFlowCondition fc : readFlowConditions()) {
                String lclfc = fc.getName().getValue();
                String intentID = lclfc.split("_")[0];
                if (intentID.equals(search)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error(FLOW_COND_ER_MESSAGE, e);
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
    private List<VtnFlowMatch> listOfFlowMatch(final String search) {
        List<VtnFlowMatch> flow = null;
        try {
            for (VtnFlowCondition fc : readFlowConditions()) {
                String lclfc = fc.getName().getValue();
                String intentID = lclfc.split("_")[0];
                if (intentID.equals(search)) {
                    flow = fc.getVtnFlowMatch();
                    return flow;
                }
            }
        } catch (Exception e) {
            LOG.error(FLOW_COND_ER_MESSAGE, e);
        }
        return flow;
    }

    /**
     * Read all the flow conditions from the MD-SAL datastore.
     *
     * @return  A list of {@link VtnFlowCondition} instances.
     */
    private List<VtnFlowCondition> readFlowConditions() {
        List<VtnFlowCondition> vlist = new ArrayList<>();
        try {
            InstanceIdentifier<VtnFlowConditions> path =
                InstanceIdentifier.create(VtnFlowConditions.class);
            LogicalDatastoreType oper = LogicalDatastoreType.OPERATIONAL;
            MdsalUtils mdsal = new MdsalUtils(dataProvider);
            VtnFlowConditions opt = mdsal.read(oper, path);
            if (opt != null) {
                vlist = opt.getVtnFlowCondition();
            }
            if (vlist == null || vlist.isEmpty()) {
                return Collections.<VtnFlowCondition>emptyList();
            }
        } catch (Exception e) {
            LOG.error(FLOW_COND_ER_MESSAGE, e);
        }
        return vlist;
    }

    /**
     * Validate new Source and exist Source IP Address should not same
     * for before updating the intent.
     *
     * @param inSrc  Exist Source Address.
     * @param outSrc  New Source Address.
     * @param inDst  Exist Destination Address.
     * @param outDsc  New Destination Address.
     * @return {@code true} if new Source and exist Source IP address are not same.
     */
    private boolean validateSrcDstInputs(String inSrc, String outSrc,String inDst,String outDsc ) {
        return (vtnRendererUtility.validateInSrcOutSrc(inSrc,outSrc)
            || vtnRendererUtility.validateInSrcOutSrc(inDst,outDsc));
    }

    /**
     * Create flow match specific to IP Address
     *
     * @param  addressSrc  Valid source IP.
     * @param  addressDst  Valid destination IP.
     * @return  list of flow match based on IP Address.
     */
    private List<VtnFlowMatch> createFlowMatchForIP(final String addressSrc, final String addressDst) throws UnknownHostException {
        List<VtnFlowMatch> matchList = new ArrayList<VtnFlowMatch>();
            VlanId vlanId = new VlanId(0);
            IpNetwork ipaddrSrc = IpNetwork.create(InetAddress.getByName(addressSrc));
            IpNetwork ipaddrDst = IpNetwork.create(InetAddress.getByName(addressDst));
            MacAddress macAddress = null;
            VtnInetMatch match = new VtnInetMatchBuilder()
                .setDestinationNetwork(ipaddrDst.getIpPrefix())
                .setSourceNetwork(ipaddrSrc.getIpPrefix())
                .setProtocol((short)1)
                .setDscp(null)
                .build();
            VtnEtherMatch ethernetMatch = new VtnEtherMatchBuilder()
                .setDestinationAddress(macAddress)
                .setSourceAddress(macAddress)
                .setVlanId(vlanId)
                .setVlanPcp(null)
                .setEtherType(new EtherType(Long.valueOf(ETHER_TYPE)))
                .build();
            VtnFlowMatch flowMatch = new VtnFlowMatchBuilder()
                .setIndex(flowCondIndex++)
                .setVtnEtherMatch(ethernetMatch)
                .setVtnInetMatch(match)
                .build();
            matchList.add(flowMatch);
            return matchList;
    }

    /**
     * Create flow match specific to MAC Address.
     *
     * @param  addressSrc  Valid MAC Address.
     * @param  addressDst  Valid MAC Address.
     * @return  list of flow match based on MAC Address.
     */
    private List<VtnFlowMatch> createFlowMatchForMAC(final String addressSrc, final String addressDst) {
        List<VtnFlowMatch> matchList = new ArrayList<VtnFlowMatch>();
            VlanId vlanId = new VlanId(0);
            MacAddress macAddressSrc = new MacAddress(addressSrc);
            MacAddress macAddressDst = new MacAddress(addressDst);
            VtnEtherMatch ethernetMatch = new VtnEtherMatchBuilder()
                .setDestinationAddress(macAddressDst)
                .setSourceAddress(macAddressSrc)
                .setVlanId(vlanId)
                .setVlanPcp(null)
                .setEtherType(null)
                .build();
            VtnFlowMatch flowMatch = new VtnFlowMatchBuilder()
                .setIndex(flowCondIndex++)
                .setVtnEtherMatch(ethernetMatch)
                .build();
            matchList.add(flowMatch);
            return matchList;
    }
}
