package org.opendaylight.nic.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.compiler.Edge;
import org.opendaylight.nic.compiler.Epg;
import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.ActionConflictType;
import org.opendaylight.nic.compiler.api.BasicAction;
import org.opendaylight.nic.compiler.api.IntentCompiler;
import org.opendaylight.nic.compiler.api.IntentCompilerException;
import org.opendaylight.nic.compiler.api.IntentCompilerFactory;
import org.opendaylight.nic.compiler.api.Policy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class NicProvider implements NicConsoleProvider {

    private static final Logger LOG = LoggerFactory
            .getLogger(NicProvider.class);
    public static final String ACTION_ALLOW = "ALLOW";
    public static final String ACTION_BLOCK = "BLOCK";

    protected DataBroker dataBroker;

    protected EndpointChangeListener endpointManager;

    protected ServiceRegistration<NicConsoleProvider> nicConsoleRegistration;

    public NicProvider(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public static final InstanceIdentifier<Intents> INTENTS_IID =
            InstanceIdentifier.builder(Intents.class).build();

    @Override
    public void close() throws Exception {
        // Close active registrations
        nicConsoleRegistration.unregister();
        LOG.info("IntentengineImpl: registrations closed");
    }

    public void init(EndpointChangeListener endpointProvider) {

        endpointManager = endpointProvider;
        // Initialize operational and default config data in MD-SAL data store
        BundleContext context =
                FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicConsoleRegistration =
                context.registerService(NicConsoleProvider.class, this, null);

        initIntentsOperational();
        initIntentsConfiguration();
        LOG.info("Initialization done");
    }

    /**
     * Populates Intents' initial operational data into the MD-SAL operational
     * data store.
     */
    protected void initIntentsOperational() {
        // Build the initial intents operational data
        Intents intents = new IntentsBuilder().build();

        // Put the Intents operational data into the MD-SAL data store
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, INTENTS_IID, intents);

        // Perform the tx.submit asynchronously
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.info("initIntentsOperational: transaction succeeded");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("initIntentsOperational: transaction failed");
            }
        });

        LOG.info("initIntentsOperational: operational status populated: {}",
                intents);
    }

    /**
     * Populates Intents' default config data into the MD-SAL configuration data
     * store. Note the database write to the tree are done in a synchronous
     * fashion
     */
    protected void initIntentsConfiguration() {
        // Build the default Intents config data
        Intents intents = new IntentsBuilder().build();

        // Place default config data in data store tree
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, INTENTS_IID, intents);
        // Perform the tx.submit synchronously
        tx.submit();

        LOG.info("initIntentsConfiguration: default config populated: {}",
                intents);
    }

    @Override
    public boolean addIntent(Intent intent) {

        Intents intents;
        List<Intent> listOfIntents = listIntents(true);

        try {
            listOfIntents.add(intent);
            intents = new IntentsBuilder().setIntent(listOfIntents).build();

            // Place default config data in data store tree
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            tx.put(LogicalDatastoreType.CONFIGURATION, INTENTS_IID, intents);
            // Perform the tx.submit synchronously
            tx.submit();
        } catch (Exception e) {
            LOG.error("addIntent: failed: {}", e);
            return false;
        }

        LOG.info("initIntentsConfiguration: default config populated: {}",
                intents);
        return true;
    }

    @Override
    public boolean addIntents(Intents intents) {
        // TODO MultiAdd will be added in a further commit
        return false;
    }

    @Override
    public boolean removeIntent(Uuid id) {
        try {
            InstanceIdentifier<Intent> iid =
                    InstanceIdentifier.create(Intents.class).child(
                            Intent.class, new IntentKey(id));
            // Removes default config data in data store tree
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            tx.delete(LogicalDatastoreType.CONFIGURATION, iid);
            // Perform the tx.submit synchronously
            tx.submit();
        } catch (Exception e) {
            LOG.info("RemoveIntent: failed: {}", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean removeIntents(List<Uuid> intents) {
        // TODO MultiRemove will be added in a further commit
        return false;
    }

    @Override
    public List<Intent> listIntents(boolean isConfigurationDatastore) {
        List<Intent> listOfIntents = null;

        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            listOfIntents =
                    tx.read((isConfigurationDatastore) ? LogicalDatastoreType.CONFIGURATION
                            : LogicalDatastoreType.OPERATIONAL, INTENTS_IID)
                            .checkedGet().get().getIntent();
        } catch (Exception e) {
            LOG.error("ListIntents: failed: {}", e);
        }

        if (listOfIntents == null) {
            listOfIntents = new ArrayList<Intent>();
        }
        LOG.info("ListIntentsConfiguration: list of intents retrieved sucessfully");
        return listOfIntents;
    }

    public void intentDeleted(List<Intent> deletedIntents) {

    }

    @Override
    public Intent getIntent(Uuid id) {
        Intent intent = null;

        try {
            InstanceIdentifier<Intent> iid =
                    InstanceIdentifier.create(Intents.class).child(
                            Intent.class, new IntentKey(id));
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            intent =
                    tx.read(LogicalDatastoreType.CONFIGURATION, iid)
                            .checkedGet().get();

            if (intent == null) {
                intent =
                        tx.read(LogicalDatastoreType.OPERATIONAL, iid)
                                .checkedGet().get();
            }

        } catch (Exception e) {
            LOG.error("getIntent: failed: {}", e);
            return null;
        }

        LOG.info("getIntent: Intent retrieved sucessfully");
        return intent;
    }

    @Override
    public String compile(boolean showGraph) {

        LOG.info("starting");

        LOG.info("yoo");

        // end printing

        List<Intent> intents = listIntents(true);
        IntentCompiler compiler = IntentCompilerFactory.createIntentCompiler();
        BasicAction allow =
                new BasicAction(ACTION_ALLOW, ActionConflictType.COMPOSABLE);
        BasicAction block =
                new BasicAction(ACTION_BLOCK, ActionConflictType.EXCLUSIVE);

        Collection<Policy> policies = new LinkedList<>();

        for (Intent intent : intents) {
            EndPointGroup sourceContainer =
                    (EndPointGroup) intent.getSubjects().get(0).getSubject();
            EndPointGroup destinationContainer =
                    (EndPointGroup) intent.getSubjects().get(1).getSubject();
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionContainer =
                    intent.getActions().get(0).getAction();
            String sourceSubject = sourceContainer.getEndPointGroup().getName();
            String destinationSubject =
                    destinationContainer.getEndPointGroup().getName();
            Epg sources = new Epg(sourceSubject);
            // try {
            // sources = compiler.parseEndpointGroup(sourceSubject);
            // } catch (UnknownHostException e) {
            // LOG.error("Invalid source subject: {}", sourceSubject, e);
            // return "[ERROR] Invalid subject: " + sourceSubject;
            // }
            Epg destinations = new Epg(destinationSubject);
            // try {
            // destinations = compiler.parseEndpointGroup(destinationSubject);
            // } catch (UnknownHostException e) {
            // LOG.error("Invalid destination subject: {}",
            // destinationSubject, e);
            // return "[ERROR] Invalid subject: " + destinationSubject;
            // }
            Action action;
            if (actionContainer instanceof Allow) {
                action = allow;
            } else if (actionContainer instanceof Block) {
                action = block;
            } else {
                String actionClass = actionContainer.getClass().getName();
                LOG.error("Invalid action: {}", actionClass);
                return "[ERROR] Invalid action: " + actionClass;
            }
            Set<Action> actions = new LinkedHashSet<>();
            actions.add(action);
            policies.add(compiler.createPolicy(sources, destinations, actions));
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> Original policies:\n");
        stringBuilder.append(formatPolicies(policies));
        stringBuilder.append('\n');
        stringBuilder.append(">>> Compiled policies:\n");
        DirectedGraph<Epg, Edge> compiledPolicies;
        try {
            compiledPolicies = compiler.compile(policies);
        } catch (IntentCompilerException e) {
            LOG.error("Compilation failure", e);
            StringBuilder builder = new StringBuilder();
            builder.append("[ERROR] Compilation failure: ");
            builder.append(e.getMessage());
            builder.append("\nRelated policies:\n");
            for (Policy policy : e.getRelatedPolicies()) {
                builder.append("    ");
                builder.append(policy.toString());
            }
            builder.append('\n');
            return builder.toString();
        }
        stringBuilder.append(formatCompiledPolicies(compiledPolicies));

        if (showGraph) {
            ViewCompiledGraph.showGraph(compiledPolicies);
        }
        Collection<org.opendaylight.nic.impl.Policy> finalpolicy =
                new LinkedList<>();

        try {
            PolicyGenerator generator =
                    new PolicyGenerator(compiledPolicies,
                            new EndpointManager().getEndpointMap());
            finalpolicy = generator.generate();
        } catch (Exception e) {

        }

        stringBuilder.append(">>>final Endpoint Policies:\n");
        stringBuilder.append(formatFinalPolicies(finalpolicy));

        return stringBuilder.toString();
    }

    private String formatFinalPolicies(
            Collection<org.opendaylight.nic.impl.Policy> fp) {

        StringBuilder stringBuilder = new StringBuilder();
        for (org.opendaylight.nic.impl.Policy policy : fp) {
            stringBuilder.append(policy.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();

    }

    private String formatCompiledPolicies(DirectedGraph<Epg, Edge> policies) {

        Collection<Edge> edges = policies.getEdges();
        StringBuilder stringBuilder = new StringBuilder();

        for (Edge edge : edges) {

            Pair<Epg> epgPair = policies.getEndpoints(edge);
            stringBuilder.append("Source : " + epgPair.getFirst().toString()
                    + "  Destination : " + epgPair.getSecond().toString()
                    + "  Acion : " + edge.getAction());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    private String formatPolicies(Collection<Policy> policies) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Policy policy : policies) {
            stringBuilder.append(policy.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
