/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 25/05/17.
 */
public class JsonFormatUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JsonFormatUtils.class);

    private static final String IPV4_ADDRESS = "ipv4_address";
    private static final String ODL_NIC = "odl-nic";
    private static final String URL_TO_RECEIVE_NOTIFICATIONS = "/notifications/alert"; //TODO: TBD

    public static String createJsonBy(final String ipv4Address) {
        final ObjectMapper objectMapper = createObjectMapper();
        final ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put(IPV4_ADDRESS, ipv4Address);
        LOG.info("\n#### JSONnode: {}", jsonNode.toString());

        return createJsonResultBy(objectMapper.createArrayNode(), jsonNode);
    }

    public static String createJsonBy(final String ipv4Address,
                                      final String urlToBeNotifier) {
        final ObjectMapper objectMapper = createObjectMapper();
        final ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put(URL_TO_RECEIVE_NOTIFICATIONS, urlToBeNotifier);
        jsonNode.put(IPV4_ADDRESS, ipv4Address);
        LOG.info("\n#### JSONnode: {}", jsonNode.toString());

        return createJsonResultBy(objectMapper.createArrayNode(), jsonNode);
    }

    private static ObjectMapper createObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        return objectMapper;
    }

    private static String createJsonResultBy(final ArrayNode arrayNode,
                                      final ObjectNode jsonNode) {
        arrayNode.add(jsonNode);
        return "{\"" + ODL_NIC + "\":" + arrayNode.toString() + "}";
    }
}
