package javato.activetesting.igoodlock;


import javato.activetesting.common.Parameters;

import java.util.LinkedList;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class GoodlockDS {
    private LockGraph lockGraph;
    private InterEdges interEdges;
    private LinkedList<Path> initialSingleNodePaths;
    private LinkedList<Path> deadlocks;

    public GoodlockDS() {
        this.lockGraph = new LockGraph();
        this.interEdges = new InterEdges();
        initialSingleNodePaths = new LinkedList<Path>();
    }


    public void lock(int iid, int threadId, int lockId) {
        Pair<Boolean, LockNode> pair = lockGraph.lock(iid, threadId, lockId);
        if (pair.fst) {
            interEdges.addLockNode(pair.snd);
            Path tmp = new Path();
            tmp.addFirstNode(pair.snd);
            initialSingleNodePaths.add(tmp);
        }
    }

    public void unlock(int iid, int threadId, int lockId) {
        lockGraph.unlock(iid, threadId, lockId);
    }

    private LinkedList<Path> findDeadlocks() {
        if (deadlocks == null) {
            deadlocks = new LinkedList<Path>();
            LinkedList<Path> bucket = new LinkedList<Path>();
            LinkedList<Path> nextBucket = new LinkedList<Path>();

            //        lockGraph.printGraph(iidToLineMap);
            //        interEdges.printInterEdges(iidToLineMap);

            for (Path path : initialSingleNodePaths) {
                path.addIntraEdges(bucket, deadlocks);
            }

            //        System.out.println("###################################");
            //        for (Path path : bucket) {
            //            path.printPath(iidToLineMap);
            //        }
            assert deadlocks.isEmpty();
            int i = 0;
            while ((Parameters.deadlockCycleLength == 0 && !bucket.isEmpty()) || i < Parameters.deadlockCycleLength) {
                i++;
                nextBucket.clear();
                for (Path path : bucket) {
                    path.addInterEdges(nextBucket, interEdges);
                }
                //            System.out.println("###################################");
                //            for (Path path : nextBucket) {
                //                path.printPath(iidToLineMap);
                //            }
                bucket.clear();
                for (Path path : nextBucket) {
                    path.addIntraEdges(bucket, deadlocks);
                }
                //            System.out.println("###################################");
                //            for (Path path : bucket) {
                //                path.printPath(iidToLineMap);
                //            }
            }
        }
        return deadlocks;
    }

    public void printDeadlocks() {
        if (deadlocks == null)
            findDeadlocks();
        for (Path path : deadlocks) {
            path.printDeadlock();
        }
    }

    public int dumpDeadlocks() {
        if (deadlocks == null)
            findDeadlocks();
        DeadlockCycleInfo ret = new DeadlockCycleInfo(deadlocks.size());
        for (Path path : deadlocks) {
            ret.addACycle();
            path.addCycleToDeadlockCycleInfo(ret);
        }
        return ret.write();
    }
}
