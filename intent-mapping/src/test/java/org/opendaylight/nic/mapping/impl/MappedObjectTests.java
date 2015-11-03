/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.mapping.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.nic.mapping.api.EgressPoint;
import org.opendaylight.nic.mapping.api.IngressPoint;
import org.opendaylight.nic.mapping.api.MplsEgressLabel;
import org.opendaylight.nic.mapping.api.MplsIngressLabel;

public class MappedObjectTests {

    @Test
    public void testMplsEgressNullLabelString() {
        MplsEgressLabel mpls = new MplsEgressLabel();
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(mpls.toString().contains("null"));
    }

    @Test
    public void testMplsIngressNullLabelString() {
        MplsIngressLabel mpls = new MplsIngressLabel();
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(mpls.toString().contains("null"));
    }

    @Test
    public void testEgressNullPointString() {
        EgressPoint mpls = new EgressPoint();
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(mpls.toString().contains("null"));
    }

    @Test
    public void testIngressNullLabelString() {
        IngressPoint mpls = new IngressPoint();
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(mpls.toString().contains("null"));
    }

    @Test
    public void testMplsEgressLabelString() {
        MplsEgressLabel mpls = new MplsEgressLabel();
        mpls.setLabel("87");
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(!mpls.toString().contains("null"));
    }

    @Test
    public void testMplsIngressLabelString() {
        MplsIngressLabel mpls = new MplsIngressLabel();
        mpls.setLabel("89");
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(!mpls.toString().contains("null"));
    }

    @Test
    public void testEgressPointString() {
        EgressPoint mpls = new EgressPoint();
        mpls.setEgressPoint("router2");
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(!mpls.toString().contains("null"));
    }

    @Test
    public void testIngressLabelString() {
        IngressPoint mpls = new IngressPoint();
        mpls.setIngressPoint("router4");
        System.out.println(mpls.toString());
        assertNotNull(mpls.toString());
        assertTrue(!mpls.toString().contains("null"));
    }

}
