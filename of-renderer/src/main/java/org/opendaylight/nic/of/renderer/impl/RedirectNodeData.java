/*
 * Copyright (c) 2016 NEC Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class RedirectNodeData {
    private Intent intent;
    private String srcMacNodeId;
    private String destMacNodeId;
    private String ingressNodeId;
    private String egressNodeId;
    private boolean isFlowApplied;
    /**
     * @return the isFlowApplied
     */
    public boolean isFlowApplied() {
        return isFlowApplied;
    }
    /**
     * @param isFlowApplied the isFlowApplied to set
     */
    public void setFlowApplied(boolean isFlowApplied) {
        this.isFlowApplied = isFlowApplied;
    }
    /**
     * @return the intent
     */
    public Intent getIntent() {
        return intent;
    }
    /**
     * @param intent the intent to set
     */
    public void setIntent(Intent intent) {
        this.intent = intent;
    }
    /**
     * @return the srcMacNodeId
     */
    public String getSrcMacNodeId() {
        return srcMacNodeId;
    }
    /**
     * @param srcMacNodeId the srcMacNodeId to set
     */
    public void setSrcMacNodeId(String srcMacNodeId) {
        this.srcMacNodeId = srcMacNodeId;
    }
    /**
     * @return the destMacNodeId
     */
    public String getDestMacNodeId() {
        return destMacNodeId;
    }
    /**
     * @param destMacNodeId the destMacNodeId to set
     */
    public void setDestMacNodeId(String destMacNodeId) {
        this.destMacNodeId = destMacNodeId;
    }
    /**
     * @return the ingressNodeId
     */
    public String getIngressNodeId() {
        return ingressNodeId;
    }
    /**
     * @param ingressNodeId the ingressNodeId to set
     */
    public void setIngressNodeId(String ingressNodeId) {
        this.ingressNodeId = ingressNodeId;
    }
    /**
     * @return the egressNodeId
     */
    public String getEgressNodeId() {
        return egressNodeId;
    }
    /**
     * @param egressNodeId the egressNodeId to set
     */
    public void setEgressNodeId(String egressNodeId) {
        this.egressNodeId = egressNodeId;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((destMacNodeId == null) ? 0 : destMacNodeId.hashCode());
        result = prime * result
                + ((egressNodeId == null) ? 0 : egressNodeId.hashCode());
        result = prime * result
                + ((ingressNodeId == null) ? 0 : ingressNodeId.hashCode());
        result = prime * result + (isFlowApplied ? 1231 : 1237);
        result = prime * result
                + ((srcMacNodeId == null) ? 0 : srcMacNodeId.hashCode());
        result = prime * result + ((intent == null) ? 0 : intent.hashCode());
        return result;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RedirectNodeData other = (RedirectNodeData) obj;
        if (destMacNodeId == null) {
            if (other.destMacNodeId != null)
                return false;
        } else if (!destMacNodeId.equals(other.destMacNodeId))
            return false;
        if (egressNodeId == null) {
            if (other.egressNodeId != null)
                return false;
        } else if (!egressNodeId.equals(other.egressNodeId))
            return false;
        if (ingressNodeId == null) {
            if (other.ingressNodeId != null)
                return false;
        } else if (!ingressNodeId.equals(other.ingressNodeId))
            return false;
        if (isFlowApplied != other.isFlowApplied)
            return false;
        if (srcMacNodeId == null) {
            if (other.srcMacNodeId != null)
                return false;
        } else if (!srcMacNodeId.equals(other.srcMacNodeId))
            return false;
        return true;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RedirectNodeData [srcMacNodeId=" + srcMacNodeId
                + ", destMacNodeId=" + destMacNodeId + ", ingressNodeId="
                + ingressNodeId + ", egressNodeId=" + egressNodeId
                + ", isFlowApplied=" + isFlowApplied + ", intent=" + intent
                + ", isFlowApplied()=" + isFlowApplied()
                + ", getSrcMacNodeId()=" + getSrcMacNodeId()
                + ", getDestMacNodeId()=" + getDestMacNodeId()
                + ", getIngressNodeId()=" + getIngressNodeId()
                + ", getEgressNodeId()=" + getEgressNodeId() + ", hashCode()="
                + hashCode() + ", getIntent()=" + getIntent() + "]";
    }
}
