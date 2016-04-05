/**
 * Copyright (c) 2015, 2016 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.UUID;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VTNRendererUtility consists of common methods to perform general functions.
 */
public class VTNRendererUtility {

    private final Logger log = LoggerFactory.getLogger(VTNRendererUtility.class);

    private DataBroker dataBroker;

    private static final int IP_DOTS = 3;

    public VTNRendererUtility(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * This method verifies if both the ip exists on the same subnet
     *
     * @param  srcIp The source IP Address.
     * @param  dstIp The destination IP Address.
     * @return  {@code = false} the given IP addresses are not in same subnet.
     */
    private boolean validateSubnet(String srcIp, String dstIp) {
        if (srcIp == null || dstIp == null) {
            throw new IllegalArgumentException("Source or Destination IP address is null.");
        }
        if (!srcIp.equalsIgnoreCase(dstIp)) {
            String[] srcIpDigits = srcIp.split("\\.");
            String[] dstIpDigits = dstIp.split("\\.");
            for (int index = 0; index < IP_DOTS; index++) {
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

    /**
     * Validate Source and destination IP Address.
     *
     * @param adressSrc  Source IP Address.
     * @param adressDst  Destination IP Address.
     *
     * @return {@code true} if Source and destination IP address are valid.
     */
    public boolean validateSrcDstIP(String adressSrc, String adressDst) {
        if (IntentUtils.validateIP(adressSrc) && IntentUtils.validateIP(adressDst)) {
            return validateSubnet(adressSrc, adressDst);
        }
        return false;
    }

    /**
     * Validate Source and destination MAC Address.
     *
     * @param adressSrc  Source MAC Address.
     * @param adressDst  Destination MAC Address.
     *
     * @return {@code true} if Source and destination MAC address are valid.
     */
    public boolean validateSrcDstMac(String adressSrc, String adressDst) {
        return (IntentUtils.validateMAC(adressSrc) && IntentUtils.validateMAC(adressDst));
    }

    /**
     * Validate Source and destination IP Address should not same.
     *
     * @param inSrc  Source IP Address.
     * @param outSrc  Destination IP Address.
     *
     * @return {@code true} if Source and destination IP address are not same.
     */
    public boolean validateInSrcOutSrc(String inSrc, String outSrc) {
        if ( inSrc == null || outSrc == null ) {
            return false;
        }
        if (inSrc.equals(outSrc)) {
            return false;
        }
        return true;
    }

    /**
     * ActionTypeEnum is a enum which is supported for mapping an action type
     * between the NIC and VTN Manager.
     */
    public enum ActionTypeEnum {
        ALLOW("allow", "PASS"),
        BLOCK("block", "DROP");
        private String actionType;
        private String label;

        private ActionTypeEnum(String actionType, String label) {
            this.actionType = actionType;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        /**
         * ActionTypeEnum is a enum for action types supported by the VTN Manager.
         *
         * @param actionType  allow or block.
         *
         * @return action type pass or drop based on requested action type.
         */
        public static ActionTypeEnum fromActionType(String actionType) {
            for (ActionTypeEnum en: ActionTypeEnum.values()) {
                if (en.actionType.equalsIgnoreCase(actionType)) {
                    return en;
                }
            }
            throw new IllegalArgumentException(String.format("Invalid Action type [%s]", actionType));
        }

    }
}
