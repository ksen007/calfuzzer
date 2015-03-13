package javato.activetesting.threadrepro;

import javato.activetesting.common.Parameters;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

/**
 * Copyright (c) 2006-2009,
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
public class CountedGlobalSequencer {
    private Sequencer seq = new SequencerImpl();
    private HashMap<Integer,Integer> counters = new HashMap<Integer, Integer>();

    public void waitUntilWithIgnores(int tick, int nIgnores, int timeoutInMS, boolean isTick) {
        Semaphore sem;
        synchronized (seq) {
            Integer val = counters.get(tick);
            if (val==null) {
                val = 0;
            }
            if (val==nIgnores) {
                counters.put(tick,val+1);
            } else {
                counters.put(tick,val+1);
                return;
            }
            if (isTick) seq.tick();
            sem = new Semaphore(0);
            seq.put(tick,sem);
        }

        try {
            if (timeoutInMS==0) {
                sem.acquire();
            } else if (!sem.tryAcquire(timeoutInMS, TimeUnit.MILLISECONDS)) {
                System.err.println(Thread.currentThread()+" Timeout!!!");
            }
        } catch (InterruptedException e) {
            System.err.println(Thread.currentThread()+" Interrupt caught by waitUntil!!!");
            e.printStackTrace();
        }
    }

    public void waitUntilWithIgnores(int tick, int nIgnores,boolean isTick) {
        waitUntilWithIgnores(tick, nIgnores, Parameters.raceBreakpointWaittime,isTick);
    }

    public void tick() {
        synchronized (seq) {
            seq.tick();            
        }
    }
}
