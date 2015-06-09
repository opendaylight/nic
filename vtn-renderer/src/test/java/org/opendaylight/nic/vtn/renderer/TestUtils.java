/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.ArrayList;
import java.util.List;


import org.junit.Assert;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.allow.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.block.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;

/**
 * Helper methods that are used by multiple tests.
 */
public class TestUtils extends Assert {
    /**
     * Interger declaration for first subject
     */
    private static final int FIRST_SUBJECT = 1;

    /**
     * Interger declaration for second subject
     */
    private static final int SECOND_SUBJECT = 2;

    /**
     * String declaration for source IP
     */
    private static final String SOURCE_IP = "10.0.0.1";

    /**
     * String declaration for destination IP
     */
    private static final String DEST_IP = "10.0.0.2";

    /**
     * Short declaration for order allow
     */
    private static final short ORDER_ALLOW = 1;

    /**
     * Short declaration for order block
     */
    private static final short ORDER_BLOCK = 2;

    /**
     * A list decleration for actionsList
     */
    private List<Actions> actionsList = null;

    /**
     * A list decleration for singleActionsList
     */
    private List<Actions> singleActionsList = null;

    /**
     * A list decleration for subjectList
     */
    private List<Subjects> subjectList = null;

    /**
     * A list decleration for multipleSubjectList
     */
    private List<Subjects> multipleSubjectList = null;


    /**
     * A list decleration for nullSubjectList
     */
    private List<Subjects> nullSubjectList = null;

    /**
     * A list decleration for epgList
     */
    private List<Subjects> epgList = null;

    /**
     * A list decleration for allowActionsList
     */
    private List<Actions> allowActionsList = null;

    /**
     * A list decleration for blockActionsList
     */
    private List<Actions> blockActionsList = null;

    /**
     * Initialize the Action object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionAllow = null;

    /**
     * Initialize the Action object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionAllowCreation = null;

    /**
     * Initialize the Action object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionBlock = null;

    /**
     * Initialize the Action object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionBlockCreation = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup from = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup multipleFrom = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup multipleFromOne = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup to = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup MultipleTo = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup MultipleToOne = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup epgFrom = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup epgTo = null;

    /**
     * Initialize the Actions object.
     */
    private Actions actions = null;
    private Actions actionsAllow = null;
    private Actions actionsBlock = null;
    private Actions actionsAllowCreation = null;
    private Actions actionsBlockCreation = null;

    /**
     * Initialize the EndPointGroup object.
     */
    private EndPointGroup endpointGroupFrom = null;
    private EndPointGroup multipleEndpointGroupFrom = null;
    private EndPointGroup endpointGroupTo = null;
    private EndPointGroup multipleEndpointGroupTo = null;

    /**
     * Initialize the Subjects object.
     */
    private Subjects subjectsOne = null;
    private Subjects subjectsTwo = null;
    private Subjects multipleSubjectsOne = null;
    private Subjects multipleSubjectsTwo = null;
    private Subjects multipleSubjectsThree = null;
    private Subjects multipleSubjectsFour = null;
    private Subjects multipleSubjectsFive = null;
    private Subjects multipleSubjectsSix = null;
    private Subjects epgSubjectsOne = null;
    private Subjects epgSubjectsTwo = null;
    private Subjects nullSubjectsOne = null;
    private Subjects nullSubjectsTwo = null;

    /**
     * Valid Intent ID's used for testing diffrent scenarios.
     */
    protected static final String UUID_ONE = "b9a13232-525e-4d8c-be21-cd65e3436034";
    protected static final String UUID_TWO = "b9a13232-525e-4d8c-be21-cd65e3436048";
    protected static final String UUID_THREE = "b9a13232-525e-4d8c-be21-cd65e3436040";


    /**
     * This method creates action with allow and block values
     * @return actionsList
     */
    protected List<Actions> createActions() {
        actionsList = new ArrayList<Actions>();
        actionAllow = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder().setAllow(new AllowBuilder().build()).build();
        actionBlock = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder().setBlock(new BlockBuilder().build()).build();
        actionsAllow = new ActionsBuilder().setOrder(ORDER_ALLOW).setAction(actionAllow).build();
        actionsBlock = new ActionsBuilder().setOrder(ORDER_BLOCK).setAction(actionBlock).build();
        actionsList.add(actionsAllow);
        actionsList.add(actionsBlock);
        return actionsList;
    }

