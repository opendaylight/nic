/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.action.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.sfc.provider.SfcProviderRpc;
import org.opendaylight.sfc.provider.api.SfcProviderServiceChainAPI;
import org.opendaylight.sfc.provider.api.SfcProviderServicePathAPI;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.common.rev151017.SfcName;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.ReadRenderedServicePathFirstHopInputBuilder;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.ReadRenderedServicePathFirstHopOutput;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.rsp.rev140701.rendered.service.path.first.hop.info.RenderedServicePathFirstHop;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfc.rev140701.service.function.chain.grouping.ServiceFunctionChain;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.ServiceFunctionPaths;
import org.opendaylight.yang.gen.v1.urn.cisco.params.xml.ns.yang.sfc.sfp.rev140701.service.function.paths.ServiceFunctionPath;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * Manage the state exchanged with SFC
 *
 * For the Proof of Concept, this manages the
 * RenderedServicePathFirstHop elements that
 * are retrieved from SFC.
 *
 */
public class ActionProvider implements BindingAwareProvider, AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(ActionProvider.class);
    private final DataBroker dataBroker;
    private final ExecutorService executor;
    private final InstanceIdentifier<Intents> allActionInstancesIid;
    private final ListenerRegistration<DataChangeListener> actionListener;

    /*
     * local cache of the RSP first hops that we've requested from SFC,
     * keyed by RSP name
     */
    private final ConcurrentMap<String, RenderedServicePathFirstHop> rspMap;

    /*
     *  TODO: these two String definitions should move to the common
     *        "chain" action, once we have it.
     */
    // the chain action
    public static final String SFC_CHAIN_ACTION = "chain";
    // the parameter used for storing the chain name
    public static final String SFC_CHAIN_NAME = "sfc-chain-name";
    // To-Do::Need to replace the below action instance variables based on nic requests
    List<String> dummyActionInstance = null;
    List<String> dummyOrgActionInstance = null;
    private static enum ActionState {
        ADD("add"),
        CHANGE("change"),
        DELETE("delete");
        private String state;
        ActionState(String state) {
            this.state = state;
        }
        @Override
        public String toString() {
            return this.state;
        }
    }

    public ActionProvider(DataBroker dataBroker, ExecutorService executor) {
        this.dataBroker = dataBroker;
        this.executor = executor;
        /*
         * Use thread-safe type only because we use an executor
         */
         this.rspMap = new ConcurrentHashMap<String, RenderedServicePathFirstHop>();
        /*
         * For now, listen to all changes in rules
         */
         allActionInstancesIid = InstanceIdentifier.builder(Intents.class).build();
         actionListener = dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
         allActionInstancesIid, this, DataChangeScope.ONE);
         LOG.debug("ActionProvider: Started");
    }

    public Set<IpAddress> getSfcSourceIps() {
        if (rspMap.isEmpty()) return null;

        Set<IpAddress> ipAddresses = new HashSet<IpAddress>();
        for (RenderedServicePathFirstHop rsp: rspMap.values()) {
            if (rsp.getIp() != null) {
                ipAddresses.add(rsp.getIp());
            }
        }
        if (ipAddresses.isEmpty()) return null;
        return ipAddresses;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("ActionProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("ActionProvider Closed");
        if (actionListener != null) {
            actionListener.close();
        }
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> actionInstanceNotification) {
        LOG.info("Action configuration changed.");

        for (DataObject dao : actionInstanceNotification.getCreatedData().values()) {
            //create new action
            executor.execute(new MatchActionDefTask(ActionState.ADD));
            LOG.debug("New ActionInstance created");
        }

        for (InstanceIdentifier<?> iid : actionInstanceNotification.getRemovedPaths()) {
            DataObject old = actionInstanceNotification.getOriginalData().get(iid);
            executor.execute(new MatchActionDefTask(ActionState.DELETE));
            LOG.debug("ActionInstance deleted");
        }

        for (Entry<InstanceIdentifier<?>, DataObject> entry:
            actionInstanceNotification.getUpdatedData().entrySet()) {
            DataObject dao = entry.getValue();
             executor.execute(new MatchActionDefTask(ActionState.CHANGE));
             LOG.debug("ActionInstance updated");
            }
        }

    /**
     * Private internal class that gets the action definition
     * referenced by the instance. If the definition has an
     * action of "chain" (or whatever we decide to use
     * here), then we need to invoke the SFC API to go
     * get the chain information, which we'll eventually
     * use during policy resolution.
     *
     */
    private class MatchActionDefTask implements Runnable, FutureCallback<Optional<?>> {
        private final ActionState state;
        private final InstanceIdentifier<Intents> adIid;
        private List<String> dumActionInstance  = null;
        private List<String> dumOrgActionInstance  = null;

        public MatchActionDefTask(ActionState state) {
            //Initialize actual, original action instance and action state
            this.state = state;
            adIid = InstanceIdentifier.builder(Intents.class).build();
        }

        /**
         * Create read transaction with callback to look up
         * the Action Definition that the Action Instance
         * references.
         */
        @Override
        public void run() {
            ReadOnlyTransaction rot = dataBroker.newReadOnlyTransaction();
            CheckedFuture dao = rot.read(LogicalDatastoreType.OPERATIONAL, adIid);
            Futures.addCallback(dao, this, executor);
        }

        @Override
        public void onFailure(Throwable arg0) {
            LOG.error("Failure reading ? {}");
        }

        /**
         * An Action Definition exists - now we need to see
         * if the Action Definition is for a chain action,
         * and implement the appropriate behavior. If it's
         * not a chain action, then we can ignore it.
         *
         * @param dao
         */
        @Override
        public void onSuccess(Optional<?> dao) {
            LOG.debug("Found ActionDefinition {}");
            if (!dao.isPresent()) return;
            //To-Do::Check Action definition id with chain Action ID
                switch (state) {
                case ADD:
                    /*
                     * Go get the RSP First Hop
                     */
                    getSfcChain();
                    break;
                case CHANGE:
                    /*
                     * We only care if the named chain changes
                     */
                    changeSfcRsp();
                    break;
                case DELETE:
                    /*
                     * If the instance is deleted, we need to remove
                     * it from our map.
                     */
                    deleteSfcRsp();
                    break;
                default:
                    break;
                }
        }

        private String getChainNameParameter(List<String> pvl) {
            if (pvl == null) return null;
            for (String pv: dummyActionInstance) {
                if (pv.equals(SFC_CHAIN_NAME)) {
                    return pv;
                }
            }
            return null;
        }

        private void changeSfcRsp() {
            String newPv = getChainNameParameter(dumActionInstance);
            String origPv = getChainNameParameter(dumOrgActionInstance);
            if (!newPv.equals(origPv)) {
                if (rspMap.containsKey(origPv)) {
                    rspMap.remove(origPv);
                }
                addSfcRsp();
            }
        }

        private void deleteSfcRsp() {
            String pv = getChainNameParameter(dumOrgActionInstance);
            if (pv == null) return;
            rspMap.remove(pv);
        }

        /**
         * Get the RenderedServicePathFirstHop from SFC
         *
         * TODO: what if SFC state isn't available at the time of
         *       this call, but becomes available later?  Do we want
         *       or need some sort of notification handler for this?
         */
        private void addSfcRsp() {
            String pv = getChainNameParameter(dumActionInstance);
            if (pv == null) return;

            LOG.trace("Invoking RPC for chain {}", pv);
            ReadRenderedServicePathFirstHopInputBuilder builder = new ReadRenderedServicePathFirstHopInputBuilder().setName(pv);
            Future<RpcResult<ReadRenderedServicePathFirstHopOutput>> result =
                SfcProviderRpc.getSfcProviderRpc().readRenderedServicePathFirstHop(builder.build());

            try {
                RpcResult<ReadRenderedServicePathFirstHopOutput> output = result.get();
                if (output.isSuccessful()) {
                    LOG.trace("RPC for chain {} succeeded!", pv);
                    RenderedServicePathFirstHop rspFirstHop = output.getResult().getRenderedServicePathFirstHop();
                    rspMap.putIfAbsent(pv, rspFirstHop);
                }
            } catch (Exception e) {
                LOG.warn("Failed ReadRenderedServicePathFirstHop RPC: {}", e);
            }
        }

        private void getSfcChain() {
            String pv = getChainNameParameter(dumActionInstance);
            if (pv == null) return;
            LOG.trace("Invoking RPC for chain {}", pv);
            SfcName chainName=new SfcName(pv);
            ServiceFunctionChain chain = SfcProviderServiceChainAPI.readServiceFunctionChain(chainName);
            ServiceFunctionPaths paths = SfcProviderServicePathAPI.readAllServiceFunctionPaths();
            for(ServiceFunctionPath path: paths.getServiceFunctionPath()) {
                if(path.getServiceChainName().equals(chainName)) {
                    LOG.info("Found path {} for chain {}",path.getName(),path.getServiceChainName());
                }
            }
        }

        /**
         * Return the first hop information for the Rendered Service Path
         *
         * @param rspName the Rendered Service Path
         * @return the first hop information for the Rendered Service Path
         */
        public RenderedServicePathFirstHop getRspFirstHop(String rspName) {
            return rspMap.get(rspName);
        }
      }
}
