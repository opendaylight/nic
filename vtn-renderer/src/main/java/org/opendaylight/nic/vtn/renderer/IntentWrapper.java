/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

/**
 * The IntentWrapper class creates the VTN entity's to be stored.
 */
public class IntentWrapper {

    private int entityValue;
    private String entityName;
    private String entityDescription;
    private String action;

    /**
     * Returns the value for the entity.
     * @return entityDescription  A brief description about the entity type.
     */
    public String getEntityDescription() {
        return entityDescription;
    }

    /**
     * Sets the value for the entity.
     * @param entityDescription  A brief description about the entity type.
     */
    public void setEntityDescription(String entityDescription) {
        this.entityDescription = entityDescription;
    }

    /**
     * Returns the entity type that is stored.
     * @return entityName  Name of the entity stored.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * The entity type to be stored.
     * @param entityName  Type of the entity stored.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * Retrieves the index value of entity.
     * @return entityValue  Entity id stored.
     */
    public int getEntityValue() {
        return entityValue;
    }

    /**
     * Sets the index value of entity.
     * @param index  index value of the entity.
     */
    public void setEntityValue(int index) {
        this.entityValue = index;
    }

    /**
     * Retrieves the action of entity
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
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
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IntentWrapper other = (IntentWrapper) obj;
        if (entityDescription == null) {
            if (other.entityDescription != null) {
                return false;
            }
        } else if (!entityDescription.equals(other.entityDescription)) {
            return false;
        }
        if (action == null) {
            if (other.action != null) {
                return false;
            }
        } else if (!action.equals(other.action)) {
            return false;
        }
        if (entityName == null) {
            if (other.entityName != null) {
                return false;
            }
        } else if (!entityName.equals(other.entityName)) {
            return false;
        }
        if (entityValue != other.entityValue) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "IntentWrapper [entityName=" + entityName + ", entityValue="
                + entityValue + ", entityDescription=" + entityDescription
                + ", action=" + action + "]";
    }
}
