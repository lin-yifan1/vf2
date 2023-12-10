package com.lyf.regionless.vf2_mono;

import java.util.concurrent.atomic.AtomicInteger;

public class VF2MonoState implements State {
    int core_len, orig_core_len;
    int added_node1;
    int t1_len, t2_len;

    int[] core_1;
    int[] core_2;
    int[] nbr_1;
    int[] nbr_2;

    int[] order;

    private int NULL_NODE = -1;

    ARGraph g1, g2;
    int n1, n2;

    class ShareCount {
        long share_count;

        ShareCount() {
            share_count = 1;
        }

        void add() {
            share_count++;
        }
    };

    ShareCount share_count;

    /*----------------------------------------------------------
     * VF2MonoState::VF2MonoState(g1, g2, sortNodes)
     * Constructor. Makes an empty state.
     ---------------------------------------------------------*/
    public VF2MonoState(ARGraph ag1, ARGraph ag2) {
        g1 = ag1;
        g2 = ag2;
        n1 = g1.nodeCount();
        n2 = g2.nodeCount();

        // TODO: ordering
        order = null;

        core_len = 0;
        orig_core_len = 0;
        t1_len = 0;
        t2_len = 0;

        added_node1 = NULL_NODE;

        core_1 = new int[n1];
        core_2 = new int[n2];
        nbr_1 = new int[n1];
        nbr_2 = new int[n2];
        share_count = new ShareCount();

        for (int i = 0; i < n1; i++) {
            core_1[i] = NULL_NODE;
        }
        for (int i = 0; i < n2; i++) {
            core_2[i] = NULL_NODE;
        }
    }

    /*----------------------------------------------------------
     * VF2MonoState.VF2MonoState(state)
     * Copy constructor.
     ---------------------------------------------------------*/
    public VF2MonoState(VF2MonoState state) {
        g1 = state.g1;
        g2 = state.g2;
        n1 = state.n1;
        n2 = state.n2;

        order = state.order;

        core_len = orig_core_len = state.core_len;
        t1_len = state.t1_len;
        t2_len = state.t2_len;

        added_node1 = NULL_NODE;

        core_1 = state.core_1;
        core_2 = state.core_2;
        nbr_1 = state.nbr_1;
        nbr_2 = state.nbr_2;
        share_count = state.share_count;

        share_count.add();
    }

    public ARGraph getGraph1() {
        return g1;
    }

    public ARGraph getGraph2() {
        return g2;
    }

    public boolean isGoal() {
        return core_len == n1;
    }

    public boolean isDead() {
        return n1 > n2 || t1_len > t2_len;
    }

    public int coreLen() {
        return core_len;
    }

    /*--------------------------------------------------------------------------
     * boolean VF2MonoState.NextPair(pn1, pn2, prev_n1, prev_n2)
     * Puts in pn1, pn2 the next pair of nodes to be tried.
     * prev_n1 and prev_n2 must be the last nodes, or NULL_NODE (default)
     * to start from the first pair.
     * Returns false if no more pairs are available.
     -------------------------------------------------------------------------*/
    public boolean nextPair(AtomicInteger pn1, AtomicInteger pn2,
            int prev_n1, int prev_n2) {
        if (prev_n1 == NULL_NODE)
            prev_n1 = 0;
        if (prev_n2 == NULL_NODE)
            prev_n2 = 0;
        else
            prev_n2++;

        if (t1_len > core_len && t2_len > core_len) {
            while (prev_n1 < n1 &&
                    (core_1[prev_n1] != NULL_NODE || nbr_1[prev_n1] == 0)) {
                prev_n1++;
                prev_n2 = 0;
            }
        } else {
            while (prev_n1 < n1 && core_1[prev_n1] != NULL_NODE) {
                prev_n1++;
                prev_n2 = 0;
            }
        }

        if (t1_len > core_len && t2_len > core_len) {
            while (prev_n2 < n2 &&
                    (core_2[prev_n2] != NULL_NODE || nbr_2[prev_n2] == 0)) {
                prev_n2++;
            }
        } else {
            while (prev_n2 < n2 && core_2[prev_n2] != NULL_NODE) {
                prev_n2++;
            }
        }

        if (prev_n1 < n1 && prev_n2 < n2) {
            pn1.set(prev_n1);
            pn2.set(prev_n2);
            return true;
        }

        return false;
    }

