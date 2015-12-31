/*
 * Copyright (c) 2016 NEC Corporation. All rights reserved.
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.vtn.manager.util.IpNetwork;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.SetFlowConditionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.VtnFlowConditionService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.flow.cond.config.VtnFlowMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.flow.cond.config.VtnFlowMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnEtherMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnEtherMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnInetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.cond.rev150313.vtn.match.fields.VtnInetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.RemoveFlowFilterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.SetFlowFilterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.flow.filter.rev150907.VtnFlowFilterService;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.AddVlanMapOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.RemoveVlanMapInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.RemoveVlanMapInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.RemoveVlanMapOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.RemoveVlanMapOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.mapping.vlan.rev150907.VtnVlanMapService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.RemoveVtnInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.RemoveVtnInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.UpdateVtnInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.UpdateVtnInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.UpdateVtnOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.UpdateVtnOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.VtnService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.Vtns;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.Vtn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.VtnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.rev150328.vtns.VtnKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VnodeUpdateMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnErrorTag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnUpdateOperationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.types.rev150209.VtnUpdateType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.RemoveVbridgeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.RemoveVbridgeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.UpdateVbridgeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.VtnVbridgeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.info.VbridgeConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.info.VbridgeConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.Vbridge;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.VbridgeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vbridge.rev150907.vtn.vbridge.list.VbridgeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.RemoveVinterfaceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.RemoveVinterfaceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.UpdateVinterfaceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.vinterface.rev150907.VtnVinterfaceService;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;

/**
 * JUnit test for {@link VTNManagerService}.
 */
public class VTNManagerServiceTest extends TestBase {
    /**
     * A map that keeps pairs of HTTP status code associated with VTN error tag.
     */
    private static final Map<VtnErrorTag, Integer> VTN_ERROR_CODES;

    /**
     * MD-SAL data broker.
     */
    @Mock
    private DataBroker dataBroker;

    /**
     * RPC consumer registry.
     */
    @Mock
    private RpcConsumerRegistry rpcRegistry;

    /**
     * RPC service for VTN management.
     */
    @Mock
    private VtnService vtnService;

    /**
     * RPC service for vBridge management.
     */
    @Mock
    private VtnVbridgeService vbridgeService;

    /**
     * RPC service for virtual interface management.
     */
    @Mock
    private VtnVinterfaceService vinterfaceService;

    /**
     * RPC service for virtual interface management.
     */
    @Mock
    private VtnVlanMapService vlanMapService;
    /**
     * RPC service for virtual interface management.
     */
    @Mock
    private VtnFlowConditionService conditionService;
    /**
     * RPC service for virtual interface management.
     */
    @Mock
    private VtnFlowFilterService flowFilterService;
    /**
     * RPC service for virtual interface management.
     */
    @Mock
    private VtnRpcResult vtnRpcResult;

    /**
     * Initialize statistics field.
     */
    static {
        VTN_ERROR_CODES = new EnumMap<>(VtnErrorTag.class);
        VTN_ERROR_CODES.put(VtnErrorTag.BADREQUEST, HTTP_BAD_REQUEST);
        VTN_ERROR_CODES.put(VtnErrorTag.UNAUTHORIZED, HTTP_UNAUTHORIZED);
        VTN_ERROR_CODES.put(VtnErrorTag.NOTFOUND, HTTP_NOT_FOUND);
        VTN_ERROR_CODES.put(VtnErrorTag.NOTACCEPTABLE, HTTP_NOT_ACCEPTABLE);
        VTN_ERROR_CODES.put(VtnErrorTag.TIMEOUT, HTTP_CLIENT_TIMEOUT);
        VTN_ERROR_CODES.put(VtnErrorTag.CONFLICT, HTTP_CONFLICT);
        VTN_ERROR_CODES.put(VtnErrorTag.GONE, HTTP_GONE);
        VTN_ERROR_CODES.put(VtnErrorTag.NOSERVICE, HTTP_UNAVAILABLE);
        VTN_ERROR_CODES.put(VtnErrorTag.INTERNALERROR, HTTP_INTERNAL_ERROR);
    }

    /**
     * Test case for {@link VTNManagerService#getTenantPath(String)}.
     */
    @Test
    public void testGetTenantPath() {
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };

