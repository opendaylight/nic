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
import org.opendaylight.nic.compiler.impl.CompilerNode;
import org.opendaylight.nic.compiler.impl.PolicyCompiler;
import org.opendaylight.nic.intent.Policy;
import org.opendaylight.nic.intent.PolicyId;
import org.opendaylight.nic.intent.impl.EndpointGroupImpl;
import org.opendaylight.nic.intent.impl.PolicyImpl;

/**
 *
 * @author Duane Mentze
 */
public class ManagerLatencyBlockTest {


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


    /**
     *
     */
    @Test
    public void latencyBlockTest() {

        final Integer EXPEDITED_FORWARDING = 46;
        final Integer ETHERTYPE_IPV4 = 0x0800;

        BasicNetwork network = BasicNetwork.buildSingleVlanNetwork(10);

        //add device
        network.addDevice("access_sw1");

        //add endpoint attribute
        network.registerAttribute("employee");
        network.registerAttribute("authorizedServer");
        //network.registerAttribute("ips");

        //make endpoints
        network.addEndpoint("access_sw1", "10.0.0.1");
        network.addEndpoint("access_sw1", "10.0.0.2");
        network.addEndpoint("access_sw1", "10.0.0.3");

        //apply attributes to endpoints
        network.addAttribute("10.0.0.1", "employee");
        network.addAttribute("10.0.0.2", "employee");
        //network.addAttribute("10.0.0.2", "infected");
        network.addAttribute("10.0.0.3", "authorizedServer");

        //add policy A
        network.addApplication("app1", 1);  //add application
        Policy pr;

        //create policy A
        pr = new PolicyImpl( new PolicyId("A"),
                                    "A",
                                    network.getApplication("app1"),
                                    new EndpointGroupImpl("10.0.0.1"),
                                    new EndpointGroupImpl("10.0.0.2"),
                                    ClassifierHelper.ethType(ETHERTYPE_IPV4),
                                    ActionHelper.latency(EXPEDITED_FORWARDING),
                                    false);
        //add policy A
        network.framework().add(pr,  network.getApplication("app1").appId());

        //create policy B
        network.addApplication("app2", 2);  //add application

        pr = new PolicyImpl( new PolicyId("B"),
                                    "B policy",
                                    network.getApplication("app2"),
                                    new EndpointGroupImpl("infected"),
                                    new EndpointGroupImpl("*"),
                                    ClassifierHelper.classifierEmpty(),
                                    ActionHelper.block(),
                                    false);

        //add policy B to framework service
        network.framework().add(pr,  network.getApplication("app1").appId());

        PolicyCompiler c = new PolicyCompiler();

        List<CompiledPolicy> list = c.compile(network.framework().getPolicies(),
                network.endpointService().getEndpoints(),
                network.framework().getActions(),
                network.framework().getTermTypes(),
                network.deviceService().getDevices());


      for (CompiledPolicy cp: list) {
          CompilerNode cn = (CompilerNode)cp;
          if (!cn.srcMembers().isEmpty() && !cn.dstMembers().isEmpty()) {
              System.out.println(cn.prettyPrint());
              //System.out.println(cn);
          }
      }
    }

}



