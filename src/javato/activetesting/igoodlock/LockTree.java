package javato.activetesting.igoodlock;


import java.util.ArrayList;
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
public class LockTree {
    private LockNode root;
    private LockNode current;
    private int threadId;

    public LockTree(int threadId) {
        current = root = new LockNode(threadId);
        this.threadId = threadId;
    }

    public LockNode getCurrent() {
        return current;
    }

    public boolean lock(int lockId, int iid) {
        LockNode child = current.getChild(lockId, iid);
        if (child == null) {
            current = current.addChild(lockId, iid);
            return true;
        } else {
            current = child;
            return false;
        }
    }

    public void unlock(int lockId, int iid) {
        assert current.getLockId() == lockId;
        current = current.getParent();
        assert current != null;
    }

    public void printTree(ArrayList<String> iidToLineMap, LockNode n, String s) {
        System.out.print(s);
        if (n.getLockId() != -1)
            n.printNode();
        LinkedList<LockNode> children = n.getChildren();
        if (children != null) {
            for (LockNode child : children) {
                printTree(iidToLineMap, child, s + "    ");
            }
        }
    }

    public void printTree(ArrayList<String> iidToLineMap) {
        printTree(iidToLineMap, root, "");
    }
}
