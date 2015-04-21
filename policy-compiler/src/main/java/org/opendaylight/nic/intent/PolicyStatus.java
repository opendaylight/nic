//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

/**
 * Represents the status of a Policy applied to a policy domain.
 *
 * @author Duane Mentze
 */
public enum PolicyStatus {
    // FIXME Need to revisit how to handle status values, and asynchronous
    // updates.
    Enforced, PreEnforcement, NoServiceGroupsAvailable, PartiallyEnforced, BadSrcEndpointGroup, BadDestEndpointGroup, BadClassifier, BadOperationContext
}
