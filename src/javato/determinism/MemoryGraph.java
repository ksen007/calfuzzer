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

package javato.determinism;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class MemoryGraph implements Serializable {

    public static class Node implements Serializable {
        public Node(Object obj) {
            this.obj = obj;
            this.fields = new TreeMap<String,Node>();
        }

        public Object obj;
        public SortedMap<String,Node> fields;
    }

    public MemoryGraph() {
        root = new Node(null);
    }

    public Node get(Path path) {
        Node n = root;
        for (String f : path) {
            n = n.fields.get(f);
            if (n == null)
                return null;
        }
        return n;
    }

    public void dump() {
        dump("", root, new IdentityHashMap<Node,Boolean>());
    }

    private void dump(String path, Node n, IdentityHashMap<Node,Boolean> seen) {
        if (seen.containsKey(n))
            return;
        seen.put(n, Boolean.TRUE);
        if (n.obj != null) {
            System.out.println(path + ": " + n.obj + " " + n.obj.getClass());
        } else {
            System.out.println(path + ": null");
        }

        for (Map.Entry<String,Node> e : n.fields.entrySet()) {
            dump(path + "." + e.getKey(), e.getValue(), seen);
        }
    }

    public Set<Path> generatePaths(int maxDepth) {
        if (maxDepth < 1)
            return Collections.emptySet();

        HashSet<Path> ret = new HashSet<Path>();

        // TODO: This code is pretty inefficient -- lots of
        // unnecessary work is done in MemoryGraph.get().

        // Initialize the queue.
        Queue<Path> Q = new LinkedList<Path>();
        for (Map.Entry<String,Node> e : root.fields.entrySet()) {
            Path p = new Path(e.getKey());
            ret.add(p);
            Q.add(p);
        }

        while (!Q.isEmpty()) {
            Path p = Q.remove();
            if (p.size() >= maxDepth)
                continue;

            for (Map.Entry<String,Node> e : get(p).fields.entrySet()) {
                if (e.getKey().startsWith("["))
                    continue;

                Path q = new Path(p, e.getKey());
                ret.add(q);
                Q.add(q);
            }
        }

        return ret;
    }

    public Node root;
}
