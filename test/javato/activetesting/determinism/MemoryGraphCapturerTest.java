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

package javato.activetesting.determinism;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.junit.*;
import static org.junit.Assert.*;

import test.TestClasses;
import static test.TestClasses.A;
import static test.TestClasses.B;
import static test.TestClasses.D;
import static test.TestClasses.E;

public class MemoryGraphCapturerTest {

    @Test
    public void testCaptureMemoryGraphLocalInstances() {
        A a = new A();
        B b = new B();

        TreeMap<String,Object> locals = new TreeMap<String,Object>();
        locals.put("a", a);
        locals.put("b", b);

        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        MemoryGraph G = mgc.capture(locals.entrySet());

        assertNotNull(G.get(new Path("a")));
        assertNotNull(G.get(new Path("b")));

        assertNull(G.get(new Path("c")));
        assertNull(G.get(new Path("a", "b1")));
        assertNull(G.get(new Path("a", "b2")));

        assertSame(a, G.get(new Path("a")).obj);
        assertSame(b, G.get(new Path("b")).obj);
    }

    @Test
    public void testCaptureMemoryGraphStatic() {
        TestClasses.o = new B();
        B.o = new A();

        // Ensure interface I is loaded.
        E e = new E();

        TreeMap<String,Object> locals = new TreeMap<String,Object>();
        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        MemoryGraph G = mgc.capture(locals.entrySet());

        assertNotNull(G.get(new Path("test.TestClasses")));
        assertNotNull(G.get(new Path("test.TestClasses", "o")));
        assertNotNull(G.get(new Path("test.TestClasses$B")));
        assertNotNull(G.get(new Path("test.TestClasses$B", "o")));
        assertNotNull(G.get(new Path("test.TestClasses$I", "constant")));

        assertNull(G.get(new Path("test.TestClasses", "a")));
        assertNull(G.get(new Path("test.TestClasses", "b")));

        assertSame(TestClasses.o, G.get(new Path("test.TestClasses", "o")).obj);
        assertSame(B.o, G.get(new Path("test.TestClasses$B", "o")).obj);
        assertEquals(13, G.get(new Path("test.TestClasses$I", "constant")).obj);
    }

    @Test
    public void testCaptureMemoryGraphArrays() {
        A a1 = new A();
        A a2 = new A();
        B.C c = (new B()).new C();
        c.as[0] = a1;
        c.as[1] = a2;
        c.as[2] = a1;

        TreeMap<String,Object> locals = new TreeMap<String,Object>();
        locals.put("c", c);

        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        MemoryGraph G = mgc.capture(locals.entrySet());

        assertNotNull(G.get(new Path("c")));
        assertNotNull(G.get(new Path("c", "as")));
        assertNotNull(G.get(new Path("c", "as", "[0]")));
        assertNotNull(G.get(new Path("c", "as", "[0]")));
        assertNotNull(G.get(new Path("c", "as", "[1]")));
        assertNotNull(G.get(new Path("c", "as", "[2]")));
        assertSame(G.get(new Path("c", "as", "[0]")),
                   G.get(new Path("c", "as", "[2]")));
    }

    @Test
    public void testCaptureMemoryGraphBigArrays() {
        Object os[] = new Object[1000];
        os[413] = new A();
        os[819] = new B();

        TreeMap<String,Object> locals = new TreeMap<String,Object>();
        locals.put("os", os);

        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        MemoryGraph G = mgc.capture(locals.entrySet());

        assertNotNull(G.get(new Path("os")));
        assertNull(G.get(new Path("os", "[413]")));
        assertNull(G.get(new Path("os", "[819]")));
    }

    @Test
    public void testCaptureMemoryGraphInherited() {
        E e = new E();
        e.setO1(3);
        e.setO2(4);
        e.setO3(5);
        e.x = 6;
        e.myO = 7;

        TreeMap<String,Object> locals = new TreeMap<String,Object>();
        locals.put("e", e);

        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        MemoryGraph G = mgc.capture(locals.entrySet());

        assertNotNull(G.get(new Path("e")));
        assertNotNull(G.get(new Path("e", "o1")));
        assertNotNull(G.get(new Path("e", "o2")));
        assertNotNull(G.get(new Path("e", "o3")));
        assertNotNull(G.get(new Path("e", "x")));
        assertNotNull(G.get(new Path("e", "myO")));

        assertNull(G.get(new Path("e", "constant")));

        assertEquals(3, G.get(new Path("e", "o1")).obj);
        assertEquals(4, G.get(new Path("e", "o2")).obj);
        assertEquals(5, G.get(new Path("e", "o3")).obj);
        assertEquals(6, G.get(new Path("e", "x")).obj);
        assertEquals(7, G.get(new Path("e", "myO")).obj);
    }

