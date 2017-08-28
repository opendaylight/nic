/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import org.opendaylight.nic.rpc.exception.JuniperRestException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

/**
 * Created by yrineu on 19/07/17.
 */
public class RestValidations {

    private final static String BASE_ERROR_MESSAGE = "Invalid request received: $request. " +
            "Request must follow the pattern: http://<odl-ip>:<odl-port>/nic/<juniper-device-ipv4>";
    private final static String IPV4_PATTERN = "'\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b'";

    public static void validateReceivedRequest(final String request) {
        if (request == null) {
            final String NULL_REQUEST_MESSAGE = BASE_ERROR_MESSAGE.replace("$request", "NULL");
            throw new JuniperRestException(NULL_REQUEST_MESSAGE);
        }

        final String INVALID_IP_ADDRESS_FORMAT_MESSAGE = BASE_ERROR_MESSAGE.replace("$request", request);
        try {
            final String deviceIp = request.split("/")[2];
            Ipv4Address.getDefaultInstance(deviceIp);
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new JuniperRestException(INVALID_IP_ADDRESS_FORMAT_MESSAGE);
        }
    }
}
