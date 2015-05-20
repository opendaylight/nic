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
import java.util.Map;
import java.util.Set;
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

import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.allow.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.block.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;


public class VTNRenderer implements AutoCloseable, DataChangeListener {
    /**
     * Logger instance.
     */
    private static final Logger LOG = LoggerFactory
            .getLogger(VTNRenderer.class);

    VTNIntentParser renderer = new VTNIntentParser();

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

        Map<InstanceIdentifier<?>, DataObject> originalDataObject = ev.getOriginalData();
        Set<InstanceIdentifier<?>> iiD = ev.getRemovedPaths();
        for (InstanceIdentifier instanceIdentifier : iiD) {
            LOG.info(" Data change was removed ");
            try {
                if (originalDataObject.get(instanceIdentifier) instanceof Intent) {
                    Intent lcl_intent = (Intent) originalDataObject.get(instanceIdentifier);
                    IntentKey lcl_intentKey = (IntentKey) lcl_intent.getKey();
                    Uuid uuid = (Uuid) lcl_intentKey.getId();
                    LOG.info(" Data change was removed with intent :{} " ,uuid.getValue());
                    renderer.delete(uuid.getValue());
                }
            } catch (Exception e) {
                LOG.error("Could not update VTN Renderer :{} ", e);
            }
       }

    }

    /**
     * This method parse the intent and calls the VTN renderer
     *
     * @param intents
     */
    public void intentParser(Intent intent) {
        String endPointSrc = "";
        String endPointDst = "";
        Map intentMap = new HashMap<String, List<IntentWrapper>>();
        List<IntentWrapper> intentList = new ArrayList<IntentWrapper>();
        List<String> subject = new ArrayList<String>();
        if (intent.getId() == null){
            return;
        }
        Uuid uuid = intent.getId();
        String intentID = uuid.getValue();

        for(Subjects subjects : intent.getSubjects()) {
            EndPointGroup endPointGroup = (EndPointGroup)subjects.getSubject();
            LOG.info("intents iteration Subjects:{} ",endPointGroup.getEndPointGroup().getName());
            subject.add(endPointGroup.getEndPointGroup().getName());
        }

        LOG.info(":::Intent Subjects :::");
        endPointSrc = subject.get(0).toString();
        endPointDst = subject.get(1).toString();
        if (intent.getActions() != null) {
            LOG.info(":::Intent Actions :::{}",intent.getActions());

            try {

                for(Actions actions : intent.getActions()) {
                    Action action = actions.getAction();
                    if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow) {
                        Allow allow = ((org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow) action).getAllow();
                        LOG.info(":::Intent Actions :::{}",allow);
                        if ( allow != null) {
                            renderer.rendering(endPointSrc, endPointDst, "allow", intentList);
                        }
                    } else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block) {
                        Block block =((org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block)action).getBlock();
                        LOG.info(":::Intent Actions :::{}",block);
                        if ( block != null) {
                            renderer.rendering(endPointSrc, endPointDst, "block", intentList);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Could not parse the intent action :{} ", e);
            }
        }

        intentMap.put(intentID, intentList);
        VTNRendererUtility.storeIntentDetail(intentMap);

    }
}
