/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.BandwidthCap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.TimingType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Command(name = "apply",
scope = "intent",
description = "Applies an intent to the controller.")
public class IntentLimiterAddShellCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(IntentLimiterAddShellCommand.class);
    private static final String ANY = "ANY";
    private static final String DEFAULT_DURING = "10-Minutes";
    private static final String DEFAULT_INTERVAL = "6-Hours";
    private static final String DEFAULT_BAND = "2000-kbps";

    private NicConsoleProvider nicConsoleProvider;

    @Option(name = "-drop-packets-from",
    aliases = {"--drop"},
    description = "Defines the source where the rate limiter will act.",
    required = true,
    multiValued = false)
    String dropPacketsFrom = ANY;

    @Option(name = "-during",
    aliases = {"--d"},
    description = "Defines the duration of a given rate limiter. That would be in HOURS, MINUTES or SECONDS",
    required = true,
    multiValued = false)
    String during = DEFAULT_DURING;

    @Option(name = "-with-interval-of",
    aliases = {"--i"},
    description = "Defines the interval that the limiter should be applied.",
    required = true,
    multiValued = false)
    String withIntervalOf = DEFAULT_INTERVAL;

    @Option(name = "-band-limit",
    aliases = {"--b"},
    description = "Defines the bandwidth limit to apply the rate-limiter.",
    required = true,
    multiValued = false)
    String bandLimit = DEFAULT_BAND;

    public IntentLimiterAddShellCommand(NicConsoleProvider nicConsoleProvider) {
        this.nicConsoleProvider = nicConsoleProvider;
    }

    @Override
    protected Object doExecute() throws Exception {

        final UUID uuid = UUID.randomUUID();

        IntentLimiterBuilder intentLimiterBuilder = new IntentLimiterBuilder();
        intentLimiterBuilder.setId(new Uuid(uuid.toString()));
        intentLimiterBuilder.setAction(IntentLimiter.Action.DROP);
        intentLimiterBuilder.setDuration(Short.valueOf(during.split("-")[0]));
        intentLimiterBuilder.setDurationType(TimingType.valueOf(during.split("-")[1].toUpperCase()));
        intentLimiterBuilder.setInterval(Short.valueOf(withIntervalOf.split("-")[0]));
        intentLimiterBuilder.setIntervalType(TimingType.valueOf(withIntervalOf.split("-")[1].toUpperCase()));
        intentLimiterBuilder.setSourceIp(new Ipv4Prefix(dropPacketsFrom));
        intentLimiterBuilder.setBandwidthLimit(Long.valueOf(bandLimit.split("-")[0]));
        intentLimiterBuilder.setBandwidthLimitType(BandwidthCap.valueOf(bandLimit.split("-")[1].toUpperCase()));

        nicConsoleProvider.addIntent(intentLimiterBuilder.build());
        return intentLimiterBuilder.build();
    }
}
