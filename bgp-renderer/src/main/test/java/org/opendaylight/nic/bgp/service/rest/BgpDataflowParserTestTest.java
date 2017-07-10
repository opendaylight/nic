/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service.rest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflows.BgpDataflowBuilder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

/**
 * Created by yrineu on 10/07/17.
 */
@PrepareForTest(BgpPrefixRESTServices.class)
@RunWith(PowerMockRunner.class)
public class BgpDataflowParserTestTest {

    @Test
    public void validateParser() {
        final BgpDataflowBuilder builder = new BgpDataflowBuilder();
        builder.setOriginatorIp(new Ipv4Address("10.0.0.1"));
        builder.setPrefix(new Ipv4Prefix("10.0.0.10/23"));
        builder.setId(Uuid.getDefaultInstance(UUID.randomUUID().toString()));
        builder.setGlobalIp(new Ipv4Address("192.168.122.254"));
        builder.setPathId(6633L);
        Assert.assertEquals(getMockResultAsString(), org.opendaylight.nic.bgp.service.parser.BgpDataflowParser.fromBgpDataFlow(builder.build()));

    }

    private String getMockResultAsString() {
        return "{\"bgp-inet:ipv4-routes\":{\"ipv4-route\":[{\"prefix\":\"10.0.0.10/23\",\"path-id\":6633," +
                "\"attributes\":{\"ipv4-next-hop\":{\"global\":\"192.168.122.254\"},\"as-path\":{}," +
                "\"origin\":{\"value\":\"igp\"},\"local-pref\":{\"pref\":\"100\"}}}]}}";
    }
}
