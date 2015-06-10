//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.api;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

public class IntentCompilerException extends Exception {
    private Collection<Policy> relatedPolicies;

    public IntentCompilerException(String message) {
        this(message, null);
    }

    public IntentCompilerException(String message, Collection<Policy> relatedPolicies) {
        super(message);
        this.relatedPolicies = relatedPolicies;
    }

    public Collection<Policy> getRelatedPolicies() {
        return ImmutableList.copyOf(relatedPolicies);
    }
}
