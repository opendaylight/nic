package org.opendaylight.nic.gbp.renderer.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class GBPRendererConstants {

    public static final IntentKey GBP_RENDERER_INTENT_KEY = new IntentKey("gbprenderer:1");

    public static final String GBP_RENDERER_INTENT_ID = "gbprenderer:1";

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();

    public static final InstanceIdentifier<Intent> INTENT_IID = InstanceIdentifier.create(Intents.class).child(
            Intent.class, GBPRendererConstants.GBP_RENDERER_INTENT_KEY);;
}
