//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opendaylight.nic.compiler.*;
import org.opendaylight.nic.compiler.api.*;

/**
 * Detects and resolves overlaps in a {@link CompilerNode} List.
 */

public class DetectResolve {
    
    private final List<CompilerNode> list;
    
    public DetectResolve(List<CompilerNode> list) {
	 this.list=list;
    }
    
    public boolean detectAndResolve() {
	if (list.size() <= 1) {
            return false;
        }
	return true;
    }
}

