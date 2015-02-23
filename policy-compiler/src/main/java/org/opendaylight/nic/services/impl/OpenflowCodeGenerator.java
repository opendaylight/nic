//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.common.DeviceId;
import org.opendaylight.nic.compiler.CompiledPolicy;
import org.opendaylight.nic.compiler.impl.ExpressionImpl;
import org.opendaylight.nic.compiler.impl.IntervalImpl;
import org.opendaylight.nic.compiler.impl.TermImpl;
import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.ActionTranslator;
import org.opendaylight.nic.extensibility.CodeGenerator;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermTranslator;
import org.opendaylight.nic.extensibility.TermType;
import org.opendaylight.nic.extensibility.terms.L4DstTermType;
import org.opendaylight.nic.extensibility.terms.L4SrcTermType;
import org.opendaylight.nic.intent.Action;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.Expression;
import org.opendaylight.nic.intent.Interval;
import org.opendaylight.nic.intent.Term;
import org.opendaylight.nic.intent.impl.IpEndpoint;
import org.opendaylight.nic.services.PolicyFramework;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates OpenFlow flow-mod messages, based upon a set of compiled policies.
 *
 * @author Shaun Wackerly
 */
public class OpenflowCodeGenerator implements CodeGenerator<Flow,Instruction,MatchBuilder> {

    private static final Logger log = LoggerFactory.getLogger(OpenflowCodeGenerator.class);

    /**
     * TODO:
     *  1. Cache per-device capabilities such as matching, masking, tables supported.
     *  2. Refer to cached capabilities to optimize generated flows.
     *  3. Create an internal class which would be created as a sub-generator on
     *     a per-device basis. That generator could then have a reference to the device
     *     for which it is generating openflow, without the need to pass device ID in
     *     each method call. Don't make the public class per-device, because that would
     *     require per-device translator registration.
     */

    /** A mapping from action label to translator. */
    private final Map<ActionLabel,ActionTranslator<Instruction>> ofActionTranslators;

    /** A mapping from term label to translator. */
    private final Map<TermLabel,TermTranslator<MatchBuilder>> ofTermTranslators;

    /** The framework to which this generator is registered. */
    private PolicyFramework framework;

    /**
     * Constructs an OpenFlow code generator.
     */
    public OpenflowCodeGenerator() {
        ofActionTranslators = new HashMap<>();
        ofTermTranslators = new HashMap<>();
    }

    private enum MatchType { SRC, DST };

    @Override
    public void register(ActionTranslator<Instruction> trans) {
        if (trans.actionLabel() == null)
            throw new IllegalArgumentException("Translator must return valid label");
        if (ofActionTranslators.containsKey(trans.actionLabel()))
            throw new IllegalArgumentException("Translator already registered for action: " + trans.actionLabel());

        ofActionTranslators.put(trans.actionLabel(), trans);
    }

    @Override
    public void register(TermTranslator<MatchBuilder> trans) {
        if (ofTermTranslators.containsKey(trans.termLabel()))
            throw new IllegalArgumentException("Translator already registered for term: " + trans.termLabel());

        ofTermTranslators.put(trans.termLabel(), trans);
    }

    @Override
    public void register(PolicyFramework framework) {
        this.framework = framework;
        framework.register(this);
    }

    /**
     * Translates an action to an OpenFlow instruction.
     *
     * @param a the action to translate
     * @param pv the OpenFlow protocol version
     * @return an OpenFlow flow action
     */
    private Set<Instruction> translateAction(Action a) {
        ActionTranslator<Instruction> at = ofActionTranslators.get(a.label());
        if (at == null)
            throw new IllegalArgumentException("No translator registered for action: " + a.label());

        return at.translate(a.data());
    }

    /**
     * Translates a classifier term to an OpenFlow match builder.
     *
     * @param t the term to translate
     * @return a single OpenFlow match
     */
    private void translateTerm(MatchBuilder mb, Term t) {
        TermTranslator<MatchBuilder> tt = ofTermTranslators.get(t.typeLabel());
        if (tt == null)
            throw new IllegalArgumentException("No translator registered for term: " + t.typeLabel());

        tt.translate(mb, t.getIntervals());
    }

