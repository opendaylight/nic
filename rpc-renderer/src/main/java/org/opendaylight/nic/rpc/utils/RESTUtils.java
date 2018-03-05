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

    public static boolean isEmptyNode(final JsonNode jsonNode) {
        return (jsonNode.equals(emptyNode));
    }
}
