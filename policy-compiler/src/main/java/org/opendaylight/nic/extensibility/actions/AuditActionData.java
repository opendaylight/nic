//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.actions;

/**
 * The auxiliary data which accompanies a {@link AuditActionType}.
 *
 * @author Duane Mentze
 */
@SuppressWarnings("serial")
public class AuditActionData extends BaseAuxiliaryData {

    private final String auditService;
    public static final String AUDIT_SERVICE_KEY = "auditService";

    public AuditActionData(String auditService) {
        super();
        this.put(AUDIT_SERVICE_KEY, auditService);
        this.auditService = auditService;
    }

    /**
     * Returns the auditService attribute
     *
     * @return auditService value
     */
    public String auditService() {
        return auditService;
    }

}
