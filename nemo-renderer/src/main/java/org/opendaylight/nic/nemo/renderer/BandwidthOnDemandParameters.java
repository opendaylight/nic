/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.nemo.renderer;

import javax.annotation.concurrent.Immutable;

import org.joda.time.LocalTime;
import org.joda.time.Period;

/**
 *
 * @author gwu
 *
 */
@Immutable
class BandwidthOnDemandParameters {
    public final String from;
    public final String to;
    public final String bandwidth;
    public final LocalTime startTime;
    public final Period duration;

    public BandwidthOnDemandParameters(String from, String to, String bandwidth, LocalTime startTime, Period duration) {
        super();
        this.from = from;
        this.to = to;
        this.bandwidth = bandwidth;
        this.startTime = startTime;
        this.duration = duration;
    }
}
