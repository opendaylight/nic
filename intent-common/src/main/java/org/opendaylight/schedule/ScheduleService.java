/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.schedule;

import org.opendaylight.nic.common.transaction.service.renderer.RendererService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.delay.configs.DelayConfig;

import java.util.Map;

/**
 * This service is used to schedule the {@link Dataflow}
 */
public interface ScheduleService {

    /**
     * Create a scheduled refresh for a given {@link Dataflow}
     * @param dataflow the {@link Dataflow}
     * @param intervalInSeconds the interval between each execution as {@link Long}
     * @param initialDelay the initial delay as {@link Long}
     */
    void createSchedule(final Dataflow dataflow,
                        final long intervalInSeconds,
                        final long initialDelay);

    /**
     * Schedule the refresh of a given {@link Dataflow}
     * @param dataflow the {@link Dataflow}
     * @param delayConfig the {@link DelayConfig} containing all data needed to schedule the refresh.
     */
    void scheduleRefresh(final Dataflow dataflow,
                         final DelayConfig delayConfig);

    /**
     * Set the {@link RendererService} to be used when execute
     * @param rendererService the {@link RendererService}
     */
    void setRendererService(RendererService rendererService);

    /**
     * Start all {@link DelayConfig} already created
     * @param delayConfigByDataflow a {@link Map} containing a {@link DelayConfig} by {@link Dataflow}
     */
    void start(final Map<Dataflow, DelayConfig> delayConfigByDataflow);

    /**
     * Stop a given scheduled {@link Dataflow} using its ID
     * @param id the {@link Dataflow} ID as {@link String}
     */
    void stop(final String id);

    /**
     * Stop all scheduled {@link Dataflow}
     */
    void stop();
}
