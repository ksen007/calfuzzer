package javato.activetesting.vc;

import java.util.HashMap;
import java.util.Map;

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
public class VectorClockTracker {
    private Map<Integer, VectorClock> threads = new HashMap<Integer, VectorClock>();
    private Map<Integer, VectorClock> notifyMessages = new HashMap<Integer, VectorClock>();

    public void startBefore(Integer parent, Integer child) {
        VectorClock vc = getVectorClock(parent);
        VectorClock vc2 = new VectorClock(vc);
        vc.inc(parent);
        threads.put(child, vc2);
        vc2.inc(child);
    }

    public void joinAfter(Integer parent, Integer child) {
        VectorClock vc = getVectorClock(parent);
        VectorClock vc2 = getVectorClock(child);
        vc.updateMax(vc2);
        vc.inc(parent);
    }

    public void notifyBefore(Integer thread, Integer lock) {
        VectorClock vc = getVectorClock(thread);
        notifyMessages.put(lock, new VectorClock(vc));
        vc.inc(thread);
    }

    public void waitAfter(Integer thread, Integer lock) {
        VectorClock vc = getVectorClock(thread);
        VectorClock vc2 = notifyMessages.get(lock);
        vc.updateMax(vc2);
        vc.inc(thread);
    }

    // make sure you make copy of this VC if you want to use in a Map
    // the returned VC changes during an execution
    public VectorClock getVectorClock(Integer thread) {
        VectorClock p = threads.get(thread);
        if (p == null) {
            p = new VectorClock();
            threads.put(thread, p);
        }
        return p;
    }

}
