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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Path implements Serializable, Comparable<Path>, Iterable<String> {

    public Path(String root, String... fields) {
        path = new ArrayList<String>();
        path.add(root);
        for (int i = 0; i < fields.length; i++) {
            path.add(fields[i]);
        }
    }

    public Path(Path p, String field) {
        path = new ArrayList<String>(p.path);
        path.add(field);
    }

    public int size() {
        return path.size();
    }

    public boolean startsWith(Path p) {
        Iterator<String> i = path.iterator();
        Iterator<String> j = p.path.iterator();
        while (i.hasNext() && j.hasNext()) {
            if (!i.next().equals(j.next()))
                return false;
        }
        return !j.hasNext();
    }

    public int compareTo(Path p) {
        Iterator<String> i = path.iterator();
        Iterator<String> j = p.path.iterator();
        while (i.hasNext() && j.hasNext()) {
            int t = i.next().compareTo(j.next());
            if (t != 0) return t;
        }
        if (j.hasNext()) {
            return -1;
        } else if (i.hasNext()) {
            return 1;
        } else {
            return 0;
        }
    }

    public int hashCode() {
        return path.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (o.getClass() != this.getClass())) return false;

        Path p = (Path)o;
        return this.path.equals(p.path);
    }

    public Iterator<String> iterator() {
        return Collections.unmodifiableList(path).iterator();
    }

    public String toString() {
        return path.toString();
    }

    // First element is the root; remaining elements are fields.
    private final ArrayList<String> path;
}
