package javato.activetesting.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

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
public class IIDAccessCounter {
    private ConcurrentHashMap<Integer, AtomicInteger> iidToCount;

    //private int[] counts;

    public IIDAccessCounter() {
        iidToCount = new ConcurrentHashMap<Integer, AtomicInteger>(7919);
        //counts = new int[7919];
    }

//    public boolean needToIgnore(int iid) {
//        int bucket = iid % 7919;
//        if (counts[bucket] > Parameters.maxPausesInActiveTesting) return true;
//        synchronized (this) {
//            counts[bucket]++;
//        }
//        return false;
//    }
    public boolean needToIgnore(Integer iid) {
//        return false;
        AtomicInteger tmp = iidToCount.get(iid);
        if (tmp==null) {
            tmp = new AtomicInteger(0);
            iidToCount.putIfAbsent(iid,tmp);
            return false;
        }
        if (tmp.get()>Parameters.maxPausesInActiveTesting) {
            return true;
        } else {
            tmp.incrementAndGet();
            return false;
        }
    }
}
