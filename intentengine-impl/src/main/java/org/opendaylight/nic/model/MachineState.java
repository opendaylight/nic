/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.model;

public enum MachineState {
    UNDEPLOYED,
    UNDEPLOYING,
    DEPLOYING,
    DEPLOYED,
    DEPLOY_FAILED,
    UNDEPLOY_FAILED,
    DISABLING,
    DISABLED
}
