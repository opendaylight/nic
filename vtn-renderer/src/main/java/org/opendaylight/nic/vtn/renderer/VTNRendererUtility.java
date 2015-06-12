/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VTNRendererUtility consists of common methods to perform general functions.
 */
public class VTNRendererUtility {

    static Map<String, List<IntentWrapper>> hashMapIntentUtil = new HashMap<String, List<IntentWrapper>>();

    private final Logger log = LoggerFactory.getLogger(VTNRendererUtility.class);

    /**
     * Stores the Intent details as a map
     * @param hashmapIntent
     */
    public static void storeIntentDetail(Map hashmapIntent) {
        hashMapIntentUtil.putAll(hashmapIntent);
    }

    /**
     * Return {@code true} if it contains the specified intent.
     *
     * @param intentId  The ID of the intent
     */
    public static boolean containsIntent(String intentId) {
        return hashMapIntentUtil.containsKey(intentId);
    }

    /**
     * Validates the received IP address
     *
     * @param ip
     * @return {@code = true} on valid IP address.
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
     * @param macAddress
     * @return {@code = true} on valid MAC address
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
     * @param srcIp
     * @param dstIp
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
}
