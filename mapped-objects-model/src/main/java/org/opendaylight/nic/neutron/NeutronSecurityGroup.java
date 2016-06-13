/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.neutron;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import java.util.List;

/**
 * See OpenStack Network API v2.0 Reference for description of
 * various Security Group attributes. The current fields are as follows:
 * <p>
 * security_group_id    uuid-str unique ID for the security group.
 * name                 String name of the security group.
 * description          String name of the security group.
 * tenant_id            uuid-str Owner of security rule..
 */

public class NeutronSecurityGroup {

    private String securityGroupID;
    private String securityGroupName;
    private String securityGroupDescription;
    private String securityTenantID;

    /**
     * Get the security_group_id
     * @return String
     */
    public String getSecurityGroupID(){
        return securityGroupID;
    }

    /**
     * Set the security_group_id
     * @param securityGroupID
     */
    public void setSecurityGroupID(String securityGroupID) {
        this.securityGroupID = securityGroupID;
    }

    /**
     * Get the group name
     * @return String
     */
    public String getSecurityGroupName() {
        return securityGroupName;
    }

    /**
     * Set the group name
     * @param securityGroupName
     */
    public void setSecurityGroupName(String securityGroupName) {
        this.securityGroupName = securityGroupName;
    }

    /**
     * Get the group description
     * @return String
     */
    public String getSecurityGroupDescription() {
        return securityGroupDescription;
    }

    /**
     * Set the group description
     * @param securityGroupDescription
     */
    public void setSecurityGroupDescription(String securityGroupDescription) {
        this.securityGroupDescription = securityGroupDescription;
    }

    /**
     * Get the tenant_id for this group
     * @return
     */
    public String getSecurityTenantID() {
        return securityTenantID;
    }

    /**
     * Set the tenant_id for this group
     * @param securityTenantID
     */
    public void setSecurityTenantID(String securityTenantID) {
        this.securityTenantID = securityTenantID;
    }
}