    @Override
    public Set<Flow> generate(List<CompiledPolicy> policies, DeviceId d) {
        Set<Flow> flows = new HashSet<>();

        // Convert policies to flow mods
        for (CompiledPolicy p : policies) {
            // Translate policy to flow-mod
            Set<Flow> fs = translatePolicyToFlows(p);
            log.debug("Split policy "+p+" into "+fs.size()+" flows");

            for (Flow flow : fs) {
                // Split this flow-mod into multiple flow-mods
                // which will be compatible with the device tables.
                Set<Flow> specificFlows = new HashSet<>();

                // FIXME: Call DIDM method to adjust flow mods ...
                specificFlows.add(flow);

                flows.addAll(specificFlows);
            }
        }

        return flows;
    }

    /**
     * Generates a set of expressions which include every possible
     * combination of the given terms, according to the restriction
     * that each expression can have only one term of any given type.
     * Starts from the list of existing expressions, and modifies each
     * to include all possible combinations of the given terms.
     *
     * @param termMap map of term types to terms
     * @return list of modified expres
     */
    private Set<ExpressionImpl> generateExpressions(Map<TermType,Set<TermImpl>> termMap) {
        // Start with a single, empty expression
        Set<ExpressionImpl> expressions = new HashSet<>();
        expressions.add(ExpressionImpl.getInstance());

        // Make one copy of every expression for each possible value of the term
        for (Map.Entry<TermType, Set<TermImpl>> entry : termMap.entrySet()) {
            // Get the set of terms for this type
            Set<TermImpl> terms = entry.getValue();

            // For each expression ...
            Set<ExpressionImpl> modExpressions = new HashSet<>();
            for (ExpressionImpl ex : expressions) {
                // Make a modified version of the expression for each term
                for (TermImpl term : terms) {
                    ExpressionImpl modExp = ExpressionImpl.getInstance(ex.getTerms());
                    modExp.addTerm(term);
                    modExpressions.add(modExp);
                }
            }

            // Make our modified set the new working set
            expressions = modExpressions;
        }

        return expressions;
    }

    /**
     * Splits a single expression into multiple expressions which perform
     * the same matching, but where each individual expression can be
     * translated into a SINGLE flow.
     *
     * For example, if an expression matched L4_SRC=1,3 and L4_DST=2,4
     * then we'd split that single expression into 4 different expressions:
     *    L4_SRC=1 L4_DST=2
     *    L4_SRC=1 L4_DST=4
     *    L4_SRC=3 L4_DST=2
     *    L4_SRC=3 L4_DST=4
     *
     * @param ex expression to split
     * @return set of expressions
     */
    private Set<ExpressionImpl> splitExpression(Expression ex) {
        // Split all terms in the given expression into a per-type set
        // where each member of the set can be put into a single flow.
        Map<TermType,Set<TermImpl>> splitTerms = new HashMap<>();
        for (Term t : ex.getTerms()) {
            Set<TermImpl> terms = new HashSet<>();
            TermType tt = framework.getTermType(t.typeLabel());
            for (Interval i : t.getIntervals()) {
                // FIXME: Right now we make the assumption that we can
                // only ever fit a single interval value into a flow.
                // This is a safe assumption, but it will cause us to
                // split into too many flows when a mask field exists.
                for (int v = i.start(); v <= i.end(); v++) {
                    IntervalImpl ii = IntervalImpl.getInstance(v);
                    terms.add(TermImpl.getInstance(tt, ii));
                }
            }
            splitTerms.put(tt, terms);
        }

        // Generate expressions for the map of terms we computed above.
        return generateExpressions(splitTerms);
    }

