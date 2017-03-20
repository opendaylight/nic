/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.free.queue.rev170315.MeteridFreeQueues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.free.queue.rev170315.MeteridFreeQueuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.free.queue.rev170315.meterid.free.queues.MeteridFreeQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.free.queue.rev170315.meterid.free.queues.MeteridFreeQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.inuse.queue.rev170315.MeteridInuseQueues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.inuse.queue.rev170315.MeteridInuseQueuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.inuse.queue.rev170315.meterid.inuse.queues.MeteridInuseQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.inuse.queue.rev170315.meterid.inuse.queues.MeteridInuseQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.queue.types.rev170316.MeteridObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.meterid.queue.types.rev170316.MeteridQueueTypesData;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MeterQueueUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MeterQueueUtils.class);
    private DataBroker dataBroker;
    private InstanceIdentifier<MeteridFreeQueues> METER_ID_FREE_QUEUES_IID = InstanceIdentifier.builder(MeteridFreeQueues.class).build();
    private InstanceIdentifier<MeteridInuseQueues> METER_ID_INUSE_QUEUES_IID = InstanceIdentifier.builder(MeteridInuseQueues.class).build();

    public MeterQueueUtils(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        createFreeQueueIfNeed();
    }

    public MeteridObject retrieveFreeMeterId() {
        MeteridObject result = null;
        final MeteridFreeQueues meteridFreeQueues = retrieveFreeMeterQueue();
        final List<MeteridFreeQueue> meteridFreeQueue = meteridFreeQueues.getMeteridFreeQueue();
        final List<Short> meterIds = Lists.newArrayList();
        for (MeteridFreeQueue queue : meteridFreeQueue) {
            meterIds.add(queue.getId().getValue());
        }
        Collections.sort(meterIds);
        final Iterator<Short> meterIdIterator = meterIds.iterator();
        if (meterIdIterator.hasNext()) {
            result = new MeteridObject(meterIdIterator.next());
            meterIdIterator.remove();
        }
        final MeteridFreeQueues queue = getQueuesBuilder(createRemainingObjects(meterIdIterator));
        sendToMdsal(queue);
        putMeterIdInUse(result);
        return result;
    }

    private List<MeteridObject> createRemainingObjects(Iterator iterator) {
        List<MeteridObject> remaining = Lists.newArrayList();
        iterator.forEachRemaining(n -> remaining.add(new MeteridObject((short) n)));
        return remaining;
    }

    private MeteridFreeQueues getQueuesBuilder(final List<MeteridObject> meteridObjects) {
        final List<MeteridFreeQueue> queue = Lists.newArrayList();
        final MeteridFreeQueueBuilder meteridFreeBuilder = new MeteridFreeQueueBuilder();
        final MeteridFreeQueuesBuilder builder = new MeteridFreeQueuesBuilder();
        for (MeteridObject meter : meteridObjects) {
            queue.add(meteridFreeBuilder.setId(meter).build());
        }
        builder.setMeteridFreeQueue(queue);
        return builder.build();
    }

    private void putMeterIdInUse(MeteridObject meteridObject) {
        final MeteridInuseQueues inuseQueues = retrieveInuseMeterQueue();
        final MeteridInuseQueueBuilder builder = new MeteridInuseQueueBuilder();
        builder.setId(meteridObject);

        List<MeteridInuseQueue> inuseList =
                (null != inuseQueues.getMeteridInuseQueue() ? inuseQueues.getMeteridInuseQueue() : Lists.newArrayList());
        inuseList.add(builder.build());
        final MeteridInuseQueuesBuilder builders = new MeteridInuseQueuesBuilder();
        builders.setMeteridInuseQueue(inuseList);

        sendToMdsal(builders.build());
    }

    public void releaseMeterId(final long meterId) {
        final MeteridObject toRelease = new MeteridObject((short)meterId);
        final MeteridInuseQueues inuseQueues = retrieveInuseMeterQueue();
        inuseQueues.getMeteridInuseQueue().remove(new MeteridInuseQueueBuilder().setId(toRelease).build());
        sendToMdsal(inuseQueues);
        final MeteridFreeQueues freeQueues = retrieveFreeMeterQueue();
        freeQueues.getMeteridFreeQueue().add(new MeteridFreeQueueBuilder().setId(toRelease).build());
        sendToMdsal(freeQueues);
    }

    private void createFreeQueueIfNeed() {
        final MeteridFreeQueuesBuilder builder = new MeteridFreeQueuesBuilder();
        builder.setMeteridFreeQueue(getFullList(MeteridQueueTypesData.SwitchType.PICA8.getIntValue()));
        sendToMdsal(builder.build());

    }

    private void createInuseQueueIfNeed() {
        final MeteridInuseQueues queue = retrieveInuseMeterQueue();
        if (null == queue) {
            final MeteridInuseQueuesBuilder builder = new MeteridInuseQueuesBuilder();
            sendToMdsal(builder.build());
        } else {
            LOG.info("\nQueue for meters in use already exist.");
        }
    }

    private List<MeteridFreeQueue> getFullList(int qnt) {
        List<MeteridFreeQueue> fullList = Lists.newArrayList();
        final MeteridFreeQueueBuilder builder = new MeteridFreeQueueBuilder();
        for (int i = 1; i < qnt; i++ ) {
            fullList.add(builder.setId(new MeteridObject((short)i)).build());
        }
        return fullList;
    }

    private MeteridFreeQueues retrieveFreeMeterQueue() {
        MeteridFreeQueues result = null;
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            Optional<MeteridFreeQueues> freeQueue = transaction.read(LogicalDatastoreType.OPERATIONAL,
                    METER_ID_FREE_QUEUES_IID).checkedGet();
            if (freeQueue.isPresent()) {
                result = freeQueue.get();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private MeteridInuseQueues retrieveInuseMeterQueue() {
        MeteridInuseQueues result = new MeteridInuseQueuesBuilder().build();
        try {
            final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
            Optional<MeteridInuseQueues> inuseQueue = transaction.read(LogicalDatastoreType.OPERATIONAL,
                    METER_ID_INUSE_QUEUES_IID).checkedGet();
            if (inuseQueue.isPresent()) {
                result = inuseQueue.get();
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private void sendToMdsal(MeteridFreeQueues meteridFreeQueues) {
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, METER_ID_FREE_QUEUES_IID, meteridFreeQueues);
        writeTransaction.submit();
    }

    private void sendToMdsal(MeteridInuseQueues meteridInuseQueues) {
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        writeTransaction.put(LogicalDatastoreType.OPERATIONAL, METER_ID_INUSE_QUEUES_IID, meteridInuseQueues);
        writeTransaction.submit();
    }
}
