//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.pm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.ApplicationImpl;
import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.DeviceId;
import org.opendaylight.nic.common.SegmentId;
import org.opendaylight.nic.compiler.CompiledPolicy;
import org.opendaylight.nic.compiler.impl.IntervalImpl;
import org.opendaylight.nic.compiler.impl.PolicyCompiler;
import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.actions.AllowActionType;
import org.opendaylight.nic.extensibility.actions.AuditActionType;
import org.opendaylight.nic.extensibility.actions.BlockActionType;
import org.opendaylight.nic.extensibility.actions.LatencyActionType;
import org.opendaylight.nic.extensibility.actions.RedirectActionType;
import org.opendaylight.nic.extensibility.actions.StatActionType;
import org.opendaylight.nic.extensibility.terms.EthTypeTermType;
import org.opendaylight.nic.extensibility.terms.IpProtoTermType;
import org.opendaylight.nic.extensibility.terms.L4DstTermType;
import org.opendaylight.nic.extensibility.terms.L4SrcTermType;
import org.opendaylight.nic.extensibility.terms.VlanTermType;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.nic.intent.PolicyId;
import org.opendaylight.nic.intent.impl.AuxiliaryDataImpl;
import org.opendaylight.nic.intent.impl.ClassifierImpl;
import org.opendaylight.nic.intent.impl.DeviceImpl;
import org.opendaylight.nic.intent.impl.EndpointGroupImpl;
import org.opendaylight.nic.intent.impl.IpEndpoint;
import org.opendaylight.nic.intent.impl.ExpressionImpl;
import org.opendaylight.nic.intent.impl.PolicyImpl;
import org.opendaylight.nic.intent.impl.PortImpl;
import org.opendaylight.nic.intent.impl.TermImpl;
import org.opendaylight.nic.services.ApplicationService;
import org.opendaylight.nic.services.DeviceService;
import org.opendaylight.nic.services.EndpointService;
import org.opendaylight.nic.services.PolicyFramework;
import org.opendaylight.nic.services.PolicyService;
import org.opendaylight.nic.services.impl.ApplicationServiceImpl;
import org.opendaylight.nic.services.impl.DeviceServiceImpl;
import org.opendaylight.nic.services.impl.OpenflowCodeGenerator;
import org.opendaylight.nic.services.impl.PolicyFrameworkImpl;
import org.opendaylight.nic.translator.action.AllowTranslator;
import org.opendaylight.nic.translator.action.AuditTranslator;
import org.opendaylight.nic.translator.action.BlockTranslator;
import org.opendaylight.nic.translator.action.InspectTranslator;
import org.opendaylight.nic.translator.action.LatencyTranslator;
import org.opendaylight.nic.translator.action.RedirectTranslator;
import org.opendaylight.nic.translator.term.EthTypeTranslator;
import org.opendaylight.nic.translator.term.IpProtoTranslator;
import org.opendaylight.nic.translator.term.L4DstTranslator;
import org.opendaylight.nic.translator.term.L4SrcTranslator;
import org.opendaylight.nic.translator.term.VlanTranslator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.TermValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.Policy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.policy.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.policy.Classifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.policy.action.Data;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.policy.classifier.Expression;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.policy.classifier.expression.Term;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.policy.classifier.expression.term.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test framework which listens for policy changes by becoming
 * a {@link PolicyListener} and feeding those policy changes to
 * its own {@link PolicyFramework} and {@link PolicyService}.
 * This test framework creates and populates
 * its own {@link EndpointService} and {@link DeviceService}
 * with hosts (99.1.2.1-10) and a single device/switch.
 * <P>
 * In the future, these services
 * should be populated via listening to the MD-SAL. Specifically,
 * L2switch's host discovery would populate {@link EndpointService}
 * and OpenFlow-plugin would populate {@link DeviceService}.
 *
 * @author Shaun Wackerly
 */
