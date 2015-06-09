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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Unit test class for {@link VTNRenderer}.
 */
public class VTNRendererTest extends TestUtils{
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
     * Intent objects for different scenarios.
     */
    Intent intent, negCase, invaildSubject, invaildAction, invaildEpg, allowIntent, blockIntent,
            actionIntent, epgIntent, subjectIntent;

    /**
     * MockDataChangedEvent Object for VTNRenderer.
     */
    MockDataChangedEvent dataChangedEvent;

    /**
     * VTNRenderer Object to perform unit testing.
     */
    VTNRenderer vtnRendererObj;

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
        intent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(actions).build();
        negCase = new IntentBuilder().build();
        invaildSubject = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(null).setActions(null).build();
        invaildAction = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(null).build();
        invaildEpg = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjectEpg).setActions(actions).build();
        allowIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(allowAction).build();
        blockIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(blockAction).build();
        actionIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(subjects).setActions(singleAction).build();
        epgIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(endPointGroup).setActions(singleAction).build();
        subjectIntent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(emptySubjects).setActions(singleAction).build();
        dataChangedEvent = new MockDataChangedEvent();
        vtnRendererObj = new VTNRenderer();
    }

    /**
     * This method makes unnecessary objects eligible for garbage collection.
     */
    @After
    public void tearDown() {
        vtnRendererObj = null;
        dataChangedEvent = null;
        subjectIntent = null;
        epgIntent = null;
        actionIntent = null;
        blockIntent = null;
        allowIntent = null;
        invaildEpg = null;
        invaildAction = null;
        invaildSubject = null;
        negCase = null;
        intent = null;
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
        dataChangedEvent.created.put(uuid, intent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, negCase);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, null);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, invaildSubject);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, invaildAction);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, invaildEpg);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, allowIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, blockIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, actionIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, epgIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.created.put(uuid, subjectIntent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * for each dataChangedEvent and then checks that Intents will be updated for each scenarios.
     */
    @Test
    public void testVtnRendererUpdate() {
        dataChangedEvent.updated.put(uuid, intent);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.updated.put(uuid, negCase);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.updated.put(uuid, null);
        vtnRendererObj.onDataChanged(dataChangedEvent);
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
        dataChangedEvent.removed.add(uuid);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.removed.add(null);
        vtnRendererObj.onDataChanged(dataChangedEvent);
        dataChangedEvent.removed.add(uuidOne);
        vtnRendererObj.onDataChanged(dataChangedEvent);
    }

    /**
     * static MockDataChangedEvent class implements AsyncDataChangeEvent and overrides
     * AsyncDataChangeEvent interface methods
     */
    static private class MockDataChangedEvent implements AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> {
        Map<InstanceIdentifier<?>,DataObject> created = new HashMap<>();
        Map<InstanceIdentifier<?>,DataObject> updated = new HashMap<>();
        Set<InstanceIdentifier<?>> removed = new HashSet<>();

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getCreatedData() {
            return created;
        }

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getUpdatedData() {
            return updated;
        }

        @Override
        public Set<InstanceIdentifier<?>> getRemovedPaths() {
            return removed;
        }

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
            VTNRendererTest vtnRenderer = new VTNRendererTest();
            Intent intent = new IntentBuilder().setId(new Uuid(UUID_ONE)).setSubjects(vtnRenderer.subjects)
                    .setActions(vtnRenderer.actions)
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

        @Override
        public DataObject getOriginalSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }

        @Override
        public DataObject getUpdatedSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }
    }
}
