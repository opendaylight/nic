//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.List;
import java.util.Set;

import org.opendaylight.nic.intent.Action;
import org.opendaylight.nic.intent.Classifier;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.Policy;



/**
 * <P>A {@link Policy} which is guaranteed to be orthogonal to any
 * other {@link CompiledPolicy} within a set.
 *
 * @author Shaun Wackerly
 */
public interface CompiledPolicy {

    /**
     * Returns the source endpoints for this policy.
     *
     * @return source endpoints
     */
    Set<Endpoint> src();

    /**
     * Returns the destination endpoints for this policy.
     *
     * @return destination endpoints
     */
    Set<Endpoint> dst();

    /**
     * Returns the classifier for this policy.
     *
     * @return the classifier
     */
    Classifier classifier();

    /**
     * Returns an ordered list of sets of actions for this policy.
     *
     * @return the set of actions
     */
    List<Set<Action>> actions();
}
