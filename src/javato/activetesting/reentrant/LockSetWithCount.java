package javato.activetesting.reentrant;


import javato.activetesting.common.IntCounter;

import java.util.Map;
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
public class LockSetWithCount {
    private Map<Integer, IntCounter> lockCounts;


    public LockSetWithCount() {
        lockCounts = new TreeMap<Integer, IntCounter>();
    }

    /**
     * adds 1 to the lock count of l
     * returns true if the lock is actually acquired
     *
     * @param l
     * @return
     */
    public boolean add(Integer l) {
        IntCounter i = lockCounts.get(l);
        boolean ret;
        if (i == null) {
            i = new IntCounter(0);
            lockCounts.put(l, i);
            ret = true;
        } else {
            ret = false;
        }
        i.inc();
        return ret;
    }

    /**
     * subtracts 1 from the lock count of l
     * throws RuntimeException if the lock is not currently held
     * returns if the lock is actually released
     *
     * @param l
     * @return
     */

    public boolean remove(Integer l) {
        IntCounter i = lockCounts.get(l);
        boolean ret;
        if (i == null) {
            throw new RuntimeException("Trying to release unacquired lock " + l);
        }
        i.dec();
        if (i.val == 0) {
            lockCounts.remove(l);
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }

    /**
     * returns the set of locks currently held by the thread
     *
     * @return
     */
    public Set<Integer> getLockSet() {
        return lockCounts.keySet();
    }

}