public class NetworkIntentCompilerTestFramework implements PolicyListener, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(NetworkIntentCompilerTestFramework.class);
    private static final String APP_NAME = "NetworkIntentCompiler";
    private static final ApplicationImpl APP = new ApplicationImpl(APP_NAME, 1);

    private final PolicyNotifier notifier;
    private final PolicyFrameworkImpl framework; // FIXME: Split into PolicyService, PolicyFramework
    private final PolicyCompiler compiler;
    private final OpenflowCodeGenerator cg;
    private final DeviceService ds;
    private final EndpointService es;
    private final ApplicationService as;

    // Dummy data
    private static final DeviceId dummyDevId = new DeviceId("dummy");
    private static final Device dummyDevice = new DeviceImpl(dummyDevId);
    private static final Integer dummyVlan = 10;

    /**
     * Creates a PolicyLogger which registers itself with the given notifier.
     *
     * @param notifier notifies of policy changes
     */
    public NetworkIntentCompilerTestFramework(PolicyNotifier notifier,
                                          DataBroker dataBroker) {
        this.ds = new DeviceServiceImpl();
        this.as = new ApplicationServiceImpl();
        this.es = new EndpointDatastore(as, dataBroker, dummyDevice, this);

        this.framework = new PolicyFrameworkImpl(as);
        this.compiler = new PolicyCompiler();
        this.notifier = notifier;

        // Register an openflow code generator
        this.cg = new OpenflowCodeGenerator();
        cg.register(framework);

        // Register action translators
        cg.register(new AllowTranslator());
        cg.register(new BlockTranslator());
        cg.register(new AuditTranslator());
        cg.register(new InspectTranslator());
        cg.register(new LatencyTranslator());
        cg.register(new RedirectTranslator());

        // Register term translators
        cg.register(new VlanTranslator());
        cg.register(new EthTypeTranslator());
        cg.register(new IpProtoTranslator());
        cg.register(new L4SrcTranslator());
        cg.register(new L4DstTranslator());

        // Register common action types
        framework.register(AllowActionType.getInstance());
        framework.register(AuditActionType.getInstance());
        framework.register(BlockActionType.getInstance());
        framework.register(LatencyActionType.getInstance());
        framework.register(StatActionType.getInstance());
        framework.register(RedirectActionType.getInstance());

        // Register common term types
        framework.register(EthTypeTermType.getInstance());
        framework.register(IpProtoTermType.getInstance());
        framework.register(L4SrcTermType.getInstance());
        framework.register(L4DstTermType.getInstance());
        framework.register(VlanTermType.getInstance());

        // Register myself as an application
        as.add(APP);

        // Register a dummy device
        ds.add(dummyDevice);

        // Listen to policy changes
        notifier.registerListener(this);

        log.info("Created "+APP_NAME+" application with "+
                 framework.getActions().keySet().size()+" actions and "+
                 framework.getTermTypes().keySet().size()+" terms");
        log.debug("Actions: "+framework.getActions().entrySet());
        log.debug("Terms: "+framework.getTermTypes().entrySet());
    }

    @Override
    public void notifyChange(Set<Policy> created, Set<Policy> updated,
                             Set<Policy> removed) {
        log.info("Received "+created.size()+" created, "+updated.size()+" updated, "+removed.size()+" removed");

        // Submit all changes to the framework
        for (Policy p : created) {
            framework.add(convertPolicy(p), APP.appId());
        }
        for (Policy p : updated) {
            // FIXME: We need to retrieve the old version of the policy before the update
            framework.update(convertPolicy(p), convertPolicy(p), APP.appId());
        }
        for (Policy p : removed) {
            framework.remove(convertPolicy(p), APP.appId());
        }
        log.info("Stored all create/update/remove in the datastore.");
        log.info("Starting compile with "+framework.getPolicies().size()+" policies.");
        log.debug("Policies: "+framework.getPolicies());

        // Trigger a recompilation
        recompile();
    }

    @Override
    public void close() throws Exception {
        notifier.unregisterListener(this);
    }

    /**
     * Recompiles all data gathered by this module.
     */
    void recompile() {
        // Compile data in the framework
        List<CompiledPolicy> cops = compiler.compile(framework.getPolicies(), es.getAll(),
                                                     framework.getActions(), framework.getTermTypes(),
                                                     ds.getAll());
        log.info("Compiled into "+cops.size()+" orthogonal policies");

        // Generate OpenFlow for a dummy device
        Set<Flow> flows = cg.generate(cops, dummyDevId);
        log.info("Finished policy compilation, generated "+flows.size()+" flows");
        if (flows.size() < 100) {
            log.info("Flows: "+flows);
        } else {
            log.info(" ... flows not displayed. Too many to list.");
        }
    }

    /**
     * Converts a YANG action list into the compiler action map.
     */
    private Map<ActionLabel,AuxiliaryData> convertActions(List<Action> actions) {
        Map<ActionLabel,AuxiliaryData> converted = new HashMap<>();
        for (Action a : actions) {
            AuxiliaryDataImpl ad = new AuxiliaryDataImpl();
            for (Data d : a.getData()) {
                ad.put(d.getName(), d.getValue());
            }
            converted.put(new ActionLabel(a.getType().getValue()), ad);
        }
        return converted;
    }

    /**
     * Converts a YANG classifier into the compiler classifier.
     */
    private ClassifierImpl convertClassifier(Classifier cls) {
        Set<ExpressionImpl> expressions = new HashSet<>();
        for (Expression e : cls.getExpression()) {
            Map<TermLabel,TermImpl> terms = new HashMap<>();
            for (Term t : e.getTerm()) {
                List<IntervalImpl> intervals = new ArrayList<>();
                if (t.getValue() != null) {
                    for (TermValue v : t.getValue()) {
                        intervals.add(IntervalImpl.getInstance(v.getValue()));
                    }
                }
                if (t.getRange() != null) {
                    for (Range r : t.getRange()) {
                        intervals.add(IntervalImpl.getInstance(r.getStart().getValue(),
                                                               r.getEnd().getValue()));
                    }
                }
                TermLabel tl = new TermLabel(t.getType().getValue());
                TermImpl tr = new TermImpl(tl, intervals);
                terms.put(tl, tr);
            }
            ExpressionImpl ex = new ExpressionImpl(terms);
            expressions.add(ex);
        }

        ClassifierImpl converted = new ClassifierImpl(expressions);
        return converted;
    }

    /**
     * Converts a YANG endpoint string into the compiler endpoint set.
     */
    private EndpointGroupImpl convertEndpoints(String regex) {
        EndpointGroupImpl converted = new EndpointGroupImpl(regex);
        return converted;
    }

    /**
     * Converts a YANG/API policy into the compiler policy structure.
     *
     * @param policy the policy to convert
     */
    private org.opendaylight.nic.intent.Policy convertPolicy(Policy p) {
        PolicyId id = new PolicyId(new Integer(p.hashCode()).toString());
        EndpointGroupImpl src = convertEndpoints(p.getSourceEndpoints());
        EndpointGroupImpl dst = convertEndpoints(p.getDestinationEndpoints());
        ApplicationImpl app = APP;
        ClassifierImpl cls = convertClassifier(p.getClassifier());
        Map<ActionLabel,AuxiliaryData> actions = convertActions(p.getAction());
        boolean isExclusive = false;
        PolicyImpl pr = new PolicyImpl(id, "AutoGenerated-"+id.toString(),
                                                     app, src, dst, cls, actions, isExclusive);
        return pr;
    }

    /**
     * Creates a dummy endpoint with the given IP, on the given port and vlan of
     * the dummy device.
     *
     * @param ip endpoint IP
     * @param port endpoint port (on dummyDevice)
     * @param vlan endpoint vlan (on dummyDevice)
     * @return endpoint
     */
    private static IpEndpoint makeEndpoint(String ip, Integer port, Integer vlan) {
        IpEndpoint ep1 = null;
        try {
            ep1 = new IpEndpoint( InetAddress.getByName(ip),
                                    new PortImpl(port),
                                    new SegmentId(vlan),
                                    dummyDevice);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Could not create endpoint: "+ip);
        }
        return ep1;
    }

}
