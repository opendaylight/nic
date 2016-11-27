/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.common.model;

public enum IntentModifier {

    TIME_SENSITIVE("Time-Sensitive");

    private final String value;

    IntentModifier(String value) {
        this.value = value;
    }
}
