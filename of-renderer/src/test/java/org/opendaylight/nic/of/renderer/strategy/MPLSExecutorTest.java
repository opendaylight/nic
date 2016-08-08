/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.impl.MplsIntentFlowManager;
import org.opendaylight.nic.of.renderer.utils.MappingServiceUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.constraints.rev150122.Constraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.FailoverConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 07/08/16.
 */
@PrepareForTest({MPLSExecutor.class, IntentUtils.class, MappingServiceUtils.class})
@RunWith(PowerMockRunner.class)
public class MPLSExecutorTest {

    @InjectMocks
    private MPLSExecutor mplsExecutorMock;
    @Mock
    private MplsIntentFlowManager mplsIntentFlowManagerMock;
    @Mock
    private IntentMappingService mappingServiceMock;
    @Mock
    private OFRendererGraphService ofRendererGraphServiceMock;
    @Mock
    private Intent intentMock;
    @Mock
    private Action actionMock;
    @Mock
    private Constraints constraintsMock;
    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints failOverConstraints;
    @Mock
    private Link linkMock;
    @Mock
    private EndPointGroup srcEndPointGroupMock;
    @Mock
    private EndPointGroup dstEndPointGroupMock;
    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup endPointGroup;
    @Mock
    private NodeId srcNodeIdMock;
    @Mock
    private NodeId dstNodeIdMock;
    private Map<String, Map<String, String>> subjectDetails;
    private List<String> endPointGroups;
    private List<Constraints> constraintsList;
    private List<Link> linkList;
    private List<List<Link>> listOfLinks;

    @Before
    public void setUp() throws IntentInvalidException {
        endPointGroups = Arrays.asList("00:00:00:00:00:01", "00:00:00:00:00:02");
        subjectDetails = new HashMap<>();
        constraintsList = Arrays.asList(constraintsMock);
        linkList = Arrays.asList(linkMock);
        listOfLinks = Arrays.asList(linkList);

        PowerMockito.mockStatic(IntentUtils.class);
        PowerMockito.mockStatic(MappingServiceUtils.class);

        Mockito.when(IntentUtils.getAction(intentMock)).thenReturn(actionMock);
        Mockito.when(IntentUtils.extractEndPointGroup(intentMock)).thenReturn(endPointGroups);
        Mockito.when(MappingServiceUtils.extractSubjectDetails(intentMock, mappingServiceMock)).thenReturn(subjectDetails);
        Mockito.when(intentMock.getConstraints()).thenReturn(constraintsList);
        Mockito.when(IntentUtils.extractSrcEndPointGroup(intentMock)).thenReturn(srcEndPointGroupMock);
        Mockito.when(IntentUtils.extractDstEndPointGroup(intentMock)).thenReturn(dstEndPointGroupMock);
        Mockito.when(ofRendererGraphServiceMock.getDisjointPaths(srcNodeIdMock, dstNodeIdMock)).thenReturn(listOfLinks);

        Mockito.when(constraintsMock.getConstraints()).thenReturn(failOverConstraints);
        Mockito.when(srcEndPointGroupMock.getEndPointGroup()).thenReturn(endPointGroup);
        Mockito.when(endPointGroup.getName()).thenReturn("EndPointName");

        mplsExecutorMock = new MPLSExecutor(mplsIntentFlowManagerMock, mappingServiceMock, ofRendererGraphServiceMock);
    }

    @Test
    public void testExecuteIntent() throws Exception {
        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);

        PowerMockito.verifyPrivate(mplsExecutorMock, Mockito.times(1)).invoke("generateMplsFlows", intentMock, FlowAction.ADD_FLOW, constraintsMock);
    }

    @Test
    public void testGenerateMplsFlows() throws Exception {
        PowerMockito.spy(mplsExecutorMock);
        MPLSExecutor spy = PowerMockito.spy(mplsExecutorMock);
        PowerMockito.when(spy,
                PowerMockito.method(MPLSExecutor.class, "isProtectedOrSlowRoute", Constraints.class)).withArguments(constraintsMock).thenReturn(true);
        spy.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testIfIsProtectedOrSlowRoute() {

    }
}
