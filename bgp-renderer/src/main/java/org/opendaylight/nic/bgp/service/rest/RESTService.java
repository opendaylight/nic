/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service.rest;

import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Service created to BGP REST requests.
 */
public interface RESTService<T extends DataObject> {

    String get();

    void post(T dataFlow);

    void delete(T dataFlow);

    T put(T dataFlow);
}
