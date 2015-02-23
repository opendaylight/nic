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
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;


public class EthTypeTranslator implements TermTranslator<MatchBuilder> {

    private static final TermTypeImpl ethTermType = TermTypeImpl.ETH_TYPE;

    @Override
    public void translate(MatchBuilder mb, List<? extends Interval> intervals) {
        for (Interval i : intervals) {
            // TODO: Handle interval that's not a single value.
            EtherType ethType1 = new EtherType(Long.valueOf(i.start()));
            EthernetType ethType = new EthernetTypeBuilder().setType(ethType1).build();
            EthernetMatch eth = new EthernetMatchBuilder().setEthernetType(ethType).build();
            mb.setEthernetMatch(eth);
        }
    }

    @Override
    public TermLabel termLabel() {
        return ethTermType.label();
    }

}
