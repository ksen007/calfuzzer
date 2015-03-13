package javato.activetesting.atominfer;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import javato.activetesting.vc.VectorClock;

/**
 * Uniquely identify an event by the iid of its thread and
 * by its context.  We can compute its iid from the context.
 * We also store for each event and index representing when
 * we saw it on our trace.  This allows us to easily determine
 * which events on the same thread happened before each other.
 */
class UniqueEvent implements Comparable<UniqueEvent>, Serializable {

    private Integer thread;
    private List<Integer> context;
    private VectorClock vectorClock;
    private int eventIndex;

    public UniqueEvent(Integer thread, List<Integer> context, VectorClock vectorClock, int eventIndex) {
	this.thread = thread;
	this.context = context;
	this.vectorClock = new VectorClock(vectorClock); // make a copy
	this.eventIndex = eventIndex;
    }

    public Integer getThread() {
	return thread;
    }

    public List<Integer> getContext() {
	return context;
    }

    // The iid of this statement is always the first thing in the context (see ContextIndexingPerThread.getContext()).
    public Integer getIid() {
	return context.get(0);
    }

    public VectorClock getVectorClock() {
	return vectorClock;
    }

    public int getEventIndex() {
	return eventIndex;
    }

    public boolean equals(Object object) {
	if (object == null) return false;
        if (!(object instanceof UniqueEvent)) return false;
        UniqueEvent o = (UniqueEvent) object;
	return thread.equals(o.thread) && context.equals(o.context);
    }

    public int hashCode() {
	return thread.hashCode() + context.hashCode();
    }

    public int compareTo(UniqueEvent o) {
	// First, compare the threads.
	int x = thread.compareTo(o.thread);
	if (x != 0)
	    return x;
	// Next, compare the lists
	Iterator<Integer> i1 = context.iterator(), i2 = o.context.iterator();
	while (i1.hasNext() && i2.hasNext()) {
	    x = i1.next().compareTo(i2.next());
	    if (x != 0)
		return x;
	}
	if (i1.hasNext())
	    return 1;
	if (i2.hasNext())
	    return -1;
	return 0;
    }

    public String toString() {
	return "(" + javato.activetesting.analysis.Observer.getIidToLine(getIid()) + ", " + thread + ", " + context + ", " + eventIndex + ")";
    }
    
}