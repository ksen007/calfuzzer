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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.junit.*;
import static org.junit.Assert.*;

import static javato.activetesting.determinism.MemoryGraph.Node;

public class MemoryGraphTest {

    public <T> Set<T> set(T... elems) {
        TreeSet ret = new TreeSet();
        Collections.addAll(ret, elems);
        return ret;
    }

    public Path p(String root, String... fields) {
        return new Path(root, fields);
    }

    @Test
    public void testGeneratePathsSimple() {
        MemoryGraph G = new MemoryGraph();
        G.root.fields.put("a", new Node(null));
        G.root.fields.put("b", new Node(null));

        assertEquals(set(p("a"), p("b")), G.generatePaths(12));
    }

    @Test
    public void testGeneratePathsCycle() {
        MemoryGraph G = new MemoryGraph();
        Node a = new Node(null);
        G.root.fields.put("a", a);
        a.fields.put("a", a);

        assertEquals(set(p("a")),
                     G.generatePaths(1));

        assertEquals(set(p("a"), p("a", "a")),
                     G.generatePaths(2));

        assertEquals(set(p("a"), p("a", "a"), p("a", "a", "a")),
                     G.generatePaths(3));

        assertEquals(32, G.generatePaths(32).size());
    }

    @Test
    public void testGeneratePathsBlowUp() {
        MemoryGraph G = new MemoryGraph();
        Node n = G.root;
        for (int i = 0; i < 32; i++) {
            Node t = new Node(null);
            n.fields.put("a", t);
            n.fields.put("b", t);
            n = t;
        }

        assertEquals(set(p("a"), p("b")),
                     G.generatePaths(1));

        assertEquals(set(p("a"), p("b"),
                         p("a", "a"), p("a", "b"),
                         p("b", "a"), p("b", "b")),
                     G.generatePaths(2));

        assertEquals(2 + 4 + 8 + 16 + 32 + 64 + 128 + 256,
                     G.generatePaths(8).size());
    }
}
