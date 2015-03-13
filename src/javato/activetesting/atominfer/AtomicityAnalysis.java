package javato.activetesting.atominfer;

import java.io.*;
import java.util.*;
import javato.activetesting.abstraction.ContextIndexingTracker;
import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.AnalysisImpl;
import javato.activetesting.lockset.LockSet;
import javato.activetesting.lockset.LockSetTracker;
import javato.activetesting.reentrant.IgnoreRentrantLock;
import javato.activetesting.vc.VectorClockTracker;

/**
 * Finds the race relation that we will use to infer atomicity.
 * This is simply the Hybrid Analysis where we count lock
 * acquires and releases as races and track locations by their
 * context indexing, not just their iid.
 * @todo Should the context for synchronized enter/exit blocks
 * include their own iid or not?
 */
public class AtomicityAnalysis extends AnalysisImpl {

    private VectorClockTracker vcTracker;
    private LockSetTracker lsTracker;
    private IgnoreRentrantLock ignoreRentrantLock;
    private AtomicRaceTracker eb;
    private ContextIndexingTracker ciTracker;
    // thread iid -> last event in that thread, which we use to build the sequentialRelation
    private Map<Integer, UniqueEvent> threadMap;
    // Only maps events to the ones immediately following them to save memory (the closure can be computed easily).
    private Map<UniqueEvent, UniqueEvent> immediateSequentialRelation;
    private Set<Integer> allBlocks, vcNonAtomicBlocks;
    private int eventIndex;

    public void initialize() {
	synchronized (ActiveChecker.lock) {
            vcTracker = new VectorClockTracker();
            lsTracker = new LockSetTracker();
            ignoreRentrantLock = new IgnoreRentrantLock();
	    eb = new AtomicRaceTracker();
	    ciTracker = new ContextIndexingTracker();
	    threadMap = new HashMap<Integer, UniqueEvent>();
	    immediateSequentialRelation = new HashMap<UniqueEvent, UniqueEvent>();
	    allBlocks = new HashSet<Integer>();
	    vcNonAtomicBlocks = new HashSet<Integer>();
	    eventIndex = 0;
	}
    }

    public void lockBefore(Integer iid, Integer thread, Integer lock, Object actualLock) {
        synchronized (ActiveChecker.lock) {
	    // We count reentrant locks as blocks
	    List<Integer> context = ciTracker.getContext(iid);
	    UniqueEvent e = new UniqueEvent(thread, context, vcTracker.getVectorClock(thread), eventIndex++);
	    // Do work before adding new block to context
	    updateSequentialRelation(iid, thread, e);
	    allBlocks.add(iid);
	    // Check for lock race before adding to lockset
	    eb.checkRace(iid, thread, lock, true, vcTracker.getVectorClock(thread), lsTracker.getLockSet(thread), e);
	    eb.addEvent(iid, thread, lock, true, vcTracker.getVectorClock(thread), lsTracker.getLockSet(thread), e);
            if (ignoreRentrantLock.lockBefore(thread, lock)) {
		// Add to lockset
		boolean isDeadlock = lsTracker.lockBefore(iid, thread, lock);
            }
	    ciTracker.blockEnterBefore(iid);
        }
    }

