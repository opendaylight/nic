package org.opendaylight.nic.compiler;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class Test {

    private static DirectedGraph<String, String> gr = new DirectedSparseGraph<String, String>();

    public void add(String s1, String s2) {

        gr.addVertex(s1);
        gr.addVertex(s2);

        gr.addEdge("1", s1, s2);

        if (gr.containsVertex(s1)) {
            gr.addVertex("lol");
            gr.addEdge("2", s1, "lol");
        }
    }

    public static void main(String[] args) {

        Test test = new Test();
        String s1 = "vinu";
        String s2 = "kinu";
        test.add(s1, s2);
        System.out.println(gr.toString());

    }

}