    /**
     * Splits a set of classifier expressions into a possibly larger
     * set which performs the same matching, but does so where each
     * individual expression can be translated into a SINGLE flow.
     *
     * @param expressions the set of expressions to split
     * @return a split set with the same matching
     */
    private Set<Expression> splitExpressions(Set<? extends Expression> expressions) {
        Set<Expression> splitExpressions = new HashSet<>();
        for (Expression e : expressions) {
            Set<ExpressionImpl> exp = splitExpression(e);
            if (exp.size() > 100)
                log.debug("Split expression into "+exp.size()+" expressions: "+e);
            splitExpressions.addAll(exp);
        }
        return splitExpressions;
    }

    /**
     * Determines whether we'll translate this term during the
     * first pass or second pass.
     *
     * @param term term to translate
     * @return true if translate in first pass, false if not
     */
    private boolean translateInFirstPass(Term t) {
        if (t.typeLabel().equals(L4DstTermType.getInstance().label()))
            return false;
        if (t.typeLabel().equals(L4SrcTermType.getInstance().label()))
            return false;

        return true;
    }

    /**
     * Translates a classifier expression to an OpenFlow match builder.
     *
     * @param c the classifier to translate
     * @param mb match builder to use for translation
     */
    private void translateExpression(Expression ex, MatchBuilder mb) {
        Set<TermLabel> labels = new HashSet<>();
        for (Term t : ex.getTerms())
            labels.add(t.typeLabel());
        log.debug("Translating terms: "+labels);

        // Translate lower-layer terms first ...
        for (Term t : ex.getTerms()) {
            if (translateInFirstPass(t))
                translateTerm(mb, t);
        }
        // ... then translate higher layer terms.
        for (Term t : ex.getTerms()) {
            if (!translateInFirstPass(t))
                translateTerm(mb, t);
        }
    }

    /**
     * Translates the given endpoint to an OpenFlow match, using the
     * given field type as the field against which the group will be matched.
     *
     * @param ep the endpoint to translate
     * @param type the type of match
     * @param mb match builder to use for translation
     */
    private void matchEndpoint(Endpoint ep, MatchType type, MatchBuilder mb) {
        // Get an L3 match builder, either from scratch or based on the existing one
        Ipv4MatchBuilder ipv4;
        if (mb.getLayer3Match() != null) {
            Layer3Match l3m = mb.getLayer3Match();
            if (!(l3m instanceof Ipv4Match))
                throw new IllegalArgumentException("Cannot match IP endpoint with existing match: "+l3m);
            ipv4 = new Ipv4MatchBuilder((Ipv4Match)l3m);
        } else {
            ipv4 = new Ipv4MatchBuilder();
        }

        // Add fields to the L3 match criteria
        if ((ep instanceof IpEndpoint) && ((IpEndpoint)ep).ip() != null) {
            IpEndpoint ipep = (IpEndpoint)ep;
            if (type == MatchType.DST)
                ipv4.setIpv4Destination(new Ipv4Prefix(ipep.ip().getHostAddress()+"/32"));
            if (type == MatchType.SRC)
                ipv4.setIpv4Source(new Ipv4Prefix(ipep.ip().getHostAddress()+"/32"));
            mb.setLayer3Match(ipv4.build());
        } else {
            log.error("Cannot match IP for endpoint '"+ep.id()+"' of type "+ep.getClass().getName());
        }
    }

    /**
     * Translates a set of source and destination endpoints into a
     * set of matches, based upon the fields already set in the given
     * match builder.
     *
     * @param srcEp set of source endpoints
     * @param dstEp set of destination endpoints
     * @param mb match builder
     * @return set of matches
     */
    private Set<Match> translateEndpoints(Set<Endpoint> srcEp, Set<Endpoint> dstEp, MatchBuilder mb) {
        Set<Match> matches = new HashSet<>();

        // Translate the endpoints. We'll generate N*M flows which match
        // all N sources and M destinations.
        if (srcEp.isEmpty()) {
            // No match specified for source. Only match destination.
            if (dstEp.isEmpty()) {
                matches.add(mb.build());
            } else {
                for (Endpoint dst : dstEp) {
                    matchEndpoint(dst, MatchType.DST, mb);
                    matches.add(mb.build());
                }
            }
        } else {
            // Match for each source
            for (Endpoint src : srcEp) {
                matchEndpoint(src, MatchType.SRC, mb);

                if (dstEp.isEmpty()) {
                    matches.add(mb.build());
                } else {
                    // Add a flow for each destination
                    for (Endpoint dst : dstEp) {
                        matchEndpoint(dst, MatchType.DST, mb);
                        matches.add(mb.build());
                    }
                }
            }
        }

        return matches;
    }

