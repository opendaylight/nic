//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.impl;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.opendaylight.nic.compiler.Edge;
import org.opendaylight.nic.compiler.Epg;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class ViewCompiledGraph {

    public static void showGraph(DirectedGraph<Epg, Edge> gr) {

        Layout<Epg, Edge> layout = new CircleLayout<Epg, Edge>(gr);
        layout.setSize(new Dimension(300, 300));
        BasicVisualizationServer<Epg, Edge> vs =
                new BasicVisualizationServer<Epg, Edge>(layout);
        vs.setPreferredSize(new Dimension(350, 350));

        vs.getRenderContext().setVertexLabelTransformer(
                new ToStringLabeller<Epg>());
        vs.getRenderContext().setEdgeLabelTransformer(
                new ToStringLabeller<Edge>());
        vs.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

        JFrame frame = new JFrame("Compiled Graph View");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.getContentPane().add(vs);
        frame.pack();
        frame.setVisible(true);

    }
}