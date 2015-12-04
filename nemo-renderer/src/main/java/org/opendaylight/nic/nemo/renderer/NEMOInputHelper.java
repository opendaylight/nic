/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.user.rev151010.UserInstance;

/**
 *
 * @author gwu
 *
 */
public class NEMOInputHelper {

    private NEMOInputHelper() {
    }

    public static BeginTransactionInput getBeginTransactionInput(UserInstance userInstance) {
        BeginTransactionInputBuilder builder = new BeginTransactionInputBuilder();
        builder.setUserId(userInstance.getUserId());
        builder.setUserName(userInstance.getUserName());
        builder.setUserRole(userInstance.getUserRole());
        builder.setUserPassword(userInstance.getUserPassword());
        return builder.build();
    }

    public static EndTransactionInput getEndTransactionInput(UserInstance userInstance) {
        EndTransactionInputBuilder builder = new EndTransactionInputBuilder();
        builder.setUserId(userInstance.getUserId());
        builder.setUserName(userInstance.getUserName());
        builder.setUserRole(userInstance.getUserRole());
        builder.setUserPassword(userInstance.getUserPassword());
        return builder.build();
    }

    public static StructureStyleNemoUpdateInputBuilder setUserInstance(StructureStyleNemoUpdateInputBuilder builder,
            UserInstance userInstance) {
        builder.setUserId(userInstance.getUserId());
        builder.setUserName(userInstance.getUserName());
        builder.setUserRole(userInstance.getUserRole());
        builder.setUserPassword(userInstance.getUserPassword());
        return builder;
    }
}
