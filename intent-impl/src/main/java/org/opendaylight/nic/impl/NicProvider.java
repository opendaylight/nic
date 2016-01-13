/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.sun.org.apache.bcel.internal.classfile.Node;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.compiler.api.*;
import org.opendaylight.nic.graph.api.*;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Log;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.ActionTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.EdgeTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.EdgesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.NodesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.*;

public class NicProvider implements NicConsoleProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NicProvider.class);
    public static final String ACTION_ALLOW = "ALLOW";
    public static final String ACTION_BLOCK = "BLOCK";
    public static final String ACTION_REDIRECT = "REDIRECT";
    public static final String ACTION_LOG = "LOG";

    protected DataBroker dataBroker;
    protected IntentMappingService mappingSvc;

    public NicProvider(DataBroker dataBroker, IntentMappingService mappingSvc) {
        this.dataBroker = dataBroker;
        this.mappingSvc = mappingSvc;
    }

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();

    @Override
    public void close() throws Exception {
        // Close active registrations
        LOG.info("IntentengineImpl: registrations closed");
    }

    public void init() {
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

        LOG.info("initIntentsOperational: operational status populated: {}", intents);
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

        LOG.info("initIntentsConfiguration: default config populated: {}", intents);
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

        LOG.info("initIntentsConfiguration: config populated: {}", intents);
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
            InstanceIdentifier<Intent> iid = InstanceIdentifier.create(Intents.class)
                    .child(Intent.class, new IntentKey(id));
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
            Optional<Intents> intents = tx.read((isConfigurationDatastore) ? LogicalDatastoreType.CONFIGURATION
                    : LogicalDatastoreType.OPERATIONAL, INTENTS_IID).checkedGet();

            if(intents.isPresent()) {
                listOfIntents = intents.get().getIntent();
            }
            else {
                LOG.info("Intent tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("ListIntents: failed: {}", e.getMessage(), e);
        }

        if (listOfIntents == null) {
            listOfIntents = new ArrayList<Intent>();
        }
        LOG.info("ListIntentsConfiguration: list of intents retrieved sucessfully");
        return listOfIntents;
    }

    @Override
    public Intent getIntent(Uuid id) {
        Intent intent = null;

        try {
            InstanceIdentifier<Intent> iid = InstanceIdentifier.create(Intents.class)
                    .child(Intent.class, new IntentKey(id));
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            intent = tx.read(LogicalDatastoreType.CONFIGURATION, iid).checkedGet().get();

            if (intent == null) {
                intent = tx.read(LogicalDatastoreType.OPERATIONAL, iid).checkedGet().get();
            }

        } catch (Exception e) {
            LOG.error("getIntent: failed: {}", e);
            return null;
        }

        LOG.info("getIntent: Intent retrieved sucessfully");
        return intent;
    }

    @Override
    public String compile() {
        List<Intent> intents = listIntents(true);
        IntentCompiler compiler = IntentCompilerFactory.createIntentCompiler();
        BasicAction allow = new BasicAction(ACTION_ALLOW, ActionConflictType.COMPOSABLE);
        BasicAction block = new BasicAction(ACTION_BLOCK, ActionConflictType.EXCLUSIVE);
        BasicAction redirect = new BasicAction(ACTION_REDIRECT, ActionConflictType.COMPOSABLE);
        BasicAction log = new BasicAction(ACTION_LOG, ActionConflictType.COMPOSABLE);

        Collection<Policy> policies = new LinkedList<>();

        for (Intent intent : intents) {
            EndPointGroup sourceContainer = (EndPointGroup) intent.getSubjects().get(0).getSubject();
            EndPointGroup destinationContainer = (EndPointGroup) intent.getSubjects().get(1).getSubject();
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionContainer =
                    (org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action)
                            intent.getActions().get(0).getAction();
            String sourceSubject = sourceContainer.getEndPointGroup().getName();
            String destinationSubject = destinationContainer.getEndPointGroup().getName();
            Set<Endpoint> sources;
            try {
                sources = translateSubject(compiler, sourceSubject);
            } catch (UnknownHostException e) {
                LOG.error("Invalid source subject: {}", sourceSubject, e);
                return "[ERROR] Invalid subject: " + sourceSubject;
            }
            Set<Endpoint> destinations;
            try {
                destinations = translateSubject(compiler, destinationSubject);
            } catch (UnknownHostException e) {
                LOG.error("Invalid destination subject: {}", destinationSubject, e);
                return "[ERROR] Invalid subject: " + destinationSubject;
            }
            Action action;
            if (actionContainer instanceof Allow) {
                action = allow;
            } else if (actionContainer instanceof Block) {
                action = block;
            } else if (actionContainer instanceof Redirect) {
                action = redirect;
            } else if (actionContainer instanceof Log) {
                action = log;
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
        Collection<Policy> compiledPolicies;
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
        stringBuilder.append(formatPolicies(compiledPolicies));
        return stringBuilder.toString();
    }

    @Override
    // Function Overloading Method to compile use PGA Graph Algorithm
    public String compile(int flag) {
        List<Intent> intents = listIntents(true);

        CompilerGraph compiler = CompilerGraphFactory.createGraphCompiler();
        Edges allow = new EdgesBuilder().setType(EdgeTypes.MustAllow).setActionType(ActionTypes.Composable).build();
        Edges block = new EdgesBuilder().setType(EdgeTypes.MustDeny).setActionType(ActionTypes.Exclusive).build();
        Edges redirect = new EdgesBuilder().setType(EdgeTypes.CanAllow).setActionType(ActionTypes.Composable).build();
        Edges log = new EdgesBuilder().setType(EdgeTypes.CanAllow).setActionType(ActionTypes.Composable).build();

        Collection<InputGraph> policies = new LinkedList<>();
        //Create a collection of input graphs

        for (Intent intent : intents) {
            EndPointGroup sourceContainer = (EndPointGroup) intent.getSubjects().get(0).getSubject();
            EndPointGroup destinationContainer = (EndPointGroup) intent.getSubjects().get(1).getSubject();
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionContainer =
                    (org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action)
                            intent.getActions().get(0).getAction();
            String sourceSubject = sourceContainer.getEndPointGroup().getName();
            String destinationSubject = destinationContainer.getEndPointGroup().getName();
            Set<Nodes> sources;
            // Creates nodes of the graph from sources
            try {
                sources = translateSubject(compiler, sourceSubject);
            } catch (UnknownHostException e) {
                LOG.error("Invalid source subject: {}", sourceSubject, e);
                return "[ERROR] Invalid subject: " + sourceSubject;
            }
            // Creates nodes of the graph from sources
            Set<Nodes> destinations;
            try {
                destinations = translateSubject(compiler, destinationSubject);
            } catch (UnknownHostException e) {
                LOG.error("Invalid destination subject: {}", destinationSubject, e);
                return "[ERROR] Invalid subject: " + destinationSubject;
            }
            Edges action;
            if (actionContainer instanceof Allow) {
                action = allow;
            } else if (actionContainer instanceof Block) {
                action = block;
            } else if (actionContainer instanceof Redirect) {
                action = redirect;
            } else if (actionContainer instanceof Log) {
                action = log;
            } else {
                String actionClass = actionContainer.getClass().getName();
                LOG.error("Invalid action: {}", actionClass);
                return "[ERROR] Invalid action: " + actionClass;
            }
            Set<Edges> actions = new LinkedHashSet<>();
            actions.add(action);
            // Create input graphs
            policies.add(compiler.createGraph(sources, destinations, actions));
        }

        // Convert this to compiler graph results
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> Original policies:\n");
        stringBuilder.append(formatPolicies(policies, 1));
        stringBuilder.append('\n');
        stringBuilder.append(">>> Compiled policies:\n");
        Collection<InputGraph> compiledPolicies;
        try {
            compiledPolicies = compiler.compile(policies);
        } catch (CompilerGraphException e) {
            LOG.error("Compilation failure", e);
            StringBuilder builder = new StringBuilder();
            builder.append("[ERROR] Compilation failure: ");
            builder.append(e.getMessage());
            builder.append("\nRelated policies:\n");
            for (InputGraph policy : e.getRelatedPolicies()) {
                builder.append("    ");
                builder.append(policy.toString());
            }
            builder.append('\n');
            return builder.toString();
        }
        stringBuilder.append(formatPolicies(compiledPolicies, 1));
        // Return the string of compiledPolices
        return stringBuilder.toString();
    }

    private Set<Endpoint> translateSubject(IntentCompiler compiler, String sourceSubject) throws UnknownHostException {
        StringBuilder csv = new StringBuilder();
        Map<String, String> innerMap = mappingSvc.get(sourceSubject);
        for(String ipAddress : innerMap.values()){
            if(csv.length() == 0) csv.append(ipAddress);
            else csv.append(",").append(ipAddress);
        }

        return compiler.parseEndpointGroup(sourceSubject);
    }

    private Set<Nodes> translateSubject(CompilerGraph compiler, String sourceSubject) throws UnknownHostException {
        StringBuilder csv = new StringBuilder();
        Map<String, String> innerMap = mappingSvc.get(sourceSubject);
        for(String ipAddress : innerMap.values()){
            if(csv.length() == 0) csv.append(ipAddress);
            else csv.append(",").append(ipAddress);
        }

        return compiler.parseEndpointGroup(sourceSubject);
    }

    private String formatPolicies(Collection<Policy> policies) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Policy policy : policies) {
            stringBuilder.append(policy.toString());
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }

    private String formatPolicies(Collection<InputGraph> policies, int flag) {
        StringBuilder stringBuilder = new StringBuilder();
        for (InputGraph policy : policies) {
            stringBuilder.append("From ");
            for (Nodes node : policy.src()) {
                stringBuilder.append(node.getName());
            }
            stringBuilder.append(" To ");
            for (Nodes node : policy.dst()) {
                stringBuilder.append(node.getName());
            }
            stringBuilder.append(" apply ");
            for (Edges edge : policy.action()) {
                stringBuilder.append(edge.getType());
            }
            stringBuilder.append('\n');
        }
        return stringBuilder.toString();
    }
}
