/**
 * Copyright (c) 2009,
 * Jacob Burnim <jburnim@cs.berkeley.edu>
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

// Must use different package here, because we filter javato.* classes.
package test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestClasses {

    public static interface I {
        public static final int constant = 13;
    }

    public static class A implements Serializable {
        public A() {
            m = new TreeMap<B,A>();
            x = 7;
            b1 = b2 = null;
            o1 = o2 = o3 = null;
        }

        public int x;
        public Map<B,A> m;
        public B b1, b2;

        public void setO1(Object o) {
            o1 = o;
        }

        public void setO2(Object o) {
            o2 = o;
        }

        public void setO3(Object o) {
            o3 = o;
        }

        protected Object o1;
        Object o2;  // Package private.
        private Object o3;
    }

    public static class E extends A implements I, Serializable {
        public Object myO = null;
    }

    public static class B implements Serializable {
        public class C implements Serializable {
            public C() {
                s = null;
                as = new A[3];
            }

            public String s;
            public A[] as;
        }

        public B() {
            c = null;
            d = 3.14;
            cs = new ArrayList<C>();
        }

        public C c;
        double d;
        public List<C> cs;

        public static Object o = null;
    }

    // Not serializable.
    public static class D {
        Object x;
        Object y;

        public D() {
            x = y = null;
        }
    }

    public static A a = null;
    public static B b = null;
    public static Object o = null;
}
