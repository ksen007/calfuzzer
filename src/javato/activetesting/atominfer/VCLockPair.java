package javato.activetesting.atominfer;


import javato.activetesting.lockset.LockSet;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Copy of hybridracedetection.VCLockPair except that
 * locksets guard UniqueEvents instead of Integers.
 */
public class VCLockPair {
    
    private long clockValue;
    private HashMap<LockSet, TreeSet<UniqueEvent>> lockSets;

    public VCLockPair(long c) {
        clockValue = c;
        lockSets = new HashMap<LockSet, TreeSet<UniqueEvent>>();
    }

    public long getClock() {
        return clockValue;
    }

    public HashMap<LockSet, TreeSet<UniqueEvent>> getLockSets() {
        return lockSets;
    }
}
