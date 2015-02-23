//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.DeviceId;
import org.opendaylight.nic.compiler.CompiledPolicy;
import org.opendaylight.nic.compiler.DetectResolve;
import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.ActionType;
import org.opendaylight.nic.extensibility.TermLabel;
import org.opendaylight.nic.extensibility.TermType;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointId;
import org.opendaylight.nic.intent.Policy;

/**
 * A policy compiler which compiles a set of {@link Policy} objects into
 * {@link CompiledPolicy} objects.
 *
 * @author Shaun Wackerly
 */
public class PolicyCompiler {

    private List<CompilerNode> nodes;

    public PolicyCompiler() {
        nodes = new LinkedList<CompilerNode>();
    }

    /**
     * Compiles the given set of policies into a set of compiled policies, using
     * the given endpoints, actions, terms, and devices to aid the compilation
     * process.
     *
     * @param policyRequests
     *            given set of policies
     * @param endpoints
     *            given set of endpoints
     * @param actions
     *            given set of actions
     * @param terms
     *            given set of terms
     * @param devices
     *            given set of devices
     * @return set of compiled policies
     */
    public List<CompiledPolicy> compile(Set<Policy> policyRequests,
            Map<EndpointId, Endpoint> endpoints,
            Map<ActionLabel, ActionType> actions,
            Map<TermLabel, TermType> terms, Map<DeviceId, Device> devices) {

        // use policies to create initial list of CompilerNodes
        List<CompilerNode> nodeList = new LinkedList<CompilerNode>();
        for (Policy pr : policyRequests) {
            CompilerNode n = CompilerNode.createNode(pr, endpoints, actions,
                    terms);
            nodeList.add(n);
        }

        // TODO for policies using service groups, check service group
        // availability
        // if not available, modify actions to use failure action

        // detect and resolve conflicts, creating orthogonal nodes in the
        // process
        detectAndResolve(nodeList, endpoints);

        // remove nodes with empty src or dst groups

        // compare with previous result, create sets of new,delete,changed

        // remove delegated compiler nodes
        List<CompiledPolicy> cops = new LinkedList<>();
        for (CompilerNode cn : nodeList) {
            // only return Nodes which have not delegated Policy Space
            if (cn.delegates().size() != 0) {
                continue;
            }
            // if either src or dst members is empty don't include CompilerNode
            // TODO: account for any and all
            if (cn.srcMembers().isEmpty() || cn.dstMembers().isEmpty()) {
                continue;
            }
            // if src and dst are equal, and size one, don't include it
            if ((cn.srcMembers().size() == 1)
                    && cn.srcMembers().equals(cn.dstMembers())) {
                continue;
            }
            cops.add(cn);
        }

        return cops;
    }

    private void detectAndResolve(List<CompilerNode> list,
            Map<EndpointId, Endpoint> endpointMap) {

        DetectResolve dr = new DetectResolve(list, endpointMap);

        while (dr.detectAndResolve()) {
            continue;
        }
    }
}