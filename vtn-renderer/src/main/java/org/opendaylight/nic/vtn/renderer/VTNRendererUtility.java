/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.Vtnintents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.VtnintentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.vtn.renderer.intent.IntentWrapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.vtnintents.VtnRendererIntent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.vtnintents.VtnRendererIntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.vtn.renderer.intent.rev150811.vtnintents.VtnRendererIntentKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * The VTNRendererUtility consists of common methods to perform general functions.
 */
public class VTNRendererUtility {

    private static final Logger LOG = LoggerFactory.getLogger(VTNRenderer.class);

    private static final Logger lOG = LoggerFactory.getLogger(VTNRendererUtility.class);

    private DataBroker dataBroker;

    private List<VtnRendererIntent> listOfIntents;

    private List<IntentWrapper> listIntentWrapper;

    /**
     * Class constructor setting the data broker.
     *
     * @param dataBroker the {@link org.opendaylight.controller.md.sal.binding.api.DataBroker}
     */
    public VTNRendererUtility(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.listOfIntents = new ArrayList<VtnRendererIntent>();
        this.listIntentWrapper = new ArrayList<IntentWrapper>();
    }

    /**
     * Return {@code true} if it contains the specified intent.
     *
     * @param intentId  The ID of the intent
     */
    public boolean containsIntent(String intentId) {
        listOfIntents = listIntentsValue();
        String lclUuid = null;
        if (listOfIntents != null) {
            Iterator<VtnRendererIntent> vtnIterator = listOfIntents.iterator();
            while (vtnIterator.hasNext()) {
                Uuid uuid = vtnIterator.next().getKey().getId();
                lclUuid = uuid.getValue();
                if (lclUuid.equals(intentId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * To get the list of intents.
     *
     * @param intentId  The ID of the intent
     * @return the result as the data object requested
     */
    public List<VtnRendererIntent> listOfIntents() {
        listOfIntents = listIntentsValue();
        return listOfIntents;
    }

    /**
     * Validates the received IP address
     *
     * @param ip IP Address
     * @return {@code = true} on valid IP address.
     */
    public static boolean validateIP(final String ip) {
        if (ip == null) {
            lOG.error("IP address is null");
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
     * @param macAddress MAC Address
     * @return {@code = true} on valid MAC address
     */
    public static boolean validateMacAddress(final String macAddress) {
        if (macAddress == null) {
            lOG.error("MAC address is null");
            throw new NullPointerException();
        }
        String macAdrressPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        Pattern pattern = Pattern.compile(macAdrressPattern);
        Matcher matcher = pattern.matcher(macAddress);
        return matcher.matches();
    }

    /**
     * This method verifies if both the IP exists on the same subnet
     *
     * @param srcIp Source IP Address
     * @param dstIp Destination IP Address
     * @return  {@code = false} the given IP addresses are not in same subnet.
     */
    public static boolean validateSubnet(String srcIp, String dstIp) {
        if (srcIp == null || dstIp == null) {
            lOG.error("Source or Destination IP address is null");
            throw new NullPointerException();
        }
        if (!srcIp.equalsIgnoreCase(dstIp)) {
            String[] srcIpDigits = srcIp.split("\\.");
            String[] dstIpDigits = dstIp.split("\\.");

            for (int index = 0; index < 3; index++) {
                if (!(Byte.parseByte(srcIpDigits[index]) == Byte
                        .parseByte(dstIpDigits[index]))) {
                    lOG.trace("Source and Destination IP addresses are not in same subnet {} - {}",
                            srcIp, dstIp);
                    return false;
                }
            }
            return true;
        }
        lOG.trace("Source and Destination IP addresses have same IP addresses {} - {}",
                srcIp, dstIp);
        return false;
    }

    /**
     * This method will return if the given address are valid or not,
     * address can be either IP address or MAC Address
     *
     * @param adressSrc Source of IP/MAC Address
     * @param adressDst Destination of IP/MAC Address
     * @return {@code = false} the given Source and Destination IP addresses are same.
     * */
    public static boolean isAdressValid(String adressSrc, String adressDst) {
        if (adressSrc == null || adressDst == null) {
            return false;
        }
        if (adressSrc.equalsIgnoreCase(adressDst)) {
            return false;
        }
        if ((validateIP(adressSrc)) && (validateIP(adressDst))) {
            if ((validateSubnet(adressSrc, adressDst))) {
                return true;
            }
        } else if ((validateMacAddress(adressSrc))
            && (validateMacAddress(adressDst))) {
            return true;
        }
        return false;
    }

    /**
     * Executes put as a blocking transaction.
     *
     * @param intent The ID of the intent
     * @param intentList List of the intent
     * @return {@code = true} the intent is added successfully.
     */
    public boolean addIntent(Intent intent, List<IntentWrapper> intentList) {
        Vtnintents vtnRendereData;
        List<VtnRendererIntent> listOfIntents = listIntentsValue();
        try {
            InstanceIdentifier<Vtnintents> identifier = InstanceIdentifier.builder(Vtnintents.class).build();
            VtnRendererIntent rendereData = new VtnRendererIntentBuilder()
                  .setId(intent.getId()).setFlowCondName("flowcond").setIntentWrapper(intentList).build();
            listOfIntents.add(rendereData);
            vtnRendereData = new VtnintentsBuilder().setVtnRendererIntent(listOfIntents).build();
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            tx.put(LogicalDatastoreType.CONFIGURATION, identifier, vtnRendereData);
            tx.submit();
        } catch (Exception e) {
            LOG.error("Add Renderer: failed: {}", e);
            return false;
        }
        LOG.info("initRendererConfiguration: default config populated:renderer data {}");
        return true;
    }

    /**
     * Executes read as a blocking transaction.
     * @return the result as the data object requested
     */
    private List<VtnRendererIntent> listIntentsValue() {
        Optional<Vtnintents> optionalDataObject;
        try {
            InstanceIdentifier<Vtnintents> identifier = InstanceIdentifier
                    .builder(Vtnintents.class).build();
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            CheckedFuture<Optional<Vtnintents>, ReadFailedException> future = tx
                    .read(LogicalDatastoreType.OPERATIONAL, identifier);
            try {
                future.checkedGet();
                optionalDataObject = future.checkedGet();
                if (optionalDataObject.isPresent()) {
                    listOfIntents = optionalDataObject.get().getVtnRendererIntent();
                }
            } catch (ReadFailedException ex) {
                LOG.error("List Intents: inner catch failed: {}", ex);
            }
        } catch (Exception e) {
            LOG.error("List of Intents: failed: {}", e);
        }
        LOG.info("List of Intents retrieved successfully");
        return listOfIntents;
    }

    /**
     * Executes read as a blocking transaction.
     *
     * @param intentID The ID of the intent
     * @param listOfIntents the list of intents
     * @return the result as the data object requested
     */
    public List<IntentWrapper> listIntentWrapper(String intentID, List<VtnRendererIntent> listOfIntents) {
        List<IntentWrapper> listInnerWrapper = new ArrayList<IntentWrapper>();
        if (listOfIntents != null) {
            Iterator<VtnRendererIntent> vtnIterator = listOfIntents.iterator();
            while (vtnIterator.hasNext()) {
                listIntentWrapper = vtnIterator.next().getIntentWrapper();
                for (IntentWrapper intentWrapper : listIntentWrapper) {
                    if (intentID.equals(intentWrapper.getId().getValue())) {
                        listInnerWrapper.add(intentWrapper);
                    }
                }
            }
        }
        return listInnerWrapper;
    }

    /**
     * To Remove the Intents in the Data store.
     *
     * @param intentID The ID of the intent
     * @param UUID The ID of the intent
     * @return {@code = true} the intent is deleted successfully.
     */
    public boolean removeintentData(Uuid id) {
        try {
            InstanceIdentifier<VtnRendererIntent> iid = InstanceIdentifier.create(Vtnintents.class)
                .child(VtnRendererIntent.class, new VtnRendererIntentKey(id));
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            tx.delete(LogicalDatastoreType.CONFIGURATION, iid);
            tx.submit();
        } catch (Exception e) {
            LOG.info("RemoveIntent: failed: {}", e);
            return false;
        }
        return true;
    }
}
