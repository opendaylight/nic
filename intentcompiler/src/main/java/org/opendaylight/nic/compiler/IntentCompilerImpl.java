//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.IntentCompiler;
import org.opendaylight.nic.compiler.PolicyCompiler;
import org.opendaylight.nic.compiler.api.Policy;

import java.util.List;

public class IntentCompilerImpl implements IntentCompiler {
    @Override
    public  List<Policy> compile(List<Policy> policies) {
        // TODO: Call the conflict resolution code
	PolicyCompiler compiler = new PolicyCompiler();
	  return compiler.compile(policies);
        
    }
}
