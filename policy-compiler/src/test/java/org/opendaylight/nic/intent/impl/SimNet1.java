//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

/**
 * Creates a simulated network based on a single vlan network, with a specific set of resources.
 * <p>
 * network devices [ "access_sw1" ]
 * endpoints[attributes] = 10.0.0.1[employee], 
 *                         10.0.0.2[database],
 *                         10.0.0.3[authorziedDnsServers]
 *                         10.0.0.4[ips]
 * Test                         
 * 
 * @author Duane Mentze
 *     
 */
public class SimNet1 extends SingleVlanNetwork {
    
    
    SimNet1() {
        super();
        
        //add device
        addDevice("access_sw1");

        //add endpoint attribute
        registerAttribute("employee");
        registerAttribute("database");
        registerAttribute("authorziedDnsServers");
        registerAttribute("ips");     

        //make endpoints
        addEndpoint("access_sw1", "10.0.0.1");
        addEndpoint("access_sw1", "10.0.0.2");
        addEndpoint("access_sw1", "10.0.0.3");
        addEndpoint("access_sw1", "10.0.0.4");        

        //apply attributes to endpoints
        addAttribute("10.0.0.1", "employee");
        addAttribute("10.0.0.2", "database");
        addAttribute("10.0.0.3", "authorizedDnsServers");
        addAttribute("10.0.0.4", "ips");      
                
    }

}
