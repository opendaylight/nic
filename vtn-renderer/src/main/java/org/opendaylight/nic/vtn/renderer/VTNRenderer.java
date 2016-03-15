/**
 * Copyright (c) 2015, 2016 NEC Corporation and others.  All rights reserved.
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

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VTNRenderer class parse the intents received.
 */
public class VTNRenderer implements BindingAwareProvider, AutoCloseable ,DataChangeListener {
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

    private ListenerRegistration<DataChangeListener> vtnRendererListener = null;

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();

    private Intent intent;

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        LOG.trace("VTNRendererListener closed.");
        if (vtnRendererListener != null) {
            vtnRendererListener.close();
        }
    }

    /**
     * Gets called on start of a bundle.
     * @param session the session object
     */
    @Override
    public void onSessionInitiated(final ProviderContext session) {

        final DataBroker dataBroker = session.getSALService(DataBroker.class);
        final MdsalUtils md = new MdsalUtils(dataBroker);
        final VTNManagerService vtn = new VTNManagerService(md, session);
        this.vtnIntentParser = new VTNIntentParser(dataBroker , vtn);

        this.vtnRendererUtility = new VTNRendererUtility(dataBroker);
        vtnRendererListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION,
                INTENTS_IID, this, DataChangeScope.SUBTREE);
    }

    /**
     * This method is called on intent data requests.
     */
    @Override
    public void onDataChanged(
            final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> ev) {
        LOG.trace("Intent configuration changed.");

        for (DataObject dao: ev.getCreatedData().values()) {
            LOG.trace("A new data object is created: {}", dao);
            if (dao instanceof Intent) {
                intent = (Intent) dao;
                LOG.trace("A new intent is created: {}", intent.getId());
                intentParser();
            }
        }
        for (DataObject dao: ev.getUpdatedData().values()) {
            LOG.trace("A data object is updated: {}", dao);
            if (dao instanceof Intent) {
                intent = (Intent) dao;
                LOG.trace("An intent is updated: {}", intent.getId());
                intentParser();
            }
        }
        Map<InstanceIdentifier<?>, DataObject> originalDataObject = ev.getOriginalData();
        Set<InstanceIdentifier<?>> iiD = ev.getRemovedPaths();
        for (InstanceIdentifier<?> instanceIdentifier : iiD) {
            try {
                if (originalDataObject.get(instanceIdentifier) instanceof Intent) {
                    Intent lclIntent = (Intent) originalDataObject.get(instanceIdentifier);
                    IntentKey lclIntentKey = lclIntent.getKey();
                    Uuid uuid = lclIntentKey.getId();
                    LOG.trace(" Intent Deleted :{} ", uuid.getValue());
                    String encodeUUID = vtnRendererUtility.encodeUUID(uuid.getValue());
                    vtnIntentParser.delFlowCondFilter(encodeUUID);
                    if (!vtnRendererUtility.deleteIntent(lclIntent)) {
                        LOG.error("Intent data's are not deleted from operational data store", uuid.getValue());
                        return;
                    }
                    LOG.trace("Intent data's are successfully deleted from operational data store", uuid.getValue());
                }
            } catch (Exception e) {
                LOG.error("Could not delete VTN Renderer :{}", e);
            }
        }
    }

    /**
     * This method parse the intent and calls the VTN renderer
     *
     */
    private void intentParser() {
        if (!verifyIntent()) {
            return;
        }
        // Retrieve the Intent ID.
        String intentID = intent.getId().getValue();
        // Retrieve the end points.
        final List<String> endPointGroups = getEPGs();
        String endPointSrc = endPointGroups.get(INDEX_OF_SRC_END_POINT_GROUP);
        String endPointDst = endPointGroups.get(INDEX_OF_DST_END_POINT_GROUP);
        // Get the type of the action.
        String actionType = getAction();
        // get the encode UUID value
        String encodeUUID = vtnRendererUtility.encodeUUID(intentID);
        Status intentStatus = Status.CompletedError;
        // Convert the intent to VTN configuration.
        if (vtnIntentParser.containsIntentID(encodeUUID)) {
            intentStatus = vtnIntentParser.updateRendering(endPointSrc, endPointDst, actionType,
                intentID, encodeUUID, intent);
        } else {
            intentStatus = vtnIntentParser.rendering(endPointSrc, endPointDst, actionType, encodeUUID, intent);
        }
        LOG.trace("intent status: intentID={}, intentStatus={}", intentID, intentStatus);
        vtnRendererUtility.addIntent(intent, intentStatus);
    }

    /**
     * Validates the intent data's
     *
     * @return {@code = true} on valid intent.
     */
    private boolean verifyIntent() {
        if (intent.getId() == null) {
            LOG.warn("Intent ID is not specified {}", intent);
            return false;
        }
        if (intent.getActions() == null || intent.getActions().size() > NUM_OF_SUPPORTED_ACTIONS) {
            LOG.warn("Intent's action is either null or there is more than {} action {}", NUM_OF_SUPPORTED_ACTIONS,
                    intent);
            return false;
        }
        if (intent.getSubjects() == null || intent.getSubjects().size() > NUM_OF_SUPPORTED_EPG) {
            LOG.warn("Intent's subjects is either null or there is more than {} subjects {}", NUM_OF_SUPPORTED_EPG,
                    intent);
            return false;
        }
        return true;
    }

    /**
     * To get the end point groups from the intent.
     *
     * @return list of end point group's.
     */
    private List<String> getEPGs() {
        final List<Subjects> listSubjects = intent.getSubjects();
        final List<String> endPointGroups = new ArrayList<String>();
        for (Subjects subjects: listSubjects) {
            Subject subject = subjects.getSubject();
            if (!(subject instanceof EndPointGroup)) {
                LOG.trace("Subject is not specified: {}", intent.getId().getValue());
                return null;
            }
            EndPointGroup endPointGroup = (EndPointGroup)subject;
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group
                .EndPointGroup epg = endPointGroup.getEndPointGroup();
            if (epg == null) {
                LOG.trace("End Point Group is not specified: {}", intent.getId().getValue());
                return null;
            }
            endPointGroups.add(epg.getName());
        }
        return endPointGroups;
    }

    /**
     * To get the action from the intent.
     *
     * @return action type.
     */
    private String getAction() {
        final List<Actions> listActions = intent.getActions();
        Action action = listActions.get(0).getAction();
        String actionType = null;
        if (action instanceof Allow) {
            actionType = "allow";
        } else if (action instanceof Block) {
            actionType = "block";
        } else {
            LOG.warn("VTN Renderer supports only allow or block: {}", intent.getId().getValue());
            return null;
        }
        return actionType;
    }
}
