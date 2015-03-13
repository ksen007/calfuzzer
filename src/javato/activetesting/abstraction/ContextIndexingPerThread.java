package javato.activetesting.abstraction;

import java.util.*;

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
public class ContextIndexingPerThread {
    private LinkedList<FrameForIndexing> stack;
    private Map<Integer, List<Integer>> objToContextMap;


    public ContextIndexingPerThread() {
        stack = new LinkedList<FrameForIndexing>();
        FrameForIndexing frame = new FrameForIndexing(-1);
        stack.addFirst(frame);
        objToContextMap = new TreeMap<Integer, List<Integer>>();
    }

    public void methodEnterBefore(Integer iid) {
	blockEnterBefore(iid);
    }

    public void methodExitAfter(Integer iid) {
        Integer entryIid = stack.removeFirst().getIid();
        while (!iid.equals(entryIid + 1)) { // this is a hack; needs better handling in future
            entryIid = stack.removeFirst().getIid();
        }
    }

    public void blockEnterBefore(Integer iid) {
        FrameForIndexing frame = new FrameForIndexing(iid);
        stack.getFirst().incFreq(iid);
        stack.addFirst(frame);
    }

    public void blockExitAfter(Integer iid) {
        stack.removeFirst();
    }

    public void newExprAfter(Integer iid, Integer o, int k) {
        logIid(iid);
        objToContextMap.put(o, getContext(iid, k));
    }

    public List<Integer> getContextForObjectCreation(Integer o) {
        List<Integer> ret = objToContextMap.get(o);
        if (ret == null) {
            return new LinkedList<Integer>();
        } else {
            return ret;
        }
    }

    public void logIid(Integer iid) {
        stack.getFirst().incFreq(iid);
    }

    public List<Integer> getContext(Integer iid, int k) {
        int i = 0;
        int tmp = iid;
        LinkedList<Integer> ret = new LinkedList<Integer>();
        for (Iterator<FrameForIndexing> iterator = stack.iterator(); i < k && iterator.hasNext();) {
            FrameForIndexing frameForIndexing = iterator.next();
            i++;
            int count = frameForIndexing.getFreq(tmp);
            ret.addLast(tmp);
            ret.addLast(count);
            tmp = frameForIndexing.getIid();
        }
        return ret;
    }
}
