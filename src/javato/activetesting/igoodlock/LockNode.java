package javato.activetesting.igoodlock;

import javato.activetesting.analysis.Observer;

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
public class LockNode {
    private int lockId;
    private int iid;

    private int threadId;
    private LockNode parent;
    private LinkedList<LockNode> children;

    public LockNode(int iid, int threadId, int lockId) {
        this.iid = iid;
        this.threadId = threadId;
        this.lockId = lockId;
        parent = null;
        children = null;
    }

    public LockNode(int threadId) {
        this.iid = -1;
        this.threadId = threadId;
        this.lockId = -1;
        parent = null;
        children = null;
    }

    public LockNode getChild(int lockId, int iid) {
        if (children != null) {
            for (LockNode tmp : children) {
                if (tmp.iid == iid && tmp.lockId == lockId) {
                    return tmp;
                }
            }
        }
        return null;
    }

    public LockNode addChild(int lockId, int iid) {
        if (children == null) {
            children = new LinkedList<LockNode>();
        }
        LockNode tmp = new LockNode(iid, threadId, lockId);
        children.add(tmp);
        tmp.parent = this;
        return tmp;
    }

    public LinkedList<LockNode> getChildren() {
        return children;
    }

    public LockNode getParent() {
        return parent;
    }

    public int getLockId() {
        return lockId;
    }

    public int getRootLockId() {
        LockNode old = this, tmp = this;
        while (tmp.getLockId() != -1) {
            old = tmp;
            tmp = tmp.getParent();
        }
        return old.getLockId();
    }

    public int getIid() {
        return iid;
    }

    public int getThreadId() {
        return threadId;
    }

    public void printContext() {
        LockNode tmp = this;
        while (tmp.getLockId() != -1) {
            tmp.printNode();
            tmp = tmp.getParent();
        }
    }

    public void printNode() {
        System.out.println("Lock " + lockId + " held by Thread " + threadId
                + " at (" + iid + ") " + Observer.getIidToLine(iid));
    }

    public LinkedList<Integer> getContext() {
        LinkedList<Integer> ret = new LinkedList<Integer>();
        LockNode tmp = this;
        while (tmp.getLockId() != -1) {
            ret.addFirst(tmp.getIid());
            tmp = tmp.getParent();
        }
        return ret;
    }
}
