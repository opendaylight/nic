package org.opendaylight.nic.gbp.renderer.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class GBPRendererConstants {

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();

}
