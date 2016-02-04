/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api;

public enum EventType {
    /** Indicates a new link is discovered. */
    LINK_DISCOVERED,
    /** Indicates an existing link is overutilized. */
    LINKOVER_UTILIZED,
    /** Indicates an existing link is removed. */
    LINK_REMOVED,
    /** Indicates normal link utilization. */
    LINK_UTILIZATION_NORMAL,
    /** Indicates new node is up. */
    NODE_ADDED,
    /** Indicates new node is updated. */
    NODE_UPDATED,
    /** Indicates existing node is removed. */
    NODE_REMOVED,
    /** Indicates a new intent is added */
    INTENT_ADDED,
    /** Indicates a intent is update */
    INTENT_UPDATE,
    /** Indicates existing intent is removed */
    INTENT_REMOVED,
    /** Indicates that a new endpoint is discovered but it doesn't mean that the just came up*/
    ENDPOINT_DISCOVERED,
    /** Indicates that a new security group is added by neutron*/
    SECURITY_GROUP_ADDED,
    /** Indicates that an existing security group is deleted by neutron*/
    SECURITY_GROUP_DELETED,
    /** Indicates that an existing security group is modified by neutron*/
    SECURITY_GROUP_UPDATED,
    /** Indicates that a new security rule is added by neutron*/
    SECURITY_RULE_ADDED,
    /** Indicates that an existing security rule is deleted by neutron*/
    SECURITY_RULE_DELETED,
    /** Indicates that an existing security rule is modified by neutron*/
    SECURITY_RULE_UPDATED,
    /** Indicates that a Network-Topology Link has been discovered. The OpenFlowPlugin
     * project is taking care of keeping an up to date Network-Topology.*/
    TOPOLOGY_LINK_DISCOVERED,
    /** Incidates that a Network-Topology Link has been removed. See TOPOLOGY_LINK_DISCOVERED.*/
    TOPOLOGY_LINK_REMOVED,
    /** Indicates that a Network-Topology Link has been updated. See TOPOLOGY_LINK_DISCOVERED.*/
    TOPOLOGY_LINK_UPDATED,
    /** Graph Edge Added*/
    GRAPH_EDGE_ADDED,
    /** Graph Edge Removed*/
    GRAPH_EDGE_DELETED,
    /** Graph Edge Updated*/
    GRAPH_EDGE_UPDATED

}
