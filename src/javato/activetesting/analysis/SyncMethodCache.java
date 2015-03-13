package javato.activetesting.analysis;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.TreeSet;

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
public class SyncMethodCache {
    private IdentityHashMap<Class, TreeSet<Integer>> synchronizedIids
            = new IdentityHashMap<Class, TreeSet<Integer>>();
    private IdentityHashMap<Class, TreeSet<Integer>> nonSynchronizedIids
            = new IdentityHashMap<Class, TreeSet<Integer>>();

    private IdentityHashMap<Class, HashSet<String>> synchronizedSigs
            = new IdentityHashMap<Class, HashSet<String>>();
    private IdentityHashMap<Class, HashSet<String>> nonSynchronizedSigs
            = new IdentityHashMap<Class, HashSet<String>>();

    private enum SYNC_STATUS {
        SYNC, NONSYNC, UNKNOWN
    }

    private SYNC_STATUS getFromCache(int iid, Class cls, String sig) {
        TreeSet<Integer> syncIids = synchronizedIids.get(cls);
        if (syncIids != null) {
            if (syncIids.contains(iid)) return SYNC_STATUS.SYNC;
        }
        TreeSet<Integer> nonSyncIids = nonSynchronizedIids.get(cls);
        if (nonSyncIids != null) {
            if (nonSyncIids.contains(iid)) return SYNC_STATUS.NONSYNC;
        }

        HashSet<String> syncSigs = synchronizedSigs.get(cls);
        if (syncSigs != null) {
            if (syncSigs.contains(sig)) return SYNC_STATUS.SYNC;
        }
        HashSet<String> nonSyncSigs = nonSynchronizedSigs.get(cls);
        if (nonSyncSigs != null) {
            if (nonSyncSigs.contains(sig)) return SYNC_STATUS.NONSYNC;
        }

        return SYNC_STATUS.UNKNOWN;
    }

    private void setToCache(int iid, Class cls, String sig, boolean isSync) {
        if (isSync) {
            TreeSet<Integer> syncIids = synchronizedIids.get(cls);
            if (syncIids == null) {
                syncIids = new TreeSet<Integer>();
                synchronizedIids.put(cls, syncIids);
            }
            syncIids.add(iid);
            HashSet<String> syncSigs = synchronizedSigs.get(cls);
            if (syncSigs == null) {
                syncSigs = new HashSet<String>();
                synchronizedSigs.put(cls, syncSigs);
            }
            syncSigs.add(sig);
        } else {
            TreeSet<Integer> nonSyncIids = nonSynchronizedIids.get(cls);
            if (nonSyncIids == null) {
                nonSyncIids = new TreeSet<Integer>();
                nonSynchronizedIids.put(cls, nonSyncIids);
            }
            nonSyncIids.add(iid);
            HashSet<String> nonSyncSigs = nonSynchronizedSigs.get(cls);
            if (nonSyncSigs == null) {
                nonSyncSigs = new HashSet<String>();
                nonSynchronizedSigs.put(cls, nonSyncSigs);
            }
            nonSyncSigs.add(sig);
        }
    }

    public synchronized boolean isSynchronized(int iid, Object lock, String sig) {
        Class c = lock.getClass();
        Class cls = c;
        SYNC_STATUS stat = getFromCache(iid, cls, sig);
        if (stat == SYNC_STATUS.SYNC) return true;
        if (stat == SYNC_STATUS.NONSYNC) return false;

        while (c != null) {
            Method[] ms = c.getDeclaredMethods();
            for (Method m : ms) {
                String mname = m.toString();
                if (mname.indexOf(sig) != -1) {
                    if (mname.indexOf("synchronized") != -1) {
                        setToCache(iid, cls, sig, true);
                        return true;
                    } else {
                        setToCache(iid, cls, sig, false);
                        return false;
                    }
                }
            }
            c = c.getSuperclass();
        }
        setToCache(iid, cls, sig, false);
        return false;
    }
}
