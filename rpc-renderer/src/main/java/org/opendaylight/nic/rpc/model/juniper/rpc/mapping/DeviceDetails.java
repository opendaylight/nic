/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.rpc.mapping;

/**
 * Created by yrineu on 07/08/17.
 */
public class DeviceDetails {

    final String httpIp;
    final String httpPort;
    final String userName;
    final String password;

    public DeviceDetails(final String httpIp,
                  final String httpPort,
                  final String username,
                  final String password) {
        this.httpIp = httpIp;
        this.httpPort = httpPort;
        this.userName = username;
        this.password = password;
    }

    public String getHttpIp() {
        return httpIp;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
