/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.exception;

import java.util.concurrent.ExecutionException;

/**
 * Created by yrineu on 18/08/17.
 */
public class NoDatamodelFindException extends ExecutionException {

    public NoDatamodelFindException(final String message) {
        super(message);
    }
}
