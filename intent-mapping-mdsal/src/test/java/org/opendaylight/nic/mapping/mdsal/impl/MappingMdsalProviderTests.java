/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.mapping.mdsal.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;

import com.google.common.util.concurrent.CheckedFuture;

public class MappingMdsalProviderTests {

    @Test
    public void delete() throws Exception {
        final WriteTransaction transaction = mock(WriteTransaction.class);

        @SuppressWarnings("unchecked")
        CheckedFuture<Void, TransactionCommitFailedException> value = mock(
                CheckedFuture.class);
        when(transaction.submit()).thenReturn(value);

        DataBroker databroker = mock(DataBroker.class);
        when(databroker.newWriteOnlyTransaction()).thenReturn(transaction);

        ProviderContext session = mock(ProviderContext.class);
        when(session.getSALService(DataBroker.class)).thenReturn(databroker);

        MappingMdsalProvider provider = new MappingMdsalProvider();
        provider.onSessionInitiated(session);

        String key = "key";

        try {
            provider.delete(key);
        } finally {
            provider.close();
        }
    }
}