        for (String tname : tnames) {
            VtnKey vtnKey = new VtnKey(new VnodeName(tname));
            InstanceIdentifier<Vtn> expath = InstanceIdentifier
                    .builder(Vtns.class).child(Vtn.class, vtnKey).build();
            assertEquals(expath, VTNManagerService.getTenantPath(tname));
        }
    }

    /**
     * Test case for
     * {@link VTNManagerService#getBridgeConfigPath(String, String)}.
     */
    @Test
    public void testGetBridgeConfigPath() {
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };
        String[] bnames = { "vbr", "vbridge_1", "vbridge_2", };

        for (String tname : tnames) {
            VtnKey vtnKey = new VtnKey(new VnodeName(tname));
            for (String bname : bnames) {
                VbridgeKey vbrKey = new VbridgeKey(new VnodeName(bname));
                InstanceIdentifier<VbridgeConfig> expath = InstanceIdentifier
                        .builder(Vtns.class).child(Vtn.class, vtnKey)
                        .child(Vbridge.class, vbrKey)
                        .child(VbridgeConfig.class).build();
                InstanceIdentifier<VbridgeConfig> path = VTNManagerService
                        .getBridgeConfigPath(tname, bname);
                assertEquals(expath, path);
            }
        }
    }

    /**
     * Test case for
     * {@link VTNManagerService#updateTenant(String, VnodeUpdateMode)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateTenant() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };

        // In case of successful completion.
        VtnUpdateType[] utypes = { null, VtnUpdateType.CREATED,
                VtnUpdateType.CHANGED, };
        for (String tname : tnames) {
            for (VnodeUpdateMode mode : VnodeUpdateMode.values()) {
                UpdateVtnInput input = new UpdateVtnInputBuilder()
                        .setTenantName(tname).setUpdateMode(mode)
                        .setOperation(VtnUpdateOperationType.SET).build();

                for (VtnUpdateType utype : utypes) {
                    UpdateVtnOutput output = new UpdateVtnOutputBuilder()
                            .setStatus(utype).build();
                    reset(vtnService);
                    when(vtnService.updateVtn(input)).thenReturn(
                            getRpcFuture(output));
                    assertEquals(true, vtnMgr.updateTenant(tname, mode));
                    verify(vtnService).updateVtn(input);
                }
            }
        }

        String tname = "vtn_fail";
        UpdateVtnInput input = new UpdateVtnInputBuilder().setTenantName(tname)
                .setOperation(VtnUpdateOperationType.SET).build();

        // In case of failure.
        for (VtnErrorTag vtag : VtnErrorTag.values()) {
            Future<RpcResult<UpdateVtnOutput>> future = getFailureFuture(vtag,
                    "Test failure");
            reset(vtnService);
            when(vtnService.updateVtn(input)).thenReturn(future);
            assertEquals(false, vtnMgr.updateTenant(tname, null));
            verify(vtnService).updateVtn(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<UpdateVtnOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(vtnService);
        when(vtnService.updateVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateTenant(tname, null));
        verify(vtnService).updateVtn(input);

        // Future returns null.
        RpcResult<UpdateVtnOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.updateVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateTenant(tname, null));
        verify(vtnService).updateVtn(input);
        // RpcResult does not contain output.
        future = getRpcFuture((UpdateVtnOutput) null);
        reset(vtnService);
        when(vtnService.updateVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateTenant(tname, null));
        verify(vtnService).updateVtn(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.updateVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateTenant(tname, null));
        verify(vtnService).updateVtn(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.updateVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateTenant(tname, null));
        verify(vtnService).updateVtn(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        // Unexpected error information.
        IllegalArgumentException iae = new IllegalArgumentException("Too big");
        result = RpcResultBuilder
                .<UpdateVtnOutput> failed()
                .withError(ErrorType.APPLICATION, "in-use", "Unknown error 1",
                        null, null, null)
                .withError(ErrorType.APPLICATION, "too-big", "Unknown error 2",
                        "unknown", null, iae).build();
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.updateVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateTenant(tname, null));
        verify(vtnService).updateVtn(input);
    }

    /**
     * Test case for {@link VTNManagerService#removeTenant(String)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveTenant() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };

        // In case of successful completion.
        for (String tname : tnames) {
            RemoveVtnInput input = new RemoveVtnInputBuilder().setTenantName(
                    tname).build();
            reset(vtnService);
            when(vtnService.removeVtn(input)).thenReturn(
                    getRpcFuture((Void) null));
            assertEquals(true, vtnMgr.removeTenant(tname));
            verify(vtnService).removeVtn(input);
        }

        String tname = "vtn_fail";
        RemoveVtnInput input = new RemoveVtnInputBuilder().setTenantName(tname)
                .build();

        // In case of failure.
        for (VtnErrorTag vtag : VtnErrorTag.values()) {
            Future<RpcResult<Void>> future = getFailureFuture(vtag,
                    "Test failure");
            reset(vtnService);
            when(vtnService.removeVtn(input)).thenReturn(future);
            assertEquals(false, vtnMgr.removeTenant(tname));
            verify(vtnService).removeVtn(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<Void>> future = Futures.immediateFailedFuture(ise);
        reset(vtnService);
        when(vtnService.removeVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.removeTenant(tname));
        verify(vtnService).removeVtn(input);

        // Future returns null.
        RpcResult<Void> result = null;
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.removeVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.removeTenant(tname));
        verify(vtnService).removeVtn(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.removeVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.removeTenant(tname));
        verify(vtnService).removeVtn(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.removeVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.removeTenant(tname));
        verify(vtnService).removeVtn(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        // Unexpected error information.
        IllegalArgumentException iae = new IllegalArgumentException("Too big");
        result = RpcResultBuilder
                .<Void> failed()
                .withError(ErrorType.APPLICATION, "in-use", "Unknown error 1",
                        null, null, null)
                .withError(ErrorType.APPLICATION, "too-big", "Unknown error 2",
                        "unknown", null, iae).build();
        future = Futures.immediateFuture(result);
        reset(vtnService);
        when(vtnService.removeVtn(input)).thenReturn(future);
        assertEquals(false, vtnMgr.removeTenant(tname));
        verify(vtnService).removeVtn(input);
    }

    /**
     * Test case for {@link VTNManagerService#hasBridge(String)}.
     *
     * @throws Exception An error occurred.
     */
    @Test
    public void testContainsBridge() throws Exception {
        VTNManagerService vtnMgr = getVTNManagerService();
        LogicalDatastoreType oper = LogicalDatastoreType.OPERATIONAL;
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };

        for (String tname : tnames) {
            VtnKey vtnKey = new VtnKey(new VnodeName(tname));
            InstanceIdentifier<Vtn> path = InstanceIdentifier
                    .builder(Vtns.class).child(Vtn.class, vtnKey).build();

            // In case where the specified VTN is not present.
            int txConut = 0;
            Vtn vtn = null;
            ReadOnlyTransaction rtx = mock(ReadOnlyTransaction.class);
            reset(dataBroker);
            when(dataBroker.newReadOnlyTransaction()).thenReturn(rtx);
            when(rtx.read(oper, path)).thenReturn(getReadResult(vtn));
            assertEquals(false, vtnMgr.hasBridge(tname));
            verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
            verify(rtx).read(oper, path);
            verify(rtx).close();

            // In case where the specified VTN contains null vBridge list.
            VnodeName vtnName = new VnodeName(tname);
            vtn = new VtnBuilder().setName(vtnName).build();
            reset(rtx);
            when(rtx.read(oper, path)).thenReturn(getReadResult(vtn));
            assertEquals(false, vtnMgr.hasBridge(tname));
            verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
            verify(rtx).read(oper, path);
            verify(rtx).close();

            // In case where the specified VTN contains an empty vBridge list.
            List<Vbridge> vbridges = Collections.<Vbridge> emptyList();
            vtn = new VtnBuilder().setName(vtnName).setVbridge(vbridges)
                    .build();
            reset(rtx);
            when(rtx.read(oper, path)).thenReturn(getReadResult(vtn));
            assertEquals(false, vtnMgr.hasBridge(tname));
            verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
            verify(rtx).read(oper, path);
            verify(rtx).close();

            // In case of unexpected error.
            reset(rtx);
            IllegalStateException ise = new IllegalStateException("Read failed");
            when(rtx.read(oper, path)).thenReturn(
                    getReadFailure(Vtn.class, ise));
            assertEquals(false, vtnMgr.hasBridge(tname));
            verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
            verify(rtx).read(oper, path);
            verify(rtx).close();

            // In case where the specified VTN contains one or more vBridges.
            for (int count = 1; count <= 5; count++) {
                vbridges = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    vbridges.add(new VbridgeBuilder().build());
                }
                vtn = new VtnBuilder().setName(vtnName).setVbridge(vbridges)
                        .build();
                reset(rtx);
                when(rtx.read(oper, path)).thenReturn(getReadResult(vtn));
                assertEquals(true, vtnMgr.hasBridge(tname));
                verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
                verify(rtx).read(oper, path);
                verify(rtx).close();

            }
        }
    }

    /**
     * Test case for
     * {@link VTNManagerService#updateBridge(String, String, String, VnodeUpdateMode)}
     * .
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateBridge() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };
        String[] bnames = { "vbr", "bridge_1", "bridge_2", };
        String[] descriptions = { null, "desc", };

        // In case of successful completion.
        List<UpdateVbridgeInput> inputs = new ArrayList<>();
        for (String tname : tnames) {
            for (String bname : bnames) {
                for (String desc : descriptions) {
                    for (VnodeUpdateMode mode : VnodeUpdateMode.values()) {
                        UpdateVbridgeInput input = new UpdateVbridgeInputBuilder()
                                .setTenantName(tname).setBridgeName(bname)
                                .setDescription(desc).setUpdateMode(mode)
                                .setOperation(VtnUpdateOperationType.SET)
                                .build();
                        inputs.add(input);
                    }
                }
            }
        }

        VtnUpdateType[] utypes = { null, VtnUpdateType.CREATED,
                VtnUpdateType.CHANGED, };
        for (UpdateVbridgeInput input : inputs) {
            for (VtnUpdateType utype : utypes) {
                UpdateVbridgeOutput output = new UpdateVbridgeOutputBuilder()
                        .setStatus(utype).build();
                reset(vbridgeService);
                when(vbridgeService.updateVbridge(input)).thenReturn(
                        getRpcFuture(output));

                String tname = input.getTenantName();
                String bname = input.getBridgeName();
                String desc = input.getDescription();
                VnodeUpdateMode mode = input.getUpdateMode();
                assertEquals(true,
                        vtnMgr.updateBridge(tname, bname, desc, mode));
                verify(vbridgeService).updateVbridge(input);
            }
        }

        String tname = "vtn_fail";
        String bname = "vbr_fail";
        UpdateVbridgeInput input = new UpdateVbridgeInputBuilder()
                .setTenantName(tname).setBridgeName(bname)
                .setOperation(VtnUpdateOperationType.SET).build();

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<UpdateVbridgeOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(vbridgeService);
        when(vbridgeService.updateVbridge(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateBridge(tname, bname, null, null));
        verify(vbridgeService).updateVbridge(input);

        // Future returns null.
        RpcResult<UpdateVbridgeOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.updateVbridge(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateBridge(tname, bname, null, null));
        verify(vbridgeService).updateVbridge(input);

        // RpcResult does not contain output.
        future = getRpcFuture((UpdateVbridgeOutput) null);
        reset(vbridgeService);
        when(vbridgeService.updateVbridge(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateBridge(tname, bname, null, null));
        verify(vbridgeService).updateVbridge(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.updateVbridge(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateBridge(tname, bname, null, null));
        verify(vbridgeService).updateVbridge(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.updateVbridge(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateBridge(tname, bname, null, null));
        verify(vbridgeService).updateVbridge(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        // Unexpected error information.
        IllegalArgumentException iae = new IllegalArgumentException("Too big");
        result = RpcResultBuilder
                .<UpdateVbridgeOutput> failed()
                .withError(ErrorType.APPLICATION, "in-use", "Unknown error 1",
                        null, null, null)
                .withError(ErrorType.APPLICATION, "too-big", "Unknown error 2",
                        "unknown", null, iae).build();
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.updateVbridge(input)).thenReturn(future);
        assertEquals(false, vtnMgr.updateBridge(tname, bname, null, null));
        verify(vbridgeService).updateVbridge(input);
    }

    /**
     * Test case for {@link VTNManagerService#removeBridge(String, String)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveBridge() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };
        String[] bnames = { "vbr", "bridge_1", "bridge_2", };

        // In case of successful completion.
        for (String tname : tnames) {
            for (String bname : bnames) {
                RemoveVbridgeInput input = new RemoveVbridgeInputBuilder()
                        .setTenantName(tname).setBridgeName(bname).build();
                reset(vbridgeService);
                when(vbridgeService.removeVbridge(input)).thenReturn(
                        getRpcFuture((Void) null));
                assertEquals(HTTP_OK, vtnMgr.removeBridge(tname, bname));
                verify(vbridgeService).removeVbridge(input);
            }
        }

        String tname = "vtn_fail";
        String bname = "vbr_fail";
        RemoveVbridgeInput input = new RemoveVbridgeInputBuilder()
                .setTenantName(tname).setBridgeName(bname).build();

        // In case of failure.
        for (VtnErrorTag vtag : VtnErrorTag.values()) {
            int expected = VTN_ERROR_CODES.get(vtag).intValue();
            Future<RpcResult<Void>> future = getFailureFuture(vtag,
                    "Test failure");
            reset(vbridgeService);
            when(vbridgeService.removeVbridge(input)).thenReturn(future);
            assertEquals(expected, vtnMgr.removeBridge(tname, bname));
            verify(vbridgeService).removeVbridge(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<Void>> future = Futures.immediateFailedFuture(ise);
        reset(vbridgeService);
        when(vbridgeService.removeVbridge(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeBridge(tname, bname));
        verify(vbridgeService).removeVbridge(input);

        // Future returns null.
        RpcResult<Void> result = null;
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.removeVbridge(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeBridge(tname, bname));
        verify(vbridgeService).removeVbridge(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.removeVbridge(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeBridge(tname, bname));
        verify(vbridgeService).removeVbridge(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.removeVbridge(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeBridge(tname, bname));
        verify(vbridgeService).removeVbridge(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        // Unexpected error information.
        IllegalArgumentException iae = new IllegalArgumentException("Too big");
        result = RpcResultBuilder
                .<Void> failed()
                .withError(ErrorType.APPLICATION, "in-use", "Unknown error 1",
                        null, null, null)
                .withError(ErrorType.APPLICATION, "too-big", "Unknown error 2",
                        "unknown", null, iae).build();
        future = Futures.immediateFuture(result);
        reset(vbridgeService);
        when(vbridgeService.removeVbridge(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeBridge(tname, bname));
        verify(vbridgeService).removeVbridge(input);
    }

    /**
     * Test case for {@link VTNManagerService#getBridgeConfig(String, String)}.
     *
     * @throws Exception
     *             An error occurred.
     */
    @Test
    public void getGetBridgeConfig() throws Exception {
        VTNManagerService vtnMgr = getVTNManagerService();
        LogicalDatastoreType oper = LogicalDatastoreType.OPERATIONAL;
        String[] tnames = { "vtn", "vtn_1", "vtn_2", };
        String[] bnames = { "vbr", "bridge_1", "bridge_2", };

        for (String tname : tnames) {
            VtnKey vtnKey = new VtnKey(new VnodeName(tname));
            for (String bname : bnames) {
                VbridgeKey vbrKey = new VbridgeKey(new VnodeName(bname));
                InstanceIdentifier<VbridgeConfig> path = InstanceIdentifier
                        .builder(Vtns.class).child(Vtn.class, vtnKey)
                        .child(Vbridge.class, vbrKey)
                        .child(VbridgeConfig.class).build();

                // In case where the specified vBridge is not present.
                int txConut = 0;
                VbridgeConfig config = null;
                ReadOnlyTransaction rtx = mock(ReadOnlyTransaction.class);
                reset(dataBroker);
                when(dataBroker.newReadOnlyTransaction()).thenReturn(rtx);
                when(rtx.read(oper, path)).thenReturn(getReadResult(config));
                assertEquals(null, vtnMgr.getBridgeConfig(tname, bname));
                verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
                verify(rtx).read(oper, path);
                verify(rtx).close();

                // In case where the specified vBridge is present.
                config = new VbridgeConfigBuilder().build();
                reset(rtx);
                when(rtx.read(oper, path)).thenReturn(getReadResult(config));
                assertEquals(config, vtnMgr.getBridgeConfig(tname, bname));
                verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
                verify(rtx).read(oper, path);
                verify(rtx).close();

                // In case of unexpected error.
                reset(rtx);
                IllegalStateException ise = new IllegalStateException(
                        "Read failed");
                when(rtx.read(oper, path)).thenReturn(
                        getReadFailure(VbridgeConfig.class, ise));
                assertEquals(null, vtnMgr.getBridgeConfig(tname, bname));
                verify(dataBroker, times(++txConut)).newReadOnlyTransaction();
                verify(rtx).read(oper, path);
                verify(rtx).close();

            }
        }
    }

    /**
     * Test case for
     * {@link VTNManagerService#updateInterface(UpdateVinterfaceInput)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateInterface() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", };
        String[] bnames = { "vbr", "bridge_1", };
        String[] inames = { "vif", "if_1", };

        // In case of successful completion.
        List<UpdateVinterfaceInput> inputs = new ArrayList<>();
        for (String tname : tnames) {
            for (String bname : bnames) {
                for (String iname : inames) {
                    for (VnodeUpdateMode mode : VnodeUpdateMode.values()) {
                        UpdateVinterfaceInput input = new UpdateVinterfaceInputBuilder()
                                .setTenantName(tname).setBridgeName(bname)
                                .setInterfaceName(iname).setUpdateMode(mode)
                                .build();
                        inputs.add(input);

                        input = new UpdateVinterfaceInputBuilder()
                                .setTenantName(tname).setBridgeName(bname)
                                .setInterfaceName(iname).setUpdateMode(mode)
                                .setEnabled(true).build();
                        inputs.add(input);

                        input = new UpdateVinterfaceInputBuilder()
                                .setTenantName(tname).setBridgeName(bname)
                                .setInterfaceName(iname).setUpdateMode(mode)
                                .setEnabled(false)
                                .setDescription("virtual interface").build();
                        inputs.add(input);
                    }
                }
            }
        }

        VtnUpdateType[] utypes = { null, VtnUpdateType.CREATED,
                VtnUpdateType.CHANGED, };
        for (UpdateVinterfaceInput input : inputs) {
            for (VtnUpdateType utype : utypes) {
                UpdateVinterfaceOutput output = new UpdateVinterfaceOutputBuilder()
                        .setStatus(utype).build();
                reset(vinterfaceService);
                when(vinterfaceService.updateVinterface(input)).thenReturn(
                        getRpcFuture(output));
                assertEquals(HTTP_OK, vtnMgr.updateInterface(input));
                verify(vinterfaceService).updateVinterface(input);
            }
        }

        String tname = "vtn_fail";
        String bname = "vbr_fail";
        String iname = "vif_fail";
        UpdateVinterfaceInput input = new UpdateVinterfaceInputBuilder()
                .setTenantName(tname).setBridgeName(bname)
                .setInterfaceName(iname).build();

        // In case of failure.
        for (VtnErrorTag vtag : VtnErrorTag.values()) {
            int expected = VTN_ERROR_CODES.get(vtag).intValue();
            Future<RpcResult<UpdateVinterfaceOutput>> future = getFailureFuture(
                    vtag, "Test failure");
            reset(vinterfaceService);
            when(vinterfaceService.updateVinterface(input)).thenReturn(future);
            assertEquals(expected, vtnMgr.updateInterface(input));
            verify(vinterfaceService).updateVinterface(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<UpdateVinterfaceOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(vinterfaceService);
        when(vinterfaceService.updateVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.updateInterface(input));
        verify(vinterfaceService).updateVinterface(input);

        // Future returns null.
        RpcResult<UpdateVinterfaceOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.updateVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.updateInterface(input));
        verify(vinterfaceService).updateVinterface(input);

        // RpcResult does not contain output.
        future = getRpcFuture((UpdateVinterfaceOutput) null);
        reset(vinterfaceService);
        when(vinterfaceService.updateVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.updateInterface(input));
        verify(vinterfaceService).updateVinterface(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.updateVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.updateInterface(input));
        verify(vinterfaceService).updateVinterface(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.updateVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.updateInterface(input));
        verify(vinterfaceService).updateVinterface(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        // Unexpected error information.
        IllegalArgumentException iae = new IllegalArgumentException("Too big");
        result = RpcResultBuilder
                .<UpdateVinterfaceOutput> failed()
                .withError(ErrorType.APPLICATION, "in-use", "Unknown error 1",
                        null, null, null)
                .withError(ErrorType.APPLICATION, "too-big", "Unknown error 2",
                        "unknown", null, iae).build();
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.updateVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.updateInterface(input));
        verify(vinterfaceService).updateVinterface(input);
    }

    /**
     * Test case for
     * {@link VTNManagerService#removeInterface(RemoveVinterfaceInput)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveInterface() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", };
        String[] bnames = { "vbr", "bridge_1", };
        String[] inames = { "vif", "if_1", };

        // In case of successful completion.
        for (String tname : tnames) {
            for (String bname : bnames) {
                for (String iname : inames) {
                    RemoveVinterfaceInput input = new RemoveVinterfaceInputBuilder()
                            .setTenantName(tname).setBridgeName(bname)
                            .setInterfaceName(iname).build();
                    reset(vinterfaceService);
                    when(vinterfaceService.removeVinterface(input)).thenReturn(
                            getRpcFuture((Void) null));
                    assertEquals(HTTP_OK, vtnMgr.removeInterface(input));
                    verify(vinterfaceService).removeVinterface(input);
                }
            }
        }

        String tname = "vtn_fail";
        String bname = "vbr_fail";
        String iname = "vif_fail";
        RemoveVinterfaceInput input = new RemoveVinterfaceInputBuilder()
                .setTenantName(tname).setBridgeName(bname)
                .setInterfaceName(iname).build();

        // In case of failure.
        for (VtnErrorTag vtag : VtnErrorTag.values()) {
            int expected = VTN_ERROR_CODES.get(vtag).intValue();
            Future<RpcResult<Void>> future = getFailureFuture(vtag,
                    "Test failure");
            reset(vinterfaceService);
            when(vinterfaceService.removeVinterface(input)).thenReturn(future);
            assertEquals(expected, vtnMgr.removeInterface(input));
            verify(vinterfaceService).removeVinterface(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<Void>> future = Futures.immediateFailedFuture(ise);
        reset(vinterfaceService);
        when(vinterfaceService.removeVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeInterface(input));
        verify(vinterfaceService).removeVinterface(input);

        // Future returns null.
        RpcResult<Void> result = null;
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.removeVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeInterface(input));
        verify(vinterfaceService).removeVinterface(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.removeVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeInterface(input));
        verify(vinterfaceService).removeVinterface(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.removeVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeInterface(input));
        verify(vinterfaceService).removeVinterface(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        // Unexpected error information.
        IllegalArgumentException iae = new IllegalArgumentException("Too big");
        result = RpcResultBuilder
                .<Void> failed()
                .withError(ErrorType.APPLICATION, "in-use", "Unknown error 1",
                        null, null, null)
                .withError(ErrorType.APPLICATION, "too-big", "Unknown error 2",
                        "unknown", null, iae).build();
        future = Futures.immediateFuture(result);
        reset(vinterfaceService);
        when(vinterfaceService.removeVinterface(input)).thenReturn(future);
        assertEquals(HTTP_INTERNAL_ERROR, vtnMgr.removeInterface(input));
        verify(vinterfaceService).removeVinterface(input);
    }

    /**
     * Create a new VTN Manager service instance.
     *
     * @return A {@link VTNManagerService} instance.
     */
    private VTNManagerService getVTNManagerService() {
        initMocks(this);
        MdsalUtils mdSal = new MdsalUtils(dataBroker);

        when(rpcRegistry.getRpcService(VtnService.class))
                .thenReturn(vtnService);
        when(rpcRegistry.getRpcService(VtnVbridgeService.class)).thenReturn(
                vbridgeService);
        when(rpcRegistry.getRpcService(VtnVinterfaceService.class)).thenReturn(
                vinterfaceService);
        when(rpcRegistry.getRpcService(VtnVlanMapService.class)).thenReturn(
                vlanMapService);
        when(rpcRegistry.getRpcService(VtnFlowConditionService.class))
                .thenReturn(conditionService);
        when(rpcRegistry.getRpcService(VtnFlowFilterService.class)).thenReturn(
                flowFilterService);
        VTNManagerService vtn = new VTNManagerService(mdSal, rpcRegistry);
        verify(rpcRegistry).getRpcService(VtnService.class);
        verify(rpcRegistry).getRpcService(VtnVbridgeService.class);
        verify(rpcRegistry).getRpcService(VtnVinterfaceService.class);
        verify(rpcRegistry).getRpcService(VtnVlanMapService.class);
        verify(rpcRegistry).getRpcService(VtnFlowFilterService.class);

        return vtn;
    }

    /**
     * Test case for {@link VTNManagerService#setVlanMap(AddVlanMapInput)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSetVlanMap() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", };
        String[] bnames = { "vbr", "bridge_1", };
        VlanId[] ids = { new VlanId(0), new VlanId(1048) };

        // In case of successful completion.
        List<AddVlanMapInput> inputs = new ArrayList<>();
        for (String tname : tnames) {
            for (String bname : bnames) {
                for (VlanId vlan : ids) {
                    AddVlanMapInput input = new AddVlanMapInputBuilder()
                            .setBridgeName(bname).setTenantName(tname)
                            .setVlanId(vlan).build();
                    inputs.add(input);
                }
            }
        }

        for (AddVlanMapInput input : inputs) {
            AddVlanMapOutput output = new AddVlanMapOutputBuilder()
                    .setActive(true).setMapId("1").build();

            reset(vlanMapService);
            when(vlanMapService.addVlanMap(input)).thenReturn(
                    getRpcFuture(output));
            assertEquals(true, vtnMgr.setVlanMap(input));
            verify(vlanMapService).addVlanMap(input);
        }

        String tname = "vtn_fail";
        String bname = "vbr_fail";
        VlanId vlan = new VlanId(123);
        AddVlanMapInput input = new AddVlanMapInputBuilder()
                .setBridgeName(bname).setTenantName(tname).setVlanId(vlan)
                .build();

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<AddVlanMapOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(vlanMapService);
        when(vlanMapService.addVlanMap(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setVlanMap(input));
        verify(vlanMapService).addVlanMap(input);
        // Future returns null.
        RpcResult<AddVlanMapOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(vlanMapService);
        when(vlanMapService.addVlanMap(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setVlanMap(input));
        verify(vlanMapService).addVlanMap(input);

        // RpcResult does not contain output.
        future = getRpcFuture((AddVlanMapOutput) null);
        reset(vlanMapService);
        when(vlanMapService.addVlanMap(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setVlanMap(input));
        verify(vlanMapService).addVlanMap(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(vlanMapService);
        when(vlanMapService.addVlanMap(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setVlanMap(input));
        verify(vlanMapService).addVlanMap(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(vlanMapService);
        when(vlanMapService.addVlanMap(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setVlanMap(input));
        verify(vlanMapService).addVlanMap(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

    }

    /**
     * Test case for {@link VTNManagerService#removeVlanMap(RemoveVlanMapInput)}
     * .
     */
    @Test
    public void testRemoveVlanMap() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", };
        String[] bnames = { "vbr", "bridge_1", };

        for (String tname : tnames) {
            for (String bname : bnames) {
                RemoveVlanMapInput input = new RemoveVlanMapInputBuilder()
                        .setTenantName(tname).setBridgeName(bname).build();
                RemoveVlanMapOutput output = new RemoveVlanMapOutputBuilder()
                        .build();
                reset(vlanMapService);
                when(vlanMapService.removeVlanMap(input)).thenReturn(
                        getRpcFuture(output));
                assertEquals(true, vtnMgr.removeVlanMap(input));
                verify(vlanMapService).removeVlanMap(input);
            }
        }

        String tname = "vtn_fail";
        String bname = "vbr_fail";
        RemoveVlanMapInput input = new RemoveVlanMapInputBuilder()
                .setTenantName(tname).setBridgeName(bname).build();

        // In case of failure.
        for (VtnErrorTag vtag : VtnErrorTag.values()) {
            Future<RpcResult<RemoveVlanMapOutput>> future = getFailureFuture(
                    vtag, "Test failure");
            reset(vlanMapService);
            when(vlanMapService.removeVlanMap(input)).thenReturn(future);
            assertEquals(false, vtnMgr.removeVlanMap(input));
            verify(vlanMapService).removeVlanMap(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<RemoveVlanMapOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(vlanMapService);
        when(vlanMapService.removeVlanMap(input)).thenReturn(future);
        assertEquals(false, vtnMgr.removeVlanMap(input));
        verify(vlanMapService).removeVlanMap(input);

        // Future returns null.
        RpcResult<RemoveVlanMapOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(vlanMapService);
        when(vlanMapService.removeVlanMap(input)).thenReturn(future);
        assertEquals(false, vtnMgr.removeVlanMap(input));
        verify(vlanMapService).removeVlanMap(input);
    }

    /**
     * Test case for {@link VTNManagerService#setVlanMap(SetFlowConditionInput)}
     * .
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSetFlowCond() throws Exception {
        VTNManagerService vtnMgr = getVTNManagerService();
        VnodeName[] vnodeNameList = { new VnodeName("fcond_1"),
                new VnodeName("fcond_2") };
        String addressSrc = "10.105.123.24";
        String addressDst = "10.105.123.23";
        int ETHER_TYPE = 0x800;
        List<VtnFlowMatch> matchList = new ArrayList<VtnFlowMatch>();
        IpNetwork ipaddrSrc;
        IpNetwork ipaddrDst;
        ipaddrSrc = IpNetwork.create(InetAddress.getByName(addressSrc));
        ipaddrDst = IpNetwork.create(InetAddress.getByName(addressDst));

        VtnInetMatch match = new VtnInetMatchBuilder()
                .setDestinationNetwork(ipaddrDst.getIpPrefix())
                .setSourceNetwork(ipaddrSrc.getIpPrefix())
                .setProtocol((short) 1).setDscp(null).build();
        VtnEtherMatch ethernetMatch = new VtnEtherMatchBuilder()
                .setDestinationAddress(null).setSourceAddress(null)
                .setVlanId(new VlanId(0)).setVlanPcp(null)
                .setEtherType(new EtherType(Long.valueOf(ETHER_TYPE))).build();
        VtnFlowMatch flowMatch = new VtnFlowMatchBuilder().setIndex(1)
                .setVtnEtherMatch(ethernetMatch).setVtnInetMatch(match).build();
        matchList.add(flowMatch);

        // In case of successful completion.
        List<SetFlowConditionInput> inputs = new ArrayList<>();
        for (VnodeName vnodeName : vnodeNameList) {
            SetFlowConditionInput input = new SetFlowConditionInputBuilder()
                    .setName(vnodeName).setVtnFlowMatch(matchList).build();
            inputs.add(input);
        }

        VtnUpdateType[] utypes = { null, VtnUpdateType.CREATED,
                VtnUpdateType.CHANGED, };
        for (SetFlowConditionInput input : inputs) {
            for (VtnUpdateType utype : utypes) {
                SetFlowConditionOutput output = new SetFlowConditionOutputBuilder()
                        .setStatus(utype).build();

                reset(conditionService);
                when(conditionService.setFlowCondition(input)).thenReturn(
                        getRpcFuture(output));
                assertEquals(true, vtnMgr.setFlowCond(input));
                verify(conditionService).setFlowCondition(input);
            }
        }

        SetFlowConditionInput input = new SetFlowConditionInputBuilder()
                .setName(new VnodeName("fcond_12345"))
                .setVtnFlowMatch(matchList).build();

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<SetFlowConditionOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(conditionService);
        when(conditionService.setFlowCondition(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowCond(input));
        verify(conditionService).setFlowCondition(input);
        // Future returns null.
        RpcResult<SetFlowConditionOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(conditionService);
        when(conditionService.setFlowCondition(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowCond(input));
        verify(conditionService).setFlowCondition(input);

        // RpcResult does not contain output.
        future = getRpcFuture((SetFlowConditionOutput) null);
        reset(conditionService);
        when(conditionService.setFlowCondition(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowCond(input));
        verify(conditionService).setFlowCondition(input);

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(conditionService);
        when(conditionService.setFlowCondition(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowCond(input));
        verify(conditionService).setFlowCondition(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(conditionService);
        when(conditionService.setFlowCondition(input)).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowCond(input));
        verify(conditionService).setFlowCondition(input);
        verify(result).isSuccessful();
        verify(result).getErrors();

    }

    /**
     * Test case for {@link VTNManagerService#setFlowFilter(SetFlowFilterInput)}
     * .
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSetFlowFilter() throws Exception {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", };
        String[] bnames = { "vbr", "bridge_1", };
        VnodeName[] vnodeNameList = { new VnodeName("fcond_1"),
                new VnodeName("fcond_2") };
        VtnPassFilterCase pass = new VtnPassFilterCaseBuilder()
                .setVtnPassFilter(new VtnPassFilterBuilder().build()).build();
        VtnDropFilterCase drop = new VtnDropFilterCaseBuilder()
                .setVtnDropFilter(new VtnDropFilterBuilder().build()).build();
        List<VtnFlowFilter> vtnFlowFilterList = new ArrayList<VtnFlowFilter>();
        VtnFlowFilter filter1 = new VtnFlowFilterBuilder().setIndex(1)
                .setCondition(vnodeNameList[0]).setVtnFlowFilterType(pass)
                .build();
        vtnFlowFilterList.add(filter1);
        VtnFlowFilter filter2 = new VtnFlowFilterBuilder().setIndex(2)
                .setCondition(vnodeNameList[1]).setVtnFlowFilterType(drop)
                .build();
        vtnFlowFilterList.add(filter2);

        // In case of successful completion.
        List<SetFlowFilterInput> inputs = new ArrayList<>();
        for (String tname : tnames) {
            for (String bname : bnames) {
                SetFlowFilterInput input = new SetFlowFilterInputBuilder()
                        .setTenantName(tname).setBridgeName(bname)
                        .setVtnFlowFilter(vtnFlowFilterList).build();
                inputs.add(input);
            }
        }

        for (SetFlowFilterInput input : inputs) {
            SetFlowFilterOutput output = new SetFlowFilterOutputBuilder()
                    .build();

            reset(flowFilterService);
            when(flowFilterService.setFlowFilter(input)).thenReturn(
                    getRpcFuture(output));
            assertEquals(true, vtnMgr.setFlowFilter(input));
            verify(flowFilterService).setFlowFilter(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<SetFlowFilterOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(flowFilterService);
        when(flowFilterService.setFlowFilter(inputs.get(0))).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowFilter(inputs.get(0)));
        verify(flowFilterService).setFlowFilter(inputs.get(0));
        // Future returns null.
        RpcResult<SetFlowFilterOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(flowFilterService);
        when(flowFilterService.setFlowFilter(inputs.get(0))).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowFilter(inputs.get(0)));
        verify(flowFilterService).setFlowFilter(inputs.get(0));

        // RpcResult does not contain output.
        future = getRpcFuture((SetFlowFilterOutput) null);
        reset(flowFilterService);
        when(flowFilterService.setFlowFilter(inputs.get(0))).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowFilter(inputs.get(0)));
        verify(flowFilterService).setFlowFilter(inputs.get(0));

        // No RpcError in RpcResult.
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(null);
        future = Futures.immediateFuture(result);
        reset(flowFilterService);
        when(flowFilterService.setFlowFilter(inputs.get(0))).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowFilter(inputs.get(0)));
        verify(flowFilterService).setFlowFilter(inputs.get(0));
        verify(result).isSuccessful();
        verify(result).getErrors();

        Collection<RpcError> errors = Collections.<RpcError> emptySet();
        result = mock(RpcResult.class);
        when(result.isSuccessful()).thenReturn(false);
        when(result.getErrors()).thenReturn(errors);
        future = Futures.immediateFuture(result);
        reset(flowFilterService);
        when(flowFilterService.setFlowFilter(inputs.get(0))).thenReturn(future);
        assertEquals(false, vtnMgr.setFlowFilter(inputs.get(0)));
        verify(flowFilterService).setFlowFilter(inputs.get(0));
        verify(result).isSuccessful();
        verify(result).getErrors();

    }

    /**
     * Test case for
     * {@link VTNManagerService#unSetFlowFilter(RemoveFlowFilterInput)}.
     */
    @Test
    public void testunSetFlowFilter() {
        VTNManagerService vtnMgr = getVTNManagerService();
        String[] tnames = { "vtn", "vtn_1", };
        String[] bnames = { "vbr", "bridge_1", };

        ArrayList<Integer> indexList = new ArrayList<Integer>();
        indexList.add(1);
        for (String tname : tnames) {
            for (String bname : bnames) {

                RemoveFlowFilterInput input = new RemoveFlowFilterInputBuilder()
                        .setTenantName(tname).setBridgeName(bname)
                        .setIndices(indexList).build();
                RemoveFlowFilterOutput output = new RemoveFlowFilterOutputBuilder()
                        .build();
                reset(flowFilterService);
                when(flowFilterService.removeFlowFilter(input)).thenReturn(
                        getRpcFuture(output));
                assertEquals(true, vtnMgr.unSetFlowFilter(input));
                verify(flowFilterService).removeFlowFilter(input);
            }
        }

        String tname = "vtn_fail";
        String bname = "vbr_fail";
        RemoveFlowFilterInput input = new RemoveFlowFilterInputBuilder()
                .setTenantName(tname).setBridgeName(bname)
                .setIndices(indexList).build();
        // In case of failure.
        for (VtnErrorTag vtag : VtnErrorTag.values()) {
            Future<RpcResult<RemoveFlowFilterOutput>> future = getFailureFuture(
                    vtag, "Test failure");
            reset(flowFilterService);
            when(flowFilterService.removeFlowFilter(input)).thenReturn(future);
            assertEquals(false, vtnMgr.unSetFlowFilter(input));
            verify(flowFilterService).removeFlowFilter(input);
        }

        // Future throws an exception.
        IllegalStateException ise = new IllegalStateException("Unexpected");
        Future<RpcResult<RemoveFlowFilterOutput>> future = Futures
                .immediateFailedFuture(ise);
        reset(flowFilterService);
        when(flowFilterService.removeFlowFilter(input)).thenReturn(future);
        assertEquals(false, vtnMgr.unSetFlowFilter(input));
        verify(flowFilterService).removeFlowFilter(input);

        // Future returns null.
        RpcResult<RemoveFlowFilterOutput> result = null;
        future = Futures.immediateFuture(result);
        reset(flowFilterService);
        when(flowFilterService.removeFlowFilter(input)).thenReturn(future);
        assertEquals(false, vtnMgr.unSetFlowFilter(input));
        verify(flowFilterService).removeFlowFilter(input);
    }

    /**
     * Return a future that contains RPC result that indicates failure.
     *
     * @param vtag
     *            A {@link VtnErrorTag} instance.
     * @param msg
     *            An error message.
     * @param <O>
     *            The type of the RPC output;
     * @return A future that contains the RPC result.
     */
    private <O> Future<RpcResult<O>> getFailureFuture(VtnErrorTag vtag,
            String msg) {
        RpcResult<O> result = RpcResultBuilder
                .<O> failed()
                .withError(ErrorType.APPLICATION, "operation-failed", msg,
                        String.valueOf(vtag), null, null).build();
        return Futures.immediateFuture(result);
    }

    /**
     * Create a response of read request on a MD-SAL transaction.
     *
     * @param obj
     *            An object to be read. {@code null} implies the target data is
     *            not present.
     * @return A {@link CheckedFuture} instance.
     * @param <T>
     *            The type of the data object.
     */
    protected static <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> getReadResult(
            T obj) {
        Optional<T> opt = Optional.fromNullable(obj);
        return Futures.immediateCheckedFuture(opt);
    }

    /**
     * Create an error response of read request on a MD-SAL transaction.
     *
     * @param type
     *            A class which indicates the type of the return value.
     * @param cause
     *            A throwable which indicates the cause of error.
     * @return A {@link CheckedFuture} instance.
     * @param <T>
     *            The type of the return value.
     */
    protected static <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> getReadFailure(
            Class<T> type, Throwable cause) {
        String msg = "DS read failed";
        RpcError err = RpcResultBuilder.newError(ErrorType.APPLICATION,
                "failed", msg, null, null, cause);
        ReadFailedException rfe = new ReadFailedException(msg, cause, err);
        return Futures.immediateFailedCheckedFuture(rfe);
    }

    /**
     * Create a timeout error response of read request on a MD-SAL transaction.
     *
     * @param type
     *            A class which indicates the type of the return value.
     * @return A {@link CheckedFuture} instance.
     * @param <T>
     *            The type of the return value.
     * @throws Exception
     *             An error occurred.
     */
    protected static <T extends DataObject> CheckedFuture<Optional<T>, ReadFailedException> getReadTimeoutFailure(
            Class<T> type) throws Exception {
        @SuppressWarnings("unchecked")
        CheckedFuture<Optional<T>, ReadFailedException> future = mock(CheckedFuture.class);

        when(future.cancel(anyBoolean())).thenReturn(false);
        when(future.isCancelled()).thenReturn(false);
        when(future.isDone()).thenReturn(false);

        when(future.get()).thenThrow(
                new AssertionError("get() should never be called."));
        when(future.get(anyLong(), any(TimeUnit.class))).thenThrow(
                new TimeoutException("DS read timed out"));
        when(future.checkedGet(anyLong(), any(TimeUnit.class))).thenThrow(
                new TimeoutException("DS read timed out"));

        return future;
    }

    /**
     * Return a future that contains RPC result that indicates successful
     * completion.
     *
     * @param output
     *            The output of the RPC.
     * @param <O>
     *            The type of the RPC output.
     * @return A future that contains the RPC result.
     */
    protected static <O> Future<RpcResult<O>> getRpcFuture(O output) {
        RpcResult<O> result = RpcResultBuilder.success(output).build();
        return Futures.immediateFuture(result);
    }
}
