/*
 * Copyright (c) 2015 NEC Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_GONE;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_ACCEPTABLE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.utils.MdsalUtils;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.ClearFlowConditionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.RemoveFlowConditionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.VtnFlowConditionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.VtnFlowFilterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.RemovePortMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.RemovePortMapOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.SetPortMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.SetPortMapOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.port.rev150907.VtnPortMapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.RemoveVlanMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.RemoveVlanMapOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.RemoveVlanMapOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.VtnVlanMapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.RemoveVtnInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.RemoveVtnInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.UpdateVtnInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.UpdateVtnInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.UpdateVtnOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.Vtn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.VtnKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.Vtns;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.VtnService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeUpdateMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnErrorTag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnUpdateOperationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnUpdateType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.RemoveVbridgeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.RemoveVbridgeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.info.VbridgeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.Vbridge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.VbridgeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.VtnVbridgeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.RemoveVinterfaceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.VtnVinterfaceService;

/**
 * {@code VTNManagerService} provides interfaces to control VTN Manager.
 */
public final class VTNManagerService {
    /**
     * Logger instance.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(VTNManagerService.class);

    /**
     * A map that keeps pairs of HTTP status code associated with VTN error
     * tag.
     */
    private static final Map<String, Integer>  VTN_ERROR_CODES;

    /**
     * The number of seconds to wait for completion of RPC.
     */
    private static final long RPC_TIMEOUT = 60L;

    /**
     * MD-SAL utility service.
     */
    private final MdsalUtils  mdSal;

    /**
     * RPC service for VTN management.
     */
    private final VtnService vtnService;

    /**
     * RPC service for vBridge management.
     */
    private final VtnVbridgeService vbridgeService;

    /**
     * RPC service for virtual interface management.
     */
    private final VtnVinterfaceService vinterfaceService;

    /**
     * RPC service for port mapping management.
     */
    private final VtnPortMapService portMapService;

    /**
     * RPC service for port mapping management.
     */
    private final VtnVlanMapService vlanMapService;

    /**
     * RPC service for Flowcondition management.
     */
    private final VtnFlowConditionService flowConditionService;

    /**
     * RPC service for Flowcondition management.
     */
    private final VtnFlowFilterService flowFilterService;

    /**
     * Initialize statis field.
     */
    static {
        VTN_ERROR_CODES = ImmutableMap.<String, Integer>builder().
            put(VtnErrorTag.BADREQUEST.toString(), HTTP_BAD_REQUEST).
            put(VtnErrorTag.UNAUTHORIZED.toString(), HTTP_UNAUTHORIZED).
            put(VtnErrorTag.NOTFOUND.toString(), HTTP_NOT_FOUND).
            put(VtnErrorTag.NOTACCEPTABLE.toString(), HTTP_NOT_ACCEPTABLE).
            put(VtnErrorTag.TIMEOUT.toString(), HTTP_CLIENT_TIMEOUT).
            put(VtnErrorTag.CONFLICT.toString(), HTTP_CONFLICT).
            put(VtnErrorTag.GONE.toString(), HTTP_GONE).
            put(VtnErrorTag.NOSERVICE.toString(), HTTP_UNAVAILABLE).
            put(VtnErrorTag.INTERNALERROR.toString(), HTTP_INTERNAL_ERROR).
            build();
    }

    /**
     * {@code VTNRpcResult} describes a result of VTN RPC invocation.
     *
     * @param <O>  The type of the RPC output.
     */
    private static final class VTNRpcResult<O> {
        /**
         * HTTP status code.
         */
        private final int statusCode;

        /**
         * The output of the RPC.
         */
        private final O  output;

        /**
         * An error message.
         */
        private final String  errorMessage;

        /**
         * Construct a new instance that indicates successful completion.
         *
         * @param out  The output of the RPC.
         */
        private VTNRpcResult(O out) {
            statusCode = HTTP_OK;
            output = out;
            errorMessage = null;
        }

        /**
         * Construct a new instance that indicates an internal error.
         *
         * @param msg  An error message.
         */
        private VTNRpcResult(String msg) {
            statusCode = HTTP_INTERNAL_ERROR;
            output = null;
            errorMessage = msg;
        }