    @Test
    public void testCaptureMemoryGraphSerialization() throws Exception {
        // Serializable.
        A a = new A();
        B b = new B();
        a.m.put(b,a);

        // Serializable.
        HashMap<Object,Object> h1 = new HashMap<Object,Object>();
        h1.put(7, a);
        h1.put(b, 13);

        // Not serializable.
        HashMap<Object,Object> h2 = new HashMap<Object,Object>();
        h2.put(19, b);
        h2.put(new D(), a);
        h2.put(h1, h1);

        // Serializable.
        B.o = new Object[] { a, null, b, null };

        // Not serializable.
        TestClasses.o = new Object[] { null, a, null, h2 };

        TreeMap<String,Object> locals = new TreeMap<String,Object>();
        locals.put("a", a);
        locals.put("b", b);
        locals.put("h1", h1);
        locals.put("h2", h2);

        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        MemoryGraph G = mgc.capture(locals.entrySet());

        // Serialize and deserialize.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(G);
        byte[] data = baos.toByteArray();
        ObjectInputStream ois =
            new ObjectInputStream(new ByteArrayInputStream(data));
        MemoryGraph H = (MemoryGraph)ois.readObject();

        // Check serializable objects remain.
        assertNotNull(H.get(new Path("a")).obj);
        assertNotNull(H.get(new Path("b")).obj);
        assertNotNull(H.get(new Path("h1")).obj);
        assertNotNull(H.get(new Path("test.TestClasses$B", "o")).obj);

        // Check non-serializable objects are gone.
        assertNull(H.get(new Path("h2")).obj);
        assertNull(H.get(new Path("test.TestClasses", "o")).obj);

        // Check children of non-serializable objects remain.
        assertNotNull(H.get(new Path("test.TestClasses", "o", "[1]")).obj);
    }

    // @Test
    public void testCaptureMemoryGraphCycles1() throws Exception {
        Object A[][] = new Object[3][5];
        A[1][2] = A;

        ArrayList<Object> L = new ArrayList<Object>();

        // List L added to itself before array A.
        L.add(L);
        L.add(A);
        A[0][1] = L;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(L);
        byte[] data = baos.toByteArray();

        System.out.println("serialized");

        ObjectInputStream ois =
            new ObjectInputStream(new ByteArrayInputStream(data));
        ArrayList<Object> L2 = (ArrayList<Object>)ois.readObject();

        System.out.println("deserialized");

        System.out.println(L.equals(L2));
    }

    // @Test
    public void testCaptureMemoryGraphCycles2() throws Exception {
        Object A[][] = new Object[3][5];
        A[1][2] = A;

        ArrayList<Object> L = new ArrayList<Object>();

        // Array A added before list L!!!
        L.add(A);
        L.add(L);
        A[0][1] = L;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(L);
        byte[] data = baos.toByteArray();

        System.out.println("serialized");

        ObjectInputStream ois =
            new ObjectInputStream(new ByteArrayInputStream(data));
        ArrayList<Object> L2 = (ArrayList<Object>)ois.readObject();

        System.out.println("deserialized");

        System.out.println(L.equals(L2));
    }

    // @Test
    public void testCaptureMemoryGraphArrayEquals() throws Exception {
        // How the hell does array equals work?

        int[] X = { 0, 1, 2 };
        int[] Y = { 0, 1, 2 };
        System.out.println(X.equals(Y));

        Object[] A = new Object[3];
        Object[] B = new Object[3];
        System.out.println(A.equals(B));

        A[0] = B[0] = "Hello, world!";
        System.out.println(A.equals(B));

        A[1] = B[1] = new Object() {
                public boolean equals(Object o) {
                    return false;
                }
            };
        System.out.println(A.equals(B));

        ArrayList<Integer> list1 = new ArrayList();
        ArrayList<Integer> list2 = new ArrayList();
        list1.add(1);
        list2.add(1);
        A[1] = list1;
        B[1] = list2;
        System.out.println(A.equals(B));
    }
}
