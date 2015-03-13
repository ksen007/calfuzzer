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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class DeterminismInferenceMain2 {

    private static final int kMaxIterableSize = 1000000;

    public static class Predicate implements Comparable<Predicate> {

        public Predicate(Path p) {
            this.p = p;
            this.eq = "";
        }

        public Predicate(Path p, String eq) {
            this.p = p;
            this.eq = eq;
        }

        public String toString() {
            if (eq.equals("")) {
                return p.toString();
            } else {
                return eq + "(" + p + ")";
            }
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if ((o == null) || (o.getClass() != this.getClass())) return false;

            Predicate op = (Predicate)o;
            return (p.equals(op.p) && eq.equals(op.eq));
        }

        public int hashCode() {
            return (p.hashCode() * 37 + eq.hashCode());
        }

        public int compareTo(Predicate op) {
            int ret = p.compareTo(op.p);
            if (ret == 0) {
                return eq.compareTo(op.eq);
            } else {
                return ret;
            }
        }

        private final Path p;
        private final String eq;
    }


    static void addAll(Set s, Iterable itbl) {
        for (Object o : itbl) {
            s.add(o);
        }
    }

    static boolean equals(Object o1, Object o2) {
        if ((o1 == null) || (o2 == null)) {
            return false;

        } else if ((o1 instanceof Object[]) && (o2 instanceof Object[])) {
            return Arrays.deepEquals((Object[])o1, (Object[])o2);

        } else if ((o1 instanceof float[]) && (o2 instanceof float[])) {
            return Arrays.equals((float[])o1, (float[])o2);

        } else if ((o1 instanceof double[]) && (o2 instanceof double[])) {
            return Arrays.equals((double[])o1, (double[])o2);

        } else if ((o1 instanceof int[]) && (o2 instanceof int[])) {
            return Arrays.equals((int[])o1, (int[])o2);

        } else if ((o1 instanceof long[]) && (o2 instanceof long[])) {
            return Arrays.equals((long[])o1, (long[])o2);

        } else if ((o1 instanceof short[]) && (o2 instanceof short[])) {
            return Arrays.equals((short[])o1, (short[])o2);

        } else if ((o1 instanceof char[]) && (o2 instanceof char[])) {
            return Arrays.equals((char[])o1, (char[])o2);

        } else if ((o1 instanceof byte[]) && (o2 instanceof byte[])) {
            return Arrays.equals((byte[])o1, (byte[])o2);

        } else if ((o1 instanceof boolean[]) && (o2 instanceof boolean[])) {
            return Arrays.equals((boolean[])o1, (boolean[])o2);

        } else if ((o1 instanceof Iterable) && (o2 instanceof Iterable)) {
            // Compare two iterable's element-by-element.
            //
            // NOTE: This will fail to terminate if either of the
            // iterables contains itself.  We stop comparing elements
            // and return false after kMaxIterableSize elements to
            // prevent non-termination when the iterable generates an
            // infinite stream of elements.
            Iterator it1 = ((Iterable)o1).iterator();
            Iterator it2 = ((Iterable)o2).iterator();
            for (int i = 0; i < kMaxIterableSize; i++) {
                if (!it1.hasNext() && !it2.hasNext())
                    return true;
                if (!it1.hasNext() || !it2.hasNext())
                    return false;

                if (!equals(it1.next(), it2.next()))
                    return false;
            }

            return false;

        } else {
            return o1.equals(o2);
        }
    }

    static boolean setEquals(Object o1, Object o2) {
        if ((o1 == null) || (o2 == null)) {
            return false;

        } else if ((o1 instanceof Object[]) && (o2 instanceof Object[])) {
            Object[] a1 = (Object[])o1;
            Object[] a2 = (Object[])o2;
            if ((a1.length == 0) || (a2.length == 0))
                return (a1.length == a2.length);

            // Try a SortedSet comparison.
            boolean comparable = true;
            for (int i = 0; i < a1.length; i++)
                if (!(a1[i] instanceof Comparable))
                    comparable = false;
            for (int i = 0; i < a2.length; i++)
                if (!(a2[i] instanceof Comparable))
                    comparable = false;

            if (comparable) {
                TreeSet ts1 = new TreeSet();
                TreeSet ts2 = new TreeSet();
                Collections.addAll(ts1, a1);
                Collections.addAll(ts2, a2);
                if (ts1.equals(ts2))
                    return true;
            }

            // Try a HashSet comparison.
            HashSet<Object> hs1 = new HashSet<Object>();
            HashSet<Object> hs2 = new HashSet<Object>();
            Collections.addAll(hs1, a1);
            Collections.addAll(hs2, a2);
            if (hs1.equals(hs2))
                return true;

        } else if ((o1 instanceof List) && (o2 instanceof List)) {
            List a1 = (List)o1;
            List a2 = (List)o2;
            if ((a1.size() == 0) || (a2.size() == 0))
                return (a1.size() == a2.size());

            // Try a SortedSet comparison.
            boolean comparable = true;
            for (Object o : a1)
                if (!(o instanceof Comparable))
                    comparable = false;
            for (Object o : a2)
                if (!(o instanceof Comparable))
                    comparable = false;

            if (comparable) {
                TreeSet ts1 = new TreeSet(a1);
                TreeSet ts2 = new TreeSet(a2);
                if (ts1.equals(ts2))
                    return true;
            }

            // Try a HashSet comparison.
            HashSet<Object> hs1 = new HashSet<Object>(a1);
            HashSet<Object> hs2 = new HashSet<Object>(a2);
            if (hs1.equals(hs2))
                return true;

        } else if ((o1 instanceof Iterable) && (o2 instanceof Iterable)) {
            Iterable i1 = (Iterable)o1;
            Iterable i2 = (Iterable)o2;

            // Check for equal sizes and if elements are Comparable.
            //
            // NOTE: We stop and return false after kMaxIterableSize
            // elements to prevent non-termination when either
            // iterable generates an infinite stream of elements.
            Iterator it1 = ((Iterable)o1).iterator();
            Iterator it2 = ((Iterable)o2).iterator();
            boolean comparable = true;
            for (int i = 0; i < kMaxIterableSize; i++) {
                if (!it1.hasNext() && !it2.hasNext())
                    break;
                if (!it1.hasNext() || !it2.hasNext())
                    return false;

                if (!(it1.next() instanceof Comparable))
                    comparable = false;
                if (!(it2.next() instanceof Comparable))
                    comparable = false;
            }
            if (it1.hasNext() || it2.hasNext())
                return false;

            // Try a SortedSet comparison.
            if (comparable) {
                TreeSet ts1 = new TreeSet();
                TreeSet ts2 = new TreeSet();
                addAll(ts1, (Iterable)o1);
                addAll(ts2, (Iterable)o2);
                if (ts1.equals(ts2))
                    return true;
            }

            // Try a HashSet comparison.
            HashSet<Object> hs1 = new HashSet<Object>();
            HashSet<Object> hs2 = new HashSet<Object>();
            addAll(hs1, (Iterable)o1);
            addAll(hs2, (Iterable)o2);
            if (hs1.equals(hs2))
                return true;
        }

        return false;
    }

    private static final double tol = 1e-10;

    static boolean approxEqualsFloat(float f1, float f2) {
        return (Math.abs(f1-f2) < tol);
    }

    static boolean approxEqualsDouble(double d1, double d2) {
        return (Math.abs(d1-d2) < tol);
    }

    static boolean approxEquals(Object o1, Object o2) {
        if ((o1 == null) || (o2 == null)) {
            return false;

        } else if ((o1 instanceof float[]) && (o2 instanceof float[])) {
            float[] f1 = (float[])o1;
            float[] f2 = (float[])o2;
            if (f1.length != f2.length)
                return false;
            for (int i = 0; i < f1.length; i++)
                if (!approxEquals(f1[i], f2[i]))
                    return false;
            return true;

        } else if ((o1 instanceof double[]) && (o2 instanceof double[])) {
            double[] d1 = (double[])o1;
            double[] d2 = (double[])o2;
            if (d1.length != d2.length)
                return false;
            for (int i = 0; i < d1.length; i++)
                if (!approxEquals(d1[i], d2[i]))
                    return false;
            return true;

        } else if ((o1 instanceof Double) && (o2 instanceof Double)) {
            return approxEqualsDouble((Double)o1, (Double)o2);

        } else if ((o1 instanceof Float) && (o2 instanceof Float)) {
            return approxEqualsFloat((Float)o1, (Float)o2);

        } else if ((o1 instanceof Object[]) && (o2 instanceof Object[])) {
            // Multi-dimensional arrays.
            Object a1[] = (Object[])o1;
            Object a2[] = (Object[])o2;
            if (a1.length != a2.length)
                return false;
            for (int i = 0; i < a1.length; i++)
                if (!approxEquals(a1[i], a2[i]))
                    return false;
            return true;
        }

        return false;
    }


    static Set<Path> findInputs(Set<Path> paths, MemoryGraph pre, MemoryGraph post) {
        Set<Path> ret = new TreeSet<Path>();

        for (Path p : paths) {
            MemoryGraph.Node n1 = pre.get(p);
            MemoryGraph.Node n2 = post.get(p);
            Object o1 = (n1 != null) ? n1.obj : null;
            Object o2 = (n2 != null) ? n2.obj : null;

            if (equals(o1, o2) || ((n1 == null) && (n2 == null))) {
                ret.add(p);
            }
        }

        return ret;
    }


    static Set<Path> findMissing(Set<Path> paths, MemoryGraph G) {
        Set<Path> ret = new TreeSet<Path>();

        for (Path p : paths) {
            if (G.get(p) == null)
                ret.add(p);
        }

        return ret;
    }

    static Set<Path> findMatches(Object obj1, Set<Path> paths, MemoryGraph G) {
        if (obj1 == null)
            return Collections.emptySet();

        Set<Path> ret = new TreeSet<Path>();

        for (Path p : paths) {
            MemoryGraph.Node n2 = G.get(p);
            Object obj2 = (n2 != null) ? n2.obj : null;

            if ((obj2 != null) && equals(obj1, obj2)) {
                ret.add(p);
            }
        }

        return ret;
    }


    static Set<Predicate> findPredicates(Set<Path> paths, MemoryGraph G, MemoryGraph H) {
        Set<Predicate> ret = new TreeSet<Predicate>();

        for (Path p : paths) {
            MemoryGraph.Node n1 = G.get(p);
            MemoryGraph.Node n2 = H.get(p);
            Object o1 = (n1 != null) ? n1.obj : null;
            Object o2 = (n2 != null) ? n2.obj : null;

            if ((n1 == null) && (n2 == null)) {
                // If the memory is not set in either, count as a match.
                // ret.add(p);

            } else if ((o1 == null) || (o2 == null)) {
                continue;

            } else if (o1 instanceof Class) {
                // Skip class objects -- not interesting to report.
                continue;

            } else {
                if (equals(o1, o2))
                    ret.add(new Predicate(p));

                if (approxEquals(o1, o2))
                    ret.add(new Predicate(p, "Approx"));

                if (setEquals(o1, o2))
                    ret.add(new Predicate(p, "Set"));
            }
        }

        return ret;
    }


    static boolean isFloatingPoint(Object o) {
        if ((o instanceof Float) || (o instanceof Double)
            || (o instanceof float[]) || (o instanceof double[])) {
            return true;
        }

        if (o instanceof Object[]) {
            Object[] a = (Object[])o;
            for (int i = 0; i < a.length; i++) {
                if (!isFloatingPoint(a[i]))
                    return false;
            }
            return true;
        }

        return false;
    }


    static boolean isLinearContainer(Object o) {
        return ((o instanceof Object[])
                || (o instanceof List)
                || (o instanceof Iterable));
    }


    static Set<Predicate> findConjuncts(Set<Path> paths, MemoryGraph G) {
        Set<Predicate> ret = new TreeSet<Predicate>();

        for (Path p : paths) {
            Object obj = G.get(p).obj;  // Should be no NPE's here.
            if (obj == null)
                continue;

            ret.add(new Predicate(p));

            if (isFloatingPoint(obj)) {
                ret.add(new Predicate(p, "Approx"));
            }

            if (isLinearContainer(obj)) {
                ret.add(new Predicate(p, "Set"));
            }
        }

        return ret;
    }


    private static MemoryGraph readMemoryGraph(FileInputStream fis)
        throws ClassNotFoundException, IOException {

        ObjectInputStream ois = new ObjectInputStream(fis);
        return (MemoryGraph)ois.readObject();
    }


    private static int countExecutions(FileInputStream fis)
        throws ClassNotFoundException, IOException {

        int count = 0;
        try {
            while (true) {
                readMemoryGraph(fis);
                readMemoryGraph(fis);
                ++count;
            }
        } catch (IOException e) {
            // OK?
        }

        return count;
    }


    public static void main(String args[]) throws Exception {
        // Process command-line arguments.
        if (args.length < 1) {
            System.out.println("Usage: DeterminismInferenceMain " +
                               "<file> [<depth> <list>]\n");
            return;
        } else if (args.length != 3) {
            FileInputStream fis = new FileInputStream(args[0]);
            int count = countExecutions(fis);
            System.out.println(count + " executions.");
            return;
        }

        final String filename = args[0];
        final int depth = Integer.valueOf(args[1]);
        final TreeSet<Integer> execs = new TreeSet();
        for (String e : args[2].split(",")) {
            execs.add(Integer.valueOf(e));
        }
        final int N = execs.size();

        if (N <= 1) {
            System.out.println("Not enough inputs: " + N);
            return;
        }

        // Read the given pre- and post- states.
        ArrayList<MemoryGraph> preStates = new ArrayList<MemoryGraph>();
        ArrayList<MemoryGraph> postStates = new ArrayList<MemoryGraph>();
        {
            FileInputStream fis = new FileInputStream(filename);
            int skipped = 0;
            int last = 0;
            for (int next : execs) {
                // Skip to next requested execution.
                for (int i = last + 1; i < next; i++) {
                    readMemoryGraph(fis);
                    readMemoryGraph(fis);
                    skipped++;
                }
                // Read the requested execution.
                preStates.add(readMemoryGraph(fis));
                postStates.add(readMemoryGraph(fis));
                last = next;
            }
            System.out.println("Skipped " + skipped + " executions.");
            System.out.println("Read " + N + " executions.");
            fis.close();
        }

        // First, generate a list of all possible paths.
        Set<Path> paths = new TreeSet<Path>();
        for (int i = 0; i < N; i++) {
            paths.addAll(preStates.get(i).generatePaths(depth));
            paths.addAll(postStates.get(i).generatePaths(depth));
        }

        // Find all "input" paths.
        TreeSet<Path> inputs = new TreeSet<Path>(paths);
        for (int i = 0; i < N; i++) {
            inputs.retainAll(findInputs(paths, preStates.get(i), postStates.get(i)));
        }

        // Find pre- and post-paths that are sometimes missing.
        TreeSet<Path> missingPre = new TreeSet<Path>();
        TreeSet<Path> missingPost = new TreeSet<Path>();
        for (int i = 0; i < N; i++) {
            missingPre.addAll(findMissing(paths, preStates.get(i)));
            missingPost.addAll(findMissing(paths, postStates.get(i)));
        }

        // Find all post-paths that are "copies".
        TreeSet<Path> copies = new TreeSet<Path>();
        TreeMap<Path,TreeSet<Path>> copySources = new TreeMap<Path,TreeSet<Path>>();
        for (Path p : paths) {
            if (!missingPost.contains(p) && !inputs.contains(p)) {
                copySources.put(p, new TreeSet<Path>(inputs));
            }
        }
        for (int i = 0; i < N; i++) {
            for (Map.Entry<Path,TreeSet<Path>> e : copySources.entrySet()) {
                Path p = e.getKey();
                MemoryGraph.Node postNode = postStates.get(i).get(p);
                if (postNode != null) {
                    Object postObj = postNode.obj;
                    e.getValue().retainAll
                        (findMatches(postObj, e.getValue(), preStates.get(i)));
                }
            }
        }
        for (Map.Entry<Path,TreeSet<Path>> e : copySources.entrySet()) {
            if (!e.getValue().isEmpty()) {
                copies.add(e.getKey());
            }
        }

        // Prepare paths for comparing pre- and post-states.
        TreeSet<Path> prePaths = new TreeSet<Path>(paths);
        TreeSet<Path> postPaths = new TreeSet<Path>(paths);

        // Eliminate inputs from post-paths.
        System.out.println("INPUTS (" + inputs.size() + "): " + inputs + "\n\n");
        postPaths.removeAll(inputs);

        // Eliminate "missing" pre-paths.
        System.out.println("MISSING PRE: " + missingPre + "\n\n");
        prePaths.removeAll(missingPre);

        // Eliminate "missing" post-paths.
        System.out.println("MISSING POST: " + missingPost + "\n\n");
        postPaths.removeAll(missingPost);

        // Eliminate "copied" post-paths.
        System.out.println("COPIED POST (" + copies.size() + "): " + copies + "\n\n");
        // postPaths.removeAll(copies);

        // Just for diagnostics/statistics, compute set of "possible" conjuncts.
        TreeSet<Predicate> preConjuncts = new TreeSet<Predicate>();
        TreeSet<Predicate> postConjuncts = new TreeSet<Predicate>();
        for (int i = 0; i < N; i++) {
            preConjuncts.addAll(findConjuncts(prePaths, preStates.get(i)));
            postConjuncts.addAll(findConjuncts(postPaths, postStates.get(i)));
        }
        System.out.println("POSSIBLE PRE CONJUNCTS: " + preConjuncts.size());
        System.out.println("POSSIBLE POST CONJUNCTS: " + postConjuncts.size());
        System.out.println();

        // Generate all pairwise comparisons.
        int M = N * (N-1) / 2;
        ArrayList<Set<Predicate>> pres = new ArrayList<Set<Predicate>>();
        ArrayList<Set<Predicate>> posts = new ArrayList<Set<Predicate>>();
        for (int i = 0; i < N; i++) {
            for (int j = i+1; j < N; j++) {
                pres.add(findPredicates(prePaths, preStates.get(i), preStates.get(j)));
                posts.add(findPredicates(postPaths, postStates.get(i), postStates.get(j)));
            }
        }
        System.out.println(M + " pairs.\n\n");
        System.out.println("PRE-PREDICATES: " + union(pres).size());
        System.out.println("POST-PREDICATES: " + union(posts).size());
        System.out.println();

        // Eliminate any pre-predicates true in every single pair.
        //
        // (Either because they are not interesting or because we
        // cannot say anything about them.)
        Set<Predicate> allPres = intersection(pres);
        System.out.println("ALL PRES (" + allPres.size() + "): " + allPres + "\n\n");
        for (Set<Predicate> pre : pres) {
            pre.removeAll(allPres);
        }

        // Eliminate any post-predicates true in every single pair.
        //
        // (Either because they are not interesting or because we
        // cannot say anything about them.)
        Set<Predicate> allPosts = intersection(posts);
        System.out.println("ALL POSTS (" + allPosts.size() + "): " + allPosts + "\n\n");
        for (Set<Predicate> post : posts) {
            post.removeAll(allPosts);
        }

        // Collect the set of occurring pre-predicates as keys in 'sp'.
        HashMap<Set<Predicate>,Set<Predicate>> sp = new HashMap<Set<Predicate>,Set<Predicate>>();
        for (Set<Predicate> pre : pres) {
            sp.put(pre, null);
        }

        // Compute the strongest post-predicate for each pre-predicate.
        for (Map.Entry<Set<Predicate>,Set<Predicate>> e : sp.entrySet()) {
            // Filter the post-predicates observed for this pre-predicate.
            List<Set<Predicate>> seenPosts = new ArrayList<Set<Predicate>>();
            for (int i = 0; i < pres.size(); i++) {
                if (pres.get(i).containsAll(e.getKey())) {
                    seenPosts.add(posts.get(i));
                }
            }
            // Intersect all of the observed posts.
            e.setValue(intersection(seenPosts));
        }

        // Collect non-redundant/non-dominated (pre, sp(pre)) pairs.
        HashMap<Set<Predicate>,Set<Predicate>> inferred = new HashMap<Set<Predicate>,Set<Predicate>>();
        for (Map.Entry<Set<Predicate>,Set<Predicate>> e1 : sp.entrySet()) {
            // Check for dominating entry.
            boolean dominated = false;
            for (Map.Entry<Set<Predicate>,Set<Predicate>> e2 : sp.entrySet()) {
                if (e2.getKey() == e1.getKey())
                    continue;
                if (e1.getKey().containsAll(e2.getKey())
                    && e2.getValue().containsAll(e1.getValue())) {
                    dominated = true;
                    break;
                }
            }
            // Save the pair if it's not dominated.
            if (!dominated) {
                inferred.put(e1.getKey(), e1.getValue());
                System.out.println("********************************\n");
                System.out.println("Pre: " + e1.getKey());
                System.out.println("\n");
                System.out.println("Post: " + e1.getValue());
                System.out.println("\n\n\n");
            }
        }
    }

    static <T> Set<T> intersection(Collection<Set<T>> sets) {
        if (sets.isEmpty())
            return new TreeSet<T>();

        Set<T> ret = new TreeSet<T>(sets.iterator().next());
        for (Set<T> s : sets) {
            ret.retainAll(s);
        }

        return ret;
    }

    static <T> Set<T> union(Collection<Set<T>> sets) {
        if (sets.isEmpty())
            return new TreeSet<T>();

        Set<T> ret = new TreeSet<T>();
        for (Set<T> s : sets) {
            ret.addAll(s);
        }

        return ret;
    }
}
