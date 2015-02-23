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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;

public class IpProtoTranslator implements TermTranslator<MatchBuilder> {

    private static final TermTypeImpl ipProtoTermType = TermTypeImpl.IP_PROTO;

    @Override
    public void translate(MatchBuilder mb, List<? extends Interval> intervals) {
        for (Interval i : intervals) {
            // TODO: Handle interval that's not a single value.
            Short ipProto = Short
                    .valueOf(Integer.valueOf(i.start()).toString());
            IpMatch ip = new IpMatchBuilder().setIpProtocol(ipProto).build();
            mb.setIpMatch(ip);

            // Provide a base for matching specific values, based on protocol
            if (i.start() == 17)
                mb.setLayer4Match(new UdpMatchBuilder().build());
            else if (i.start() == 6)
                mb.setLayer4Match(new TcpMatchBuilder().build());
        }
    }

    @Override
    public TermLabel termLabel() {
        return ipProtoTermType.label();
    }

}
