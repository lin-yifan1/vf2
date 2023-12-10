package com.lyf.regionless.vf2_mono;

import java.util.concurrent.atomic.AtomicInteger;

public interface State {
    ARGraph getGraph1();

    ARGraph getGraph2();

    boolean isGoal();

    boolean isDead();

    int coreLen();

    boolean nextPair(AtomicInteger pn1, AtomicInteger pn2, int prev_n1, int prev_n2);

    boolean isFeasiblePair(int node1, int node2);

    void addPair(int node1, int node2);

    void getCoreSet(int c1[], int c2[]);

    State copy();

    void backTrack();
}
