package com.lyf.regionless.vf2_mono;

import java.util.BitSet;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;


public class ARGraph {
    MutableValueGraph<Integer, EdgeAttr> graph;
    public BitSet[] bitsets;

    public ARGraph() {
        graph = ValueGraphBuilder.undirected().build();
        bitsets = null;
    }

    public void insertEdge(int node1, int node2, double bw, double ltc) {
        EdgeAttr edge_attr = new EdgeAttr();
        edge_attr.bw = bw;
        edge_attr.ltc = ltc;
        graph.putEdgeValue(node1, node2, edge_attr);
    }

    public int nodeCount() {
        return graph.nodes().size();
    }

    int degree(int node) {
        return graph.degree(node);
    }

    boolean compatibleNode(int node1, int node2) {
        if (bitsets == null) {
            return true;
        }
        else {
            return bitsets[node1].get(node2);
        }
    }

    int getNbrEdge(int node, int i, EdgeAttr attr) {
        Integer[] adjs_arr = graph.adjacentNodes(node).toArray(new Integer[0]);
        EdgeAttr attr2 = graph.edgeValueOrDefault(node, adjs_arr[i], new EdgeAttr());
        attr.bw = attr2.bw;
        attr.ltc = attr2.ltc;
        return adjs_arr[i];
    }

    int getNbrEdge(int node, int i) {
        Integer[] adjs_arr = graph.adjacentNodes(node).toArray(new Integer[0]);
        return adjs_arr[i];
    }

    boolean compatibleEdge(EdgeAttr attr1, EdgeAttr attr2) {
        return (attr1.bw <= attr2.bw) && (attr1.ltc >= attr2.ltc);
    }

    EdgeAttr getEdgeAttr(int node1, int node2) {
        return graph.edgeValueOrDefault(node1, node2, new EdgeAttr());
    }

    boolean hasEdge(int node1, int node2) {
        return graph.adjacentNodes(node1).contains(node2);
    }
}
