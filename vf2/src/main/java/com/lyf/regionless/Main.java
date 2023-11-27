package com.lyf.regionless;

import com.lyf.regionless.vf2_mono.*;
import static com.lyf.regionless.vf2_mono.Match.match;

public class Main {
    public static void main(String[] args) {
        ARGraph g1 = new ARGraph();
        g1.insertEdge(0, 1, 1, 1);
        g1.insertEdge(1, 2, 1, 1);

        ARGraph g2 = new ARGraph();
        g2.insertEdge(0, 1, 1, 1);
        g2.insertEdge(0, 2, 1, 1);
        g2.insertEdge(0, 3, 0, 0);
        g2.insertEdge(1, 2, 1, 1);
        g2.insertEdge(2, 3, 0, 0);

        // 输出第一个解
        State s = new VF2MonoState(g1, g2);
        int[] c1 = new int[g1.nodeCount()];
        int[] c2 = new int[g1.nodeCount()];
        match(s, c1, c2);
        for (int i = 0; i < c2.length; i++) {
            System.out.println(c2[i]);
        }

        // 输出所有解
        State s0 = new VF2MonoState(g1, g2);

        class MyVisitor extends Visitor {
            public boolean visit(int[] c1, int[] c2) {
                System.out.println("solution:");
                for (int i = 0; i < c2.length; i++) {
                    System.out.println(c2[i]);
                }
                return false;
            }
        }
        MyVisitor vis = new MyVisitor();
        match(s0, vis);
    }
}