        /**
         * Construct a new instance.
         *
         * @param msg   An error message.
         * @param code  A HTTP status code that indicates the cause of error.
         */
        private VTNRpcResult(String msg, int code) {
            statusCode = code;
            output = null;
            errorMessage = msg;
        }

        /**
         * Return the HTTP status code that indicates the result of the RPC
         * invocation.
         *
         * @return  A HTTP status code.
         *          {@link java.net.HttpURLConnection#HTTP_OK} indicates
         *          successful completion.
         */
        private int getStatusCode() {
            return statusCode;
        }

        /**
         * Return the output of the RPC invocation.
         *
         * @return  The RPC output.
         */
        private O getOutput() {
            return output;
        }

        /**
         * Return an error message.
         *
         * @return  An error message.
         */
        private String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Return an instance identifier that specifies the VTN.
     *
     * @param tname  The name of the VTN.
     * @return  An {@link InstanceIdentifier} instance.
     */
    public static InstanceIdentifier<Vtn> getTenantPath(String tname) {
        VtnKey vtnKey = new VtnKey(new VnodeName(tname));
        return InstanceIdentifier.builder(Vtns.class).
            child(Vtn.class, vtnKey).
            build();
    }

    /**
     * Return an instance identifier that specifies the vBridge configuration.
     *
     * @param tname  The name of the VTN.
     * @param bname  The name of the vBridge.
     * @return  An {@link InstanceIdentifier} instance.
     */
    public static InstanceIdentifier<VbridgeConfig> getBridgeConfigPath(
        String tname, String bname) {
        VtnKey vtnKey = new VtnKey(new VnodeName(tname));
        VbridgeKey vbrKey = new VbridgeKey(new VnodeName(bname));
        return InstanceIdentifier.builder(Vtns.class).
            child(Vtn.class, vtnKey).
            child(Vbridge.class, vbrKey).
            child(VbridgeConfig.class).
            build();
    }

    /**
     * Wait for completion of the RPC task associated with the given future.
     *
     * @param f    A {@link Future} instance associated with the RPC task.
     * @param <O>  The type of the RPC output.
     * @return  A {@link VTNRpcResult} instance that contains the result of
     *          the RPC invocation.
     */
    private static <O> VTNRpcResult<O> getRpcResult(Future<RpcResult<O>> f) {
        return getRpcResult(f, false);
    }

