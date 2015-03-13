package javato.activetesting.hybridracedetection;

import javato.activetesting.common.Parameters;
import javato.activetesting.common.MutableLong;
import javato.activetesting.lockset.LockSet;
import javato.activetesting.vc.VectorClock;

import java.io.*;
import java.util.*;

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
public class HybridRaceTracker {
    private LinkedHashSet<CommutativePair> alreadySeenRaces;

    // m -> t -> list(vc,ls->Set(iid))
    private Map<Long, Map<Integer, LinkedList<VCLockPair>>> readMap;
    private Map<Long, Map<Integer, LinkedList<VCLockPair>>> writeMap;

    private Map<Integer, MutableLong> iidVisitCount;
    private int lockRaceCount = 0;
    private int dataRaceCount = 0;


    public HybridRaceTracker() {
        readMap = new TreeMap<Long, Map<Integer, LinkedList<VCLockPair>>>();
        writeMap = new TreeMap<Long, Map<Integer, LinkedList<VCLockPair>>>();
        iidVisitCount = new TreeMap<Integer, MutableLong>();

        alreadySeenRaces = getRacesFromFile();
    }

    public static LinkedHashSet<CommutativePair> getRacesFromFile() {
        LinkedHashSet<CommutativePair> alreadySeenRaces;
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(Parameters.ERROR_LOG_FILE)));
            alreadySeenRaces = (LinkedHashSet<CommutativePair>) in.readObject();
            in.close();
        } catch (IOException e) {
            alreadySeenRaces = new LinkedHashSet<CommutativePair>();
        } catch (ClassNotFoundException e) {
            alreadySeenRaces = new LinkedHashSet<CommutativePair>();
        }
        return alreadySeenRaces;
    }

    public void dumpRaces() {
        ObjectOutputStream out;
        javato.activetesting.analysis.Observer.writeIntegerList(Parameters.ERROR_LIST_FILE, alreadySeenRaces.size());
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(Parameters.ERROR_LOG_FILE)));
            out.writeObject(alreadySeenRaces);
            for(CommutativePair cp:alreadySeenRaces) {
                cp.printcryptic(System.out);
            }
            System.out.println("# of data races " + dataRaceCount + " and lock races "+lockRaceCount);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkRace(Integer iid, Integer t, Long m, boolean isRead, VectorClock vc, LockSet ls,
                          boolean isLock, boolean isVolatile) {
        Map<Integer, LinkedList<VCLockPair>> threadLists1;
        Map<Integer, LinkedList<VCLockPair>> threadLists2 = null;

        long visitC = incAndGetVisitCount(iid);

        if (isRead) {
            threadLists1 = writeMap.get(m);
        } else {
            threadLists1 = writeMap.get(m);
            threadLists2 = readMap.get(m);
        }
        if (threadLists1 != null) {
            checkRaceAux(iid, threadLists1, t, vc, ls, m,visitC,isLock,isRead,false,isVolatile);
        }
        if (threadLists2 != null) {
            checkRaceAux(iid, threadLists2, t, vc, ls, m, visitC,isLock,isRead,true,isVolatile);
        }
    }

    private long incAndGetVisitCount(Integer iid) {
        if (Parameters.LOG_IID_VISIT_COUNT) {
            MutableLong l = iidVisitCount.get(iid);
            if (l==null) {
                l = new MutableLong(0);
                iidVisitCount.put(iid,l);
            }
            (l.val)++;
            return l.val;
        }
        return 0;
    }

    protected void checkRaceAux(Integer iid, Map<Integer, LinkedList<VCLockPair>> threadLists,
                                Integer t, VectorClock vc, LockSet ls, Long m, long iidVisitCount,
                                boolean isLock, boolean isRead1, boolean isRead2, boolean isVolatile) {
        for (Integer t2 : threadLists.keySet()) {
            if (!t2.equals(t)) {
                Long c2 = vc.getValue(t2);
                LinkedList<VCLockPair> vcs = threadLists.get(t2);
                for (VCLockPair c : vcs) {
                    if (c2 < c.getClock()) {
                        Map<LockSet, TreeMap<Integer,Long>> lockSets = c.getLockSets();
                        for (LockSet ls2 : lockSets.keySet()) {
                            if (!ls2.intersects(ls)) {
                                TreeMap<Integer,Long> iids = lockSets.get(ls2);
                                CommutativePair cp;
                                for (Integer iid2 : iids.keySet()) {
                                    long iid2Count = 0;
                                    if (Parameters.LOG_IID_VISIT_COUNT) {
                                        iid2Count = iids.get(iid2);
                                    }
                                    cp = new CommutativePair(iid, iidVisitCount,isRead1,iid2,iid2Count,isRead2,isLock,isVolatile);
                                    if (Parameters.trackWaitNotifyOnly && !isLock)
                                        return;
                                    if (!alreadySeenRaces.contains(cp)) {
                                        //printLocation();
                                        alreadySeenRaces.add(cp);
                                        if (isLock) lockRaceCount++;
                                        else dataRaceCount++;
                                        if (Parameters.LOG_IID_VISIT_COUNT) {
                                            if (isLock)
                                                System.out.print("Lock race between ");
                                            else
                                                System.out.print("Data race between ");

                                            System.out.println(javato.activetesting.analysis.Observer.getIidToLine(iid) + ":"+iidVisitCount+ " and "
                                                    + javato.activetesting.analysis.Observer.getIidToLine(iid2)+":"+iid2Count);
                                        } else {
                                            if (isLock)
                                                System.out.print("Lock race between ");
                                            else
                                                System.out.print("Data race between ");
                                            System.out.println(javato.activetesting.analysis.Observer.getIidToLine(iid) + " and "
                                                    + javato.activetesting.analysis.Observer.getIidToLine(iid2));

                                        }
                                    } else if (Parameters.removeOlderRace) {
                                        alreadySeenRaces.remove(cp);
                                        alreadySeenRaces.add(cp);
                                    }
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }

            }
        }
    }

    public void addEvent(Integer iid, Integer t, Long m, boolean isRead, VectorClock vc, LockSet ls) {
        Map<Long, Map<Integer, LinkedList<VCLockPair>>> currentMap = isRead ? readMap : writeMap;
        Map<Integer, LinkedList<VCLockPair>> threadLists = currentMap.get(m);
        if (threadLists == null) {
            threadLists = new TreeMap<Integer, LinkedList<VCLockPair>>();
            currentMap.put(m, threadLists);
        }
        addEventAux(iid, threadLists, t, vc, ls);
    }

    protected void addEventAux(Integer iid, Map<Integer, LinkedList<VCLockPair>> threadLists, Integer t, VectorClock vc, LockSet ls) {
        LinkedList<VCLockPair> vcLists = threadLists.get(t);
        if (vcLists == null) {
            vcLists = new LinkedList<VCLockPair>();
            threadLists.put(t, vcLists);
        }
        long c = vc.getValue(t);
        VCLockPair cPair;
        if (vcLists.isEmpty() || vcLists.getFirst().getClock() < c) {
            cPair = new VCLockPair(c);
            vcLists.addFirst(cPair);
            if (vcLists.size() > Parameters.N_VECTOR_CLOCKS_WINDOW) {
                vcLists.removeLast();
            }
        } else {
            cPair = vcLists.getFirst();
        }
        HashMap<LockSet, TreeMap<Integer,Long>> lockSets = cPair.getLockSets();
        TreeMap<Integer,Long> iids = lockSets.get(ls);
        if (iids == null) {
            iids = new TreeMap<Integer,Long>();
            lockSets.put(new LockSet(ls), iids);
        }
        if (Parameters.removeOlderAccess || (!Parameters.removeOlderAccess && !iids.containsKey(iid))) {
            if (Parameters.LOG_IID_VISIT_COUNT) {
                iids.put(iid,iidVisitCount.get(iid).val);
            } else {
                iids.put(iid,0l);
            }
        }
    }


    public void printLocation() {
        Throwable t = new Throwable();
        StackTraceElement[] elems = t.getStackTrace();
        for (int i = 0; i < elems.length; i++) {
            if (!elems[i].getClassName().startsWith("javato.") && (elems[i].getLineNumber() != -1)) {
                System.out.print(elems[i].getClassName());
                System.out.print(":");
                System.out.print(elems[i].getFileName());
                System.out.print(":");
                System.out.println(elems[i].getLineNumber());
                return;
            }
        }
    }

}
