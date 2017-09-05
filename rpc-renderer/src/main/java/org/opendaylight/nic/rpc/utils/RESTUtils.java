/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Timestamp;

/**
 * Created by yrineu on 17/07/17.
 */
public class RESTUtils {

    private static final String DATA = "data";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonNode emptyNode = mapper.createObjectNode();

    public static String extractStringData(final JsonNode jsonNode) {
        return jsonNode.elements().next().get(DATA).asText();
    }

    public static Long extractLongData(final JsonNode jsonNode) {
        return jsonNode.elements().next().get(DATA).asLong();
    }

    public static Integer extractIntData(final JsonNode jsonNode) {
        return jsonNode.elements().next().get(DATA).asInt();
    }

    public static Integer extractIntData(final JsonArray jsonArray) {
        return jsonArray.get(0).getAsJsonObject().get(DATA).getAsInt();
    }

    public static Ipv4Address extractIpv4Address(final JsonArray jsonArray) {
        return Ipv4Address.getDefaultInstance(getFirstData(jsonArray));
    }

    public static MacAddress extractMacAddress(final JsonArray jsonArray) {
        return MacAddress.getDefaultInstance(getFirstData(jsonArray));
    }

    public static Timestamp extractTimeStamp(final JsonArray jsonArray) {
        return Timestamp.getDefaultInstance(getFirstData(jsonArray));
    }

    private static String getFirstData(final JsonArray jsonArray) {
        return jsonArray.get(0).getAsJsonObject().get(DATA).getAsString();
    }

    public static boolean isEmptyNode(final JsonNode jsonNode) {
        return (jsonNode.equals(emptyNode));
    }

    public static String buildDeviceURLRequest(
            final String httpIp,
            final Integer httpPort,
            final String requestedRPC) {
        final StringBuffer url = new StringBuffer();
        url.append("http://");
        url.append(httpIp);
        url.append(":");
        url.append(httpPort);
        url.append("/rpc/");
        url.append(requestedRPC);
        return url.toString();
    }
}
