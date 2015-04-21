//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import java.util.Map;

import org.opendaylight.nic.common.Application;
import org.opendaylight.nic.extensibility.ActionLabel;

/**
 * A principle which applications (or administrators) specify to guide decisions
 * on the treatment of traffic on the network. A policy is a statement of intent
 * with respect to specific communications between endpoints on the
 * network.Application request for setting network behavior.
 * <p>
 * Network policies are expressed as source and destination
 * {@link EndpointGroup}s, {@link Classifier} describing the traffic of interest
 * between {@link Endpoint}s, and a set of {@link Action}s to take on the
 * traffic.
 *
 * @author Duane Mentze
 */
public interface Policy {

    /**
     * Returns policy name.
     */
    String name();

    /**
     * Returns policy id.
     */
    PolicyId PolicyId();

    /**
     * Returns the application for this policy.
     *
     * @return application
     */
    Application application();

    /**
     * Returns the source endpoint group for this policy.
     *
     * @return source endpoint group
     */
    EndpointGroup src();

    /**
     * Returns the destination endpoint group for this policy.
     *
     * @return destination endpoint group
     */
    EndpointGroup dst();

    /**
     * Returns the classifier for this policy.
     *
     * @return the classifier
     */
    Classifier classifier();

    /**
     * Returns the map of ActionLabls to AuxiliaryData for this policy.
     *
     * @return the map
     */
    Map<ActionLabel, AuxiliaryData> actionLabelToAuxDataMap();

    /**
     * Returns true if exclusive, otherwise false.
     *
     * @return exclusivity of policy
     */
    boolean isExclusive();
}
