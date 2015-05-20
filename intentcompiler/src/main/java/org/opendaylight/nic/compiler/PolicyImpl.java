package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.*;

import java.util.Set;

public class PolicyImpl implements Policy {
    private Set<Endpoint> src;
    private Set<Endpoint> dst;
    Action action;

    public PolicyImpl(Set<Endpoint> src, Set<Endpoint> dst, Action action) {
        this.src = src;
        this.dst = dst;
        this.action = action;
    }

    @Override
    public Set<Endpoint> src(){
	return src;
    }
    
    @Override
    public Set<Endpoint> dst() {
	return dst;
    }
    
    @Override
    public Action action() {
	return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PolicyImpl policy = (PolicyImpl) o;

        if (src != null ? !src.equals(policy.src) : policy.src != null) return false;
        if (dst != null ? !dst.equals(policy.dst) : policy.dst != null) return false;
        return action == policy.action;

    }

    @Override
    public int hashCode() {
        int result = src != null ? src.hashCode() : 0;
        result = 31 * result + (dst != null ? dst.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        return result;
    }
}