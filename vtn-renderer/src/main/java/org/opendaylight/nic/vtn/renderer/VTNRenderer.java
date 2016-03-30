/**
 * Copyright (c) 2015, 2016 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

/**
 * The VTNRenderer class parse the intents received.
 */
public class VTNRenderer implements BindingAwareProvider, AutoCloseable ,DataChangeListener {
    private static final Logger LOG = LoggerFactory
            .getLogger(VTNRenderer.class);

    private VTNIntentParser vtnIntentParser;

    private VTNRendererUtility vtnRendererUtility;

    /**
     * The index of the source end point group in an intent.
     */
    private static final int  INDEX_OF_SRC_END_POINT_GROUP = 0;

    /**
     * The index of the destination end point group in an intent.
     */
    private static final int  INDEX_OF_DST_END_POINT_GROUP = 1;

    private DataBroker dataProvider;

    private ListenerRegistration<DataChangeListener> vtnRendererListener = null;

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();

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
     * Method invoked by Factory class.
     * @param session the session object
     */
    @Override
    public void onSessionInitiated(ProviderContext session) {

        this.dataProvider = session.getSALService(DataBroker.class);
        MdsalUtils md = new MdsalUtils(this.dataProvider);
        VTNManagerService vtn = new VTNManagerService(md, session);
        this.vtnIntentParser = new VTNIntentParser(dataProvider , vtn);

        this.vtnRendererUtility = new VTNRendererUtility(dataProvider);
        vtnRendererListener = dataProvider.registerDataChangeListener(
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
                Intent intent = (Intent) dao;
                LOG.trace("A new intent is created: {}", intent.getId());
                intentParser(intent);
            }
        }
        for (DataObject dao: ev.getUpdatedData().values()) {
            LOG.trace("A data object is updated: {}", dao);
            if (dao instanceof Intent) {
                Intent intent = (Intent) dao;
                LOG.trace("An intent is updated: {}", intent.getId());
                intentParser(intent);
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
                LOG.error("Could not delete VTN Renderer :{} ", e);
            }
        }
    }

    /**
     * This method parse the intent and calls the VTN renderer
     *
     */
    private void intentParser(Intent intent) {
        if (!IntentUtils.verifyIntent(intent)) {
            return;
        }
        // Retrieve the Intent ID.
        String intentID = intent.getId().getValue();
        // Retrieve the end points.
        final List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        String endPointSrc = endPointGroups.get(INDEX_OF_SRC_END_POINT_GROUP);
        String endPointDst = endPointGroups.get(INDEX_OF_DST_END_POINT_GROUP);
        // Get the type of the action.
        String actionType = getAction(intent);
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
     * To get the action from the intent.
     *
     * @return action type.
     */
    private String getAction(Intent intent) {
        Action action = intent.getActions().get(0).getAction();
        String actionType = null;
        if (action instanceof Allow) {
            actionType = "allow";
        } else if (action instanceof Block) {
            actionType = "block";
        } else {
            throw new IntentElementNotFoundException("VTN Renderer supports only allow or block: {}" + intent.getId());
        }
        return actionType;
    }
}
