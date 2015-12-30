/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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
    public void register(Observer obj);

    /** Unregister observers
     * @param obj Observer
     */
    public void unregister(Observer obj);


    /**
     * Notify observers of change
     */
    public void notifyObservers();


    /** Get updates from subject
     * @param obj Observer
     * @return Get update message
     */
    public Object getUpdate(Observer obj);

}
