package benchmarks.dstest;

import benchmarks.instrumented.java15.util.Collections;
import benchmarks.instrumented.java15.util.List;

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
public class ListDeadlockTest extends Thread {
    List l1, l2;
    public int c;
    public static java.util.Random rand = new java.util.Random();

    public ListDeadlockTest(List l1, List l2, int c) {
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
                l1.add(new Integer(getInput()));
                break;
            case 1:
                l1.addAll(l2);
                break;
            case 2:
                l1.clear();
                break;
            case 3:
                l1.contains(new Integer(getInput()));
                break;
            case 4:
                l1.hashCode();
                break;
            case 5:
                l1.indexOf(new Integer(getInput()));
                break;
            case 6:
                l1.isEmpty();
                break;
            case 7:
                l1.iterator();
                break;
            case 8:
                l1.lastIndexOf(new Integer(getInput()));
                break;
            case 9:
                l1.listIterator();
                break;
            case 10:
                l1.remove(new Integer(getInput()));
                break;
            case 11:
                l1.removeAll(l2);
                break;
            case 12:
                l1.retainAll(l2);
                break;
            case 13:
                l1.size();
                break;
            default:
                l1.toArray();
                break;
        }
    }

//    public static void testList(List l1, List l2) throws InterruptedException {
////        for (int i = 0; i < 10; i++) {
////            l1.add(new Integer(getInput()));
////            l2.add(new Integer(getInput()));
////        }
//        for (int i = 0; i < 15; i++) {
//            (new ListDeadlockTest(l1, l2, i)).start();
//        }
//        try {
//            Thread.sleep(10);
//        } catch (Exception e) {
//        }
//
//        for (int i = 0; i < 15; i++) {
//            (new ListDeadlockTest(l2, l1, i)).start();
//        }
////        for (int i = 0; i < 16; i++) {
////            for (int j = 0; j < 16; j++) {
////                testList2ops(l1, l2, i, j);
////            }
////        }
//    }
//
//    public static void testLinkedList2(int j, int k) throws InterruptedException {
//        List l1 = Collections.synchronizedList(new LinkedList());
//        List l2 = Collections.synchronizedList(new LinkedList());
//        for (int i = 0; i < 2; i++) {
//            l1.add(new Integer(i));
//            l2.add(new Integer(i + 1));
//        }
//        Thread t1 = (new ListDeadlockTest(l1, l2, j));
//        t1.start();
//        try {
//            Thread.sleep(5);
//        } catch (Exception e) {
//        }
//        Thread t2 = (new ListDeadlockTest(l2, l1, k));
//        t2.start();
//    }
//
//    public static void testArrayList2(int j, int k) throws InterruptedException {
//        List l1 = Collections.synchronizedList(new ArrayList());
//        List l2 = Collections.synchronizedList(new ArrayList());
//        for (int i = 0; i < 2; i++) {
//            l1.add(new Integer(i));
//            l2.add(new Integer(i + 1));
//        }
//        Thread t1 = (new ListDeadlockTest(l1, l2, j));
//        t1.start();
//        try {
//            Thread.sleep(5);
//        } catch (Exception e) {
//        }
//        Thread t2 = (new ListDeadlockTest(l2, l1, k));
//        t2.start();
//    }
//
//    public static void testStack2(int j, int k) throws InterruptedException {
//        List l1 = Collections.synchronizedList(new Stack());
//        List l2 = Collections.synchronizedList(new Stack());
//        for (int i = 0; i < 2; i++) {
//            l1.add(new Integer(i));
//            l2.add(new Integer(i + 1));
//        }
//        Thread t1 = (new ListDeadlockTest(l1, l2, j));
//        t1.start();
//        try {
//            Thread.sleep(5);
//        } catch (Exception e) {
//        }
//        Thread t2 = (new ListDeadlockTest(l2, l1, k));
//        t2.start();
//    }
//
//    public static void testList2ops(List l1, List l2, int j, int k) throws InterruptedException {
//        l1.clear();
//        l2.clear();
//        for (int i = 0; i < 10; i++) {
//            l1.add(new Integer(i));
//            l2.add(new Integer(i + 5));
//        }
//        Thread t1 = (new ListDeadlockTest(l1, l2, j));
//        t1.start();
//        try {
//            Thread.sleep(50);
//        } catch (Exception e) {
//        }
//        Thread t2 = (new ListDeadlockTest(l2, l1, k));
//        t2.start();
//        t2.join();
//        t1.join();
//    }

    public static void testList1() throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            testList2(i);
        }
    }

    private static void testList2(int i) {
        for (int j = 0; j < 15; j++) {
            testList3(i, j);
        }
    }

    private static ListFactory factory;

    private static void testList3(int j, int k) {
        List l1 = Collections.synchronizedList(factory.createList());
        List l2 = Collections.synchronizedList(factory.createList());
        for (int i = 0; i < 2; i++) {
            l1.add(new Integer(i));
            l2.add(new Integer(i + 1));
        }
        Thread t1 = (new ListDeadlockTest(l1, l2, j));
        Thread t2 = (new ListDeadlockTest(l2, l1, k));

        t1.start();
        try {
            Thread.sleep(5);
        } catch (Exception e) {
        }
        t2.start();
    }

    public static void testArrayList() throws InterruptedException {
        factory = new ArrayListFactory();
        testList1();
    }

    public static void testLinkedList() throws InterruptedException {
        factory = new LinkedListFactory();
        testList1();
    }

    public static void testStack() throws InterruptedException {
        factory = new StackFactory();
        testList1();
    }

}