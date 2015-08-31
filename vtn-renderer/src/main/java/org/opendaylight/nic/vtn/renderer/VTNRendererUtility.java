/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * The VTNRendererUtility consists of common methods to perform general functions.
 */
public class VTNRendererUtility {

    private final Logger log = LoggerFactory.getLogger(VTNRendererUtility.class);

    private DataBroker dataBroker;

    private List<Intent> listOfIntents;

    public VTNRendererUtility(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.listOfIntents = new ArrayList<Intent>();
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
     * Executes put as a blocking transaction.
     *
     * @param intent the intent instance 
     * @param isStatus the intent status.
     * @return {@code = true} the intent is added successfully.
     */
    public boolean addIntent(Intent intent, Status isStatus) {
        Intents intents;
        List<Intent> listOfIntents = listIntents();
        try {
            InstanceIdentifier<Intents> identifier = InstanceIdentifier.builder(Intents.class).build();
            Intent intentData = new IntentBuilder().setId(intent.getId()).setStatus(isStatus).build();

            listOfIntents.add(intentData);
            intents = new IntentsBuilder().setIntent(listOfIntents).build();
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            tx.put(LogicalDatastoreType.CONFIGURATION, identifier, intents);
            tx.submit();
        } catch (Exception e) {
            log.error("Add Intents: failed: {}", e);
            return false;
        }
        log.info("The intent status is inserted successfully");
        return true;
    }

    /**
     * Executes read as a blocking transaction.
     * @return the result as the data object requested
     */
    private List<Intent> listIntents() {
        Optional<Intents> optionalDataObject;
        try {
            InstanceIdentifier<Intents> identifier = InstanceIdentifier
                    .builder(Intents.class).build();
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            CheckedFuture<Optional<Intents>, ReadFailedException> future = tx
                    .read(LogicalDatastoreType.CONFIGURATION, identifier);
            try {
                future.checkedGet();
                optionalDataObject = future.checkedGet();
                if (optionalDataObject.isPresent()) {
                    listOfIntents = optionalDataObject.get().getIntent();
                }
            } catch (ReadFailedException ex) {
                log.error("List Intents: inner catch failed: {}", ex);
            }
        } catch (Exception e) {
            log.error("List of Intents: failed: {}", e);
        }
        log.info("List of Intents retrieved successfully");
        return listOfIntents;
    }
}
