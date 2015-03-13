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

import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.CheckerAnalysisImpl;

/**
 * IDEA (single deterministic block):
 *
 * We use two active checkers/schedulers -- one for deterministic
 * replay up until we hit the deterministic block, and one for inside
 * the deterministic block.  (These might be the same class -- e.g. a
 * random scheduler -- but two different instances with different
 * seeds.)
 *
 * Across executions, we have to record the seed for deterministic replay
 * to reach the block, and and also the results from previous runs inside
 * the deterministic block -- that is, a serialized invariant state and
 * sufficient information to guarantee we use a new seed in the future.
 *
 * When we reach a deterministic assertion, we record the post-state,
 * and compare it against a previous saved post-state if we have one.
 *
 *
 * IDEA (multiple deterministic blocks):
 *
 * We have to keep a stack of open deterministic blocks.  The dynamic
 * deterministic blocks form a tree -- the children of block A are the
 * blocks dynamically nested in A.  For each node, we have a
 * deterministic schedule to that node and need to try our "inner"
 * scheduler on that block to see if we can violate its determinism.
 *
 * Conceptually, when Determinism.open() is called, we fork two
 * different sub-searches:
 *
 *  (1) One switches to the inner scheduler and tries to break the
 *      opened block, as if it is the program's single deterministic
 *      block.
 *
 *  (2) One continues with the outer scheduler, searching for inner
 *      deterministic blocks.  (If we do this one first, we get
 *      something like a depth-first search for each top-most outer
 *      schedule.)
 *
 * [This is a little weird, because we ignore nested deterministic
 *  blocks found with the inner scheduler, but this is maybe simpler?
 *  And it allows us to use different schedulers inside and out.]
 *
 * In particular, we probably just want a dumb, random scheduler for
 * the outer scheduler, but some mixture of race/atomicity/etc.-biased
 * for the inner scheduler.
 *
 * Also, we can refer to the deterministic blocks by their position in
 * the tree, or just by the sequence number -- i.e. the fifth block
 * encountered during this random schedule.
 *
 * Parameters:
 *  - Random seed for outer schedule.
 *  - Number of deterministic block to explore (initially infinite?).
 *  - Number of trials left for current deterministic block.
 *  - Random seed for inner scheduler.
 *  - Result of last trial.
 *
 * Update (when there are trials left):
 *  - Check this result against last result.
 *  - Update seed for inner trial.
 *  - Decrease trials left.
 *
 * Update (when trials for current block are done):
 *  - Decrease number for deterministic block to explore (done if this hits 0).
 *  - Update seed for inner trial.
 *  - Set result to output of last trial.  (Safe? Or run new trial?)
 *
 * Do we have to execute deterministic blocks atomically?  It doesn't
 * seem like it.  This search strategy guarantees (in so far as the
 * outer search is deterministic) that the n-th deterministic block is
 * always the same.
 *
 * But, then it is not clear how to pick a consistent state.  Because the block
 * may make a write which is overwritten by a different thread before we run the
 * post-condition.
 */

public class DeterminismFuzzerAnalysis extends CheckerAnalysisImpl {
    private DeterministicBlockTracker dbt;

    public void initialize() {
        dbt = new DeterministicBlockTracker();
    }

    public void openDeterministicBlock(Integer thread) {
        synchronized (ActiveChecker.lock) {
            dbt.open(thread);
        }
    }

    public void closeDeterministicBlock(Integer thread) {
        synchronized (ActiveChecker.lock) {
            dbt.close(thread);
        }
    }

    /** Parameter 'invariant' must be serializable! */
    public void requireDeterministic(Integer thread, Object invariant) {
        // TODO(jburnim): What to do here?
    }

    /** Parameter 'invariant' must be serializable! */
    public void assertDeterministic(Integer thread, Object invariant) {
        synchronized (ActiveChecker.lock) {
            dbt.assertDeterministic(thread, invariant);
        }
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void newExprAfter(Integer iid, Integer object, Integer objOnWhichMethodIsInvoked) {
    }

    public void methodEnterBefore(Integer iid, Integer thread) {
    }

    public void methodExitAfter(Integer iid, Integer thread) {
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
            dbt.threadSpawn(parent, child);
        }
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
    }

    public void finish() {
    }

}