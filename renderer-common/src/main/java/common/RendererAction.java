/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package common;

/**
 * Created by yrineu on 30/03/16.
 */
public enum RendererAction {

    ALLOW(0), DENY(1);

    private int value;
    private RendererAction(int value) {
        this.value = value;
    }
}
