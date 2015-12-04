/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import javax.annotation.concurrent.Immutable;

/**
 * This class is immutable so all fields are public, final, and immutable.
 *
 * @author gwu
 *
 */
@Immutable
public class NEMOData {

    public final String from;
    public final String to;
    public final String bandwidth;
    public final String startTime;
    public final String duration;

    public NEMOData(String from, String to, String bandwidth, String startTime, String duration) {
        this.from = from;
        this.to = to;
        this.bandwidth = bandwidth;
        this.startTime = startTime;
        this.duration = duration;
    }
}
