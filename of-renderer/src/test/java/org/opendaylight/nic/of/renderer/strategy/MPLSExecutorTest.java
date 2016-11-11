/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.impl.MplsIntentFlowManager;
import org.opendaylight.nic.of.renderer.impl.NetworkGraphManager;
import org.opendaylight.nic.of.renderer.impl.OFRendererConstants;
import org.opendaylight.nic.of.renderer.utils.MappingServiceUtils;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.constraints.rev150122.FailoverType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ConstraintsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.FailoverConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ProtectionConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.BandwidthConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Source;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.link.attributes.Destination;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 07/08/16.
 */
// TODO: Explore more scenarios for executeMplsIntentFlowManager()
@PrepareForTest({ IntentUtils.class, MappingServiceUtils.class,
        NetworkGraphManager.class, TopologyUtils.class })
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
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup srcEndPoint;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup dstEndPoint;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId srcNodeIdMock;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId dstNodeIdMock;

    @Mock
    private NodeId srcNodeIdDeprecatedMock;

    @Mock
    private NodeId dstNodeIdDeprecatedMock;

    @Mock
    private TpId srcTpIdMock;

    @Mock
    private TpId dstTpIdMock;

    @Mock
    private NetworkGraphManager networkGraphManagerMock;

    @Mock
    private Source sourceMock;

    @Mock
    private Destination destinationMock;

    private Constraints constraints;

    private MPLSExecutor spy;

    private Map<String, Map<String, String>> subjectDetails;

    private List<String> endPointGroups;

    private List<Constraints> constraintsList;

    private List<Link> linkList;

    private List<List<Link>> listOfLinks;

    private Map<String, String> endPointGroupNamesSource;

    private Map<String, String> endPointGroupNamesDestination;

    private Map<String, String> connectorIdMapSource;

    private Map<String, String> connectorIdMapDestination;

    @Before
    public void setUp() throws Exception {
        endPointGroups = Arrays.asList("00:00:00:00:00:01",
                "00:00:00:00:00:02");
        subjectDetails = new HashMap<>();
        constraints = new ConstraintsBuilder().setConstraints(
                new FailoverConstraintBuilder().setFailoverConstraint(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.failover.constraint.FailoverConstraintBuilder()
                                .setFailoverSelector(FailoverType.SlowReroute)
                                .build())
                        .build())
                .build();

        constraintsList = Arrays.asList(constraints);
        linkList = Arrays.asList(linkMock);
        listOfLinks = Arrays.asList(linkList);

        when(linkMock.getLinkId()).thenReturn(
                new LinkId(String.valueOf(new Random().nextLong())));

        when(linkMock.getSource()).thenReturn(sourceMock);
        when(sourceMock.getSourceNode()).thenReturn(srcNodeIdMock);
        when(srcNodeIdMock.getValue()).thenReturn("source_id");
        when(sourceMock.getSourceTp()).thenReturn(srcTpIdMock);
        when(srcTpIdMock.getValue()).thenReturn("source_tp_id");

        when(linkMock.getDestination()).thenReturn(destinationMock);
        when(destinationMock.getDestNode()).thenReturn(dstNodeIdMock);
        when(dstNodeIdMock.getValue()).thenReturn("destination_id");
        when(destinationMock.getDestTp()).thenReturn(dstTpIdMock);
        when(dstTpIdMock.getValue()).thenReturn("destination_tp_id");

        endPointGroupNamesSource = new HashMap<>();
        endPointGroupNamesSource.put(OFRendererConstants.SWITCH_PORT_KEY, "1");

        endPointGroupNamesDestination = new HashMap<>();
        endPointGroupNamesDestination.put(OFRendererConstants.SWITCH_PORT_KEY,
                "2");

        connectorIdMapSource = new HashMap<>();
        connectorIdMapSource.put(OFRendererConstants.SWITCH_PORT_KEY,
                "connectorId_1");
        connectorIdMapDestination = new HashMap<>();
        connectorIdMapDestination.put(OFRendererConstants.SWITCH_PORT_KEY,
                "connectorId_2");

        PowerMockito.mockStatic(IntentUtils.class);
        PowerMockito.mockStatic(MappingServiceUtils.class);
        PowerMockito.mockStatic(NetworkGraphManager.class);
        PowerMockito.mockStatic(TopologyUtils.class);

        when(IntentUtils.getAction(intentMock)).thenReturn(actionMock);
        when(IntentUtils.extractEndPointGroup(intentMock))
                .thenReturn(endPointGroups);
        when(IntentUtils.extractSrcEndPointGroup(intentMock))
                .thenReturn(srcEndPointGroupMock);
        when(IntentUtils.extractDstEndPointGroup(intentMock))
                .thenReturn(dstEndPointGroupMock);

        when(MappingServiceUtils.extractSubjectDetails(intentMock,
                mappingServiceMock)).thenReturn(subjectDetails);

        when(TopologyUtils.extractTopologyNodeId(
                connectorIdMapSource.get(OFRendererConstants.SWITCH_PORT_KEY)))
                        .thenReturn(srcNodeIdMock);
        when(TopologyUtils.extractTopologyNodeId(connectorIdMapDestination
                .get(OFRendererConstants.SWITCH_PORT_KEY)))
                        .thenReturn(dstNodeIdMock);

        when(srcEndPointGroupMock.getEndPointGroup()).thenReturn(srcEndPoint);
        when(srcEndPoint.getName()).thenReturn("endPointSource");

        when(dstEndPointGroupMock.getEndPointGroup()).thenReturn(dstEndPoint);
        when(dstEndPoint.getName()).thenReturn("dstPointSource");

        when(intentMock.getConstraints()).thenReturn(constraintsList);

        when(ofRendererGraphServiceMock.getDisjointPaths(srcNodeIdMock,
                dstNodeIdMock)).thenReturn(listOfLinks);

        when(mappingServiceMock.get(srcEndPoint.getName()))
                .thenReturn(endPointGroupNamesSource);
        when(mappingServiceMock.get(dstEndPoint.getName()))
                .thenReturn(endPointGroupNamesDestination);
        when(mappingServiceMock.get(endPointGroupNamesSource
                .get(OFRendererConstants.SWITCH_PORT_KEY)))
                        .thenReturn(connectorIdMapSource);
        when(mappingServiceMock.get(endPointGroupNamesDestination
                .get(OFRendererConstants.SWITCH_PORT_KEY)))
                        .thenReturn(connectorIdMapDestination);

        spy = PowerMockito.spy(mplsExecutorMock);

        mplsExecutorMock = new MPLSExecutor(mplsIntentFlowManagerMock,
                mappingServiceMock, ofRendererGraphServiceMock);
    }

    @Test
    public void testExecuteIntentAddFlowSlowRoute() throws Exception {
        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
        PowerMockito.verifyPrivate(mplsExecutorMock, Mockito.times(1)).invoke(
                "generateMplsFlows", intentMock, FlowAction.ADD_FLOW,
                constraints);
    }

    @Test
    public void testExecuteIntentRemoveFlow() throws Exception {
        NetworkGraphManager.ProtectedLinks.put(intentMock, listOfLinks);

        mplsExecutorMock.execute(intentMock, FlowAction.REMOVE_FLOW);

        NetworkGraphManager.ProtectedLinks.put(intentMock, listOfLinks);

        PowerMockito.verifyPrivate(mplsExecutorMock, Mockito.times(1)).invoke(
                "generateMplsFlows", intentMock, FlowAction.REMOVE_FLOW,
                constraints);
    }

    @Test
    public void testGenerateMplsFlowsWithEmptyEndPointName() throws Exception {
        endPointGroupNamesSource.put(OFRendererConstants.SWITCH_PORT_KEY, "");

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testGenerateMplsFlowsWithInvalidEndPointName()
            throws Exception {
        endPointGroupNamesSource.clear();

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteIntentAddFlowWithEmptyContrainst() throws Exception {
        when(intentMock.getConstraints()).thenReturn(null);

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteIntentAddFlowFastRoute() throws Exception {
        constraints = new ConstraintsBuilder().setConstraints(
                new FailoverConstraintBuilder().setFailoverConstraint(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.failover.constraint.FailoverConstraintBuilder()
                                .setFailoverSelector(FailoverType.FastReroute)
                                .build())
                        .build())
                .build();

        constraintsList = Arrays.asList(constraints);

        when(intentMock.getConstraints()).thenReturn(constraintsList);

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteIntentAddFlowFailoverNull() throws Exception {
        constraints = new ConstraintsBuilder()
                .setConstraints(new FailoverConstraintBuilder()
                        .setFailoverConstraint(
                                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.failover.constraint.FailoverConstraintBuilder()
                                        .setFailoverSelector(null).build())
                        .build())
                .build();

        constraintsList = Arrays.asList(constraints);

        when(intentMock.getConstraints()).thenReturn(constraintsList);

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteIntentAddFlowProtectionConstraint()
            throws Exception {
        constraints = new ConstraintsBuilder()
                .setConstraints(
                        new ProtectionConstraintBuilder()
                                .setProtectionConstraint(
                                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.protection.constraint.ProtectionConstraintBuilder()
                                                .setIsProtected(true).build())
                                .build())
                .build();

        constraintsList = Arrays.asList(constraints);

        when(intentMock.getConstraints()).thenReturn(constraintsList);

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteIntentAddFlowProtectionConstraintFalse()
            throws Exception {
        constraints = new ConstraintsBuilder()
                .setConstraints(
                        new ProtectionConstraintBuilder()
                                .setProtectionConstraint(
                                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.protection.constraint.ProtectionConstraintBuilder()
                                                .setIsProtected(false).build())
                                .build())
                .build();

        constraintsList = Arrays.asList(constraints);

        when(intentMock.getConstraints()).thenReturn(constraintsList);

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Test
    public void testExecuteIntentAddFlowBandwitchConstraint() throws Exception {
        constraints = new ConstraintsBuilder()
                .setConstraints(
                        new BandwidthConstraintBuilder()
                                .setBandwidthConstraint(
                                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.bandwidth.constraint.BandwidthConstraintBuilder()
                                                .setBandwidth("teste").build())
                                .build())
                .build();

        constraintsList = Arrays.asList(constraints);

        when(intentMock.getConstraints()).thenReturn(constraintsList);

        mplsExecutorMock.execute(intentMock, FlowAction.ADD_FLOW);
    }

    @Ignore
    @Test
    public void testGenerateMplsFlowWithValidEndPointName() throws Exception {
        Map<String, String> endPointNameMap = PowerMockito
                .spy(endPointGroupNamesSource);
        endPointGroupNamesSource.put("openflow:1", "00:00:00:00:00:01");
        when(mappingServiceMock.get(Mockito.anyString()))
                .thenReturn(endPointNameMap);
        when(srcEndPointGroupMock.getEndPointGroup()).thenReturn(srcEndPoint);
        when(dstEndPointGroupMock.getEndPointGroup()).thenReturn(dstEndPoint);

        when(srcEndPoint.getName()).thenReturn("openflow:1");
        when(dstEndPoint.getName()).thenReturn("openflow:2");

        when(endPointNameMap.get(Mockito.anyString()))
                .thenReturn("00:00:00:00:00:01");
        when(endPointGroup.getName()).thenReturn("openflow:1");
        PowerMockito.when(spy,
                PowerMockito.method(MPLSExecutor.class, "getDisjointPaths",
                        EndPointGroup.class, EndPointGroup.class))
                .withArguments(srcEndPointGroupMock, dstEndPointGroupMock)
                .thenReturn(listOfLinks);
        PowerMockito
                .when(spy,
                        PowerMockito.method(MPLSExecutor.class,
                                "extractPathByFlowAction", Intent.class,
                                FlowAction.class, Constraints.class))
                .withArguments(intentMock, FlowAction.ADD_FLOW, constraints)
                .thenReturn(linkList);

        when(mappingServiceMock.get(Mockito.anyString()))
                .thenReturn(endPointGroupNamesSource);

        when(linkMock.getSource()).thenReturn(sourceMock);
        when(sourceMock.getSourceNode()).thenReturn(srcNodeIdMock);

        when(linkMock.getDestination()).thenReturn(destinationMock);
        when(destinationMock.getDestNode()).thenReturn(dstNodeIdMock);

        when(sourceMock.getSourceTp()).thenReturn(srcTpIdMock);
        when(destinationMock.getDestTp()).thenReturn(dstTpIdMock);

        PowerMockito.whenNew(NodeId.class).withArguments(Mockito.anyString())
                .thenReturn(srcNodeIdDeprecatedMock);
        PowerMockito.whenNew(NodeId.class).withArguments(Mockito.anyString())
                .thenReturn(dstNodeIdDeprecatedMock);

        spy.execute(intentMock, FlowAction.ADD_FLOW);
        PowerMockito.verifyPrivate(spy, Mockito.times(3))
                .invoke("extractConnectorId", srcEndPointGroupMock);

        spy.execute(intentMock, FlowAction.REMOVE_FLOW);
        PowerMockito.verifyPrivate(spy, Mockito.times(1)).invoke(
                "containsLinkNodeId", Mockito.any(EndPointGroup.class),
                Mockito.any(NodeId.class));
        PowerMockito.verifyPrivate(spy, Mockito.times(1)).invoke(
                "executeMplsIntentFlowManager", intentMock, linkMock,
                FlowAction.REMOVE_FLOW);
    }

    @Ignore
    @Test(expected = IntentElementNotFoundException.class)
    public void testGenerateMplsFlowsShouldThrowsIntenElementNotFoundExceptionWhenTryExtractConnectorId()
            throws Exception {
        PowerMockito
                .when(spy, PowerMockito.method(MPLSExecutor.class,
                        "extractPathByFlowAction", Intent.class,
                        FlowAction.ADD_FLOW.getClass(), Constraints.class))
                .withArguments(intentMock, FlowAction.ADD_FLOW, constraints)
                .thenReturn(linkList);

        spy.execute(intentMock, FlowAction.ADD_FLOW);
    }
}
