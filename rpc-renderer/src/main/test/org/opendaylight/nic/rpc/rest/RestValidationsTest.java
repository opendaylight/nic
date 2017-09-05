/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import com.google.common.collect.Sets;
import com.google.gson.*;
import com.google.gson.internal.bind.JsonTreeWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.nic.rpc.exception.JuniperRestException;
import org.opendaylight.nic.rpc.model.juniper.information.device.SwitchInformation;
import org.opendaylight.nic.rpc.model.juniper.information.evpn.DatabaseInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Timestamp;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by yrineu on 19/07/17.
 */
@PrepareForTest(RestValidations.class)
@RunWith(PowerMockRunner.class)
public class RestValidationsTest {

    @Test(expected = JuniperRestException.class)
    public void testNullRequest() {
        RestValidations.validateReceivedRequest(null);
    }

    @Test(expected = JuniperRestException.class)
    public void testInvalidRequest() {
        RestValidations.validateReceivedRequest("/192.1681111123::");
    }

    @Test(expected = JuniperRestException.class)
    public void testRequestWithInvalidIp() {
        RestValidations.validateReceivedRequest("/nic/192.166666");
    }

    @Test
    public void testWithValidRequest() {
        RestValidations.validateReceivedRequest("/nic/192.168.1.1");
    }

    @Test
    public void test() {

        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(getMockResultAsString());
        final JsonElement cosElement = element.getAsJsonObject().get("evpn-database-information");
//        System.out.println(element.getAsJsonObject().get("interface-cos-summary").toString());
        final Iterator<JsonElement> iterator = cosElement.getAsJsonArray().iterator();
        while (iterator.hasNext()) {
            JsonElement el = iterator.next();
//            System.out.println(el.getAsJsonObject().get("intf-cos-forwarding-classes-supported").getAsJsonArray().get(0).getAsJsonObject().get("data"));
        }
//
//        DatabaseInfo databaseInfo = new DatabaseInfo(
//                100,
//                MacAddress.getDefaultInstance("00:00:00:00:00:01"),
//                Ipv4Address.getDefaultInstance("192.168.1.1"),
//                Ipv4Address.getDefaultInstance("10.0.0.1"));
////        System.out.println(getMockResultAsString());
//
//        SwitchInformation switchInformation = new SwitchInformation(Ipv4Address.getDefaultInstance("192.168.1.1"), 300, "Serro", "1234");
//        Gson gson = new Gson();
////        System.out.println(gson.toJson(switchInformation));
////        switchInformation = new SwitchInformation(switchInformation, databaseInfo);
//        System.out.println(result);

        Set<DatabaseInfo> jsons = Sets.newConcurrentHashSet();
        DatabaseInfo databaseInfoA = new DatabaseInfo("SwitchA", "f4:15:63:7e:84:25");
        DatabaseInfo databaseInfoB = new DatabaseInfo("SwitchB", "f4:15:63:7e:84:35");

        databaseInfoA.extractEvpnInfoToJson(getMockResultAsString());
        databaseInfoB.extractEvpnInfoToJson(getMockResultAsString());
        jsons.add(databaseInfoA);
        jsons.add(databaseInfoB);

        Gson res = new Gson();
        System.out.println(res.toJson(jsons));
    }

    public String getMockResultAsString() {
        return "{\"evpn-database-information\":[{\"evpn-database-instance\":[{\"attributes\":{\"xmlns\":" +
                "\"http://xml.juniper.net/junos/17.3R1/junos-routing\",\"junos:style\":\"normal\"},\"instance-name\"" +
                ":[{\"data\":\"default-switch\"}],\"mac-entry\":[{\"attributes\":{\"xmlns\":" +
                "\"http://xml.juniper.net/junos/17.3R1/junos-routing\",\"junos:style\":\"normal\"}," +
                "\"vni-id\":[{\"data\":\"999\"}],\"mac-address\":[{\"data\":\"f4:15:63:7e:84:25\"}]," +
                "\"active-source\":[{\"data\":\"10.200.19.21\"}],\"active-source-timestamp\":[{\"data\":" +
                "\"Sep 01 21:43:01\"}],\"ip-address\":[{\"data\":\"10.132.252.8\"}]}]}]}]}";
    }
}
