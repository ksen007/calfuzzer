package benchmarks.testcases;

import javato.cbreakpoints.RaceBreakpoint2;

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

class IncThread extends Thread {
    private int id;
    private CyclicDemo shared;

    public IncThread(int id, CyclicDemo shared) {
        this.id = id;
        this.shared = shared;
    }

    public void run() {
        //if (id==0) 
        //    (new RaceBreakpoint2(shared,true,false)).breakHere(false,1000);
        synchronized (shared) {
            shared.count++;
        }
        // (new RaceBreakpoint2(shared,true,true)).breakHere(true,100);
        shared.leader = id;
    }
}

public class CyclicDemo {
    public int count = 0, leader;

    public static void main(String[] args) throws InterruptedException {
        CyclicDemo shared = new CyclicDemo();
        IncThread threads[] = new IncThread[4];
        for(int i=0; i<4; i++) {
            threads[i] = new IncThread(i,shared);
            threads[i].start();
        }
        for(int i=1; i<4; i++) {
            threads[i].join();
        }
        // (new RaceBreakpoint2(shared,false,false)).breakHere(true,1000);
        synchronized (shared) {
            assert shared.count==4;
            // System.out.println(shared.count);
        }
    }
}
