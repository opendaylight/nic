/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.information.device;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import org.opendaylight.nic.rpc.exception.JuniperModelNotSupportedException;
import org.opendaylight.nic.rpc.model.juniper.information.evpn.DatabaseInfo;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by yrineu on 01/09/17.
 */
public class SwitchInformation {

    private static final String SWITCH_NAME = "switch-info:name";
    private static final String HTTP_IP = "switch-info:http-ip";
    private static final String HTTP_PORT = "switch-info:http-port";
    private static final String HTTP_USER = "switch-info:http-user";
    private static final String HTTP_PASS = "switch-info:http-password";

    private String switchName;
    private String httpIp;
    private Integer httpPort;
    private String httpUser;
    private String httpPassword;
    private DatabaseInfo databaseInfo;

    private SwitchInformation(final String switchName,
                              final Ipv4Address httpIp,
                              final Integer httpPort,
                              final String httpUser,
                              final String httpPassword) {
        this.switchName = switchName;
        this.httpIp = httpIp.getValue();
        this.httpPort = httpPort;
        this.httpUser = httpUser;
        this.httpPassword = httpPassword;
    }

    public String getHttpIp() {
        return httpIp;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public String getHttpUser() {
        return httpUser;
    }

    public String getHttpPassword() {
        return httpPassword;
    }

    public String getSwitchName() {
        return switchName;
    }

    public static Set<SwitchInformation> extractSwitchInformation(final JsonElement jsonElement) {
        Set<SwitchInformation> switchInformationSet = Sets.newHashSet();
        try {
            final Iterator<JsonElement> iterator = jsonElement.getAsJsonArray().iterator();
            while (iterator.hasNext()) {
                final JsonElement element = iterator.next();
                final String switchName = element.getAsJsonObject().get(SWITCH_NAME).getAsString();
                final Ipv4Address httpIp = Ipv4Address.getDefaultInstance(element.getAsJsonObject().get(HTTP_USER).toString());
                final Integer httpPort = element.getAsJsonObject().get(HTTP_PORT).getAsInt();
                final String httpUser = element.getAsJsonObject().get(HTTP_USER).getAsString();
                final String httpPass = element.getAsJsonObject().get(HTTP_PASS).getAsString();

                switchInformationSet.add(new SwitchInformation(switchName, httpIp, httpPort, httpUser, httpPass));
            }
        } catch (Exception e) {
            throw new JuniperModelNotSupportedException(e.getMessage());
        }
        return switchInformationSet;
    }
}
