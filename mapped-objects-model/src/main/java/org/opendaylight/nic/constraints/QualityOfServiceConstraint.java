/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.constraints;

import org.opendaylight.nic.mapped.MappedObject;

public class QualityOfServiceConstraint implements MappedObject{

    public static String type = "QosConstraint";
    private String profileName;

    public QualityOfServiceConstraint(String profileName){
        this.profileName = profileName;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String key() {
        return profileName;
    }

    public String getProfileName() {
        return profileName;
    }
}