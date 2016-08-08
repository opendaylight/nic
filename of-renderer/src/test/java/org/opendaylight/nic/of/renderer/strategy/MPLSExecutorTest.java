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
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.impl.MplsIntentFlowManager;
import org.opendaylight.nic.of.renderer.impl.NetworkGraphManager;
import org.opendaylight.nic.of.renderer.utils.MappingServiceUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 07/08/16.
 */
@PrepareForTest({MPLSExecutor.class, IntentUtils.class, MappingServiceUtils.class, NetworkGraphManager.class})
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
    private Map<Intent, List<List<Link>>> protectedLinks;
    private MPLSExecutor spy;
    private Map<String, Map<String, String>> subjectDetails;
    private List<String> endPointGroups;
    private List<Constraints> constraintsList;
    private List<Link> linkList;
    private List<List<Link>> listOfLinks;
    private Map<org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent, List<List<Link>>> intentByLinkListMap;

    @Before
    public void setUp() throws Exception {
        endPointGroups = Arrays.asList("00:00:00:00:00:01", "00:00:00:00:00:02");
        subjectDetails = new HashMap<>();
        constraintsList = Arrays.asList(constraintsMock);
        linkList = Arrays.asList(linkMock);
        listOfLinks = Arrays.asList(linkList);
        protectedLinks = new HashMap<>();
        intentByLinkListMap = new HashMap<>();

        PowerMockito.mockStatic(IntentUtils.class);
        PowerMockito.mockStatic(MappingServiceUtils.class);
        PowerMockito.mockStatic(NetworkGraphManager.class);

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

        List<List<Link>> list1 = new ArrayList<>();

        List<Link> currentLinks = new ArrayList<>();
        List<Link> newLinks = new ArrayList<>();

        Link link1 = mock(Link.class);
        when(link1.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link2 = mock(Link.class);
        when(link2.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link3 = mock(Link.class);
        when(link3.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link4 = mock(Link.class);
        when(link4.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));

        currentLinks.add(link1);
        currentLinks.add(link2);
        currentLinks.add(link3);
        currentLinks.add(link4);

        newLinks.add(link1);
        newLinks.add(link2);
        newLinks.add(link3);

        list1.add(newLinks);

        NetworkGraphManager.ProtectedLinks.put(intentMock, list1);

        spy = PowerMockito.spy(mplsExecutorMock);
        PowerMockito.when(spy,
                PowerMockito.method(MPLSExecutor.class, "isProtectedOrSlowRoute", Constraints.class)).withArguments(constraintsMock).thenReturn(true);
        mplsExecutorMock = new MPLSExecutor(mplsIntentFlowManagerMock, mappingServiceMock, ofRendererGraphServiceMock);
    }

    @Test
    public void testExecuteIntentAddFlow() throws Exception {
        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
        PowerMockito.verifyPrivate(mplsExecutorMock, Mockito.times(1)).invoke("generateMplsFlows", intentMock, FlowAction.ADD_FLOW, constraintsMock);
    }

    @Test
    public void testExecuteIntentRemoveFlow() throws Exception {
        mplsExecutorMock.execute(intentMock, FlowAction.REMOVE_FLOW);
        PowerMockito.verifyPrivate(mplsExecutorMock, Mockito.times(1)).invoke("generateMplsFlows", intentMock, FlowAction.REMOVE_FLOW, constraintsMock);
    }

    @Test
    public void testGenerateMplsFlows() {

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test (expected = IntentElementNotFoundException.class)
    public void testGenerateMplsFlowsShouldThrowsIntenElementNotFoundExceptionWhenTryExtractConnectorId() throws Exception {
        PowerMockito.when(spy, PowerMockito.method(MPLSExecutor.class, "extractPathByFlowAction",
                Intent.class, FlowAction.ADD_FLOW.getClass(), Constraints.class))
                .withArguments(intentMock, FlowAction.ADD_FLOW, constraintsMock).thenReturn(linkList);
        spy.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteMplsIntentFlowManager() {

    }

    @Test
    public void testIfIsProtectedOrSlowRoute() {

    }
}
