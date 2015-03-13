package javato.activetesting.racefuzzer;

import javato.activetesting.activechecker.ActiveChecker;

import java.util.Collection;

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
public class RaceChecker extends ActiveChecker {
    private Long mem;
    private boolean isWrite;
    private Integer iid;
    public static boolean isRace = false;
    private boolean isLock;

    public RaceChecker(Long mem, boolean write, Integer iid, boolean isLock) {
        this.mem = mem;
        isWrite = write;
        this.iid = iid;
        this.isLock = isLock;
    }

    public void check(Collection<ActiveChecker> checkers) {
        //System.out.println("check("+mem+","+isWrite+","+iid+")");
        for (ActiveChecker other : checkers) {
            RaceChecker rc = (RaceChecker) other;
            if (rc.mem.equals(mem) && (rc.isWrite || isWrite)) {
                if (isLock) {
                    System.err.println("***************************************** Real lock race (e.g. atomicity violation) detected between "
                            + javato.activetesting.analysis.Observer.getIidToLine(iid) + " and "
                            + javato.activetesting.analysis.Observer.getIidToLine(rc.iid));
                } else {
                    System.err.println("***************************************** Real data race detected between "
                            + javato.activetesting.analysis.Observer.getIidToLine(iid) + " and "
                            + javato.activetesting.analysis.Observer.getIidToLine(rc.iid));
                }
                isRace = true;
                if (rand.nextBoolean()) {
                    block(100);
                    rc.unblock(0);
                } else {
                    block(0);
                    rc.unblock(100);
                }
                return;
            }
        }
        block(0);
    }
}
