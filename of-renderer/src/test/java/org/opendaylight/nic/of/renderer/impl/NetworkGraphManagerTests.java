package org.opendaylight.nic.of.renderer.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

public class NetworkGraphManagerTests {
    @Test
    public void testIdentifyLinkChanged() {
        List<Link> currentLinks = new ArrayList<>();
        List<Link> newLinks = new ArrayList<>();

        Link link1 = mock(Link.class);
        when(link1.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link2 = mock(Link.class);
        when(link2.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link3 = mock(Link.class);
        when(link3.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link4 = mock(Link.class);
        when(link4.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));

        currentLinks.add(link1);
        currentLinks.add(link2);
        currentLinks.add(link3);
        currentLinks.add(link4);

        newLinks.add(link1);
        newLinks.add(link2);
        newLinks.add(link3);

        NetworkGraphManager graph = new NetworkGraphManager();
        List<Link> changed = graph.identifyChangedLink(currentLinks, newLinks);

        assertTrue(changed.get(0).hashCode() == link4.hashCode());
    }

    @Test
    public void testGetAffectedIntents() {

        List<List<Link>> list1 = new ArrayList<>();
        List<List<Link>> list2 = new ArrayList<>();

        List<Link> currentLinks = new ArrayList<>();
        List<Link> newLinks = new ArrayList<>();

        Link link1 = mock(Link.class);
        when(link1.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link2 = mock(Link.class);
        when(link2.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link3 = mock(Link.class);
        when(link3.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link4 = mock(Link.class);
        when(link4.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));

        currentLinks.add(link1);
        currentLinks.add(link2);
        currentLinks.add(link3);
        currentLinks.add(link4);

        newLinks.add(link1);
        newLinks.add(link2);
        newLinks.add(link3);

        list1.add(newLinks);
        list2.add(currentLinks);

        NetworkGraphManager graph = new NetworkGraphManager();
        Intent intent1 = mock(Intent.class);
        Intent intent2 = mock(Intent.class);

        NetworkGraphManager.ProtectedLinks.put(intent1, list1);
        NetworkGraphManager.ProtectedLinks.put(intent2, list2);

        List<Link> changed = graph.identifyChangedLink(currentLinks, newLinks);

        List<Intent> affectedIntents = graph.getAffectedIntents(changed.get(0));

        assertTrue(affectedIntents.get(0).hashCode() == intent2.hashCode());
    }

    @Test
    public void testGetNoneAffectedIntents() {

        List<List<Link>> list1 = new ArrayList<>();
        List<List<Link>> list2 = new ArrayList<>();

        List<Link> currentLinks = new ArrayList<>();
        List<Link> newLinks = new ArrayList<>();

        Link link1 = mock(Link.class);
        when(link1.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link2 = mock(Link.class);
        when(link2.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link3 = mock(Link.class);
        when(link3.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));
        Link link4 = mock(Link.class);
        when(link4.getLinkId()).thenReturn(new LinkId(String.valueOf(new Random().nextLong())));

        currentLinks.add(link1);
        currentLinks.add(link2);
        currentLinks.add(link3);
        currentLinks.add(link4);

        newLinks.add(link1);
        newLinks.add(link2);
        newLinks.add(link3);
        newLinks.add(link4);

        list1.add(newLinks);
        list2.add(currentLinks);

        NetworkGraphManager graph = new NetworkGraphManager();
        Intent intent1 = mock(Intent.class);
        Intent intent2 = mock(Intent.class);

        NetworkGraphManager.ProtectedLinks.put(intent1, list1);
        NetworkGraphManager.ProtectedLinks.put(intent2, list2);

        List<Link> changed = graph.identifyChangedLink(currentLinks, newLinks);

        assertTrue(changed.size() == 0);
    }
}
