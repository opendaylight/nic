/*
 * Copyright (c) 2016 NEC Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.QosConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.QosConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.dscp.type.DscpType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.dscp.type.DscpTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

@Command(name = "qosConfig",
         scope = "intent",
         description = "Adds a QoS Configuration to the controller."
                 + "\nExamples: --name <name> --dscp <dscp>")

public class IntentQoSConfigAddShellCommand extends OsgiCommandSupport {

    protected NicConsoleProvider provider;
    private static final int FIRST_QOS_PROFILE_NAME = 1;
    private static final int SECOND_DSCP_VALUE = 2;

    @Option(name = "-p",
            aliases = { "--name" },
            description = "profile name for the QoS Configuration.\n-p / --name <name>",
            required = true,
            multiValued = false)
    String name = "any";

    @Option(name = "-d",
            aliases = { "--dscp" },
            description = "dscp Value to be set.\n-d / --dscp <dscp>",
            required = true,
            multiValued = false)
    String dscp = "3";

    public IntentQoSConfigAddShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    /**
     * Set the QoS Configuration to intent.
     */
    @Override
    protected Object doExecute() throws Exception {
        UUID uuid = UUID.randomUUID();
        List<QosConfig> intentQosConfig = createQosConfig();

        Intent intent = new IntentBuilder().
                setId(new Uuid(uuid.toString()))
                .setQosConfig(intentQosConfig)
                .build();

        if (provider.addIntent(intent)) {
            return String.format("QoS profile is configured (id: %s)", uuid.toString());
        } else {
            return "Error creating new QoS Configuration.";
        }
    }

    /**
     * Returns the list of QoS Configuration profiles.
     */
    protected List<QosConfig> createQosConfig() {
        int dscpToInt = Integer.parseInt(this.dscp);
        Dscp dscpValue = new Dscp((short) dscpToInt);
        final List<QosConfig> qosConfigList = new ArrayList<QosConfig>();
        DscpType nameType = new DscpTypeBuilder().setName(this.name).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.DscpType qosName =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos
                .DscpTypeBuilder().setDscpType(nameType).build();
        QosConfig qosConfigName = new QosConfigBuilder().setOrder((short) FIRST_QOS_PROFILE_NAME).setQos(qosName).build();
        DscpType dscpType = new DscpTypeBuilder().setDscp(dscpValue).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.DscpType qosDscpValue =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos
                .DscpTypeBuilder().setDscpType(dscpType).build();
        QosConfig qosConfigDscp = new QosConfigBuilder().setOrder((short) SECOND_DSCP_VALUE).setQos(qosDscpValue).build();
        qosConfigList.add(qosConfigName);
        qosConfigList.add(qosConfigDscp);
        return qosConfigList;
    }
}
