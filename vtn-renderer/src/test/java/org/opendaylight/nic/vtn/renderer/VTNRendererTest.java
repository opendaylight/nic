/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Unit test class for {@link VTNRenderer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class VTNRendererTest extends TestUtils{

    /**
     * Customized userfriendly messages.
     */
     protected static final String CUSTOM_MESSAGE =
            "If unable to update VTN elements due to improper inputs, Intent list should not updated";

    /**
     * Initialize the InstanceIdentifier object with UUID Value.
     */
    InstanceIdentifier<Intent> uuid = InstanceIdentifier.builder(Intents.class)
                .child(Intent.class, new IntentKey(new Uuid(UUID_ONE)))
                .build();
    /**
     * A list of subjects values.
     */
    List<Subjects> subjects;

    /**
     * A list of actions values.
     */
    List<Actions> actions;

    /**
     * A list of multiple subject values.
     */
    List<Subjects>  subjectEpg;

    /**
     * A list of allow action.
     */
    List<Actions> allowAction;

    /**
     * A list of block action.
     */
    List<Actions> blockAction;

    /**
     * A list of single action.
     */
    List<Actions> singleAction;

    /**
     * A list of end point group values.
     */
    List<Subjects> endPointGroup;

    /**
     * A list of empty subject values.
     */
    List<Subjects> emptySubjects;

    /**
     * A list of intentList values.
     */
    List<IntentWrapper> intentList;

    /**
     * MockDataChangedEvent Object for VTNRenderer.
     */
    MockDataChangedEvent dataChangedEvent;

    /**
     * VTNRenderer Object to perform unit testing.
     */
    VTNRenderer vtnRendererObj;

    /**
     * Mock Object to perform unit testing.
     */
    @Mock VTNIntentParser intentParser;

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public  void setUp() {
        subjects = createSubjects();
        actions = createActions();
        subjectEpg = createMultipleSubjects();
        allowAction = createAllowAction();
        blockAction = createBlockAction();
        singleAction = createSingleAction();
        endPointGroup = createEndPointGroups();
        emptySubjects = createSubjectsWithNull();
        dataChangedEvent = new MockDataChangedEvent();
        vtnRendererObj = new VTNRenderer();
        vtnRendererObj.renderer = intentParser;
        intentList = new ArrayList<IntentWrapper>();
    }

    /**
     * This method makes unnecessary objects eligible for garbage collection.
     */
    @After
    public void tearDown() {
        intentList = null;
        vtnRendererObj = null;
        dataChangedEvent = null;
        emptySubjects = null;
        endPointGroup = null;
        singleAction = null;
        blockAction = null;
        allowAction = null;
        subjectEpg = null;
        actions = null;
        subjects = null;
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * for each dataChangedEvent and then checks that Intents will be created for each scenarios.
     */
    @Test
    public void testVtnRendererCreation() {
        int initialSize, newSize;
        VTNRendererUtility.hashMapIntentUtil.clear();
        Intent negCase = new IntentBuilder().build();
        Intent intent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(actions).build();
        Intent invaildSubject = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(null).setActions(null).build();
        Intent invaildAction = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(null).build();
        Intent invaildEpg = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjectEpg).setActions(actions).build();
        Intent allowIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(allowAction).build();
        Intent blockIntent = new IntentBuilder().setId(new Uuid(UUID_THREE)).setSubjects(subjects).setActions(blockAction).build();
        Intent actionIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(singleAction).build();
        Intent epgIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(endPointGroup).setActions(singleAction).build();
        Intent subjectIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(emptySubjects).setActions(singleAction).build();
        /**
         * Verifying intent is created for allow action.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, allowIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verify(intentParser).rendering(SOURCE_IP, DEST_IP, ALLOW, intentList);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be increased by one from initial list size.
         */
        assertEquals(initialSize + 1, newSize);

        /**
         * Verifying intent is created for block action.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, blockIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verify(intentParser).rendering(SOURCE_IP, DEST_IP, BLOCK, intentList);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be increased by one from initial list size.
         */
        assertEquals(initialSize + 1, newSize);

        /**
         * Verifying intent is not created for more than one action.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, intent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created for without actions and subjects.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, negCase);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created by passing null values.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, null);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created for invalid subject.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, invaildSubject);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created for invalid action.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, invaildAction);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created for multiple subjects.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, invaildEpg);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created for single action without ALLOW or BLOCK.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, actionIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created for single action with different EPG values.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, epgIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);

        /**
         * Verifying intent is not created for subject with null values.
         */
        initialSize = VTNRendererUtility.hashMapIntentUtil.size();
        dataChangedEvent.created.put(uuid, subjectIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuid);
        newSize = VTNRendererUtility.hashMapIntentUtil.size();
        /**
         * Comparing VTNRendererUtility List, new size should be same as initial list size.
         */
        assertEquals(CUSTOM_MESSAGE, initialSize, newSize);
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * for each dataChangedEvent and then checks that Intents will be updated for each scenarios.
     */
    @Test
    public void testVtnRendererUpdate() {
        InstanceIdentifier<Intent> uuidUpdate = InstanceIdentifier.builder(Intents.class)
                .child(Intent.class, new IntentKey(new Uuid(UUID_THREE)))
                .build();
        Intent negCaseOne = new IntentBuilder().setSubjects(subjects).build();
        Intent negCaseUpdate = new IntentBuilder().build();
        Intent allowIntentUpdate = new IntentBuilder().setId(new Uuid(UUID_TWO)).setSubjects(subjects).setActions(allowAction).build();
        Intent allowIntentOne = new IntentBuilder().setId(new Uuid(UUID_TWO)).setSubjects(subjects).setActions(blockAction).build();
        Intent blockIntentOne = new IntentBuilder().setId(new Uuid(UUID_FOUR)).setSubjects(subjects).setActions(allowAction).build();
        Intent blockIntentUpdate = new IntentBuilder().setId(new Uuid(UUID_FOUR)).setSubjects(subjects).setActions(blockAction).build();
        /**
         * Verifying intent is not updated for negative case.
         */
        dataChangedEvent.created.put(uuidUpdate, negCaseUpdate);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.updated.put(uuidUpdate, negCaseOne);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuidUpdate);
        dataChangedEvent.updated.remove(uuidUpdate);

        /**
         * Verifying intent is not updated for null objects.
         */
        dataChangedEvent.created.put(uuidUpdate, negCaseUpdate);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.updated.put(uuidUpdate, null);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.created.remove(uuidUpdate);
        dataChangedEvent.updated.remove(uuidUpdate);

        /**
         * Verifying intent is updated for positive case by changing allow action into block action.
         */
        dataChangedEvent.created.put(uuidUpdate, allowIntentUpdate);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.updated.put(uuidUpdate, allowIntentOne);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verify(intentParser).updateRendering(SOURCE_IP, DEST_IP, BLOCK, intentList, UUID_TWO);
        dataChangedEvent.created.remove(uuidUpdate);
        dataChangedEvent.updated.remove(uuidUpdate);

        /**
         * Verifying intent is updated for positive case by changing block action into allow action.
         */
        dataChangedEvent.created.put(uuidUpdate, blockIntentUpdate);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.updated.put(uuidUpdate, blockIntentOne);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verify(intentParser).updateRendering(SOURCE_IP, DEST_IP, ALLOW, intentList, UUID_FOUR);
        dataChangedEvent.created.remove(uuidUpdate);
        dataChangedEvent.updated.remove(uuidUpdate);
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * for each dataChangedEvent and then checks that Intents will be deleted for each scenarios.
     */
    @Test
    public void testVtnRendererDelete() {
        InstanceIdentifier<Intent> uuidOne = InstanceIdentifier.builder(Intents.class)
                .child(Intent.class, new IntentKey(new Uuid(UUID_TWO)))
                .build();

        /**
         * Verifying intent is deleted for valid intent.
         */
        dataChangedEvent.removed.add(uuid);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verify(intentParser).delete(UUID_ONE);
        dataChangedEvent.removed.remove(uuid);

        /**
         * Verifying intent is not deleted for null objects.
         */
        dataChangedEvent.removed.add(null);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
        dataChangedEvent.removed.remove(null);

        /**
         * Verifying intent is not deleted for intent with null values.
         */
        dataChangedEvent.removed.add(uuidOne);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(intentParser);
    }

    /**
     * static MockDataChangedEvent class implements AsyncDataChangeEvent and overrides
     * AsyncDataChangeEvent interface methods
     */
    static private class MockDataChangedEvent implements AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> {
        Map<InstanceIdentifier<?>,DataObject> created = new HashMap<>();
        Map<InstanceIdentifier<?>,DataObject> updated = new HashMap<>();
        Set<InstanceIdentifier<?>> removed = new HashSet<>();

        /**
         * Override method for getCreatedData of AsyncDataChangeEvent interface
         * @return A map of {@link InstanceIdentifier}.
         */
        @Override
        public Map<InstanceIdentifier<?>, DataObject> getCreatedData() {
            return created;
        }

        /**
         * Override method for getUpdatedData of AsyncDataChangeEvent interface
         * @return A map of {@link InstanceIdentifier}.
         */
        @Override
        public Map<InstanceIdentifier<?>, DataObject> getUpdatedData() {
            return updated;
        }

        /**
         * Override method for getRemovedPaths of AsyncDataChangeEvent interface
         * @return A map of {@link InstanceIdentifier}.
         */
        @Override
        public Set<InstanceIdentifier<?>> getRemovedPaths() {
            return removed;
        }

        /**
         * Override method for getOriginalData of AsyncDataChangeEvent interface
         * @return A map of {@link Intent}.
         */
        @Override
        public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
            VTNRendererTest vtnRenderer = new VTNRendererTest();
            Intent intent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(vtnRenderer.subjects)
                    .setActions(vtnRenderer.allowAction)
                    .build();
            Intent invalidIntent = new IntentBuilder().build();
            Intent invalidIntent1 = new IntentBuilder().build();
            HashMap map = new HashMap();
            map.put(InstanceIdentifier.builder(Intents.class)
                .child(Intent.class, new IntentKey(new Uuid(UUID_ONE)))
                .build(),intent);
            map.put(InstanceIdentifier.builder(Intents.class)
                .child(Intent.class, new IntentKey(new Uuid(UUID_TWO)))
                .build(), null);
            map.put(InstanceIdentifier.builder(Intents.class)
                .child(Intent.class, new IntentKey(new Uuid(UUID_THREE)))
                .build(), invalidIntent);
            map.put(null, invalidIntent1);
            return map;
        }

        /**
         * Override method for getOriginalSubtree of AsyncDataChangeEvent interface
         * throw UnsupportedOperationException
         */
        @Override
        public DataObject getOriginalSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }

        /**
         * Override method for getOriginalSubtree of AsyncDataChangeEvent interface
         * throw UnsupportedOperationException
         */
        @Override
        public DataObject getUpdatedSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }
    }
}