    /**
     * Translates a policy to an OpenFlow flow-mod message.
     *
     * @param p the policy to translate
     * @param pv the OpenFlow protocol version
     * @return an OpenFlow flow-mod message
     */
    private Set<Flow> translatePolicyToFlows(CompiledPolicy p) {
        Set<Flow> flows = new HashSet<>();

        // Create a new flow add message
        FlowBuilder flow = new FlowBuilder();

        // NOTE: We put all actions into a single APPLY_ACTIONS instruction.
        // This is a convention that will be known by the device drivers, which
        // will reformat the flat list of actions into a device-specific way.
        // For example, Comware implements COPY using both WRITE_ACTIONS and
        // APPLY_ACTIONS (see FlowModComware.java). This CodeGen class will
        // provide the copy actions in a single APPLY_ACTIONS instruction.

        // Translate each action set
        InstructionsBuilder ib = new InstructionsBuilder();
        ib.setInstruction( new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>() );
        if (p.actions() != null) { // FIXME: Remove this clause, once compiler sends good data.
            for (Set<Action> as : p.actions()) {
                // Translate each action
                for (Action a : as) {
                    log.debug("Translated action "+a+" into "+translateAction(a).size()+" instructions");
                    for (Instruction i : translateAction(a)) {
                        // FIXME: There should be a better way to do things than the cast on the following line ...
                        ib.getInstruction().add((org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction)i);
                    }
                }
            }
        }

        // Add the instruction(s) to the flow
        flow.setInstructions(ib.build());

        // Some terms cannot be fit into a single flow, even with masking (such as L4 ports=1,3)
        // so we'll do a first pass and split the expressions so that each expression can be
        // represented in a single flow.
        Set<Expression> expressions = splitExpressions(p.classifier().getExpressions());
        log.debug("Split "+p.classifier().getExpressions().size()+" expressions into "+expressions.size()+" expressions");

        // If there are no expressions in the list, insert a dummy expression so that we'll
        // make at least one pass through the loop to match on endpoints.
        if (expressions.isEmpty()) {
            expressions.add(ExpressionImpl.getInstance()); // no match criteria
        }

        // Translate the expressions. We will generate a separate flow for each expression
        // in the classifier, because expressions are logically OR'ed together.
        for (Expression ex : expressions) {
            // Create a match builder which will allow matching both classifier and endpoints.
            MatchBuilder mb = new MatchBuilder();

            // Translate the expression into a set of matches
            translateExpression(ex, mb);

            // FIXME How should the policy engine represent 'ALL' endpoints to
            // code generation? Presently, code generation assumes that an
            // empty set implies 'ALL' endpoints (ie: no match). See below ...

            // FIXME: For a classifier with multiple expressions, we're redoing the
            // same computation for EACH expression. Is there a way to only do it once?
            // Translate the endpoints into a set of matches, then add
            // a flow for each match.
            Set<Match> matches = translateEndpoints(p.src(), p.dst(), mb);
            log.debug("Translated a set of "+p.src().size()+" source endpoints and "+p.dst().size()+" destination endpoints into a set of "+matches.size()+" matches");
            for (Match m : matches) {
                // Add one flow for each distinct match
                flow.setMatch(m);
                flows.add(flow.build());
            }
        }

        return flows;
    }

    @Override
    public boolean isApplied(CompiledPolicy policy, DeviceId dev) {
        // TODO Auto-generated method stub
        return true;
    }

}
