/**
 * Copyright (c) 2009,
 * Jacob Burnim <jburnim@cs.berkeley.edu>
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

package javato.activetesting.determinism;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DeterministicBlockTracker {
    // Structures for tracking which threads are in which deterministic blocks.
    //  - curBlock[t]:        The block currently open in this thread t.
    //  - parentThread[t]:    The parent thread of thread t.
    //  - parentBlock[b]:     The block in which block b was opened.
    //  - originThread[b]:    The thread in which block b was opened.
    //  - blockInvariants[b]: List of serialized invariants asserted in block b.
    private int nBlocks;
    private final Map<Integer,Integer> curBlock;
    private final Map<Integer,Integer> parentThread;
    private final Map<Integer,Integer> parentBlock;
    private final Map<Integer,Integer> originThread;
    private final Map<Integer,List<byte[]>> blockInvariants;

    public DeterministicBlockTracker() {
        nBlocks = 0;
        curBlock = new HashMap<Integer,Integer>();
        parentThread = new HashMap<Integer,Integer>();
        parentBlock = new HashMap<Integer,Integer>();
        originThread = new HashMap<Integer,Integer>();
        blockInvariants = new HashMap<Integer,List<byte[]>>();
    }

    public boolean inBlock(Integer thread) {
        // Could make this more efficient.
        Integer block = curBlock.get(thread);
        while (block != null) {
            if (thread.equals(block)) {
                return true;
            }
            block = parentBlock.get(curBlock);
        }
        return false;
    }

    public void open(Integer thread) {
        Integer cb = curBlock.get(thread);
        curBlock.put(thread, nBlocks);
        if (cb != null) {
            parentBlock.put(cb, nBlocks);
        }
        originThread.put(nBlocks, thread);
        blockInvariants.put(nBlocks, new ArrayList<byte[]>());
        nBlocks++;
    }

    public void close(Integer thread) {
        // Assumption: This is the thread that opened the block.
        // (Although we don't check that spawned threads have been joined.)
        int cb = curBlock.get(thread);
        if (!thread.equals(originThread.get(cb))) {
            System.err.println("Deterministic block opened in thread "
                               + originThread.get(cb) + " but closed "
                               + "in thread " + thread + ".");
            System.exit(1);
        }

        Integer pb = parentBlock.get(cb);
        if (pb != null) {
            curBlock.put(thread, pb);
        } else {
            curBlock.remove(thread);
        }
    }

    public void assertDeterministic(Integer thread, Object invariant) {
        // Assumption: We're in a deterministic block.
        // (Could ignore this assert if not in a block, rather than crash.)
        int cb = curBlock.get(thread);

        // Serialize the invariant object.
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(invariant);
            blockInvariants.get(cb).add(baos.toByteArray());
        } catch (IOException e) {
            System.err.println("Error while serializing object: " + e);
            System.exit(1);
        }
    }

    public void threadSpawn(Integer parent, Integer child) {
        parentThread.put(child, parent);
        Integer block = curBlock.get(parent);
        if (block != null) {
            curBlock.put(child, block);
        }
    }

}