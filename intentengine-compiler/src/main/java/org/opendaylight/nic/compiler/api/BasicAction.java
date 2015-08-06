/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.compiler.api;

public class BasicAction implements Action {
    private final String name;
    private final ActionConflictType type;

    public BasicAction(String name, ActionConflictType type) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (type == null) {
            throw new NullPointerException("type");
        }
        this.name = name;
        this.type = type;
    }

    public BasicAction(String name) {
        this(name, ActionConflictType.COMPOSABLE);
    }

    public String getName() {
        return name;
    }

    @Override
    public ActionConflictType getType() {
        return type;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        BasicAction action = (BasicAction) object;

        return name.equals(action.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
