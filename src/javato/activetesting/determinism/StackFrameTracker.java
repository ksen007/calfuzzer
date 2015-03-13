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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StackFrameTracker {

    public StackFrameTracker() {
        // threadStacks = new ConcurrentHashMap<Integer,StackFrame>();

        threadStacks = new ThreadLocal<StackFrame>();
    }

    public void push() {
        StackFrame old = threadStacks.get();
        threadStacks.set(new StackFrame(old));
    }

    public void pop() {
        StackFrame old = threadStacks.get();
        threadStacks.set(old.getParent());
    }

    public void set(String name, Object o) {
        StackFrame f = threadStacks.get();
        if (f == null) {
            f = new StackFrame(null);
            threadStacks.set(f);
        }
        f.put(name, o);
    }


    public Set<Map.Entry<String,Object>> getLocals() {
        return getLocals(0);
    }

    public Set<Map.Entry<String,Object>> getLocals(int n) {
        StackFrame f = threadStacks.get();
        if (f == null) {
            return Collections.emptySet();
        }

        for (int i = 0; i < n; i++) {
            f = f.getParent();
            if (f == null) {
                return Collections.emptySet();
            }
        }

        return f.getLocals();
    }

    // TODO: Method for enumerating the local variables in the current
    // stack frame.

    // private ConcurrentMap<Integer,StackFrame> threadStacks;
    private ThreadLocal<StackFrame> threadStacks;

    private static class StackFrame {
        public StackFrame(StackFrame parent) {
            this.parent = parent;
            this.locals = new HashMap<String,Object>();
            this.count = 0;
        }

        public StackFrame getParent() {
            return parent;
        }

        public void put(String name, Object o) {
            locals.put(name, o);
        }

        public Set<Map.Entry<String,Object>> getLocals() {
            return locals.entrySet();
        }

        private final StackFrame parent;
        private final Map<String,Object> locals;
        private int count;
    }
}