    /**
     * Wait for completion of the RPC task associated with the given future.
     *
     * @param f         A {@link Future} instance associated with the RPC task.
     * @param nillable  Set {@code true} if the result can be null.
     * @param <O>       The type of the RPC output.
     * @return  A {@link VTNRpcResult} instance that contains the result of
     *          the RPC invocation.
     */
    private static <O> VTNRpcResult<O> getRpcResult(Future<RpcResult<O>> f,
                                                    boolean nillable) {
        RpcResult<O> result;
        try {
            result = f.get(RPC_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            String msg =
                "Caught an exception while waiting for RPC completion";
            LOG.error(msg, e);
            return new VTNRpcResult<O>(msg + ": " + e.toString());
        }

        VTNRpcResult<O> vres;
        if (result == null) {
            // This should never happen.
            vres = new VTNRpcResult<O>("RPC did not set result.");
        } else if (result.isSuccessful()) {
            O res = result.getResult();
            if (!nillable && res == null) {
                // This should never happen.
                vres = new VTNRpcResult<O>("RPC did not set output.");
            } else {
                vres = new VTNRpcResult<O>(res);
            }
        } else {
            vres = getRpcErrorResult(result);
        }

        return vres;
    }

    /**
     * Construct a {@link VTNRpcResult} instance that indicates an error.
     *
     * @param result  An {@link RpcResult} instance that indicates an error.
     * @param <O>     The type of the RPC output.
     * @return  A {@link VTNRpcResult} instance that contains the result of
     *          the RPC invocation.
     */
    private static <O> VTNRpcResult<O> getRpcErrorResult(RpcResult<O> result) {
        VTNRpcResult<O> vres;
        Collection<RpcError> errors = result.getErrors();
        if (errors == null || errors.isEmpty()) {
            // This should never happen.
            String msg = "RPC failed without error information: " + result;
            vres = new VTNRpcResult<O>(msg);
        } else {
            // VTN RPC sets only one RpcError, and it contains encoded
            // VtnErrorTag value in application tag.
            RpcError rerr = errors.iterator().next();
            String appTag = rerr.getApplicationTag();
            Integer code = VTN_ERROR_CODES.get(appTag);
            if (code != null) {
                String msg = appTag + ": " + rerr.getMessage();
                vres = new VTNRpcResult<O>(msg, code.intValue());
            } else {
                // Unexpected error.
                int index = 0;
                for (RpcError re: errors) {
                    Throwable cause = re.getCause();
                    String msg = "RPC failed: error[" + index + "]=" + re;
                    if (cause == null) {
                        LOG.error(msg);
                    } else {
                        LOG.error(msg, cause);
                    }
                    index++;
                }

                vres = new VTNRpcResult<O>("Internal error");
            }
        }

        return vres;
    }

    /**
     * Construct a new instance.
     *
     * @param md   A {@link MdsalUtils} instance.
     * @param rpc  A {@link RpcConsumerRegistry} instance.
     */
    public VTNManagerService(MdsalUtils md, RpcConsumerRegistry rpc) {
        mdSal = md;
        vtnService = rpc.getRpcService(VtnService.class);
        vbridgeService = rpc.getRpcService(VtnVbridgeService.class);
        vinterfaceService = rpc.getRpcService(VtnVinterfaceService.class);
        portMapService = rpc.getRpcService(VtnPortMapService.class);
        vlanMapService = rpc.getRpcService(VtnVlanMapService.class);
        flowConditionService = rpc.getRpcService(VtnFlowConditionService.class);
        flowFilterService = rpc.getRpcService(VtnFlowFilterService.class);
    }

    /**
     * Create or update a VTN with default parameters.
     *
     * @param name  The name of the VTN.
     * @param mode  A {@link VnodeUpdateMode} instance that specifies how to
     *              update the VTN.
     *              {@code null} implies {@link VnodeUpdateMode#UPDATE}.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean updateTenant(String name, VnodeUpdateMode mode) {
        UpdateVtnInput input = new UpdateVtnInputBuilder().
            setTenantName(name).
            setUpdateMode(mode).
            setOperation(VtnUpdateOperationType.SET).
            build();
        VTNRpcResult<UpdateVtnOutput> result =
            getRpcResult(vtnService.updateVtn(input));
        int code = result.getStatusCode();
        if (code != HTTP_OK) {
            LOG.error("Failed to update VTN: input={}, err={}",
                      input, result.getErrorMessage());
            return false;
        } else {
            VtnUpdateType utype = result.getOutput().getStatus();
            String msg;
            if (utype == VtnUpdateType.CREATED) {
                msg = "A VTN has been created";
            } else if (utype == VtnUpdateType.CHANGED) {
                msg = "A VTN has been changed";
            } else {
                assert utype == null;
                msg = "A VTN is present and not changed";
            }

            LOG.debug("{}: name={}", msg, name);
            return true;
        }
    }

    /**
     * Remove the specified VTN.
     *
     * @param name  The name of the VTN.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean removeTenant(String name) {
        RemoveVtnInput input = new RemoveVtnInputBuilder().
            setTenantName(name).build();
        VTNRpcResult<?> result =
            getRpcResult(vtnService.removeVtn(input), true);
        int code = result.getStatusCode();
        if (code == HTTP_OK) {
            LOG.debug("A VTN has been removed: name={}", name);
            return true;
        } else {
            LOG.error("Failed to remove VTN: name={}, err={}",
                      name, result.getErrorMessage());
            return false;
        }

    }

    /**
     * Determine whether the specified VTN contains at least one vBridge or
     * not.
     *
     * @param tname  The name of the VTN.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean hasBridge(String tname) {
        LogicalDatastoreType oper = LogicalDatastoreType.OPERATIONAL;
        Vtn opt = mdSal.read(LogicalDatastoreType.OPERATIONAL, getTenantPath(tname));
        boolean ret;
        if (opt != null) {
            List<Vbridge> vbridges = opt.getVbridge();
            ret = (vbridges != null && !vbridges.isEmpty());
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * Create or update the specified vBridge.
     *
     * @param tname  The name of the VTN.
     * @param bname  The name of the vBridge.
     * @param desc   A brief description about the vBridge.
     * @param mode   A {@link VnodeUpdateMode} instance that specifies how to
     *               update the vBridge.
     *               {@code null} implies {@link VnodeUpdateMode#UPDATE}.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean updateBridge(String tname, String bname, String desc,
                            VnodeUpdateMode mode) {
        UpdateVbridgeInput input = new UpdateVbridgeInputBuilder().
            setTenantName(tname).
            setBridgeName(bname).
            setDescription(desc).
            setUpdateMode(mode).
            setOperation(VtnUpdateOperationType.SET).
            build();
        VTNRpcResult<UpdateVbridgeOutput> result =
            getRpcResult(vbridgeService.updateVbridge(input));
        int code = result.getStatusCode();
        if (code == HTTP_CONFLICT){
            return true;
        }
        if (code != HTTP_OK) {
            LOG.error("Failed to update vBridge: input={}, err={}",
                      input, result.getErrorMessage());
            return false;
        } else {
            VtnUpdateType utype = result.getOutput().getStatus();
            String msg;
            if (utype == VtnUpdateType.CREATED) {
                msg = "A vBridge has been created";
            } else if (utype == VtnUpdateType.CHANGED) {
                msg = "A vBridge has been changed";
            } else {
                assert utype == null;
                msg = "A vBridge is present and not changed";
            }

            LOG.debug("{}: path={}/{}, desc={}", msg, tname, bname, desc);
            return true;
        }
   }

    /**
     * Remove the specified vBridge.
     *
     * @param tname  The name of the VTN.
     * @param bname  The name of the vBridge.
     * @return  A HTTP status code that indicates the result.
     *          {@link java.net.HttpURLConnection#HTTP_OK} indicates
     *          successful completion.
     */
    public int removeBridge(String tname, String bname) {
        RemoveVbridgeInput input = new RemoveVbridgeInputBuilder().
            setTenantName(tname).
            setBridgeName(bname).
            build();
        VTNRpcResult<?> result =
            getRpcResult(vbridgeService.removeVbridge(input), true);
        int code = result.getStatusCode();
        if (code == HTTP_OK) {
            LOG.debug("A vBridge has been removed: path={}/{}", tname, bname);
        } else {
            LOG.error("Failed to remove vBridge: path={}/{}, err={}",
                      tname, bname, result.getErrorMessage());
        }

        return code;
    }

    /**
     * Return the current configuration of the specified vBridge.
     *
     * @param tname  The name of the VTN.
     * @param bname  The name of the vBridge.
     * @return  A {@link VbridgeConfig} instance if the specified vBridge is
     *          present. {@code null} otherwise.
     */
    public VbridgeConfig getBridgeConfig(String tname, String bname) {
        LogicalDatastoreType oper = LogicalDatastoreType.OPERATIONAL;
        return mdSal.read(oper, getBridgeConfigPath(tname, bname));
    }

    /**
     * Create or update the specified virtual interface.
     *
     * @param input  A {@link UpdateVinterfaceInput} instance.
     * @return  A HTTP status code that indicates the result.
     *          {@link java.net.HttpURLConnection#HTTP_OK} indicates
     *          successful completion.
     */
    public int updateInterface(UpdateVinterfaceInput input) {
        VTNRpcResult<UpdateVinterfaceOutput> result =
            getRpcResult(vinterfaceService.updateVinterface(input));
        int code = result.getStatusCode();
        if (code != HTTP_OK) {
            LOG.error("Failed to update virtual interface: input={}, err={}",
                      input, result.getErrorMessage());
        } else if (LOG.isDebugEnabled()) {
            VtnUpdateType utype = result.getOutput().getStatus();
            String msg;
            if (utype == VtnUpdateType.CREATED) {
                msg = "A virtual interface has been created";
            } else if (utype == VtnUpdateType.CHANGED) {
                msg = "A virtual interface has been changed";
            } else {
                assert utype == null;
                msg = "A virtual interface is present and not changed";
            }

            LOG.debug("{}: input={}", msg, input);
        }

        return code;
    }

    /**
     * Remove the specified virtual interface.
     *
     * @param input  A {@link RemoveVinterfaceInput} instance.
     * @return  A HTTP status code that indicates the result.
     *          {@link java.net.HttpURLConnection#HTTP_OK} indicates
     *          successful completion.
     */
    public int removeInterface(RemoveVinterfaceInput input) {
        VTNRpcResult<?> result =
            getRpcResult(vinterfaceService.removeVinterface(input), true);
        int code = result.getStatusCode();
        if (code == HTTP_OK) {
            LOG.debug("A virtual interface has been removed: input={}",
                      input);
        } else {
            LOG.error("Failed to remove virtual interface: input={}, err={}",
                      input, result.getErrorMessage());
        }

        return code;
    }

     /**
     * Configure vlan Mapping into the virtual interface specified by the
     * given RPC input.
     *
     * @param input  A {@link AddVlanMapInput} instance.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean setVlanMap(AddVlanMapInput input) {
        VTNRpcResult<AddVlanMapOutput> result =
            getRpcResult(vlanMapService.addVlanMap(input));
        int code = result.getStatusCode();
        if (code == HTTP_CONFLICT){
            return true;
        }
        if (code != HTTP_OK) {
            LOG.error("Failed to set VlanMap: input={}, err={}",
                      input, result.getErrorMessage());
            return false;
        } else {
            return true;
        }
    }

     /**
     * Remove the Vlan mapping configuration from the specified virtual
     * interface.
     *
     * @param input  A {@link RemoveVlanMapInput} instance.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean removeVlanMap(RemoveVlanMapInput input) {
        VTNRpcResult<RemoveVlanMapOutput> result =
            getRpcResult(vlanMapService.removeVlanMap(input), true);
        int code = result.getStatusCode();
        if (code != HTTP_OK) {
            LOG.error("Failed to remove VlanMap: input={}, err={}",
                      input, result.getErrorMessage());
            return false;
        } else {
            return true;
        }
    }

     /**
     * Configure a FlowCondition into the virtual interface specified by the
     * given RPC input.
     *
     * @param input  A {@link SetFlowConditionInput} instance.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean setFlowCond(SetFlowConditionInput input) {
        VTNRpcResult<SetFlowConditionOutput> result =
            getRpcResult(flowConditionService.setFlowCondition(input));
        int code = result.getStatusCode();
        if (code != HTTP_OK) {
            LOG.error("Failed to set FlowCondition: input={}, err={}",
                      input, result.getErrorMessage());
            return false;
        } else {
            VtnUpdateType utype = result.getOutput().getStatus();
            String msg;
            if (utype == VtnUpdateType.CREATED) {
                msg = "Flow Conditon has been created";
            } else if (utype == VtnUpdateType.CHANGED) {
                msg = "Flow COndition has been changed";
            }
            return true;
        }
    }

     /**
     * Unset a FlowCondition into the virtual interface specified by the
     * given RPC input.
     *
     * @param input  A {@link RemoveFlowConditionInput} instance.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean unsetFlowCond(RemoveFlowConditionInput input) {
        VTNRpcResult result =
            getRpcResult(flowConditionService.removeFlowCondition(input), true);
        int code = result.getStatusCode();
        if (code != HTTP_OK) {
            LOG.error("Failed to unset FlowCondition: input={}, err={}",
                      input, result.getErrorMessage());
            return false;
        } else {
            return true;
        }
    }

     /**
     * Set FlowFilter into the virtual interface specified by the
     * given RPC input.
     *
     * @param input  A {@link SetFlowFilterInput} instance.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
    public boolean setFlowFilter(SetFlowFilterInput input) {
        VTNRpcResult<SetFlowFilterOutput> result =
            getRpcResult(flowFilterService.setFlowFilter(input));
        int code = result.getStatusCode();
        if (code != HTTP_OK) {
            return false;
        } else {
            return true;
        }
    }

     /**
     * Unset a FlowFilter into the virtual interface specified by the
     * given RPC input.
     *
     * @param input  A {@link RemoveFlowFilterInput} instance.
     * @return      True is returned on successful completion,
                    else false is returned.
     */
     public boolean unSetFlowFilter(RemoveFlowFilterInput input) {
        VTNRpcResult<RemoveFlowFilterOutput> result =
            getRpcResult(flowFilterService.removeFlowFilter(input), true);
        int code = result.getStatusCode();
        if (code != HTTP_OK) {
            LOG.error("Failed to unset Flow Filter: input={}, err={}",
                      input, result.getErrorMessage());
            return false;
        } else {
            return true;
        }
    }
}
