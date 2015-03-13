package javato.activetesting.abstraction;

import java.util.List;
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
public class ContextIndexingTracker {
    public ThreadLocal contextStack = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new ContextIndexingPerThread();
        }
    };

    public void methodEnterBefore(Integer iid) {
        ((ContextIndexingPerThread)contextStack.get()).methodEnterBefore(iid);
    }

    public void methodExitAfter(Integer iid) {
        ((ContextIndexingPerThread)contextStack.get()).methodExitAfter(iid);
    }

    public void blockEnterBefore(Integer iid) {
        ((ContextIndexingPerThread)contextStack.get()).blockEnterBefore(iid);
    }

    public void blockExitAfter(Integer iid) {
        ((ContextIndexingPerThread)contextStack.get()).blockExitAfter(iid);
    }

    public void newExprAfter(Integer iid, Integer o, int k) {
        ((ContextIndexingPerThread)contextStack.get()).newExprAfter(iid,o,k);
    }

    public void logIid(Integer iid) {
	((ContextIndexingPerThread)contextStack.get()).logIid(iid);
    }

    public List<Integer> getContextForObjectCreation(Integer o) {
        return ((ContextIndexingPerThread)contextStack.get()).getContextForObjectCreation(o);
    }

    public List<Integer> getContext(Integer iid, int k) {
        return ((ContextIndexingPerThread)contextStack.get()).getContext(iid,k);

    }

    public List<Integer> getContext(Integer iid) {
        return ((ContextIndexingPerThread)contextStack.get()).getContext(iid,Integer.MAX_VALUE);

    }

}
