/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.information.evpn;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.opendaylight.nic.rpc.exception.JuniperModelNotSupportedException;
import org.opendaylight.nic.rpc.utils.RESTUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by yrineu on 01/09/17.
 */
public class DatabaseInfo {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseInfo.class);

    private static final String VNI_ID = "vni-id";
    private static final String MAC_ADDRESS = "mac-address";
    private static final String ACTIVE_SOURCE = "active-source";
    private static final String IP_ADDRESS = "ip-address";

    private String switchName;
    private String macAddress;
    private Integer vlanId;
    private String ipAddress;
    private String activeSource;
    private Boolean isMacLearned;

    public DatabaseInfo(final String switchName, final String macAddress) {
        this.switchName = switchName;
        this.macAddress = macAddress;
    }

    private void extractDatabaseInfo(final JsonElement mainNode) {
        try {
            final JsonArray jsonArray = ((JsonObject) mainNode).getAsJsonArray("evpn-database-information");
            final Iterator<JsonElement> iterator = jsonArray.iterator();

            while (iterator.hasNext()) {
                final JsonElement element = iterator.next();
                final JsonArray databaseInstance = ((JsonObject) element).getAsJsonArray("evpn-database-instance");
                final JsonElement macEntry = databaseInstance.get(0).getAsJsonObject().get("mac-entry");
                final JsonObject macEntryElements = ((JsonArray) macEntry).get(0).getAsJsonObject();

                this.vlanId = RESTUtils.extractIntData(extractJsonArray(macEntryElements, VNI_ID));
                this.ipAddress = RESTUtils.extractIpv4Address(extractJsonArray(macEntryElements, IP_ADDRESS)).getValue();
                this.activeSource = RESTUtils.extractIpv4Address(extractJsonArray(macEntryElements, ACTIVE_SOURCE)).getValue();
                break;
            }
        } catch (Exception e) {
            throw new JuniperModelNotSupportedException(e.getMessage());
        }
    }

    public void extractEvpnInfoToJson(final String databaseInfos) {
        if (isMacLearned(databaseInfos)) {
            final JsonParser parser = new JsonParser();
            final JsonElement element = parser.parse(databaseInfos);
            extractDatabaseInfo(element);
        }
    }

    private boolean isMacLearned(final String elementStr) {
        LOG.info("\n#### DatabaseInfos: {}", elementStr);
        LOG.info("\n#### Verified MACAddress: {}", this.macAddress);
        this.isMacLearned = elementStr.contains(this.macAddress);
        LOG.info("\n#### Is MAC address learned? {}", this.isMacLearned);
        return this.isMacLearned;
    }

    private static JsonArray extractJsonArray(final JsonElement element, final String member) {
        return ((JsonObject) element).get(member).getAsJsonArray();
    }
}
