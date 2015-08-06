/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.compiler.api;

import org.opendaylight.nic.compiler.IntentCompilerImpl;

public final class IntentCompilerFactory {

    private IntentCompilerFactory() {
    }

    public static IntentCompiler createIntentCompiler() {
        return new IntentCompilerImpl();
    }
}
