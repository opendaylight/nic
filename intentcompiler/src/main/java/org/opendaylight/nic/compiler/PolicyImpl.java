//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.*;

import java.util.Set;

public class PolicyImpl implements Policy {
    
    private Set<Endpoint> src;
    private Set<Endpoint> dst;
    Set<Classifier> classifier;
    Action action;
    
    @Override
    public Set<Endpoint> src(){
	return src;
    }
    
    @Override
    public Set<Endpoint> dst() {
	return dst;
    }
    
  //  @Override
  //  public Set<Classifier> classifier() {
  //	return classifier;
  //  }
    
    @Override
    public Action action() {
	return action;
    }
    
    
}