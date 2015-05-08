package org.opendaylight.intentengine;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class NicProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NicProvider.class);

    protected DataBroker dataBroker;

    public DataBroker getDataBroker() {
        return dataBroker;
    }

    protected void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();

    @Override
    public void close() throws Exception {
        // Close active registrations
        LOG.info("IntentengineImpl: registrations closed");
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        // Initialize operational and default config data in MD-SAL data store
        setDataBroker(session.getSALService(DataBroker.class));
        initIntentsOperational();
        initIntentsConfiguration();
        LOG.info("onSessionInitiated: initialization done");
    }

    /**
     * Populates Intents' initial operational data into the MD-SAL operational
     * data store.
     */
    protected void initIntentsOperational() {
        // Build the initial intents operational data
        Intents intents = new IntentsBuilder().build();

        // Put the Intents operational data into the MD-SAL data store
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, INTENTS_IID, intents);

        // Perform the tx.submit asynchronously
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.info("initIntentsOperational: transaction succeeded");
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.error("initIntentsOperational: transaction failed");
            }
        });

        LOG.info("initIntentsOperational: operational status populated: {}", intents);
    }

    /**
     * Populates Intents' default config data into the MD-SAL configuration
     * data store. Note the database write to the tree are done in a
     * synchronous fashion
     */
    protected void initIntentsConfiguration() {
        // Build the default Intents config data
        Intents intents = new IntentsBuilder().build();

        // Place default config data in data store tree
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, INTENTS_IID, intents);
        // Perform the tx.submit synchronously
        tx.submit();

        LOG.info("initIntentsConfiguration: default config populated: {}", intents);
    }
}
