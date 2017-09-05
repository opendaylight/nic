/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface;

import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationInterface;
import org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface.unit.family.Unit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

import java.util.Set;

/**
 * Created by yrineu on 21/07/17.
 */
public class PhysicalInterfaceConfigs implements ConfigurationInterface {

    private interface UnitAction {
        void doExecute();
    }

    private StringBuffer result;
    private String interfaceName;
    private Unit unit;
    private UnitAction unitAction;

    private PhysicalInterfaceConfigs(final String interfaceName,
                                     final Unit unit,
                                     final StringBuffer result) {
        this.interfaceName = interfaceName;
        this.unit = unit;
        this.result = result;
        this.unitAction = () -> unit.generateRPCStructure();
    }

    public static PhysicalInterfaceConfigs create(final String interfaceName,
                                                  final Set<String> vlans,
                                                  final StringBuffer buffer) {
        return new PhysicalInterfaceConfigs(
                interfaceName,
                Unit.create(vlans, buffer),
                buffer);
    }

    public static PhysicalInterfaceConfigs create(final String interfaceName,
                                                  final Ipv4Prefix ipv4Prefix,
                                                  final StringBuffer buffer) {
        return new PhysicalInterfaceConfigs(
                interfaceName,
                Unit.create(ipv4Prefix, buffer),
                buffer);
    }

    public void createCleanup() {
        this.unitAction = () -> unit.generateInterfaceCleanupRPCStructure();
        generateStructure();
    }

    @Override
    public void generateRPCStructure() {
        this.unitAction = () -> unit.generateRPCStructure();
        generateStructure();
    }

    public void generateStructure() {
        result.append("<interfaces>");
        result.append("<interface>");
        result.append("<name>" + interfaceName + "</name>");
        result.append("<description>" + interfaceName + "_</description>");
        unitAction.doExecute();
        result.append("</interface>");
        result.append("</interfaces>");
    }

    @Override
    public void generateDeleteRPCStructure() {
        generateRPCStructure();
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public Set<String> getAggregatedVlans() {
        return unit.getEthernetSwitchingAggregatedVlans();
    }
}