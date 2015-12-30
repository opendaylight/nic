/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.api;

public interface Subject {

    /** Register observers
     * @param obj Observer
     */
    void register(Observer obj);

    /** Unregister observers
     * @param obj Observer
     */
    void unregister(Observer obj);

    /**
     * Notify observers of change
     */
    void notifyObservers();

    /** Get updates from subject
     * @param obj Observer
     * @return Get update message
     */
    Object getUpdate(Observer obj);

}
