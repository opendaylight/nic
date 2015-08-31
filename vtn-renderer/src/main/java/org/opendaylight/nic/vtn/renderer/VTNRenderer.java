/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * The VTNRenderer class parse the intents received.
 */
public class VTNRenderer implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory
            .getLogger(VTNRenderer.class);

    private VTNIntentParser vtnIntentParser;

    private VTNRendererUtility vtnRendererUtility;

    /**
     * The number of supported actions by VTN Renderer.
     */
    private static final int  NUM_OF_SUPPORTED_ACTIONS = 1;

    /**
     * The number of supported end point groups by VTN Renderer.
     */
    private static final int  NUM_OF_SUPPORTED_EPG = 2;

    /**
     * The index of the source end point group in an intent.
     */
    private static final int  INDEX_OF_SRC_END_POINT_GROUP = 0;

    /**
     * The index of the destination end point group in an intent.
     */
    private static final int  INDEX_OF_DST_END_POINT_GROUP = 1;

    private DataBroker dataProvider;

    public VTNRenderer(DataBroker dataBroker) {
        this.dataProvider = dataBroker;
        this.vtnRendererUtility = new VTNRendererUtility(dataProvider);
        this.vtnIntentParser = new VTNIntentParser(dataProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
    }

    /**
     * This method is called on intent data requests.
     */
    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> ev) {
        LOG.info("Intent configuration changed.");

        for (DataObject dao: ev.getCreatedData().values()) {
            LOG.trace("A new data object is created: {}", dao);
            if (dao instanceof Intent) {
                Intent intent = (Intent) dao;
                LOG.debug("A new intent is created: {}", intent.getId());
                intentParser(intent);
            }
        }
        for (DataObject dao: ev.getUpdatedData().values()) {
            LOG.trace("A data object is updated: {}", dao);
            if (dao instanceof Intent) {
                Intent intent = (Intent) dao;
                LOG.debug("An intent is updated: {}", intent.getId());
                intentParser(intent);
            }
        }
        Map<InstanceIdentifier<?>, DataObject> originalDataObject = ev.getOriginalData();
        Set<InstanceIdentifier<?>> iiD = ev.getRemovedPaths();
        for (InstanceIdentifier<?> instanceIdentifier : iiD) {
            try {
                if (originalDataObject.get(instanceIdentifier) instanceof Intent) {
                    Intent lclIntent = (Intent) originalDataObject.get(instanceIdentifier);
                    IntentKey lclIntentKey = (IntentKey) lclIntent.getKey();
                    Uuid uuid = (Uuid) lclIntentKey.getId();
                    LOG.trace(" Intent Deleted :{} " ,uuid.getValue());
                    String encodeUUID = vtnRendererUtility.encodeUUID(uuid.getValue());
                    vtnIntentParser.delete(encodeUUID);
                }
            } catch (Exception e) {
                LOG.error("Could not delete VTN Renderer :{} ", e);
            }
        }
    }

    /**
     * This method parse the intent and calls the VTN renderer
     *
     * @param intent
     */
    private void intentParser(Intent intent) {
        // Retrieve the ID.
        Uuid uuid = intent.getId();
        if (uuid == null) {
            LOG.error("Intent ID is not specified: {}", intent);
            return;
        }
        String intentID = uuid.getValue();
        // Retrieve the end points.
        final List<Subjects> listSubjects = intent.getSubjects();
        if (listSubjects == null) {
            LOG.info("Subjects are not specified: {}", intentID);
            return;
        } else if (listSubjects.size() != NUM_OF_SUPPORTED_EPG) {
            LOG.warn("VTN Renderer supports only two end point groups per Intent: {}", intentID);
            return;
        }
        List<String> endPointGroups = new ArrayList<String>();
        for (Subjects subjects: listSubjects) {
            Subject subject = subjects.getSubject();
            if (!(subject instanceof EndPointGroup)) {
                LOG.info("Subject is not specified: {}", intentID);
                return;
            }
            EndPointGroup endPointGroup = (EndPointGroup)subject;

            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group
                .EndPointGroup epg = endPointGroup.getEndPointGroup();
            if (epg == null) {
                LOG.info("End Point Group is not specified: {}", intentID);
                return;
            }
            endPointGroups.add(epg.getName());
        }
        String endPointSrc = endPointGroups.get(INDEX_OF_SRC_END_POINT_GROUP);
        String endPointDst = endPointGroups.get(INDEX_OF_DST_END_POINT_GROUP);
        // Retrieve the action.
        final List<Actions> listActions = intent.getActions();
        if (listActions == null) {
            LOG.info("Actions are not specified: {}", intentID);
            return;
        } else if (listActions.size() != NUM_OF_SUPPORTED_ACTIONS) {
            LOG.warn("VTN Renderer supports only one action per Intent: {}", intentID);
            return;
        }
        Action action = listActions.get(0).getAction();
        // Get the type of the action.
        String actionType;
        if (action instanceof Allow) {
            actionType = "allow";
        } else if (action instanceof Block) {
            actionType = "block";
        } else {
            LOG.warn("VTN Renderer supports only allow or block: {}", intentID);
            return;
        }
        // get the encode UUID value
        String encodeUUID = vtnRendererUtility.encodeUUID(intentID);
        // Convert the intent to VTN configuration.
        if (hasRendered(encodeUUID)) {
            vtnIntentParser.updateRendering(endPointSrc, endPointDst, actionType, intentID, encodeUUID, intent);
        } else {
            vtnIntentParser.rendering(endPointSrc, endPointDst, actionType, encodeUUID, intent);
        }
    }

    /**
     * Return {@code true} if it has already rendered the specified intent.
     *
     * @param intentId  The ID of the intent
     */
    private boolean hasRendered(String intentId) {
        return vtnIntentParser.containsIntentID(intentId);
    }
}
