//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.nic.compiler.CompiledPolicy;
import org.opendaylight.nic.compiler.impl.PolicyCompiler;
import org.opendaylight.nic.intent.Policy;
import org.opendaylight.nic.intent.PolicyId;

/**
 *
 * @author Duane Mentze
 */
public class ManagerTest {

    // private static PrefixExpression makePrefixExpression(String e) {
    // return new PrefixExpression(new EndpointGroupImpl(e));
    // }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        // fail("Not yet implemented");
    }

    @Test
    public void misc1Test() {
        // multipleOverlapAndDisjoint
        // c1 = VLAN[20,30][40,50] and ipProto[60,70]
        // c2 = VLAN[10,45][48,48] and ipProto[62,68]
        // c = e1 & e2
        // Assert(c == (VLAN[20,30][40,45][48,48] and ipProto[62,68])

        /*
         * 
         * TermTypeLabel vlanLabel = new TermTypeLabel("VLAN"); TermTypeLabel
         * ipProtoLabel = new TermTypeLabel("IP_PROTO");
         * 
         * List<ExpHelper> helperList; List<ExpressionImpl> expressionList = new
         * LinkedList<>(); ClassifierImpl c1,c2,c;
         * 
         * //build c1 expressionList = new LinkedList<ExpressionImpl>(); //build
         * e1 helperList = new LinkedList<ExpHelper>(); helperList.add(new
         * ExpHelper(vlanLabel, new int[] {20,40}, new int[] {30,50}));
         * helperList.add(new ExpHelper(ipProtoLabel, new int[] {60}, new int[]
         * {70})); expressionList.add(buildExp(helperList)); c1 = new
         * ClassifierImpl(expressionList);
         * 
         * 
         * 
         * //build c2 expressionList = new LinkedList<ExpressionImpl>(); //build
         * e1 helperList = new LinkedList<ExpHelper>(); helperList.add(new
         * ExpHelper(vlanLabel, new int[] {10,48}, new int[] {45,48}));
         * helperList.add(new ExpHelper(ipProtoLabel, new int[] {62}, new int[]
         * {68})); expressionList.add(buildExp(helperList)); c2 = new
         * ClassifierImpl(expressionList);
         * 
         * //c = c1.and(c2);
         * 
         * //build expected result for first expression expressionList = new
         * LinkedList<ExpressionImpl>(); helperList = new
         * LinkedList<ExpHelper>(); helperList.add(new ExpHelper(vlanLabel, new
         * int[] {20,40,48}, new int[] {30,45,48})); helperList.add(new
         * ExpHelper(ipProtoLabel, new int[] {62}, new int[] {68}));
         * expressionList.add(buildExp(helperList)); ClassifierImpl result = new
         * ClassifierImpl(expressionList); //assertTrue( c.equals(result));
         */

    }

    /*
     * Policy 1 only: S1=1 S2=1 D1=2 D2=3 C1= vlan[10,20] C2=[100,100] 1.1: S1
     * and not S2 D1 and not D2 C1 0 2 . result=0 1.2: S1 and not S2 D1 and D2
     * C1 0 0 . result=0 1.3: S1 and not S2 D2 and not D1 C1 0 3 . result=0 1.4:
     * S1 and S2 D1 and not D2 C1 1 2 . result= (1,2),(2),(c1) 1.5: S1 and S2 D1
     * and D2 C1 and not C2 1 0 c1 result=0
     * 
     * Policy 1+2: 12.1: S1 and S2 D1 and D2 C1 and C2 1 0 0 result=0
     * 
     * Policy 2 only: 2.1: S2 and not S1 D1 and not D2 C2 0 1 . result=0 2.2: S2
     * and not S1 D1 and D2 C2 0 0 . result=0 2.3: S2 and not s1 D2 and not D1
     * C2 0 3 . result=0 2.4: S1 and S2 D2 and not D1 C2 1,2 3 . result= (1,2),
     * (3), c2) 2.5: S1 and S2 D1 and D2 C2 and not C1 1 0 . result=0
     */

    /**
     *
     */
    @Test
    public void singleClassifierTermNoOverLapTest() {
        /*
         * using SimNet1 Policy('A', app1 sourceGroup='employee'
         * dstGroup='database' classifier = vlan([10,20]) actions = [allow]
         * 
         * Policy('B', app1, sourceGroup='employee'
         * dstGroup='authorizedDnsServers' classifier= vlan([100,100]) actions =
         * [block]
         * 
         * Set Calcs: endpoints on network: id using last octect in IP, 10.0.0.1
         * = 1 S1=1 S2=1 D1=2 D2=3 C1= vlan[10,20] C2=[100,100]
         * 
         * Policy 1+2: 12.1: S1 and S2 D1 and D2 C1 and C2 1 0 0 result=0
         */

        SimNet1 n = new SimNet1();
        n.addApplication("app1", 1); // add application

        // add policy A
        Policy pr;

        // create policy A
        pr = new PolicyImpl(new PolicyId("A"), "A", n.getApplication("app1"),
                new EndpointGroupImpl("employee"), new EndpointGroupImpl(
                        "database"), ClassifierHelper.vlan(10, 20),
                ActionHelper.allow(), false);
        // add policy A
        n.framework().add(pr, n.getApplication("app1").appId());

        // create policy B
        pr = new PolicyImpl(new PolicyId("B"), "B policy",
                n.getApplication("app1"), new EndpointGroupImpl("employee"),
                new EndpointGroupImpl("authorizedDnsServers"),
                ClassifierHelper.vlan(100, 100), ActionHelper.block(), false);

        // add policy B to framework service
        n.framework().add(pr, n.getApplication("app1").appId());

        PolicyCompiler c = new PolicyCompiler();

        List<CompiledPolicy> list = c.compile(n.framework().getPolicies(), n
                .endpointService().getEndpoints(), n.framework().getActions(),
                n.framework().getTermTypes(), n.deviceService().getDevices());

        for (CompiledPolicy cp : list) {
            System.out.println(cp);
        }

        // TODO compare list to to expected answer
    }

    /**
     *
     */
    @Test
    public void singleClassifierTermNoOverLapTestx() {
        /*
         * using SimNet1 Policy('A', app1 sourceGroup='employee'
         * dstGroup='database or authorizedDnsServers' classifier =
         * vlan([10,150]) actions = [allow]
         * 
         * Policy('B', app1, sourceGroup='employee'
         * dstGroup='authorizedDnsServers' classifier= vlan([100,200]) actions =
         * [block]
         * 
         * Set Calcs: endpoints on network: id using last octect in IP, 10.0.0.1
         * = 1 S1=1 S2=1 D1=2,3 D2=3 C1= vlan[10,150] C2=[100,200]
         * 
         * Policy 1+2: 12.1: S1 and S2 D1 and D2 C1 and C2 1 3 vlan[100,150]
         * a=block
         * 
         * Policy 1 only: 1.1: S1 and not S2 D1 and not D2 C1 0 2 . action=n/a
         * 1.2: S1 and not S2 D1 and D2 C1 0 2,3 . action=n/a 1.3: S1 and not S2
         * D2 and not D1 C1 0 0 . action=n/a 1.4: S1 and S2 D1 and not D2 C1 1 2
         * . a=allow 1.5: S1 and S2 D1 and D2 C1 and not C2 1 3 vlan[10,99]
         * a=allow
         * 
         * Policy 2 only: 2.1: S2 and not S1 D1 and not D2 C2 0 2 . action=n/a
         * 2.2: S2 and not S1 D1 and D2 C2 0 3 . action=n/a 2.3: S2 and not s1
         * D2 and not D1 C2 0 0 . action=n/a 2.4: S1 and S2 D2 and not D1 C2 1 0
         * . action=n/a 2.5: S1 and S2 D1 and D2 C2 and not C1 1 3 vlan[151,200]
         * action=block
         */

        SimNet1 n = new SimNet1();
        n.addApplication("app1", 1); // add application

        // add policy A
        Policy pr;

        // create policy A
        pr = new PolicyImpl(new PolicyId("A"), "A", n.getApplication("app1"),
                new EndpointGroupImpl("employee"), new EndpointGroupImpl(
                        "database or authorizedDnsServers"),
                ClassifierHelper.vlan(10, 150), ActionHelper.allow(), false);
        // add policy A
        n.framework().add(pr, n.getApplication("app1").appId());

        // create policy B
        pr = new PolicyImpl(new PolicyId("B"), "B policy",
                n.getApplication("app1"), new EndpointGroupImpl("employee"),
                new EndpointGroupImpl("authorizedDnsServers"),
                ClassifierHelper.vlan(100, 200), ActionHelper.block(), false);

        // add policy B to framework service
        n.framework().add(pr, n.getApplication("app1").appId());

        PolicyCompiler c = new PolicyCompiler();
        List<CompiledPolicy> list = c.compile(n.framework().getPolicies(), n
                .endpointService().getEndpoints(), n.framework().getActions(),
                n.framework().getTermTypes(), n.deviceService().getDevices());

        for (CompiledPolicy cp : list) {
            System.out.println(cp);
        }

    }

}
