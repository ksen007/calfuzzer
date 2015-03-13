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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javato.activetesting.determinism.MemoryGraph;
import javato.activetesting.determinism.Path;

public class DeterminismInferenceMain {

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

        } else {
            return o1.equals(o2);
        }
    }


    static Set<Path> findInputs(Set<Path> paths, MemoryGraph pre, MemoryGraph post) {
        Set<Path> ret = new TreeSet<Path>();

        for (Path p : paths) {
            MemoryGraph.Node n1 = pre.get(p);
            MemoryGraph.Node n2 = post.get(p);
            Object o1 = (n1 != null) ? n1.obj : null;
            Object o2 = (n2 != null) ? n2.obj : null;

            if (equals(o1, o2)) {
                ret.add(p);
            }
        }

        return ret;
    }


    static Set<Path> findPredicates(Set<Path> paths, MemoryGraph G, MemoryGraph H) {
        Set<Path> ret = new TreeSet<Path>();

        for (Path p : paths) {
            MemoryGraph.Node n1 = G.get(p);
            MemoryGraph.Node n2 = H.get(p);
            Object o1 = (n1 != null) ? n1.obj : null;
            Object o2 = (n2 != null) ? n2.obj : null;

            if ((n1 == null) && (n2 == null)) {
                // If the memory is not set in either, count as a match.
                ret.add(p);

            } else if ((o1 == null) || (o2 == null)) {
                continue;

            } else if (o1 instanceof Class) {
                // Skip class objects -- not interesting to report.
                continue;

            } else {
                if (equals(o1, o2))
                    ret.add(p);
            }
        }

        return ret;
    }


    static Set<Path> findPredicates(int depth, MemoryGraph G, MemoryGraph H) {
        Set<Path> ret = new TreeSet<Path>();

        Set<Path> paths = G.generatePaths(depth);
        paths.addAll(H.generatePaths(depth));
        for (Path p : paths) {
            MemoryGraph.Node n1 = G.get(p);
            MemoryGraph.Node n2 = H.get(p);
            Object o1 = (n1 != null) ? n1.obj : null;
            Object o2 = (n2 != null) ? n2.obj : null;

            if ((o1 == null) || (o2 == null)) {
                continue;

            } else if (o1.getClass() != o2.getClass()) {
                continue;

            } else if (o1 instanceof Class) {
                // Skip class objects -- not interesting to report.
                continue;

            } else if ((o1 instanceof Object[]) && (o2 instanceof Object[])) {
                if (Arrays.deepEquals((Object[])o1, (Object[])o2))
                    ret.add(p);

            } else if ((o1 instanceof float[]) && (o2 instanceof float[])) {
                if (Arrays.equals((float[])o1, (float[])o2))
                    ret.add(p);

            } else if ((o1 instanceof double[]) && (o2 instanceof double[])) {
                if (Arrays.equals((double[])o1, (double[])o2))
                    ret.add(p);

            } else if ((o1 instanceof int[]) && (o2 instanceof int[])) {
                if (Arrays.equals((int[])o1, (int[])o2))
                    ret.add(p);

            } else {
                if (o1.equals(o2))
                    ret.add(p);
            }
        }

        return ret;
    }


    public static void findAndPrintFacts(MemoryGraph G, MemoryGraph H) {
        // Get all possible paths.
        Set<Path> paths = G.generatePaths(2);
        paths.addAll(H.generatePaths(2));

        for (Path p : paths) {
            MemoryGraph.Node n1 = G.get(p);
            MemoryGraph.Node n2 = H.get(p);
            Object o1 = (n1 != null) ? n1.obj : null;
            Object o2 = (n2 != null) ? n2.obj : null;

            if ((o1 == null) && (o2 == null)) {
                System.out.println(p + ": both null");

            } else if ((o1 == null) || (o2 == null)) {
                System.out.println(p + ": one null");

            } else if (o1.getClass() != o2.getClass()) {
                System.out.println(p + ": different types");
                System.out.println("    " + o1.getClass());
                System.out.println("    " + o2.getClass());

            } else if ((o1 instanceof Object[]) && (o2 instanceof Object[])) {
                System.out.println(p + " (array): "
                                   + Arrays.deepEquals((Object[])o1, (Object[])o2));

            } else if ((o1 instanceof float[]) && (o2 instanceof float[])) {
                System.out.println(p + " (float[]): "
                                   + Arrays.equals((float[])o1, (float[])o2));

            } else if ((o1 instanceof double[]) && (o2 instanceof double[])) {
                System.out.println(p + " (double[]): "
                                   + Arrays.equals((double[])o1, (double[])o2));

            } else if ((o1 instanceof int[]) && (o2 instanceof int[])) {
                System.out.println(p + " (int[]): "
                                   + Arrays.equals((int[])o1, (int[])o2));

            } else {
                System.out.println(p + ": " + o1.equals(o2));
            }
        }
    }


    private static MemoryGraph readMemoryGraph(FileInputStream fis)
        throws ClassNotFoundException, IOException {

        ObjectInputStream ois = new ObjectInputStream(fis);
        return (MemoryGraph)ois.readObject();
    }


    public static void main(String args[]) throws Exception {
        // Process command-line arguments.
        if (args.length != 2) {
            System.out.println("Usage: DeterminismInferenceMain <file> <depth>.\n");
            return;
        }
        final String filename = args[0];
        final int depth = Integer.valueOf(args[1]);

        // First, generate a list of all possible paths.
        Set<Path> paths = new TreeSet<Path>();
        FileInputStream fis = new FileInputStream(filename);
        try {
            while (true) {
                paths.addAll(readMemoryGraph(fis).generatePaths(depth));
            }
        } catch (IOException e) {
            // End of file -- fine.
        } finally {
            fis.close();
        }


        // Read all exec pairs and find possible pre- and post-predicates.
        ArrayList<Set<Path>> pres = new ArrayList<Set<Path>>();
        ArrayList<Set<Path>> posts = new ArrayList<Set<Path>>();
        Set<Path> inputs = new TreeSet<Path>(paths);
        fis = new FileInputStream(filename);
        int count = 0;
        while (true) {
             // Read a pair of executions.
            MemoryGraph pre1, post1, pre2, post2;
            try {
                pre1 = readMemoryGraph(fis);
                post1 = readMemoryGraph(fis);
                pre2 = readMemoryGraph(fis);
                post2 = readMemoryGraph(fis);
            } catch (IOException e) {
                // End of file -- fine.
                break;
            }

            count++;

            inputs.retainAll(findInputs(paths, pre1, post1));
            inputs.retainAll(findInputs(paths, pre1, post1));

            pres.add(findPredicates(paths, pre1, pre2));
            posts.add(findPredicates(paths, post1, post2));
        }
        fis.close();

        System.out.println(count + " pairs.\n\n");

        if (pres.size() == 0) {
            System.out.println("No execution pairs!\n");
            return;
        }

        // Eliminate any post-predicates involving inputs.
        System.out.println("INPUTS: " + inputs + "\n\n");
        for (Set<Path> post : posts) {
            post.removeAll(inputs);
        }

        // Eliminate any pre-predicates true in every single pair.
        //
        // (Either because they are not interesting or because we
        // cannot say anything about them.)
        Set<Path> allPres = intersection(pres);
        System.out.println("ALL PRES: " + allPres + "\n\n");
        for (Set<Path> pre : pres) {
            pre.removeAll(allPres);
        }

        // Eliminate any post-predicates true in every single pair.
        //
        // (Either because they are not interesting or because we
        // cannot say anything about them.)
        Set<Path> allPosts = intersection(posts);
        System.out.println("ALL POSTS: " + allPosts + "\n\n");
        for (Set<Path> post : posts) {
            post.removeAll(allPosts);
        }

        // Collect the set of occurring pre-predicates as keys in 'sp'.
        HashMap<Set<Path>,Set<Path>> sp = new HashMap<Set<Path>,Set<Path>>();
        for (Set<Path> pre : pres) {
            sp.put(pre, null);
        }

        // Compute the strongest post-predicate for each pre-predicate.
        for (Map.Entry<Set<Path>,Set<Path>> e : sp.entrySet()) {
            // Filter the post-predicates observed for this pre-predicate.
            List<Set<Path>> seenPosts = new ArrayList<Set<Path>>();
            for (int i = 0; i < pres.size(); i++) {
                if (pres.get(i).containsAll(e.getKey())) {
                    seenPosts.add(posts.get(i));
                }
            }
            // Intersect all of the observed posts.
            e.setValue(intersection(seenPosts));
        }

        // Collect non-redundant/non-dominated (pre, sp(pre)) pairs.
        HashMap<Set<Path>,Set<Path>> inferred = new HashMap<Set<Path>,Set<Path>>();
        for (Map.Entry<Set<Path>,Set<Path>> e1 : sp.entrySet()) {
            // Check for dominating entry.
            boolean dominated = false;
            for (Map.Entry<Set<Path>,Set<Path>> e2 : sp.entrySet()) {
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

}