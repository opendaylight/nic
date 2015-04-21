//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.opendaylight.nic.compiler.ActionSet;
import org.opendaylight.nic.compiler.ActionSetList;
import org.opendaylight.nic.compiler.CompilerAction;

/**
 * An implementation of {@link ActionSetList} specific to the compiler.
 *
 * @author Duane Mentze
 */
public class ActionSetListImpl implements ActionSetList {

    private List<ActionSet> list;

    @Override
    public List<ActionSet> getList() {
        return list;
    }

    @Override
    public boolean isObserver() {
        for (ActionSet as : getList()) {
            if (!(as.isObserver())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isComposable() {
        for (ActionSet as : getList()) {
            if (!(as.isComposable())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long maxActionPrecedence() {
        long max = 0;
        for (ActionSet as : getList()) {
            max = as.maxActionPrecedence() > max ? as.maxActionPrecedence()
                    : max;
        }
        return max;
    }

    public ActionSetListImpl(List<ActionSet> list) {
        super();
        this.list = new LinkedList<ActionSet>(list);
    }

    @Override
    public ActionSetList compose(ActionSetList list) {
        List<ActionSet> result = new LinkedList<ActionSet>();
        result.addAll(list.getList());
        result.addAll(getList());

        if (result.size() <= 1) {
            return new ActionSetListImpl(result);
        }

        Collections.sort(result);

        int index = 1;
        for (ActionSet s1 : result) {
            for (ActionSet s2 : result.subList(index, result.size())) {
                for (CompilerAction a1 : s1.getActions()) {
                    for (CompilerAction a2 : s2.getActions()) {
                        if (a1.label().equals(a2.label())) {
                            a1.resolveDuplicate(a2);
                        }
                    }
                }
            }
            index++;
        }

        return new ActionSetListImpl(result);
    }

    @Override
    public String toString() {
        return "ActionSetListImpl {" + list + "}";
    }

}
