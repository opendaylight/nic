/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli.completers;

import java.util.List;

import org.apache.karaf.shell.console.Completer;
import org.apache.karaf.shell.console.completer.StringsCompleter;
import org.opendaylight.nic.impl.NicProvider;

/**
 * <p>
 * A very simple completer.
 * </p>
 */
public class ActionCompleter implements Completer {

    /**
     * @param buffer
     *            the beginning string typed by the user
     * @param cursor
     *            the position of the cursor
     * @param candidates
     *            the list of completions proposed to the user
     */
    @Override
    public int complete(String buffer, int cursor, List<String> candidates) {
        StringsCompleter delegate = new StringsCompleter();
        delegate.getStrings().add(NicProvider.ACTION_ALLOW);
        delegate.getStrings().add(NicProvider.ACTION_BLOCK);
        return delegate.complete(buffer, cursor, candidates);
    }
}
