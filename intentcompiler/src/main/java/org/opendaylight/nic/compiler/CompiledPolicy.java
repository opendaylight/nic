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

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.Classifier;
import org.opendaylight.nic.compiler.api.Endpoint;

// A Policy which is guaranteed to be orthogonal 

public interface CompiledPolicy {
	
	Set<Endpoint> src();
	Set<Endpoint> dst();
	Set<Classifier> classifier();
	List<Action> actions();
}