    /*---------------------------------------------------------------
     * bool VF2MonoState.IsFeasiblePair(node1, node2)
     * Returns true if (node1, node2) can be added to the state
     * NOTE:
     *   The attribute compatibility check (methods compatibleNode
     *   and compatibleEdge of ARGraph) is always performed
     *   applying the method to g1, and passing the attribute of
     *   g1 as first argument, and the attribute of g2 as second
     *   argument. This may be important if the compatibility
     *   criterion is not symmetric.
     --------------------------------------------------------------*/
    public boolean isFeasiblePair(int node1, int node2) {
        assert (node1 < n1);
        assert (node2 < n2);
        assert (core_1[node1] == NULL_NODE);
        assert (core_2[node2] == NULL_NODE);

        if (!g1.compatibleNode(node1, node2))
            // 直接将 node id 作为 attr 进行比较
            return false;

        int other1, other2;
        int term1 = 0, term2 = 0, new1 = 0, new2 = 0;
        EdgeAttr attr = new EdgeAttr();

        for (int i = 0; i < g1.degree(node1); i++) {
            other1 = g1.getNbrEdge(node1, i, attr);
            if (core_1[other1] != NULL_NODE) {
                other2 = core_1[other1];
                if (!g2.hasEdge(node2, other2) ||
                        !g1.compatibleEdge(attr, g2.getEdgeAttr(node2, other2)))
                    return false;
            } else {
                if (nbr_1[other1] != 0) {
                    term1++;
                } else {
                    new1++;
                }
            }
        }

        for (int i = 0; i < g2.degree(node2); i++) {
            other2 = g2.getNbrEdge(node2, i);
            if (core_2[other2] != NULL_NODE) {
                /* Do nothing */
            } else {
                if (nbr_2[other2] != 0) {
                    term2++;
                } else {
                    new2++;
                }
            }
        }

        return term1 <= term2 && (term1 + new1) <= (term2 + new2);
    }

    /*--------------------------------------------------------------
     * void VF2MonoState.AddPair(node1, node2)
     * Adds a pair to the Core set of the state.
     * Precondition: the pair must be feasible
     -------------------------------------------------------------*/
    public void addPair(int node1, int node2) {
        assert (node1 < n1);
        assert (node2 < n2);
        assert (core_len < n1);
        assert (core_len < n2);

        core_len++;
        added_node1 = node1;

        if (nbr_1[node1] == 0) {
            nbr_1[node1] = core_len;
            t1_len++;
        }

        if (nbr_2[node2] == 0) {
            nbr_2[node2] = core_len;
            t2_len++;
        }

        core_1[node1] = node2;
        core_2[node2] = node1;

        for (int i = 0; i < g1.degree(node1); i++) {
            int other = g1.getNbrEdge(node1, i);
            if (nbr_1[other] == 0) {
                nbr_1[other] = core_len;
                t1_len++;
            }
        }

        for (int i = 0; i < g2.degree(node2); i++) {
            int other = g2.getNbrEdge(node2, i);
            if (nbr_2[other] == 0) {
                nbr_2[other] = core_len;
                t2_len++;
            }
        }
    }

    /*--------------------------------------------------------------
     * void VF2MonoState.GetCoreSet(c1, c2)
     * Reads the core set of the state into the arrays c1 and c2.
     * The i-th pair of the mapping is (c1[i], c2[i])
     --------------------------------------------------------------*/
    public void getCoreSet(int c1[], int c2[]) {
        int i, j;
        for (i = 0, j = 0; i < n1; i++)
            if (core_1[i] != NULL_NODE) {
                c1[j] = i;
                c2[j] = core_1[i];
                j++;
            }
    }

    /*----------------------------------------------------------------
     * Clones a VF2MonoState, allocating with new the clone.
     --------------------------------------------------------------*/
    public VF2MonoState copy() {
        return new VF2MonoState(this);
    }

    /*----------------------------------------------------------------
     * Undoes the changes to the shared vectors made by the
     * current state. Assumes that at most one AddPair has been
     * performed.
     ----------------------------------------------------------------*/
    public void backTrack() {
        assert (core_len - orig_core_len <= 1);
        assert (added_node1 != NULL_NODE);

        if (orig_core_len < core_len) {
            if (nbr_1[added_node1] == core_len)
                nbr_1[added_node1] = 0;
            for (int i = 0; i < g1.degree(added_node1); i++) {
                int other = g1.getNbrEdge(added_node1, i);
                if (nbr_1[other] == core_len) {
                    nbr_1[other] = 0;
                }
            }

            int node2 = core_1[added_node1];

            if (nbr_2[node2] == core_len)
                nbr_2[node2] = 0;
            for (int i = 0; i < g2.degree(node2); i++) {
                int other = g2.getNbrEdge(node2, i);
                if (nbr_2[other] == core_len) {
                    nbr_2[other] = 0;
                }
            }

            core_1[added_node1] = NULL_NODE;
            core_2[node2] = NULL_NODE;

            core_len = orig_core_len;
            added_node1 = NULL_NODE;
        }
    }
}