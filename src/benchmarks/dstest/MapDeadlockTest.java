package benchmarks.dstest;

import benchmarks.instrumented.java15.util.Collections;
import benchmarks.instrumented.java15.util.Map;

import java.util.Random;

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
public class MapDeadlockTest extends Thread {
    Map l1, l2;
    public int c;
    public static Random rand = new Random();

    public MapDeadlockTest(Map l1, Map l2, int c) {
        this.l1 = l1;
        this.l2 = l2;
        this.c = c;
    }

    public static int getInput() {
        return rand.nextInt();
    }

    public void run() {
        switch (c) {
            case 0:
                l1.clear();
                break;
            case 1:
                l1.containsKey(new Integer(getInput()));
                break;
            case 2:
                l1.containsValue(new Integer(getInput()));
                break;
            case 3:
                l1.entrySet();
                break;
            case 4:
                l1.equals(l2);
                break;
            case 5:
                l1.get(new Integer(getInput()));
                break;
            case 6:
                l1.hashCode();
                break;
            case 7:
                l1.isEmpty();
                break;
            case 8:
                l1.keySet();
                break;
            case 9:
                l1.put(new Integer(getInput()), new Integer(getInput()));
                break;
            case 10:
                l1.remove(new Integer(getInput()));
                break;
            case 11:
                l1.size();
                break;
            default:
                l1.values();
                break;
        }
    }

    public static void testMap1() throws InterruptedException {
        for (int i = 0; i < 13; i++) {
            testMap2(i);
        }
    }

    private static void testMap2(int i) {
        for (int j = 0; j < 13; j++) {
            testMap3(i, j);
        }
    }

    private static MapFactory factory;

    private static void testMap3(int j, int k) {
        Map l1 = Collections.synchronizedMap(factory.createMap());
        Map l2 = Collections.synchronizedMap(factory.createMap());
        for (int i = 0; i < 2; i++) {
            l1.put(new Integer(i), new Integer(i));
            l2.put(new Integer(i), new Integer(i));
        }
        Thread t1 = (new MapDeadlockTest(l1, l2, j));
        Thread t2 = (new MapDeadlockTest(l2, l1, k));

        t1.start();
        try {
            Thread.sleep(5);
        } catch (Exception e) {
        }
        t2.start();
    }

    public static void testHashMap() throws InterruptedException {
        factory = new HashMapFactory();
        testMap1();
    }

    public static void testTreeMap() throws InterruptedException {
        factory = new TreeMapFactory();
        testMap1();
    }

    public static void testLinkedHashMap() throws InterruptedException {
        factory = new LinkedHashMapFactory();
        testMap1();
    }

    public static void testWeakHashMap() throws InterruptedException {
        factory = new WeakHashMapFactory();
        testMap1();
    }

    public static void testIdentityHashMap() throws InterruptedException {
        factory = new IdentityHashMapFactory();
        testMap1();
    }

}
