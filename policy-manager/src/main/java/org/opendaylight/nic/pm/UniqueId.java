//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.pm;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.AppId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.DomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.PolicyId;

/**
 * A globally-unique ID which identifies a specific policy within
 * a specific application, within a specific domain.
 *
 * @author Shaun Wackerly
 */
public class UniqueId {

    private final DomainId dom;
    private final AppId app;
    private final PolicyId pol;

    public UniqueId(DomainId dom, AppId app, PolicyId pol) {
        this.dom = dom;
        this.app = app;
        this.pol = pol;
    }

    /**
     * Gets the domain ID associated with this unique ID.
     *
     * @return domain ID
     */
    public DomainId domainId() {
        return dom;
    }

    /**
     * Gets the app ID associated with this unique ID.
     *
     * @return app ID
     */
    public AppId appId() {
        return app;
    }

    /**
     * Gets the policy ID associated with this unique ID.
     *
     * @return policy ID
     */
    public PolicyId policyId() {
        return pol;
    }

    @Override
    public String toString() {
        return "UniqueId [dom=" + dom + ", app=" + app + ", pol=" + pol + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((app == null) ? 0 : app.hashCode());
        result = prime * result + ((dom == null) ? 0 : dom.hashCode());
        result = prime * result + ((pol == null) ? 0 : pol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UniqueId other = (UniqueId) obj;
        if (app == null) {
            if (other.app != null)
                return false;
        } else if (!app.equals(other.app))
            return false;
        if (dom == null) {
            if (other.dom != null)
                return false;
        } else if (!dom.equals(other.dom))
            return false;
        if (pol == null) {
            if (other.pol != null)
                return false;
        } else if (!pol.equals(other.pol))
            return false;
        return true;
    }
}
