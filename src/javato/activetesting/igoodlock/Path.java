package javato.activetesting.igoodlock;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

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
public class Path {
    private LinkedList<LockNode> path;
    private TreeSet<Integer> threadsInvolved;
    private TreeSet<Integer> guardLocksUnion;
    private int minThreadId;

    public Path() {
        path = new LinkedList<LockNode>();
        threadsInvolved = new TreeSet<Integer>();
        guardLocksUnion = new TreeSet<Integer>();
    }

    public Path(Path path) {
        this.path = new LinkedList<LockNode>(path.path);
        threadsInvolved = new TreeSet<Integer>(path.threadsInvolved);
        guardLocksUnion = new TreeSet<Integer>(path.guardLocksUnion);
        this.minThreadId = path.minThreadId;
    }

    public void addInterEdges(LinkedList<Path> bucket, InterEdges edges) {
        LockNode endNode = path.getLast();
        Iterator interEdgeIterator = edges.getIterator(endNode.getLockId(), threadsInvolved, minThreadId);
        while (interEdgeIterator.hasNext()) {
            LockNode ln = (LockNode) interEdgeIterator.next();
            boolean notInGuards = true;
            LockNode tmp = ln.getParent();
            TreeSet<Integer> tmpGuards = new TreeSet<Integer>();
            while (tmp.getLockId() != -1 && notInGuards) {
                if (guardLocksUnion.contains(tmp.getLockId())) {
                    notInGuards = false;
                } else {
                    tmpGuards.add(tmp.getLockId());
                }
                tmp = tmp.getParent();
            }
            if (notInGuards) {
                Path tmp2 = new Path(this);
                tmp2.addNode(ln);
                tmp2.guardLocksUnion.addAll(tmpGuards);
                bucket.add(tmp2);
            }
        }
    }

    public void addIntraEdges(LinkedList<Path> bucket, LinkedList<Path> deadlocks) {
        LockNode endNode = path.getLast();
        LinkedList<LockNode> children = endNode.getChildren();
        if (children != null) {
            LockNode first = path.getFirst();
            for (LockNode child : children) {
                if (first.getLockId() == child.getLockId()) {
                    Path tmp = new Path(this);
                    tmp.addNode(child);
                    deadlocks.add(tmp);
                } else if (!guardLocksUnion.contains(child.getLockId())) {
                    Path tmp = new Path(this);
                    tmp.addNode(child);
                    tmp.guardLocksUnion.add(child.getLockId());
                    bucket.add(tmp);
                    tmp.addIntraEdges(bucket, deadlocks);
                }
            }
        }
    }

    private void addNode(LockNode ln) {
        path.addLast(ln);
        threadsInvolved.add(ln.getThreadId());
    }

    public void addFirstNode(LockNode ln) {
        path.addLast(ln);
        threadsInvolved.add(ln.getThreadId());
        minThreadId = ln.getThreadId();
        LockNode tmp = ln;
        while (tmp.getLockId() != -1) {
            guardLocksUnion.add(tmp.getLockId());
            tmp = tmp.getParent();
        }

    }

    public void printPath(ArrayList<String> iidToLineMap) {
        System.out.println("Printing Path:");
        for (LockNode cur : path) {
            cur.printNode();
        }
    }

    public void printDeadlock() {
        LockNode old = null;
        System.out.println("##################################### Printing deadlock:");
        for (LockNode cur : path) {
            if (old != null && old.getThreadId() != cur.getThreadId()) {
                System.out.println("******** Thread " + old.getThreadId());
                old.printContext();
            }
            old = cur;
        }
        if (old != null) {
            System.out.println("******** Thread " + old.getThreadId());
            old.printContext();
        }
    }

    public void addCycleToDeadlockCycleInfo(DeadlockCycleInfo ret) {
        LockNode old = null;
        for (LockNode cur : path) {
            if (old != null && old.getThreadId() != cur.getThreadId()) {
                ret.addALockNode(old); // parkcs: addANode renamed to addALockNode
            }
            old = cur;
        }
        if (old != null) {
            ret.addALockNode(old); // parkcs: addANode renamed to addALockNode
        }
    }
}
