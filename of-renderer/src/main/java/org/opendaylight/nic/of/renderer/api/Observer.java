/*
 * Copyright (c) 2016 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.api;

public interface Observer {
    /**
     * Update the observer, used by subject
     */
    void update();

    /** Attach with subject to observe
     * @param sub Subject
     */
    void setSubject(Subject sub);
}
