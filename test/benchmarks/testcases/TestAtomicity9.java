package benchmarks.testcases;

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
public class TestAtomicity9 {
    public static int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0, i = 0;
    public final static Object lock1 = new Object();
    public final static Object lock2 = new Object();
    public final static Object lock3 = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
		a++;
		b++;
		f++;
		g++;
            }
        };
        Thread t2 = new Thread() {
            public void run() {
		c++;
		d++;
		g++;
		h++;
            }
        };
        Thread t3 = new Thread() {
            public void run() {
		d++;
		e++;
		h++;
		i++;
            }
        };
	t1.start();
	t2.start();
	t3.start();
	// Not atomic through path with two threads
	synchronized (lock1) {
	    int foo;
	    foo = a;
	    foo = b;
	    foo = c;
	    // Not atomic through path with two threads
	    synchronized (lock2) {
		// Not atomic through path with four threads
		synchronized (lock3) {
		    foo = f;
		    foo = i;
		}
		foo = c;
		foo = e;
	    }
	}
        t1.join();
	t2.join();
	t3.join();
    }
}
