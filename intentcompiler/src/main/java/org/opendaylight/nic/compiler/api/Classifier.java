//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
qpackage org.opendaylight.nic.compiler.api;

public interface Classifier {
    enum Type {
        TCP_SRC, TCP_DST
    }
    Type getType();
    // TODO: Find a good abstraction for the value
    Object getValue();
}
