package com.lyf.regionless.vf2_mono;
import java.util.concurrent.atomic.AtomicInteger;

public class Match {
    private static int NULL_NODE = -1;

    /*-------------------------------------------------------------
     * bool match(s0, pn, c1, c2)
     * Finds a matching between two graph, if it exists, given the
     * initial state of the matching process.
     * Returns true a match has been found.
     * *pn is assigned the number of matched nodes, and
     * c1 and c2 will contain the ids of the corresponding nodes
     * in the two graphs
     ------------------------------------------------------------*/
    public static boolean match(State s0, int c1[], int c2[]) {
        return match(c1, c2, s0);
    }

    /*------------------------------------------------------------
     * int match(s0, vis, usr_data)
     * Visits all the matches between two graphs, given the
     * initial state of the match.
     * Returns the number of visited matches.
     * Stops when there are no more matches, or the visitor vis
     * returns true.
     ----------------------------------------------------------*/
    public static int match(State s0, Visitor vis) {
        ARGraph g1 = s0.getGraph1();
        int n1 = g1.nodeCount();
        
        int[] c1 = new int[n1];
        int[] c2 = new int[n1];

        AtomicInteger count = new AtomicInteger();
        match(c1, c2, vis, s0, count);

        return count.get();
    }

    /*-------------------------------------------------------------
     * static boolean match(pn, c1, c2, s)
     * Finds a matching between two graphs, if it exists, starting
     * from state s.
     * Returns true a match has been found.
     * *pn is assigned the numbero of matched nodes, and
     * c1 and c2 will contain the ids of the corresponding nodes
     * in the two graphs.
     ------------------------------------------------------------*/
    static boolean match(int c1[], int c2[], State s) {
        if (s.isGoal()) {
            s.getCoreSet(c1, c2);
            return true;
        }

        if (s.isDead()) {
            return false;
        }

        AtomicInteger pn1 = new AtomicInteger(NULL_NODE);
        AtomicInteger pn2 = new AtomicInteger(NULL_NODE);
        boolean found = false;
        while (!found && s.nextPair(pn1, pn2, pn1.get(), pn2.get())) {
            if (s.isFeasiblePair(pn1.get(), pn2.get())) {
                State s1 = s.copy();
                s1.addPair(pn1.get(), pn2.get());
                found = match(c1, c2, s1);
                s1.backTrack();
            }
        }
        return found;
    }

    /*-------------------------------------------------------------
     * static boolean match(c1, c2, vis, pcount)
     * Visits all the matchings between two graphs,  starting
     * from state s.
     * Returns true if the caller must stop the visit.
     * Stops when there are no more matches, or the visitor vis
     * returns true.
     ------------------------------------------------------------*/
    static boolean match(int c1[], int c2[], Visitor vis, State s, AtomicInteger pcount) {
        if (s.isGoal()) {
            pcount.getAndIncrement();
            s.getCoreSet(c1, c2);
            return vis.visit(c1, c2);
        }

        if (s.isDead()) {
            return false;
        }

        AtomicInteger n1 = new AtomicInteger(NULL_NODE);
        AtomicInteger n2 = new AtomicInteger(NULL_NODE);

        while (s.nextPair(n1, n2, n1.get(), n2.get())) {
            if (s.isFeasiblePair(n1.get(), n2.get())) {
                State s1 = s.copy();
                s1.addPair(n1.get(), n2.get());
                if (match(c1, c2, vis, s1, pcount)) {
                    s1.backTrack();
                    return true;
                }
                else {
                    s1.backTrack();
                }
            }
        }
        return false;
    }
}
