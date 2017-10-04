/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflow;

/**
 * Created by yrineu on 10/07/17.
 */
public final class BgpDataflowParser {

    private static final String BGP_INET_IPV4_ROUTES = "bgp-inet:ipv4-routes";
    private static final String PREFIX = "prefix";
    private static final String IPV4_ROUTE = "ipv4-route";
    private static final String PATH_ID = "path-id";
    private static final String ATTRIBUTES = "attributes";
    private static final String IPV4_NEXT_HOP = "ipv4-next-hop";
    private static final String GLOBAL = "global";
    private static final String AS_PATH = "as-path";
    private static final String ORIGIN = "origin";
    private static final String VALUE = "value";
    private static final String LOCAL_PREF = "local-pref";
    private static final String PREF = "pref";

    private static final String ORIGIN_IGP = "igp";
    private static final String FIXED_PREF = "100";

    private BgpDataflowParser() {
    }

    public static String fromBgpDataFlow(final BgpDataflow bgpDataflow) {
        final ObjectMapper objectMapper = createObjectMapper();
        final ObjectNode ipv4NextHopNode = objectMapper.createObjectNode();
        final ObjectNode originNode = objectMapper.createObjectNode();
        final ObjectNode localPrefNode = objectMapper.createObjectNode();
        final ObjectNode asPathNode = objectMapper.createObjectNode();

        final ObjectNode attributesNode = objectMapper.createObjectNode();

        ipv4NextHopNode.put(GLOBAL, bgpDataflow.getGlobalIp().getValue());
        attributesNode.put(IPV4_NEXT_HOP, ipv4NextHopNode);
        attributesNode.put(AS_PATH, asPathNode);
        originNode.put(VALUE, ORIGIN_IGP);
        attributesNode.put(ORIGIN, originNode);
        localPrefNode.put(PREF, FIXED_PREF);
        attributesNode.put(LOCAL_PREF, localPrefNode);


        final ObjectNode ipv4RouteAttributesNode = objectMapper.createObjectNode();
        ipv4RouteAttributesNode.put(PREFIX, bgpDataflow.getPrefix().getValue());
        ipv4RouteAttributesNode.put(PATH_ID, bgpDataflow.getPathId());
        ipv4RouteAttributesNode.put(ATTRIBUTES, attributesNode);

        final ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.add(ipv4RouteAttributesNode);

        final ObjectNode ipv4RouteNode = objectMapper.createObjectNode();
        ipv4RouteNode.put(IPV4_ROUTE, arrayNode);

        final ObjectNode bgpInetIpv4Routes = objectMapper.createObjectNode();
        bgpInetIpv4Routes.put(BGP_INET_IPV4_ROUTES, ipv4RouteNode);

        return bgpInetIpv4Routes.toString();
    }

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        return objectMapper;
    }
}
