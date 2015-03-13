package javato.activetesting.atominfer;

import java.io.*;
import java.util.*;
import javato.activetesting.activechecker.ActiveChecker;
import javato.activetesting.analysis.CheckerAnalysisImpl;
import javato.activetesting.hybridracedetection.CommutativePair;
import javato.activetesting.hybridracedetection.HybridRaceTracker;
import javato.activetesting.vc.VectorClock;

/*
 */
public class InferAtomicBlocks {

    private Map<UniqueEvent, Map<Integer, List<UniqueEvent>>> sortedRaceRelation;
    // Only maps racing events to their immediate racing successors.
    private Map<UniqueEvent, UniqueEvent> immediateSequentialRelation;
    private Set<Integer> allBlocks, vcNonAtomicBlocks, algoNonAtomicBlocks;

    public static void main(String[] args) {
	new InferAtomicBlocks();
    }

    public InferAtomicBlocks() {
	int maxNumThreads = Integer.getInteger("javato.activetesting.atominfer.max_num_threads");
	final Map<Integer, ArrayList<UniqueEvent>> sortedRacingEventsByThread = initDataStructures();
	System.out.println("Total number of threads: " + sortedRacingEventsByThread.keySet().size());
	System.out.println("Total number of blocks: " + allBlocks.size());
	for (int i = 2; i <= maxNumThreads; i++)
	    findPaths(sortedRacingEventsByThread, i);
	System.out.println("Found " + vcNonAtomicBlocks.size() + " non-atomic block" + getPluralSuffix(vcNonAtomicBlocks.size()) + " (from vc): " + lineNumberStringOfBlockSet(vcNonAtomicBlocks) + " or " + vcNonAtomicBlocks);
	System.out.println("Found " + algoNonAtomicBlocks.size() + " non-atomic block" + getPluralSuffix(algoNonAtomicBlocks.size()) + " (from algorithm): " + lineNumberStringOfBlockSet(algoNonAtomicBlocks) + " or " + algoNonAtomicBlocks);
	System.out.println("Inferring " + (allBlocks.size() - algoNonAtomicBlocks.size() - vcNonAtomicBlocks.size()) + " likely or unknown atomic blocks.");
    }

    /* Initialize data structures */

