/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

public class OFRendererConstants {

    public static final String NODE_CONNECTOR = "switch_port";

    public static final Integer DEFAULT_IDLE_TIMEOUT = 0;

    public static final Integer DEFAULT_HARD_TIMEOUT = 0;

    public static final Integer DEFAULT_PRIORITY = 9000;

    public static final Short FALLBACK_TABLE_ID = 0;

    public static final String ARP_REPLY_TO_CONTROLLER_FLOW_NAME = "arpReplyToController";

    public static final int ARP_REPLY_TO_CONTROLLER_FLOW_PRIORITY = 10000;

    public static final Integer SRC_END_POINT_GROUP_INDEX = 0;

    public static final Integer DST_END_POINT_GROUP_INDEX = 1;

    public static final String ANY_MATCH = "any";

    public static final String INTENT_L2_FLOW_NAME = "L2_Rule_";

    public static final String MPLS_LABEL_KEY = "mpls_label";

    public static final String LLDP_REPLY_TO_CONTROLLER_FLOW_NAME = "lldpReplyToController";

    public static final int LLDP_REPLY_TO_CONTROLLER_FLOW_PRIORITY = 9500;

    public static final int LLDP_ETHER_TYPE = 35020;

    public static final String ETHERNET_TYPE = "_EthernetType_";

    public static final String IP_PREFIX_KEY = "ip_prefix";

    public static final String SWITCH_PORT_KEY = "switch_port";

    public static final String INTENT_MPLS_FLOW_NAME = "MPLS_Rule_";

    public static final int FAILOVER_CONSTRAINT_INPUT_SIZE = 2;

}