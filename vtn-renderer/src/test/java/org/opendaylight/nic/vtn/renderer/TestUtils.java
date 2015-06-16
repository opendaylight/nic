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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
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
     * Integer declaration for first subject.
     */
    private static final int FIRST_SUBJECT = 1;

    /**
     * Integer declaration for second subject.
     */
    private static final int SECOND_SUBJECT = 2;

    /**
     * String declaration for source IP.
     */
    protected static final String SOURCE_IP = "10.0.0.1";

    /**
     * String declaration for destination IP.
     */
    protected static final String DEST_IP = "10.0.0.2";

    /**
     * Short declaration for order allow.
     */
    private static final short ORDER_ALLOW = 1;

    /**
     * String declaration for ALLOW.
     */
    protected static final String ALLOW = "allow";

    /**
     * String declaration for BLOCK.
     */
    protected static final String BLOCK = "block";

    /**
     * Short declaration for order block.
     */
    private static final short ORDER_BLOCK = 2;

    /**
     * Valid Intent IDs used for testing diffrent scenarios.
     */
    protected static final String UUID_ONE = "b9a13232-525e-4d8c-be21-cd65e3436034";
    protected static final String UUID_TWO = "b9a13232-525e-4d8c-be21-cd65e3436048";
    protected static final String UUID_THREE = "b9a13232-525e-4d8c-be21-cd65e3436040";
    protected static final String UUID_FOUR = "b9a13232-525e-4d8c-be21-cd65e3436042";

    /**
     * This method creates action with allow and block values
     * @return A list of {@link Actions}.
     */
    protected List<Actions> createActions() {
        List<Actions> actionsList = new ArrayList<Actions>();
        Action actionAllow = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder()
                .setAllow(new AllowBuilder().build())
                .build();
        Action actionBlock = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder()
                .setBlock(new BlockBuilder().build())
                .build();
        Actions actionsAllow = new ActionsBuilder().setOrder(ORDER_ALLOW).setAction(actionAllow).build();
        Actions actionsBlock = new ActionsBuilder().setOrder(ORDER_BLOCK).setAction(actionBlock).build();
        actionsList.add(actionsAllow);
        actionsList.add(actionsBlock);
        return actionsList;
    }

    /**
     * This method creates subjects with EndPointGroup
     * @return A list of {@link Subjects}.
     */
    protected List<Subjects> createSubjects() {
        List<Subjects>subjectListOne = new ArrayList<Subjects>();
        EndPointGroup endpointGroupFrom = new EndPointGroupBuilder().setName(SOURCE_IP).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup from =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(endpointGroupFrom)
                .build();
        Subjects subjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(from).build();
        EndPointGroup endpointGroupTo = new EndPointGroupBuilder().setName(DEST_IP).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup to =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(endpointGroupTo)
                .build();
        Subjects subjectsTwo = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(to).build();
        subjectListOne.add(subjectsOne);
        subjectListOne.add(subjectsTwo);
        return subjectListOne;
    }

    /**
     * This method creates multiple subjects with EndPointGroup as value and null scenarios.
     * return A list of {@link Subjects}.
     */
    protected List<Subjects> createMultipleSubjects() {
        List<Subjects> multipleSubjectList = new ArrayList<Subjects>();
        EndPointGroup multipleEndpointGroupFrom = new EndPointGroupBuilder().setName(null).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup multipleFrom =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(multipleEndpointGroupFrom)
                .build();
        Subjects multipleSubjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(multipleFrom).build();
        Subjects multipleSubjectsTwo = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(null).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup multipleFromOne =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(null)
                .build();
        Subjects multipleSubjectsThree = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(multipleFromOne).build();
        EndPointGroup multipleEndpointGroupTo = new EndPointGroupBuilder().setName(null).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup MultipleTo =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(multipleEndpointGroupTo)
                .build();
        Subjects multipleSubjectsFour = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(MultipleTo).build();
        Subjects multipleSubjectsFive = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(null).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup MultipleToOne =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(null)
                .build();
        Subjects multipleSubjectsSix = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(MultipleToOne).build();
        multipleSubjectList.add(multipleSubjectsOne);
        multipleSubjectList.add(multipleSubjectsTwo);
        multipleSubjectList.add(multipleSubjectsThree);
        multipleSubjectList.add(multipleSubjectsFour);
        multipleSubjectList.add(multipleSubjectsFive);
        multipleSubjectList.add(multipleSubjectsSix);
        return multipleSubjectList;
    }

    /**
     * This method creates multiple Endpoints.
     * return A list of {@link Subjects}.
     */
    protected List<Subjects> createEndPointGroups() {
        List<Subjects> epgList = new ArrayList<Subjects>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup epgFrom =
        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(null)
                .build();
        Subjects epgSubjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(epgFrom).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup epgTo =
        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(null)
                .build();
        Subjects epgSubjectsTwo = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(epgTo).build();
        epgList.add(epgSubjectsOne);
        epgList.add(epgSubjectsTwo);
        return epgList;
    }

    /**
     * This method creates list of Allow actions.
     * @return A list of {@link Actions}.
     */
    protected List<Actions> createAllowAction() {
        List<Actions> allowActionsList = new ArrayList<Actions>();
        Action actionAllowCreation = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder()
                .setAllow(new AllowBuilder().build())
                .build();
        Actions actionsAllowCreation = new ActionsBuilder().setOrder(ORDER_ALLOW).setAction(actionAllowCreation).build();
        allowActionsList.add(actionsAllowCreation);
        return allowActionsList;
    }

    /**
     * This method creates list of Block actions.
     * @return A list of {@link Actions}.
     */
    protected List<Actions> createBlockAction() {
        List<Actions> blockActionsList = new ArrayList<Actions>();
        Action actionBlockCreation = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder()
                .setBlock(new BlockBuilder().build())
                .build();
        Actions actionsBlockCreation = new ActionsBuilder().setOrder(ORDER_BLOCK).setAction(actionBlockCreation).build();
        blockActionsList.add(actionsBlockCreation);
        return blockActionsList;
    }

    /**
     * This method creates only single action.
     * @return A list of {@link Actions}.
     */
    protected List<Actions> createSingleAction() {
        List<Actions> singleActionsList = new ArrayList<Actions>();
        Actions actions = new ActionsBuilder().setOrder(ORDER_BLOCK).build();
        singleActionsList.add(actions);
        return singleActionsList;
    }

    /**
     * This method creates subjects with null values..
     * @return A list of {@link Subjects}.
     */
    protected List<Subjects> createSubjectsWithNull() {
        List<Subjects> nullSubjectList = new ArrayList<Subjects>();
        Subjects nullSubjectsOne = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).build();
        Subjects nullSubjectsTwo = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).build();
        nullSubjectList.add(nullSubjectsOne);
        nullSubjectList.add(nullSubjectsTwo);
        return nullSubjectList;
    }
}