    private Map<Integer, ArrayList<UniqueEvent>> initDataStructures() {
	Map<UniqueEvent, Set<UniqueEvent>> raceRelation;
        try {
	    final String ATOMICITY_DATA_FILE = System.getProperty("javato.activetesting.atominfer.atomicity_data_file", "atomicity_data.out");
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(ATOMICITY_DATA_FILE)));
            raceRelation = (HashMap<UniqueEvent, Set<UniqueEvent>>) in.readObject();
	    immediateSequentialRelation = (HashMap<UniqueEvent, UniqueEvent>) in.readObject();
	    allBlocks = (HashSet<Integer>) in.readObject();
	    vcNonAtomicBlocks = (HashSet<Integer>) in.readObject();
            in.close();
        } catch (Exception e) {
            raceRelation = new HashMap<UniqueEvent, Set<UniqueEvent>>();
	    immediateSequentialRelation = new HashMap<UniqueEvent, UniqueEvent>();
	    allBlocks = new HashSet<Integer>();
	    vcNonAtomicBlocks = new HashSet<Integer>();
        }
	algoNonAtomicBlocks = new HashSet<Integer>();
	sortedRaceRelation = getSortedRaceRelation(raceRelation);
	return getSortedRacingEventsByThread(raceRelation);
    }

    // thread -> sorted list of racing events that occurred on that thread
    private Map<Integer, ArrayList<UniqueEvent>> getSortedRacingEventsByThread(Map<UniqueEvent, Set<UniqueEvent>> raceRelation) {
	Map<Integer, ArrayList<UniqueEvent>> racingEventsByThread = new HashMap<Integer, ArrayList<UniqueEvent>>();
	for (UniqueEvent e: raceRelation.keySet()) {
	    Integer t = e.getThread();
	    if (!racingEventsByThread.containsKey(t))
		racingEventsByThread.put(t, new ArrayList<UniqueEvent>());
	    ArrayList<UniqueEvent> curList = racingEventsByThread.get(t);
	    boolean added = false;
	    for (int i = 0; i < curList.size(); i++) {
		if (e.getEventIndex() < curList.get(i).getEventIndex()) {
		    curList.add(i, e);
		    added = true;
		    break;
		}
	    }
	    if (!added)
		curList.add(e);
	}
	return racingEventsByThread;
    }

    // event -> (thread -> sorted list of events that occurred on that thread that race with this event)
    private Map<UniqueEvent, Map<Integer, List<UniqueEvent>>> getSortedRaceRelation(Map<UniqueEvent, Set<UniqueEvent>> raceRelation) {
	Map<UniqueEvent, Map<Integer, List<UniqueEvent>>> sortedRaceRelation = new HashMap<UniqueEvent, Map<Integer, List<UniqueEvent>>>();
	for (UniqueEvent from: raceRelation.keySet()) {
	    Map<Integer, List<UniqueEvent>> curMap = new HashMap<Integer, List<UniqueEvent>>();
	    sortedRaceRelation.put(from, curMap);
	    for (UniqueEvent to: raceRelation.get(from)) {
		Integer toThread = to.getThread();
		if (!curMap.containsKey(toThread))
		    curMap.put(toThread, new LinkedList<UniqueEvent>());
		List<UniqueEvent> curList = curMap.get(toThread);
		boolean added = false;
		ListIterator<UniqueEvent> it = curList.listIterator();
		while (it.hasNext()) {
		    if (to.getEventIndex() < it.next().getEventIndex()) {
			it.previous();
			it.add(to);
			added = true;
			break;
		    }
		}
		if (!added)
		    it.add(to);
		
	    }
	}
	return sortedRaceRelation;
    }

    /* Main algorithm */

    private void findPaths(final Map<Integer, ArrayList<UniqueEvent>> sortedRacingEventsByThread, int numThreads) {
	if (Utilities.shouldPrint())
	    System.out.println("Checking for atomicity violations with " + numThreads + " threads.");
	Set<Integer> newNonAtomicBlocks = new HashSet<Integer>();
	int numPairs = 0, numPairsExamined = 0;
	for (Integer thread: sortedRacingEventsByThread.keySet()) {
	    // @todo Optimize order more?
	    ArrayList<UniqueEvent> racingEventsOnThisThread = sortedRacingEventsByThread.get(thread);
	    for (int i = 0; i < racingEventsOnThisThread.size(); i++) {
		for (int j = i + 1; j < racingEventsOnThisThread.size(); j++) {
		    UniqueEvent start = racingEventsOnThisThread.get(i), dest = racingEventsOnThisThread.get(j);
		    List<Integer> sharedPrefix = findSharedPrefix(start, dest);
		    //System.out.println("Shared prefix is " + sharedPrefix);
		    // Optimization: Only search for pairs that have a shared prefix.
		    // Optimization: Do not search pairs where we know all of the blocks in their shared prefix are non-atomic.
		    if (containsViableBlocks(sharedPrefix, newNonAtomicBlocks)) {
			if (findPath(start, dest, numThreads)) {
			    addNonAtomicBlocks(sharedPrefix, newNonAtomicBlocks);
			}
			numPairsExamined++;
			//if (numPairsExamined % 200 == 0)
			//System.out.println("Examined " + numPairsExamined + " pairs.");
		    }
		    numPairs++;
		    //if (numPairs % 200 == 0)
		    //System.out.println("Considered " + numPairs + " pairs.");
		}
	    }
	}
	if (Utilities.shouldPrint())
	    System.out.println("Found " + newNonAtomicBlocks.size() + " new non-atomic block" + getPluralSuffix(newNonAtomicBlocks.size()) + " while checking " + numThreads + " threads after examining " + numPairsExamined + " out of " + numPairs + " pairs: " + lineNumberStringOfBlockSet(newNonAtomicBlocks) + " or " + newNonAtomicBlocks);
	else
	    System.out.println("Found " + newNonAtomicBlocks.size() + " new non-atomic block" + getPluralSuffix(newNonAtomicBlocks.size()) + " while checking " + numThreads + " threads after examining " + numPairsExamined + " out of " + numPairs + " pairs.");
	algoNonAtomicBlocks.addAll(newNonAtomicBlocks);
    }

    private enum Result {
	PATH_FOUND, NO_PATH_FOUND, ILLEGAL_CYCLE, VECTOR_CLOCK_PRUNABLE;
    }

    private boolean findPath(UniqueEvent start, UniqueEvent dest, int numThreads) {
	Stack<UniqueEvent> curPath = new Stack<UniqueEvent>();
	HashMap<Integer, Integer> threadEventIndexMap = new HashMap<Integer, Integer>();
	//System.out.println("Searching for a path from " + start + " to " + dest + ".");
	Result found = findPath(start, start, dest, numThreads, curPath, threadEventIndexMap);
	if (found == Result.PATH_FOUND) {
	    simplifyPath(curPath);
	    if (Utilities.shouldPrint())
		System.out.println("Found path from " + javato.activetesting.analysis.Observer.getIidToLine(start.getIid()) + " to " + javato.activetesting.analysis.Observer.getIidToLine(dest.getIid()) + " with " + numThreads + " threads: " + curPath + ".");
	}
	return (found == Result.PATH_FOUND);
    }

    private Result findPath(UniqueEvent cur, UniqueEvent start, UniqueEvent dest, int numThreads, Stack<UniqueEvent> curPath, HashMap<Integer, Integer> threadEventIndexMap) {
	//System.out.println("Exploring event index " + cur.getEventIndex() + " with cur path " + curPath);
	int curThread = cur.getThread();
	// Check for illegal cycles and infinite loops
	if (threadEventIndexMap.containsKey(curThread) && cur.getEventIndex() <= threadEventIndexMap.get(curThread))
	    return Result.ILLEGAL_CYCLE;
	// Optimization: We can ignore paths that go to events with vector clocks less than the start event's vector clock or events with greater vector clock value for the start/end thread than the end event.
	if (VectorClock.isVC1LessThanVC2(cur.getVectorClock(), start.getVectorClock()) || cur.getVectorClock().getValue(dest.getThread()) > dest.getVectorClock().getValue(dest.getThread()))
	    return Result.VECTOR_CLOCK_PRUNABLE;
	// Optimization: Don't go beyond the end event on its thread
	if (dest.getThread().equals(curThread) && cur.getEventIndex() > dest.getEventIndex())
	    return Result.NO_PATH_FOUND;
	// Add current node to current path
	Integer initialEventIndex = null;
	if (threadEventIndexMap.containsKey(curThread))
	    initialEventIndex = threadEventIndexMap.get(curThread);
	else
	    initialEventIndex = -1;  // Real event indices are always >= 0
	threadEventIndexMap.put(curThread, cur.getEventIndex());
	curPath.push(cur);
	// Base cases
	int numThreadsOnThisPath = threadEventIndexMap.keySet().size();
	if (numThreadsOnThisPath > numThreads)
	    return backtrack(curPath, threadEventIndexMap, curThread, initialEventIndex);
	if (cur.equals(dest)) {
	    if (numThreadsOnThisPath == numThreads)
		return Result.PATH_FOUND;
	    else
		return backtrack(curPath, threadEventIndexMap, curThread, initialEventIndex);
	}
	// Check sequential relation
	if (immediateSequentialRelation.containsKey(cur))
	    if (findPath(immediateSequentialRelation.get(cur), start, dest, numThreads, curPath, threadEventIndexMap) == Result.PATH_FOUND)
		return Result.PATH_FOUND;
	// Check race relation
	Map<Integer, List<UniqueEvent>> racingEvents = sortedRaceRelation.get(cur);
	for (Integer nextThread: racingEvents.keySet()) {
	    // Optimization: We don't need to consider race relations that go to threads to which we have already been since those events will be covered through sequential recursion.
	    if (nextThread.equals(dest.getThread()) || !threadEventIndexMap.containsKey(nextThread)) {
		for (UniqueEvent e: racingEvents.get(nextThread)) {
		    Result result = findPath(e, start, dest, numThreads, curPath, threadEventIndexMap);
		    if (result == Result.PATH_FOUND)
			return Result.PATH_FOUND;
		    else if  (result == Result.NO_PATH_FOUND)  // Optimization: We only need to try the first valid race relation that does not make an illegal cycle, since it can reach all the others through the sequential relation.
			break;
		}
	    }
	}
	// We found nothing, so backtrack
	return backtrack(curPath, threadEventIndexMap, curThread, initialEventIndex);
    }

    private Result backtrack(Stack<UniqueEvent> curPath, HashMap<Integer, Integer> threadEventIndexMap, Integer curThread, Integer initialEventIndex) {
	if (initialEventIndex.intValue() == -1)
	    threadEventIndexMap.remove(curThread);
	else
	    threadEventIndexMap.put(curThread, initialEventIndex);
	curPath.pop();
	return Result.NO_PATH_FOUND;
    }

    /**
     * Simplifies a path by replacing e1 S e2 S e3 with e1 S e3
     * and e1 S/R e2 R e3 and e1 R e3 with e1 R e3.
     */
    private void simplifyPath(Stack<UniqueEvent> path) {
	if (path.size() <= 3)
	    return;
	Stack<UniqueEvent> s = new Stack<UniqueEvent>();
	UniqueEvent oldestEvent = path.pop();
	UniqueEvent olderEvent = path.pop();
	while (!path.empty()) {
	    UniqueEvent curEvent = path.pop();
	    if (oldestEvent.getThread().equals(olderEvent.getThread()) && oldestEvent.getThread().equals(curEvent.getThread()))
		olderEvent = curEvent;
	    else if (sortedRaceRelation.get(oldestEvent).containsKey(curEvent.getThread()) && sortedRaceRelation.get(oldestEvent).get(curEvent.getThread()).contains(curEvent))
		olderEvent = curEvent;
	    else {
		s.push(oldestEvent);
		oldestEvent = olderEvent;
		olderEvent = curEvent;
	    }
	}
	s.push(oldestEvent);
	s.push(olderEvent);
	while (!s.empty())
	    path.push(s.pop());
    }

    /**
     * Finds the shared prefix of two events.
     * We get their contexts and walk through them.
     * Note that contexts go from newest to oldest, so we
     * have to walk backwards.
     */
    private List<Integer> findSharedPrefix(UniqueEvent e1, UniqueEvent e2) {
	LinkedList<Integer> sharedPrefix = new LinkedList<Integer>();
	List<Integer> context1 = e1.getContext(), context2 = e2.getContext();
	ListIterator<Integer> it1 = context1.listIterator(context1.size()), it2 = context2.listIterator(context2.size());
	// Contexts go from newest to oldest
	while (it1.hasPrevious() && it2.hasPrevious()) {
	    Integer count1 = it1.previous(), count2 = it2.previous();
	    Integer iid1 = it1.previous(), iid2 = it2.previous();
	    if (iid1.equals(iid2) && count1.equals(count2)) {
		sharedPrefix.addFirst(count1);
		sharedPrefix.addFirst(iid1);
	    } else
		break;
	}
	return sharedPrefix;
    }

    /**
     * Marks all the blocks in the given context as
     * not atomic.
     * Note that we only mark blocks as non-atomic if
     * they are in allBlocks.
     */
    private void addNonAtomicBlocks(List<Integer> context, Set<Integer> nonAtomicBlockSet) {
	ListIterator<Integer> it = context.listIterator(context.size());
	while (it.hasPrevious()) {
	    it.previous();  // ignore count
	    Integer iid = it.previous();
	    // Do not mark it if we have previously marked it
	    if (allBlocks.contains(iid) && !vcNonAtomicBlocks.contains(iid) && !algoNonAtomicBlocks.contains(iid))
		nonAtomicBlockSet.add(iid);
	}
    }

    /**
     * Finds whether or not this context contains at least
     * one block is not known non-atomic but that could be
     * marked as such (i.e. are in allBlocks).
     */
    private boolean containsViableBlocks(List<Integer> context, Set<Integer> newNonAtomicBlocks) {
	ListIterator<Integer> it = context.listIterator(context.size());
	while (it.hasPrevious()) {
	    it.previous();  // ignore count
	    Integer iid = it.previous();
	    if (allBlocks.contains(iid) && !vcNonAtomicBlocks.contains(iid) && !algoNonAtomicBlocks.contains(iid) && !newNonAtomicBlocks.contains(iid))
		return true;
	}
	return false;
    }

    /* Helper methods */

    private String lineNumberStringOfBlockSet(Set<Integer> s) {
	String str = "[";
	Iterator<Integer> it = s.iterator();
	while (it.hasNext()) {
	    str += javato.activetesting.analysis.Observer.getIidToLine(it.next());
	    if (it.hasNext())
		str += ", ";
	}
	return str + "]";
    }

    private String getPluralSuffix(int count) {
	if (count == 1)
	    return "";
	else
	    return "s";
    }
    
}