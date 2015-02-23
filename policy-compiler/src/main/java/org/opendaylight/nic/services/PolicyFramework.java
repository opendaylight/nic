//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services;

import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.ActionType;
import org.opendaylight.nic.extensibility.CodeGenerator;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermType;

/**
 * Provides a mechanism by which policy framework is populated with
 * registered {@link ActionType}s, {@link TermType}s, and
 * {@link CodeGenerator}s. This framework is closely tied to the
 * {@link PolicyService}, which uses the registered entities to perform
 * policy compilation.
 *
 * @author Shaun Wackerly
 */
public interface PolicyFramework {

    /**
     * Registers the given action to be used as the implementation
     * of the given action label.
     *
     * @param act action description
     */
    void register(ActionType act);

    /**
     * Gets the action type associated with the given label. If no action
     * is registered for the label, null is returned.
     *
     * @param label action label
     * @return registered action
     */
    ActionType getAction(ActionLabel label);

    /**
     * Registers the given term type to be used as the implementation
     * of the given term type label.
     *
     * @param tt term type implementation
     */
    void register(TermType tt);

    /**
     * Gets the term type associated with the given label. If no term type
     * is registered for the label, null is returned.
     *
     * @param label term label
     * @return registered action
     */
    TermType getTermType(TermLabel label);

    /**
     * Registers a code generator which will translate compiled policies
     * into a native format.
     *
     * @param codegen code generator implementation
     */
    void register(CodeGenerator<?,?,?> codegen);

}
