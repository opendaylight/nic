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
    
    @Override
    public Set<Classifier> classifier() {
	return classifier;
    }
    
    @Override
    public Action action() {
	return action;
    }
    
    
}