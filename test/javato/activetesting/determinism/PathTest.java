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

import java.util.Iterator;

import org.junit.*;
import static org.junit.Assert.*;

public class PathTest {

    @Test
    public void testPathBasic() {
        Path p1 = new Path("org.fake.Main", "trees", "[2]");
        assertEquals(3, p1.size());

        Iterator<String> i = p1.iterator();
        assertEquals("org.fake.Main", i.next());
        assertEquals("trees", i.next());
        assertEquals("[2]", i.next());
        assertFalse(i.hasNext());

        Path p2 = new Path("x");
        assertEquals(1, p2.size());

        i = p2.iterator();
        assertEquals("x", i.next());
        assertFalse(i.hasNext());

        Path p3 = new Path(p2, "monkeys");
        assertEquals(2, p3.size());

        i = p3.iterator();
        assertEquals("x", i.next());
        assertEquals("monkeys", i.next());
        assertFalse(i.hasNext());
    }

    @Test
    public void testPathStartsWith() {
        Path p1 = new Path("org.fake.Main", "trees", "[2]");
        Path p2 = new Path("org.fake.Main", "trees");
        Path p3 = new Path("l2");
        Path p4 = new Path("l2", "after", "x", "vel");

        assertTrue(p1.startsWith(p1));
        assertTrue(p1.startsWith(p2));
        assertFalse(p1.startsWith(p3));
        assertFalse(p1.startsWith(p4));

        assertFalse(p2.startsWith(p1));
        assertTrue(p2.startsWith(p2));
        assertFalse(p2.startsWith(p3));
        assertFalse(p2.startsWith(p4));

        assertFalse(p3.startsWith(p1));
        assertFalse(p3.startsWith(p2));
        assertTrue(p3.startsWith(p3));
        assertFalse(p3.startsWith(p4));

        assertFalse(p4.startsWith(p1));
        assertFalse(p4.startsWith(p2));
        assertTrue(p4.startsWith(p3));
        assertTrue(p4.startsWith(p4));
    }

    @Test
    public void testPathSerializable() {
    }

    @Test
    public void testPathComparable() {
    }

    @Test
    public void testPathEqualsHashCode() {
    }
}
