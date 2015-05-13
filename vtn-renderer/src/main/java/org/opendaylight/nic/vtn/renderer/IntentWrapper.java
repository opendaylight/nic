/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.vtn.renderer;

public class IntentWrapper {

    String entityName;
    int entityValue;
    String entityDescription;
    public String getEntityDescription() {
        return entityDescription;
    }
    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }
    public String getEntityName() {
        return entityName;
    }
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public int getEntityValue() {
        return entityValue;
    }
    public void setEntityValue(int index) {
        this.entityValue = index;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((entityDescription == null) ? 0 : entityDescription
                        .hashCode());
        result = prime * result
                + ((entityName == null) ? 0 : entityName.hashCode());
        result = prime * result + entityValue;
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
        IntentWrapper other = (IntentWrapper) obj;
        if (entityDescription == null) {
            if (other.entityDescription != null)
                return false;
        } else if (!entityDescription.equals(other.entityDescription))
            return false;
        if (entityName == null) {
            if (other.entityName != null)
                return false;
        } else if (!entityName.equals(other.entityName))
            return false;
        if (entityValue != other.entityValue)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IntentWrapper [entityName=" + entityName + ", entityValue="
                + entityValue + ", entityDescription=" + entityDescription
                + "]";
    }
}
