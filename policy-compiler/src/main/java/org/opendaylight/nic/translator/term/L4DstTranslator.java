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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;


public class L4DstTranslator implements TermTranslator<MatchBuilder> {

    private static final TermTypeImpl l4DstTermType = TermTypeImpl.L4_DST;

    @Override
    public void translate(MatchBuilder mb, List<? extends Interval> intervals) {
        for (Interval i : intervals) {
            // TODO: Handle interval that's not a single value.
            Layer4Match l4 = mb.getLayer4Match();
            if (l4 == null)
                throw new IllegalArgumentException("Must translate IP protocol before L4 port");

            if (l4 instanceof UdpMatch) {
                UdpMatch udpMatch = (UdpMatch)l4;
                l4 = new UdpMatchBuilder(udpMatch).setUdpDestinationPort(new PortNumber(i.start())).build();
            }
            else if (l4 instanceof TcpMatch) {
                TcpMatch tcpMatch = (TcpMatch)l4;
                l4 = new TcpMatchBuilder(tcpMatch).setTcpDestinationPort(new PortNumber(i.start())).build();
            }
            else
                throw new IllegalArgumentException("Unsupported L4 protocol");

            mb.setLayer4Match(l4);
        }
    }

    @Override
    public TermLabel termLabel() {
        return l4DstTermType.label();
    }

}