    /**
     * This method creates subjects with EndPointGroup
     * @return subjectList
     */
    protected List<Subjects> createSubjects() {
        subjectList = new ArrayList<Subjects>();
        endpointGroupFrom = new EndPointGroupBuilder().setName(SOURCE_IP).build();
        from = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(endpointGroupFrom).build();
        subjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(from).build();
        endpointGroupTo = new EndPointGroupBuilder().setName(DEST_IP).build();
        to = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(endpointGroupTo).build();
        subjectsTwo = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(to).build();
        subjectList.add(subjectsOne);
        subjectList.add(subjectsTwo);
        return subjectList;
    }

    /**
     * This method creates multiple subjects.
     * @return multiple subjectList with EndPointGroup as value and null scenarios.
     */
    protected List<Subjects> createMultipleSubjects() {
        multipleSubjectList = new ArrayList<Subjects>();
        multipleEndpointGroupFrom = new EndPointGroupBuilder().setName(null).build();
        multipleFrom = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(multipleEndpointGroupFrom).build();
        multipleSubjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(multipleFrom).build();
        multipleSubjectsTwo = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(null).build();
        multipleFromOne = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(null).build();
        multipleSubjectsThree = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(multipleFromOne).build();
        multipleEndpointGroupTo = new EndPointGroupBuilder().setName(null).build();
        MultipleTo = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(multipleEndpointGroupTo).build();
        multipleSubjectsFour = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(MultipleTo).build();
        multipleSubjectsFive = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(null).build();
        MultipleToOne = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(null).build();
        multipleSubjectsSix = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(MultipleToOne).build();
        multipleSubjectList.add(multipleSubjectsOne);
        multipleSubjectList.add(multipleSubjectsTwo);
        multipleSubjectList.add(multipleSubjectsThree);
        multipleSubjectList.add(multipleSubjectsFour);
        multipleSubjectList.add(multipleSubjectsFive);
        multipleSubjectList.add(multipleSubjectsSix);
        return multipleSubjectList;
    }

    /**
     * This method creates multiple Endpoits.
     * @return EndPointGroups List
     */
    protected List<Subjects> createEndPointGroups() {
        epgList = new ArrayList<Subjects>();
        epgFrom = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(null).build();
        epgSubjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(epgFrom).build();
        epgTo = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(null).build();
        epgSubjectsTwo = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(epgTo).build();
        epgList.add(epgSubjectsOne);
        epgList.add(epgSubjectsTwo);
        return epgList;
    }

    /**
     * This method creates list of Allow actions.
     * @return allow action list
     */
    protected List<Actions> createAllowAction() {
        allowActionsList = new ArrayList<Actions>();
        actionAllowCreation = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder().setAllow(new AllowBuilder().build()).build();
        actionsAllowCreation = new ActionsBuilder().setOrder(ORDER_ALLOW).setAction(actionAllowCreation).build();
        allowActionsList.add(actionsAllowCreation);
        return allowActionsList;
    }

    /**
     * This method creates list of Block actions.
     * @return block action list
     */
    protected List<Actions> createBlockAction() {
        blockActionsList = new ArrayList<Actions>();
        actionBlockCreation = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder().setBlock(new BlockBuilder().build()).build();
        actionsBlockCreation = new ActionsBuilder().setOrder(ORDER_BLOCK).setAction(actionBlockCreation).build();
        blockActionsList.add(actionsBlockCreation);
        return blockActionsList;
    }

    /**
     * This method creates only single action.
     * @return only one action.
     */
    protected List<Actions> createSingleAction() {
        List<Actions> singleActionsList = new ArrayList<Actions>();
        actions = new ActionsBuilder().setOrder(ORDER_BLOCK).build();
        singleActionsList.add(actions);
        return singleActionsList;
    }

    /**
     * This method creates subjects with null values..
     * @return empty subject list.
     */
    protected List<Subjects> createSubjectsWithNull() {
        nullSubjectList = new ArrayList<Subjects>();
        nullSubjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).build();
        nullSubjectsTwo = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).build();
        nullSubjectList.add(nullSubjectsOne);
        nullSubjectList.add(nullSubjectsTwo);
        return nullSubjectList;
    }
}