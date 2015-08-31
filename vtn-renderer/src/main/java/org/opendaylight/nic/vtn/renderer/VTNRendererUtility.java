/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VTNRendererUtility consists of common methods to perform general functions.
 */
public class VTNRendererUtility {

    private final Logger log = LoggerFactory.getLogger(VTNRendererUtility.class);

    /**
     * Validates the received IP address
     *
     * @param  IP
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
     * @param  macAddress
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
        log.trace(
                "Source and Destination IP addresses have same IP addresses {} - {}",
                srcIp, dstIp);
        return false;
    }

    /**
     * This method encode the UUID value
     *
     * @param  uuid
     * @return  encode UUID value.
     */
    public String encodeUUID(String uuid) {
        UUID lcluuid = UUID.fromString(uuid);
        byte[] uuidArr = asByteArray(lcluuid);
        // Convert a byte array to base64 string
        //String encodeValue = Base64.getEncoder().encodeToString(uuidArr);
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
     * @param  uuid
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
}
