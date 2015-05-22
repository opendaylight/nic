package org.opendaylight.nic.gbp.renderer.impl;

import java.util.List;
import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.gbp.renderer.rev150511.GbpRendererAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.gbp.renderer.rev150511.GbpRendererAugmentationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TargetName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.UniqueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.Tenants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Contract;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.ContractBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Target;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.TargetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Conditions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.selector.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.selector.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class GBPRendererDataChangeListener implements DataChangeListener,
        AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(GBPRendererDataChangeListener.class);
    @SuppressWarnings("unused")
    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> gbpRendererListener = null;
    private final ReadWriteTransaction transaction;

    public GBPRendererDataChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.transaction = dataBroker.newReadWriteTransaction();
        gbpRendererListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION,
                GBPRendererConstants.INTENTS_IID, this, DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        create(changes);
        update(changes);
        delete(changes);
    }

    private void delete(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes
                .getUpdatedData().entrySet()) {
            LOG.info("Delete requested on intent id {}", updated.getKey());
            // TODO implement delete, verify old data versus new data
        }
    }

    private void update(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes
                .getUpdatedData().entrySet()) {
            LOG.info("Intent {} has been modified.", updated.getKey());
            // TODO implement update
        }
    }

    private void create(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> created : changes
                .getCreatedData().entrySet()) {
            LOG.info("New intent added with id {}.", created.getKey());
            if (created.getValue() != null) {
                if (created instanceof Intent) {
                    Intent intent = (Intent)created.getValue();
                    IntentBuilder ib = new IntentBuilder();
                    ib.addAugmentation(GbpRendererAugmentation.class,
                            createGbpRendererAugmentation(intent));
                    // TODO Add intent the new intent node
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        LOG.info("GBPDataChangeListener closed.");
        if (gbpRendererListener != null) {
            gbpRendererListener.close();
        }
    }

    private Augmentation<Intent> createGbpRendererAugmentation(Intent intent) {
        List<Subjects> subjects = intent.getSubjects();
        List<Actions> actions = intent.getActions();
        List<Conditions> conditions = intent.getConditions();
        List<Constraints> constraints = intent.getConstraints();
        for (Actions action: actions) {
            applyActions(action, subjects, conditions, constraints);
        }
        GbpRendererAugmentationBuilder gbpRendererAugmentationBuilder = new GbpRendererAugmentationBuilder();
        //gbpRendererAugmentationBuilder.setGbpNodeRef(new GbpNodeRef(createGbpIID()));
        return gbpRendererAugmentationBuilder.build();
    }

    private void applyActions(Actions action, List<Subjects> subjects,
            List<Conditions> conditions, List<Constraints> constraints) {
        List<EndpointGroup> endpointGroups = translateToGBPEndpoints(subjects);
        // Create contracts
        Contract contract = createContract(endpointGroups);
        contract.getId();
        // Submit contract
    }

    private Contract createContract(List<EndpointGroup> endpointGroups) {
        ContractBuilder cb = new ContractBuilder();
        List<Target> targets = Lists.newArrayList();
        Target target = new TargetBuilder().setName(new TargetName("asdf")).build();
        targets.add(target);
        cb.setTarget(targets);
        return null;
    }

    private List<EndpointGroup> translateToGBPEndpoints(List<Subjects> subjects) {
        List<EndpointGroup> enpointsGroup = Lists.newArrayList();
        // We assume the selector is a UUID corresponding to the
        // GBP endpoint group UUID because the typedef is string
        for (Subjects subs: subjects) {
            if (subs.getSubject() instanceof EndPointSelector) {
                EndPointSelector endPointSelector = (EndPointSelector) subs.getSubject();
                String endpointGroupUUID = endPointSelector.getEndPointSelector();
                Optional<EndpointGroup> node = readNode(transaction, endpointGroupUUID);
                if (node.isPresent()) {
                    enpointsGroup.add(node.get());
                }
                else {
                    LOG.info("Could not create intent because the EndpointGroup doesn't exist.");
                }
            }
            if (subs.getSubject() instanceof EndPointGroupSelector) {
                EndPointGroupSelector endPointGroupSelector = (EndPointGroupSelector) subs.getSubject();
                String endPointGroupSelectorUUID = endPointGroupSelector.getEndPointGroupSelector();
                Optional<EndpointGroup> node = readNode(transaction, endPointGroupSelectorUUID);
                if (node.isPresent()) {
                    enpointsGroup.add(node.get());
                }
                else {
                    LOG.info("Could not create intent because the EndpointGroup doesn't exist.");
                }
            }
            if (subs.getSubject() instanceof EndPointGroup) {
                EndPointGroup endPointGroup = (EndPointGroup) subs.getSubject();
                String endPointGroupUUID = endPointGroup.getName();
                Optional<EndpointGroup> node = readNode(transaction, endPointGroupUUID);
                if (node.isPresent()) {
                    enpointsGroup.add(node.get());
                }
                else {
                    LOG.info("Could not create intent because the EndpointGroup doesn't exist.");
                }
            }
        }
        return enpointsGroup;
    }

    private Optional<EndpointGroup> readNode(ReadWriteTransaction transaction,
            String endpointGroupUUID) {
        Optional<EndpointGroup> node = Optional.absent();
        InstanceIdentifier<EndpointGroup> nodePath = InstanceIdentifier
                .create(Tenants.class)
                .child(Tenant.class)
                .child(EndpointGroup.class,
                        new EndpointGroupKey(
                                buildEndpointGroupId(endpointGroupUUID)));
        try {
            node = transaction.read(LogicalDatastoreType.OPERATIONAL, nodePath)
                    .checkedGet();
        } catch (final ReadFailedException e) {
            LOG.warn("Read Operational/DS for Node fail! {}",
                    nodePath, e);
        }
        return node;
    }

    private EndpointGroupId buildEndpointGroupId(String uuid) {
        EndpointGroupId endpointGroupId = new EndpointGroupId(new UniqueId(uuid));
        return endpointGroupId;
    }

}
