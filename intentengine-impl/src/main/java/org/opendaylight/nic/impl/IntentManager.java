package org.opendaylight.nic.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentManager implements BindingAwareProvider, DataChangeListener,
        AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(IntentManager.class);

    public static final InstanceIdentifier<Intents> INTENT_IID = InstanceIdentifier
            .builder(Intents.class).build();

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        DataObject dataObject = change.getUpdatedSubtree();
        if (dataObject instanceof Intents) {
            Intents intent = (Intents) dataObject;
            LOG.info("onDataChanged - new Intent config: {}", intent);
        }
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        session.getSALService(DataBroker.class).registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION, INTENT_IID, this,
                DataChangeScope.SUBTREE);

        LOG.info("onSessionInitiated: initialization done");
    }

}
