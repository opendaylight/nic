//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility;

import java.util.List;
import java.util.Set;

import org.opendaylight.nic.common.DeviceId;
import org.opendaylight.nic.compiler.CompiledPolicy;
import org.opendaylight.nic.services.PolicyFramework;

/**
 * Generates native protocol formats, based upon compiled policies. The
 * registered set of translators will be used to translate compiled policies
 * into a native protocol format.
 *
 * @param N
 *            native protocol output type
 * @param A
 *            native type required for translating actions
 * @param B
 *            native builder required for translating terms
 * @author Shaun Wackerly
 */
public interface CodeGenerator<N, A, B> {

    /**
     * Registers the given action translator to be used when translating actions
     * to OpenFlow as a native protocol.
     *
     * @param trans
     *            action translator
     */
    void register(ActionTranslator<A> trans);

    /**
     * Registers the given action or term translator to be used when translating
     * actions to OpenFlow as a native protocol.
     *
     * @param trans
     *            translator to register
     */
    void register(TermTranslator<B> trans);

    /**
     * Generates a set of native protocol objects which will implement the
     * behavior described by the policies on the given device.
     *
     * @param policies
     *            the policies to apply
     * @param dev
     *            the device to apply the policies against
     * @return the set of natively-typed objects
     */
    Set<N> generate(List<CompiledPolicy> policies, DeviceId dev);

    /**
     * Checks whether the given {@link CompiledPolicy} has been applied to the
     * given device.
     *
     * @param policy
     *            the compiled policy
     * @param dev
     *            the device
     * @return true if applied, false if not
     */
    boolean isApplied(CompiledPolicy policy, DeviceId dev);

    /**
     * Registers this code generator with the given policy framework.
     *
     * @param framework
     *            policy framework
     */
    void register(PolicyFramework framework);
}
