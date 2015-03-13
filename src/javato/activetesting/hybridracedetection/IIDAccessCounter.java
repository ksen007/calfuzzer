package javato.activetesting.hybridracedetection;


import javato.activetesting.activechecker.ActiveChecker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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
    private ConcurrentHashMap<Integer, AtomicLong> iidToCount;

    public IIDAccessCounter() {
        iidToCount = new ConcurrentHashMap<Integer, AtomicLong>(7919);
    }

    public boolean needToIgnore(Integer iid) {
//        return false;
        AtomicLong tmp = iidToCount.get(iid);
        if (tmp==null) {
            tmp = new AtomicLong(1);
            iidToCount.putIfAbsent(iid,tmp);
            return false;
        }
        long val = tmp.addAndGet(2);
        if (val%2==1) {
            long x = val/128;
            double r = 0.0d;
            synchronized (ActiveChecker.rand) {
                r = ActiveChecker.rand.nextDouble();
            }
            if (r<(1.0d/(x+1.0d))) {
                tmp.addAndGet(1);
                return false;
            } else {
                return true;
            }
        } else {
            if (val%16==0) {
                tmp.addAndGet(1);
            }
            return false;
        }
    }
}
