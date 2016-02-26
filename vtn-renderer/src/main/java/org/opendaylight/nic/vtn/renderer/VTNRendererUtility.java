/**
 * Copyright (c) 2015, 2016 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VTNRendererUtility consists of common methods to perform general functions.
 */
public class VTNRendererUtility {

    private final Logger log = LoggerFactory.getLogger(VTNRendererUtility.class);

    private DataBroker dataBroker;

    public VTNRendererUtility(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Validates the received IP address
     *
     * @param  ip the IP Address.
     * @return  {@code = true} on valid IP address.
     */
    public boolean validateIP(final String ip) {
        if (ip == null) {
            log.error("IP address is null");
            throw new NullPointerException();
        }
        String ipAddressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        Pattern pattern = Pattern.compile(ipAddressPattern);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    /**
     * Validate the given MAC address
     *
     * @param  macAddress The MAC Address
     * @return  {@code = true} on valid MAC address
     */
    public boolean validateMacAddress(final String macAddress) {
        if (macAddress == null) {
            log.error("MAC address is null");
            throw new NullPointerException();
        }
        String macAdrressPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        Pattern pattern = Pattern.compile(macAdrressPattern);
        Matcher matcher = pattern.matcher(macAddress);
        return matcher.matches();
    }

    /**
     * This method verifies if both the ip exists on the same subnet
     *
     * @param  srcIp The source IP Address.
     * @param  dstIp The destination IP Address.
     * @return  {@code = false} the given IP addresses are not in same subnet.
     */
    public boolean validateSubnet(String srcIp, String dstIp) {
        if (srcIp == null || dstIp == null) {
            log.error("Source or Destination IP address is null");
            throw new NullPointerException();
        }
        if (!srcIp.equalsIgnoreCase(dstIp)) {
            String[] srcIpDigits = srcIp.split("\\.");
            String[] dstIpDigits = dstIp.split("\\.");
            for (int index = 0; index < 3; index++) {
                if (!(Byte.parseByte(srcIpDigits[index]) == Byte
                        .parseByte(dstIpDigits[index]))) {
                    log.trace(
                            "Source and Destination IP addresses are not in same subnet {} - {}",
                            srcIp, dstIp);
                    return false;
                }
            }
            return true;
        }
        log.trace("Source and Destination IP addresses have same IP addresses {} - {}",
                srcIp, dstIp);
        return false;
    }

    /**
     * This method encode the UUID value
     *
     * @param  uuid the intent ID.
     * @return  encode UUID value.
     */
    public String encodeUUID(String uuid) {
        UUID lcluuid = UUID.fromString(uuid);
        byte[] uuidArr = asByteArray(lcluuid);
        // Convert a byte array to base64 string
        String encodeValue = new sun.misc.BASE64Encoder().encode(uuidArr);
        String encodeUUID = encodeValue.split("=")[0];
        encodeUUID = encodeUUID.replace("+", "");
        encodeUUID = encodeUUID.replace("/", "");
        encodeUUID = encodeUUID.replace("_", "");
        return encodeUUID;
    }

    /**
     * This method byteArray value of UUID
     *
     * @param  uuid the intent ID.
     * @return  byte array value.
     */
    private static byte[] asByteArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];
        for (int temp = 0; temp < 8; temp++) {
            buffer[temp] = (byte) (msb >>> 8 * (7 - temp));
        }
        for (int temp = 8; temp < 16; temp++) {
            buffer[temp] = (byte) (lsb >>> 8 * (7 - temp));
        }
        return buffer;
    }

    /**
     * Put an operational information of an intent into the OPERATIONAL data store.
     *
     * @param intent  The intent to be added into the OPERATIONAL data store.
     * @param status  The intent status.
     *
     * @return {@code true} if the intent is added successfully.
     */
    public boolean addIntent(Intent intent, Status status) {
        MdsalUtils mdsal = new MdsalUtils(dataBroker);
        InstanceIdentifier<Intent> identifier = InstanceIdentifier.builder(Intents.class)
            .child(Intent.class, new IntentKey(intent.getId()))
            .build();
        Intent operationalIntent = new IntentBuilder(intent).setStatus(status).build();
        return mdsal.put(LogicalDatastoreType.OPERATIONAL, identifier, operationalIntent);
    }

    /**
     * Delete an intent information from the OPERATIONAL data store.
     *
     * @param intent  The intent to be deleted from the OPERATIONAL data store.
     *
     * @return {@code true} if the intent is deleted successfully.
     */
    public boolean deleteIntent(Intent intent) {
        MdsalUtils mdsal = new MdsalUtils(dataBroker);
        InstanceIdentifier<Intent> identifier = InstanceIdentifier.builder(Intents.class)
            .child(Intent.class, new IntentKey(intent.getId()))
            .build();
        return mdsal.delete(LogicalDatastoreType.OPERATIONAL, identifier);
    }
}
