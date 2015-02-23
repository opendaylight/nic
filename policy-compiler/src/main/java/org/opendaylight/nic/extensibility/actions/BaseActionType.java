//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.actions;

import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.ActionType;
import org.opendaylight.nic.intent.AuxiliaryData;



/**
 * A base @link ActionType} which may be used for creating a specific action type.
 *
 * @author Shaun Wackerly
 * @authur Duane Mentze
 */
public abstract class BaseActionType implements ActionType {

    private final ActionLabel label;
    private final boolean isObserver;
    private final boolean isComposable;
    private final boolean allowDuplicate;
    private final long precedence;

    /**
     * Constructs an action with the given fields.
     *
     * @param label the action label
     * @param isObserver whether or not the action is an observer
     * @param isComposable whether or not the action is composable
     * @param allowDuplicate whether or not the action can have duplicates
     * @param precedence action precedence value
     */
    protected BaseActionType(ActionLabel label,
           boolean isObserver,
           boolean isComposable,
           boolean allowDuplicate,
           long precedence) {
        if (label == null)
            throw new IllegalArgumentException("Label cannot be null");
        this.label = label;
        this.isObserver = isObserver;
        this.isComposable = isComposable;
        this.allowDuplicate = !allowDuplicate;
        this.precedence = precedence;
    }

    @Override
    public ActionLabel label() {
        return label;
    }

    @Override
    public String readableName() {
        return label.toString();
    }

    @Override
    public boolean isObserver() {
        return isObserver;
    }

    @Override
    public boolean isComposable() {
        return isComposable;
    }

    @Override
    public boolean allowDuplicate() {
        return allowDuplicate;
    }

    @Override
    public long precedence() {
        return precedence;
    }

    @Override
    public int resolveDuplicate(AuxiliaryData a, AuxiliaryData b) {
    	//default is 0 - no preference
    	//actions with a preference must override this member
        return 0;
    }

    @Override
    public boolean validate(AuxiliaryData data) {
    	//default is true - no data needed
    	//if an action needs data, it must override this member
        return true;
    }

	@Override
	public String toString() {
		return "BaseActionType [label=" + label + ", isObserver=" + isObserver
				+ ", isComposable=" + isComposable + ", allowDuplicate="
				+ allowDuplicate + ", precedence=" + precedence + "]";
	}

}
