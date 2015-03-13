package javato.activetesting.igoodlock;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;

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
public class InterEdges {
    // lock -> thread -> lockNode list
    private TreeMap<Integer, TreeMap<Integer, LinkedList<LockNode>>> map;

    public InterEdges() {
        map = new TreeMap<Integer, TreeMap<Integer, LinkedList<LockNode>>>();
    }

    public void addLockNode(LockNode lockNode) {
        int threadId = lockNode.getThreadId();
        int lockId = lockNode.getLockId();
        TreeMap<Integer, LinkedList<LockNode>> threadToLockNodes = map.get(lockId);
        if (threadToLockNodes == null) {
            threadToLockNodes = new TreeMap<Integer, LinkedList<LockNode>>();
            map.put(lockId, threadToLockNodes);
        }
        LinkedList<LockNode> lockNodes = threadToLockNodes.get(threadId);
        if (lockNodes == null) {
            lockNodes = new LinkedList<LockNode>();
            threadToLockNodes.put(threadId, lockNodes);
        }
        lockNodes.add(lockNode);
    }

    public Iterator getIterator(int lockId, Set<Integer> ignoredThreadIds, int minThreadId) {
        return new InterEdgeIterator(lockId, ignoredThreadIds, minThreadId);
    }

    private class InterEdgeIterator implements Iterator {
        private Iterator<Integer> threadIterator;
        private Iterator<LockNode> lockNodeIterator;
        private Set<Integer> ignoredThreadIds;
        private LockNode toReturn;
        private TreeMap<Integer, LinkedList<LockNode>> threadsToLockNodes;
        private int minThreadId;

        public InterEdgeIterator(int lockId, Set<Integer> ignoredThreadIds, int minThreadId) {
            threadsToLockNodes = map.get(lockId);
            threadIterator = threadsToLockNodes.keySet().iterator();
            this.ignoredThreadIds = ignoredThreadIds;
            this.minThreadId = minThreadId;
        }

        public boolean hasNext() {
            while (true) {
                while (lockNodeIterator == null && threadIterator.hasNext()) {
                    Integer threadId = threadIterator.next();
                    if (threadId > minThreadId && !ignoredThreadIds.contains(threadId)) {
                        lockNodeIterator = threadsToLockNodes.get(threadId).iterator();
                    }
                }
                if (lockNodeIterator == null)
                    return false;
                if (lockNodeIterator.hasNext()) {
                    toReturn = lockNodeIterator.next();
                    return true;
                } else {
                    lockNodeIterator = null;
                }
            }
        }

        public LockNode next() {
            return toReturn;
        }

        public void remove() {
        }
    }

    public void printInterEdges() {
        System.out.println("Printing InterEdges:");
        for (Integer lockId : map.keySet()) {
            TreeMap<Integer, LinkedList<LockNode>> threadToLockNodes = map.get(lockId);
            System.out.println("Lock " + lockId);
            for (Integer threadId : threadToLockNodes.keySet()) {
                System.out.println("Thread " + threadId);
                LinkedList<LockNode> lockNodes = threadToLockNodes.get(threadId);
                for (LockNode ln : lockNodes) {
                    ln.printNode();
                }

            }
        }
    }
}
