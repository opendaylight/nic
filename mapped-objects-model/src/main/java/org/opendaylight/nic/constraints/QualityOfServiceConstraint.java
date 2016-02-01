/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.constraints;

import org.opendaylight.nic.mapped.MappedObject;

public class QualityOfServiceConstraint extends MappedObject{

    public static final String PROFILE_NAME = "profileName";
    public static String TYPE = "QosConstraint";

    public void setProfileName(String profileName) {
        this.properties.put(PROFILE_NAME, profileName);
    }

    public QualityOfServiceConstraint(String profileName){
        setProfileName(profileName);
        this.type = TYPE;
    }

    public String type() {
        return type;
    }

    public String key() {
        return getProfileName();
    }

    public String getProfileName() {
        return this.getProperty(PROFILE_NAME);
    }

    public void setKey(String key) { 
        this.key = key;
    }

    public static QualityOfServiceConstraint fromMappedObject(MappedObject obj) {
        QualityOfServiceConstraint qosConstraint = new QualityOfServiceConstraint(obj.getProperty(PROFILE_NAME));
        return qosConstraint;
    }
}
