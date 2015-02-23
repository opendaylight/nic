//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.util.HashMap;

import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.actions.AllowActionType;
import org.opendaylight.nic.extensibility.actions.AuditActionData;
import org.opendaylight.nic.extensibility.actions.AuditActionType;
import org.opendaylight.nic.extensibility.actions.BlockActionType;
import org.opendaylight.nic.extensibility.actions.LatencyActionData;
import org.opendaylight.nic.extensibility.actions.LatencyActionType;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.nic.intent.impl.AuxiliaryDataImpl;

public class ActionHelper {

    public static HashMap<ActionLabel, AuxiliaryData> allow() {
        HashMap<ActionLabel, AuxiliaryData> actions = new HashMap<ActionLabel, AuxiliaryData>();
        actions.put(AllowActionType.getInstance().label(),
                new AuxiliaryDataImpl());
        return actions;
    }

    public static HashMap<ActionLabel, AuxiliaryData> block() {
        HashMap<ActionLabel, AuxiliaryData> actions = new HashMap<ActionLabel, AuxiliaryData>();
        actions.put(BlockActionType.getInstance().label(),
                new AuxiliaryDataImpl());
        return actions;
    }

    public static HashMap<ActionLabel, AuxiliaryData> latency(Integer dscp) {
        LatencyActionData data = new LatencyActionData(dscp);
        HashMap<ActionLabel, AuxiliaryData> actions = new HashMap<ActionLabel, AuxiliaryData>();
        actions.put(LatencyActionType.getInstance().label(), data);
        return actions;
    }

    public static HashMap<ActionLabel, AuxiliaryData> audit(
            String pktCaptureService) {
        AuditActionData data = new AuditActionData(pktCaptureService);
        HashMap<ActionLabel, AuxiliaryData> actions = new HashMap<ActionLabel, AuxiliaryData>();
        actions.put(AuditActionType.getInstance().label(), data);
        return actions;
    }

}
