/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserPassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserRoleName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.user.rev151010.UserInstance;
import org.opendaylight.yangtools.yang.binding.DataContainer;


public class UserInstanceImpl implements UserInstance {

    private final UserId userId;
    private final UserName userName;
    private final UserPassword userPassword;
    private final UserRoleName userRole;

    public UserInstanceImpl(String id, String name, String password, String role) {
        userId = new UserId(id);
        userName = new UserName(name);
        userPassword = new UserPassword(password);
        userRole = new UserRoleName(role);
    }

    @Override
    public Class<? extends DataContainer> getImplementedInterface() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserId getUserId() {
        return userId;
    }

    @Override
    public UserName getUserName() {
        return userName;
    }

    @Override
    public UserPassword getUserPassword() {
        return userPassword;
    }

    @Override
    public UserRoleName getUserRole() {
        return userRole;
    }

}
