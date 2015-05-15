/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.List;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.lang.String;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.allow.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.block.Block;

public class VTNRenderer implements AutoCloseable, DataChangeListener {
    /**
     * Logger instance.
     */
    private static final Logger LOG = LoggerFactory
            .getLogger(VTNRenderer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> ev) {
        // TODO: Change the log level to TRACE.
        LOG.info("Intent configuration changed.");

        for (DataObject dao : ev.getCreatedData().values()) {
            LOG.info("Data change For Loop ENTER:::");
            if (dao instanceof Intents) {
                LOG.info("FOR LOOP enter Intents :::");
                try {
                    Intents lcl_iB = (Intents)dao;
                    List<Intent> lcl_intent = lcl_iB.getIntent();
                    for(Intent intent : lcl_intent) {
                        LOG.info("intents iteration :{} ",intent.getId());
                        intentParser(intent);
                    }
                } catch (Exception e) {
                    LOG.error("Could not create VTN Renderer", e);
                }
            }
        }

        for (DataObject dao : ev.getUpdatedData().values()) {
            LOG.info("Updated data change ");
            if (dao instanceof Intents) {
                LOG.info("FOR LOOP enter Intents :::");
                try {
                    Intents lcl_iB = (Intents)dao;
                    List<Intent> lcl_intent = lcl_iB.getIntent();
                    for(Intent intent : lcl_intent) {
                        LOG.info("intents iteration :{} ",intent.getId());
                        intentParser(intent);
                    }
                } catch (Exception e) {
                    LOG.error("Could not update VTN Renderer", e);
                }
            }
        }

        /**
         * Delete the following lines, and implements the renderer.
         *
         * try { List<VTenant> vtns = mgr.getTenants(); for (VTenant vtn: vtns)
         * { LOG.info("{}", vtn); } } catch (Exception e) {
         * LOG.info("Failed to get tenants: {}", e); }
         */
    }

    /**
     * This method parse the intent and calls the VTN renderer
     *
     * @param intents
     */
    public void intentParser(Intent intent) {
        String endPointSrc = "";
        String endPointDst = "";
        HashMap hashMap = new HashMap<String, ArrayList<IntentWrapper>>();
        ArrayList<IntentWrapper> arrayList = new ArrayList<IntentWrapper>();
        ArrayList<String> subject = new ArrayList<String>();
        String intentID = "";
        VTNIntentParser renderer = new VTNIntentParser();
        LOG.info(":::Intent Parser :::");
        //if (intent.getStatus() != null) {

        for(Subjects subjects : intent.getSubjects()) {
            EndPointGroup endPointGroup = (EndPointGroup)subjects.getSubject();
            LOG.info("intents iteration Subjects:{} ",endPointGroup.getEndPointGroup().getName());
            subject.add(endPointGroup.getEndPointGroup().getName());
        }

        //if (intent.getSubjects().size() == 2) {
        LOG.info(":::Intent Subjects :::");
        endPointSrc = subject.get(0).toString();
        endPointDst = subject.get(1).toString();
        if (intent.getActions() != null) {
            LOG.info(":::Intent Actions :::{}",intent.getActions());

            try {
                //action = intent.getActions().get(0);

                for(Actions actions : intent.getActions()) {
                    Action action = actions.getAction();
                    if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow) {
                        Allow allow = ((org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow) action).getAllow();
                        LOG.info(":::Intent Actions :::{}",allow);
                        if ( allow != null) {
                            renderer.rendering(endPointSrc, endPointDst, "allow", arrayList);
                        }
                    } else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block) {
                        Block block =((org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block)action).getBlock();
                        LOG.info(":::Intent Actions :::{}",block);
                        if ( block != null) {
                            renderer.rendering(endPointSrc, endPointDst, "block", arrayList);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //}

        hashMap.put(intentID, arrayList);
        VTNRendererUtility.storeIntentDetail(hashMap);
        //}
    }
}