    public void unlockAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
	    ciTracker.blockExitAfter(iid);
            if (ignoreRentrantLock.unlockAfter(thread, lock)) {
		// Remove from lockset before checking for lock race
		lsTracker.unlockAfter(thread);
	    }
	    // We count reentrant locks as blocks
	    List<Integer> context = ciTracker.getContext(iid);
	    // Do work after popping block from context
	    UniqueEvent e = new UniqueEvent(thread, context, vcTracker.getVectorClock(thread), eventIndex++);
	    updateSequentialRelation(iid, thread, e);
        }
    }
    
    public void methodEnterBefore(Integer iid, Integer thread) {
	synchronized (ActiveChecker.lock) {
	    allBlocks.add(iid);
	    ciTracker.methodEnterBefore(iid);
	}
    }

    public void methodExitAfter(Integer iid, Integer thread) {
	synchronized (ActiveChecker.lock) {
	    ciTracker.methodExitAfter(iid);
	}
    }

    public void startBefore(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
	    vcTracker.startBefore(parent, child);
	    markBlocksAsNonAtomic(iid, parent);
        }
    }

    public void waitAfter(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
	    vcTracker.waitAfter(thread, lock);
	    markBlocksAsNonAtomic(iid, thread);
        }
    }

    public void notifyBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
	    vcTracker.notifyBefore(thread, lock);
	    markBlocksAsNonAtomic(iid, thread);
        }
    }

    public void notifyAllBefore(Integer iid, Integer thread, Integer lock) {
        synchronized (ActiveChecker.lock) {
	    vcTracker.notifyBefore(thread, lock);
	    markBlocksAsNonAtomic(iid, thread);
        }
    }

    public void joinAfter(Integer iid, Integer parent, Integer child) {
        synchronized (ActiveChecker.lock) {
	    vcTracker.joinAfter(parent, child);
	    markBlocksAsNonAtomic(iid, parent);
        }
    }

    public void readBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        synchronized (ActiveChecker.lock) {
	    ciTracker.logIid(iid);
            LockSet ls = lsTracker.getLockSet(thread);
	    UniqueEvent e = new UniqueEvent(thread, ciTracker.getContext(iid), vcTracker.getVectorClock(thread), eventIndex++);
            eb.checkRace(iid, thread, memory, true, vcTracker.getVectorClock(thread), ls, e);
            eb.addEvent(iid, thread, memory, true, vcTracker.getVectorClock(thread), ls, e);
	    updateSequentialRelation(iid, thread, e);
        }
    }

    public void writeBefore(Integer iid, Integer thread, Long memory, boolean isVolatile) {
        synchronized (ActiveChecker.lock) {
	    ciTracker.logIid(iid);
            LockSet ls = lsTracker.getLockSet(thread);
	    UniqueEvent e = new UniqueEvent(thread, ciTracker.getContext(iid), vcTracker.getVectorClock(thread), eventIndex++);
            eb.checkRace(iid, thread, memory, false, vcTracker.getVectorClock(thread), ls, e);
            eb.addEvent(iid, thread, memory, false, vcTracker.getVectorClock(thread), ls, e);
	    updateSequentialRelation(iid, thread, e);
        }
    }

    /**
     * When we finish, write out races and iid -> context indexing map.
     */
    public void finish() {
        synchronized (ActiveChecker.lock) {
	    Map<UniqueEvent, Set<UniqueEvent>> raceRelation = buildRaceRelation();
	    Map<UniqueEvent, UniqueEvent> prunedImmediateSequentialRelation = simplifySequentialRelation(raceRelation.keySet());
	    dumpInfo(raceRelation, prunedImmediateSequentialRelation);
        }
    }
    
    /* Helper methods. */
    
    private void updateSequentialRelation(Integer iid, Integer thread, UniqueEvent e) {
	if (threadMap.containsKey(thread))
	    immediateSequentialRelation.put(threadMap.get(thread), e);
	threadMap.put(thread, e);
    }

    /**
     * Mark all blocks that we are tracking in the current
     * context as non-atomic.
     */
    private void markBlocksAsNonAtomic(Integer iid, Integer thread) {
	ListIterator<Integer> it = ciTracker.getContext(iid).listIterator();
	// Loop over all the block iids and ignore the counts.
	while (it.hasNext()) {
	    Integer curBlock = it.next();
	    if (allBlocks.contains(curBlock))
		vcNonAtomicBlocks.add(curBlock);
	    it.next();  // skip count
	}
    }

    /*
     * Helper methods to build the race relation.
     * We have to transitively close it but not count
     * races between events on the same thread.
     */

    private Map<UniqueEvent, Set<UniqueEvent>> buildRaceRelation() {
	Set<CommutativePair> seenRaces = eb.getRaces();
	System.out.println(seenRaces.size() + " seen races.");
	//System.out.println("Seen races: " + seenRaces);
	int initialCapacity = (int) Math.sqrt((double)seenRaces.size());
	Map<UniqueEvent, Set<UniqueEvent>> raceRelation = new HashMap<UniqueEvent, Set<UniqueEvent>>(initialCapacity);
	for (CommutativePair p: seenRaces) {
	    UniqueEvent first = p.getFirst(), second = p.getSecond();
	    addRace(raceRelation, first, second, initialCapacity);
	    addRace(raceRelation, second, first, initialCapacity);
	}
	transitivelyCloseRaceRelation(raceRelation);
	return raceRelation;
    }

    private void addRace(Map<UniqueEvent, Set<UniqueEvent>> raceRelation, UniqueEvent from, UniqueEvent to, int initialCapacity) {
	if (!raceRelation.containsKey(from))
	    raceRelation.put(from, new HashSet<UniqueEvent>(initialCapacity));
	raceRelation.get(from).add(to);
    }

    // @todo: This takes ~7s on sor.  Optimize?  Or do I not care since we only do it once?
    private void transitivelyCloseRaceRelation(Map<UniqueEvent, Set<UniqueEvent>> raceRelation) {
	// First, compute the complete transitive closure.
	for (UniqueEvent from: raceRelation.keySet()) {
	    Set<UniqueEvent> tos = raceRelation.get(from);
	    for (UniqueEvent to: tos) {
		raceRelation.get(to).addAll(tos);
		raceRelation.get(to).remove(to);
	    }
	}
	// Next, remove pairs of events that have the same thread.
	for (UniqueEvent from: raceRelation.keySet()) {
	    Iterator<UniqueEvent> it = raceRelation.get(from).iterator();
	    while (it.hasNext()) {
		if (from.getThread().equals(it.next().getThread()))
		    it.remove();
	    }
	}
    }

    /**
     * Helper method to simplify the sequential relation.
     * We keep only relations between events that are in some race,
     * and we still only map an event to its immediate successor.
     */
    private Map<UniqueEvent, UniqueEvent> simplifySequentialRelation(Set<UniqueEvent> racingEvents) {
	Map<UniqueEvent, UniqueEvent> prunedImmediateSequentialRelation = new HashMap<UniqueEvent, UniqueEvent>();
	for (UniqueEvent racingEvent: racingEvents) {
	    // Find the next immediate sequential successor of this event (if any)
	    UniqueEvent cur = racingEvent;
	    while (true) {
		if (!immediateSequentialRelation.containsKey(cur)) // No more successors, so this racing event has no racing successor
		    break;
		UniqueEvent next = immediateSequentialRelation.get(cur);
		if (racingEvents.contains(next)) {  // We've found a racing successor
		    prunedImmediateSequentialRelation.put(racingEvent, next);
		    break;
		}
		cur = next;
	    }
	}
	return prunedImmediateSequentialRelation;
    }

    /**
     * Save our state.
     */
    private void dumpInfo(Map<UniqueEvent, Set<UniqueEvent>> raceRelation, Map<UniqueEvent, UniqueEvent> prunedImmediateSequentialRelation) {
        try {
	    final String ATOMICITY_DATA_FILE = System.getProperty("javato.activetesting.atominfer.atomicity_data_file", "atomicity_data.out");
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(ATOMICITY_DATA_FILE)));
            out.writeObject(raceRelation);
            out.writeObject(prunedImmediateSequentialRelation);
	    out.writeObject(allBlocks);
	    out.writeObject(vcNonAtomicBlocks);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}