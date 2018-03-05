/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Set;

/**
 * Created by yrineu on 28/07/17.
 */
@PrepareForTest(PolicyOptionsConfig.class)
@RunWith(PowerMockRunner.class)
public class PolicyOptionConfigTest {

    @Test
    public void testRPCStructure() {
        final StringBuffer result = new StringBuffer();
        final String policyName = "Intent Dev team";
        final Set<Integer> vlans = Sets.newHashSet();
        vlans.add(100);
        final PolicyOptionsConfig policyOptionsConfig = PolicyOptionsConfig
                .create(vlans, PolicyMatchType.community, PolicyActionType.accept, result);
        System.out.println(result.toString());
    }

    private String getStructure() {
        return "<policy-options><policy-statement><name>Intent_Dev_team</name><term><name>vni100</name>" +
                "<from><community>vni100</community></from><then><accept/></then></term></policy-statement>" +
                "<community><name>vni100</name><members>target:10000:100</members></community></policy-options>";
    }
}
