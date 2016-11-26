/*
 * Copyright (c) 2016 Serro LCC and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.api;

import org.opendaylight.nic.model.RendererCommon;

import java.util.Set;
import java.util.UUID;

/**
 * Created by yrineu on 04/10/16.
 */
public interface OFRendererQoSFlowService <T extends RendererCommon> {

    void deployQoS(T rendererCommon);

    void undeployQoS(Set<UUID> rendererCommonIds);
}
