package org.opendaylight.nic.mapping.mdsal.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import com.google.common.util.concurrent.CheckedFuture;

public class MappingMdsalProviderTests {

    @Test
    public void delete() throws Exception {
        DataBroker databroker = mock(DataBroker.class);
        final WriteTransaction transaction = mock(WriteTransaction.class);
        @SuppressWarnings("unchecked")
        CheckedFuture<Void, TransactionCommitFailedException> value = mock(CheckedFuture.class);
        when(transaction.submit()).thenReturn(value);
        when(databroker.newWriteOnlyTransaction()).thenReturn(transaction);

        MappingMdsalProvider provider = new MappingMdsalProvider();

        String key = "key";
        try {
            provider.delete(key);
        } finally {
            provider.close();
        }
    }
}
