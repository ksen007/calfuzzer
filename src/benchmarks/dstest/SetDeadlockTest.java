package benchmarks.dstest;

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
public class SetDeadlockTest extends Thread {
    Set l1, l2;
    public int c;
    public static Random rand = new Random();

    public SetDeadlockTest(Set l1, Set l2) {
        this.l1 = l1;
        this.l2 = l2;
    }

    public static int getInput() {
        return rand.nextInt();
    }

    public void run() {
        for (int i = 0; i < 100; i++) {
            c = getInput();
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
                    l1.containsAll(l2);
                    break;
                case 5:
                    l1.equals(l2);
                    break;
                case 6:
                    l1.hashCode();
                    break;
                case 7:
                    l1.isEmpty();
                    break;
                case 8:
                    l1.iterator();
                    break;
                case 9:
                    l1.remove(new Integer(getInput()));
                    break;
                case 10:
                    l1.removeAll(l2);
                    break;
                case 11:
                    l1.retainAll(l2);
                    break;
                case 12:
                    l1.size();
                    break;
                default:
                    l1.toArray();
                    break;
            }
        }
    }

    public static void testHashSet() {
        Set l1 = Collections.synchronizedSet(new HashSet());
        Set l2 = Collections.synchronizedSet(new HashSet());
        for (int i = 0; i < 10; i++) {
            if (getInput() > 10) {
                (new SetDeadlockTest(l1, l2)).start();
            } else {
                (new SetDeadlockTest(l2, l1)).start();
            }
        }
    }

    public static void testTreeSet() {
        Set l1 = Collections.synchronizedSet(new TreeSet());
        Set l2 = Collections.synchronizedSet(new TreeSet());
        for (int i = 0; i < 10; i++) {
            if (getInput() > 10) {
                (new SetDeadlockTest(l1, l2)).start();
            } else {
                (new SetDeadlockTest(l2, l1)).start();
            }
        }
    }

    public static void testLinkedHashSet() {
        Set l1 = Collections.synchronizedSet(new LinkedHashSet());
        Set l2 = Collections.synchronizedSet(new LinkedHashSet());
        for (int i = 0; i < 10; i++) {
            if (getInput() > 10) {
                (new SetDeadlockTest(l1, l2)).start();
            } else {
                (new SetDeadlockTest(l2, l1)).start();
            }
        }
    }

    public static void main(String[] args) {
        testHashSet();
        testLinkedHashSet();
        testTreeSet();
    }

}
