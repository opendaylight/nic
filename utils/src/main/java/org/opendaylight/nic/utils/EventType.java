/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.utils;

/**
 * Represents received Events based on user actions or network behaviors
 */
public enum EventType {
    /** Indicates a new link is discovered. */
    LINK_DISCOVERED,
    /** Indicates an existing link is overutilized. */
    LINKOVER_UTILIZED,
    /** Indicates an existing link is remove. */
    LINK_REMOVED,
    /** Indicates normal link utilization. */
    LINK_UTILIZATION_NORMAL,
    /** Indicates new node is up. */
    NODE_ADDED,
    /** Indicates new node is updated. */
    NODE_UPDATED,
    /** Indicates existing node is remove. */
    NODE_REMOVED,
    /** Indicates a new intent is add */
    INTENT_ADDED,
    /** Indicates that a given Intent was disabled */
    INTENT_DISABLED,
    /** Indicates that that the disable event was performed for a given Intent */
    INTENT_DISABLE,
    /** Indicates that a given Intent was enabled */
    INTENT_ENABLED,
    /** Indicates that NIC will retry to apply a given Intent after a failure */
    INTENT_ADDED_RETRY,
    /** Indicates a new intent-NBI is add */
    INTENT_NBI_ADDED,
    /** Indicates a intent is update */
    INTENT_UPDATE,
    /** Indicates a intent-NBI is update */
    INTENT_NBI_UPDATE,
    /** Indicates existing intent is remove */
    INTENT_REMOVED,
    /** Indicates that NIC will retry to remove a given Intent after a failure */
    INTENT_REMOVED_RETRY,
    /** Indicates existing intent-NBI is remove */
    INTENT_NBI_REMOVED,
    /** Indicates that a new endpoint is discovered but it doesn't mean that the just came up*/
    ENDPOINT_DISCOVERED,
    /** Indicates that a new security group is add by neutron*/
    SECURITY_GROUP_ADDED,
    /** Indicates that an existing security group is deleted by neutron*/
    SECURITY_GROUP_DELETED,
    /** Indicates that an existing security group is modified by neutron*/
    SECURITY_GROUP_UPDATED,
    /** Indicates that a new security rule is add by neutron*/
    SECURITY_RULE_ADDED,
    /** Indicates that an existing security rule is deleted by neutron*/
    SECURITY_RULE_DELETED,
    /** Indicates that an existing security rule is modified by neutron*/
    SECURITY_RULE_UPDATED,
    /** Indicates that a Network-Topology Link has been discovered. The OpenFlowPlugin
     * project is taking care of keeping an up to date Network-Topology.*/
    TOPOLOGY_LINK_DISCOVERED,
    /** Incidates that a Network-Topology Link has been remove. See TOPOLOGY_LINK_DISCOVERED.*/
    TOPOLOGY_LINK_REMOVED,
    /** Indicates that a Network-Topology Link has been updated. See TOPOLOGY_LINK_DISCOVERED.*/
    TOPOLOGY_LINK_UPDATED,
    /** Graph Edge Added*/
    GRAPH_EDGE_ADDED,
    /** Graph Edge Removed*/
    GRAPH_EDGE_DELETED,
    /** Graph Edge Updated*/
    GRAPH_EDGE_UPDATED,
    /** New transaction created*/
    INTENT_STATE_TRANSACTION,
    /** New Intent limiter created*/
    INTENT_LIMITER_ADDED,
    /** Intent limiter updated*/
    INTENT_LIMITER_UPDATED,
    /** Intent limiter remove*/
    INTENT_LIMITER_REMOVED,
    /** Intent add with success*/
    INTENT_ADDED_WITH_SUCCESS,
    /** Intent remove with sucess*/
    INTENT_REMOVED_WITH_SUCCESS,
    /** Intent add error*/
    INTENT_ADDED_ERROR,
    /** Intent remove error*/
    INTENT_REMOVE_ERROR,
    /** Indicates that the max retries attempts was achieved.*/
    INTENT_ADD_RETRY_WITH_MAX_ATTEMPTS,
    /** Indicates that the max attempts to remove a given Intent was achieved.*/
    INTENT_REMOVE_RETRY_WITH_MAX_ATTEMPTS,
    /** Indicates that an Intent was created */
    INTENT_CREATED,
    /** Indicates that the Intent is being added */
    INTENT_BEING_ADDED,
    /** Indicates that the Intent is being removed */
    INTENT_BEING_REMOVED,
    /** Indicates that the Intent is being disabled */
    INTENT_BEING_DISABLED,
    /** Indicates a new attempt to add a given Intent */
    INTENT_ADD_ATTEMPT,
    /** Indicates a new attempt to remove a given Intent */
    INTENT_REMOVE_ATTEMPT,
    /** Indicates that a given Intent was removed with inconsistencies */
    INTENT_DISABLED_WITH_INCONSISTENCIES,
    /** Indicates that a given Intent for Internet Service Providers was created */
    INTENT_ISP_ADDED,
    /** Indicates that a given Intent for Internet Service Providers was updated */
    INTENT_ISP_UPDATED,
    /** Indicates that a given Intent for Internet Service Providers was removed */
    INTENT_ISP_REMOVED
}
