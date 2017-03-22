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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ScheduleServiceManager implements ScheduleService {

    private class Executor extends TimerTask {

        private final Dataflow dataflow;

        public Executor(final Dataflow dataflow) {
            this.dataflow = dataflow;
        }

        @Override
        public void run() {
            if (rendererService != null) {
                rendererService.execute(dataflow);
            }
        }
    }

    private Map<String, Timer> timers;
    private RendererService rendererService;

    public ScheduleServiceManager() {
        timers = new HashMap<>();
    }

    @Override
    public void scheduleRefresh(final Dataflow dataflow,
                                final DelayConfig delayConfig) {
        final TimeUnit timeUnit = TimeUnit.valueOf(delayConfig.getTimeUnit());
        final long delay = timeUnit.toMillis(delayConfig.getDelay());
        createSchedule(dataflow, delay, delay);
    }

    @Override
    public void setRendererService(RendererService rendererService) {
        this.rendererService = rendererService;
    }

    @Override
    public void start(final Map<Dataflow, DelayConfig> delayConfigByDataflow) {
        delayConfigByDataflow.entrySet().forEach(consumer -> {
            final Dataflow dataflow = consumer.getKey();
            final long creationTime = Long.valueOf(dataflow.getCreationTime());
            final DelayConfig delayConfig = consumer.getValue();
            final TimeUnit timeUnit = TimeUnit.valueOf(delayConfig.getTimeUnit());
            final long delay = timeUnit.toMillis(delayConfig.getDelay());
            final long interval = calcInterval(creationTime);
            if (interval > 0) {
                createSchedule(dataflow, calcInterval(creationTime), delay);
            } else {
                createSchedule(dataflow, delay, delay);
            }
        });
    }

    @Override
    public void createSchedule(final Dataflow dataflow,
                               final long initialDelay,
                               final long delay) {

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new Executor(dataflow),
                initialDelay,
                delay);
        timers.put(dataflow.getId().getValue(), timer);
    }

    private long calcInterval(final long pastDate) {
        return System.currentTimeMillis() - pastDate;
    }

    @Override
    public void stop(String id) {
        final Timer timerTask = timers.get(id);
        if (timerTask != null) {
            timers.get(id).cancel();
            timers.remove(id);
        }
    }

    @Override
    public void stop() {
        timers.entrySet().forEach(timer -> timer.getValue().cancel());
    }
}