package javato.activetesting.common;

import java.io.*;

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
public class Parameters {
    // CalFuzzer Specific
    public static final long stallCheckerInterval = Long.getLong("javato.activeChecker.stallCheckerInterval", 10);
    public static final long livelockCheckerInterval = Long.getLong("javato.activeChecker.livelockCheckerInterval", 200);
    public static final int deadlockCycleLength = Integer.getInteger("javato.deadlock.cycle.length", 2);
    public static final int maxPausesInActiveTesting = Integer.getInteger("javato.max.iid.count",100);

    // hybrid race detection and thrille specific
    public static final int N_VECTOR_CLOCKS_WINDOW = 5;
    public static final boolean trackLockRaces = Boolean.getBoolean("javato.track.lock.races");
    public static final boolean LOG_IID_VISIT_COUNT = Boolean.getBoolean("javato.track.iid.visit.count");
    public static final boolean resolveOrder = Boolean.getBoolean("javato.race.resolve.order");
    public static final int errorId = Integer.getInteger("javato.activetesting.errorid", -1);
    public static final long thrilleStallCheckerInterval = 20;
    public final static int raceBreakpointWaittime = 1000;
    public final static boolean removeOlderRace = Boolean.getBoolean("javato.hybrid.removeoldrace");
    public final static boolean removeOlderAccess = Boolean.getBoolean("javato.hybrid.removeoldaccess");
    public static final boolean trackWaitNotifyOnly = Boolean.getBoolean("javato.track.waitnotifyonly");

    // instrumentation specific
    public static final boolean ignoreArrays = Boolean.getBoolean("javato.ignore.arrays");
    public static final boolean ignoreMethods = Boolean.getBoolean("javato.ignore.methods");
    public static final boolean ignoreAlloc = Boolean.getBoolean("javato.ignore.allocs");
    public static final boolean ignoreFields = Boolean.getBoolean("javato.ignore.fields");
    public static final boolean ignoreConcurrency = Boolean.getBoolean("javato.ignore.concurrency");
    public static final boolean trackLocals = Boolean.getBoolean("javato.track.locals");
    public static final boolean trackDeterministicLocals
        = Boolean.getBoolean("javato.track.locals.deterministic");

    // various files for persistent data and logs
    public static final String iidToLineMapFile = "iidToLine.map";
    public static final String usedObjectId = "javato.usedids";
    public static final String ERROR_STAT_FILE = System.getProperty("javato.activetesting.errorstat.file", "error.stat");
    public static final String ERROR_LOG_FILE = System.getProperty("javato.activetesting.errorlog.file", "error.log");
    public static final String ERROR_LIST_FILE = System.getProperty("javato.activetesting.errorlist.file", "error.list");
    public static final String ERROR_STALL_FILE = System.getProperty("javato.activetesting.errorstall.file", "error.stall");

    // entry class and must be specified
    public static final String analysisClass = System.getProperty("javato.activetesting.analysis.class");

    // deterministic scheduler specific
    public static final boolean isDeterministicSchedule = Boolean.getBoolean("javato.schedule.deterministic");
    public static final long deterministicSchedulerRandomSeed = Long.getLong("javato.schedule.seed",682190);
    public static final float deterministicSchedulerContextSwitchProbability = 0.1f;
    public static long afterStartSleepDuration = 5;
}
