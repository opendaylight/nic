/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.utils;

import org.opendaylight.nic.common.model.RendererCommonL2;
import org.opendaylight.nic.common.model.RendererCommonL3;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class Utils {

    public static RendererCommonL3 buildRendererCommonL3(final String src, final String dst) {
        return new RendererCommonL3(IpAddressBuilder.getDefaultInstance(src), IpAddressBuilder.getDefaultInstance(dst));
    }

    public static RendererCommonL2 buildRendererCommonL2(final String src, final String dst) {
        return new RendererCommonL2(MacAddress.getDefaultInstance(src), MacAddress.getDefaultInstance(dst));
    }
}
