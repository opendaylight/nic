//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Map;

import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.ActionType;
import org.opendaylight.nic.intent.Action;
import org.opendaylight.nic.intent.AuxiliaryData;

/**
 * An implementation of {@link Action} which provides specific functionality
 * related to the policy compiler.
 *
 * @author Duane Mentze
 */
public class CompilerAction implements ActionType, Action, AuxiliaryData {

    ActionType actionType;
    AuxiliaryData auxData;
    CompilerAction supercededBy;

    public CompilerAction(ActionType actionType, AuxiliaryData auxData) {
        super();
        this.actionType = actionType;
        this.auxData = auxData;
        this.supercededBy = null;
    }

    public boolean isSuperceded() {
        if (this.supercededBy == null) {
            return true;
        }
        return false;
    }

    public AuxiliaryData getAuxData() {
        return this.auxData;
    }

    public CompilerAction getSupercededBy() {
        return this.supercededBy;
    }

    private void setSupercededBy(CompilerAction other) {
        this.supercededBy = other;
    }

    public void resolveDuplicate(CompilerAction other) {

        int resolve = resolveDuplicate(this.getAuxData(), other.getAuxData());
        if (resolve >= 0) {
            other.setSupercededBy(this);
        } else {
            this.setSupercededBy(other);
        }
    }

    @Override
    public ActionLabel label() {
        return actionType.label();
    }

    @Override
    public String readableName() {
        return actionType.readableName();
    }

    @Override
    public boolean isObserver() {
        return actionType.isObserver();
    }

    @Override
    public boolean isComposable() {
        return actionType.isComposable();
    }

    @Override
    public boolean allowDuplicate() {
        return actionType.allowDuplicate();
    }

    @Override
    public long precedence() {
        return actionType.precedence();
    }

    @Override
    public int resolveDuplicate(AuxiliaryData a, AuxiliaryData b) {
        return actionType.resolveDuplicate(a, b);
    }

    @Override
    public boolean validate(AuxiliaryData data) {
        return actionType.validate(data);
    }

    @Override
    public AuxiliaryData data() {
        return auxData;
    }

    @Override
    public Map<String, String> getData() {
        return auxData.getData();
    }

    @Override
    public String toString() {
        return "CompilerAction [actionType=" + actionType + ", auxData="
                + auxData + ", supercededBy=" + supercededBy + "]";
    }

}
