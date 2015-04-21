//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.translator.term;

import java.util.List;

import org.opendaylight.nic.compiler.impl.TermTypeImpl;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermTranslator;
import org.opendaylight.nic.intent.Interval;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;

public class VlanTranslator implements TermTranslator<MatchBuilder> {

    private static final TermTypeImpl vlanTermType = TermTypeImpl.VLAN;

    @Override
    public void translate(MatchBuilder mb, List<? extends Interval> intervals) {
        for (Interval i : intervals) {
            // TODO: Handle interval that's not a single value.
            VlanId vid = new VlanIdBuilder()
                    .setVlanId(
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(
                                    i.start())).build();
            VlanMatch vlan = new VlanMatchBuilder().setVlanId(vid).build();
            mb.setVlanMatch(vlan);
        }
    }

    @Override
    public TermLabel termLabel() {
        return vlanTermType.label();
    }

}
