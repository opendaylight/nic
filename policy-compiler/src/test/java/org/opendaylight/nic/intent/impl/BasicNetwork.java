//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.common.ApplicationImpl;
import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.DeviceId;
import org.opendaylight.nic.common.SegmentId;
import org.opendaylight.nic.extensibility.actions.AllowActionType;
import org.opendaylight.nic.extensibility.actions.AuditActionType;
import org.opendaylight.nic.extensibility.actions.BlockActionType;
import org.opendaylight.nic.extensibility.actions.LatencyActionType;
import org.opendaylight.nic.extensibility.terms.EthTypeTermType;
import org.opendaylight.nic.extensibility.terms.VlanTermType;
import org.opendaylight.nic.intent.EndpointId;
import org.opendaylight.nic.intent.impl.DeviceImpl;
import org.opendaylight.nic.intent.impl.EndpointAttributeImpl;
import org.opendaylight.nic.intent.impl.IpEndpoint;
import org.opendaylight.nic.intent.impl.PortImpl;
import org.opendaylight.nic.services.impl.ApplicationServiceImpl;
import org.opendaylight.nic.services.impl.DeviceServiceImpl;
import org.opendaylight.nic.services.impl.EndpointServiceImpl;
import org.opendaylight.nic.services.impl.PolicyFrameworkImpl;

public class BasicNetwork {


    final long MGMT_PRIORITY = 100;

    public PolicyFrameworkImpl framework;
    public ApplicationServiceImpl appService;
    public EndpointServiceImpl endpointService;
    public DeviceServiceImpl deviceService;

    SegmentId segId;
    ApplicationImpl managementApp;


    public PolicyFrameworkImpl framework() {
        return framework;
    }

    public ApplicationServiceImpl appService() {
        return appService;
    }

    public EndpointServiceImpl endpointService() {
        return endpointService;
    }

    public DeviceServiceImpl deviceService() {
        return deviceService;
    }

    public SegmentId odlSegmentId() {
        return segId;
    }

    public ApplicationImpl getManagementApp() {
        return managementApp;
    }

    public ApplicationImpl getApplication(String app) {
        return new ApplicationImpl(appService.get(new AppId(app)));
    }

    public void addAttribute(String endpointIp, String attribute) {
        EndpointId id;
        try {
            id = new IpEndpointId(InetAddress.getByName(endpointIp), segId);
            endpointService.apply(new EndpointAttributeImpl(attribute), id);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void addDevice(String device) {
        //add device
        Device odlDevice = new DeviceImpl(new DeviceId(device));
        deviceService.add(odlDevice);
    }

    public void addEndpoint(String device, String endpointIp) {
        DeviceId id = new DeviceId(device);
        IpEndpoint ep = makeEndpoint(endpointIp, Integer.valueOf(1), segId, deviceService.getDevices().get(id));
        endpointService.add(ep);
    }


    public void registerAttribute(String attribute) {
        endpointService.register(new EndpointAttributeImpl(attribute), managementApp.appId());
    }

    public void addApplication(String app, long priority) {
        //add application
        ApplicationImpl a = new ApplicationImpl(app, priority);
        appService.add(a);

    }

    public static BasicNetwork buildSingleVlanNetwork(Integer vlan) {
        SegmentId segId = new SegmentId(vlan);
        return new BasicNetwork(segId);

    }

    private BasicNetwork(SegmentId segId) {

        //create policy framework
        appService = new ApplicationServiceImpl();
        endpointService = new EndpointServiceImpl(appService);
        deviceService = new DeviceServiceImpl();
        framework = new PolicyFrameworkImpl(appService);

        //register TermTypes
        framework.register(VlanTermType.getInstance());
        framework.register(EthTypeTermType.getInstance());

        //add actions
        framework.register(BlockActionType.getInstance());
        framework.register(AllowActionType.getInstance());
        framework.register(AuditActionType.getInstance());
        framework.register(LatencyActionType.getInstance());

        managementApp = new ApplicationImpl("mgmt", MGMT_PRIORITY);
        appService.add(managementApp);

        this.segId = segId;

    }

    private static IpEndpoint makeEndpoint(String ip, Integer port, SegmentId seg, Device device) {
        IpEndpoint ep1 = null;
        try {
            ep1 = new IpEndpoint( InetAddress.getByName(  ip),
                                                            new PortImpl(port),
                                                            seg,
                                                            device);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            fail("UnknownHostException");
        }
        return ep1;
    }

}
