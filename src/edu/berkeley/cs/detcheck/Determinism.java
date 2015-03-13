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

package edu.berkeley.cs.detcheck;

import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Determinism {

    public static final boolean doCheck =
        Boolean.getBoolean("edu.berkeley.cs.detcheck.check");

    public static final String pairsFileName =
        System.getProperty("edu.cs.berkeley.cs.detcheck.file", "pairs");

    public static final int kMaxMatchingPairsToSave =
        Integer.getInteger("edu.berkeley.cs.detcheck.max-saved-pairs", 3);


    /**
     * Opens a deterministic block.
     */
    public static void openDeterministicBlock() {
        if (!doCheck) {
            return;
        }

        synchronized (Determinism.class) {

            // Open a new block.
            Block block = new Block(++numBlocks,
                                    curBlock.get(),
                                    new PrePostPair(getLocation()));
            blocks.put(block.id, block);

            // Update the current/active block for this thread.
            curBlock.set(block.id);

        }
    }


    /**
     * Close the enclosing deterministic block.
     */
    public static void closeDeterministicBlock() {
        if (!doCheck) {
            return;
        }

        synchronized (Determinism.class) {

            // Get the current block.
            Block block = blocks.get(curBlock.get());
            if (block == null) {
                throw new IllegalStateException("No open block.");
            }

            /*
            System.err.println("\nClosing block " + block.id + ":");
            System.err.println("    parent:   " + block.parent);
            System.err.println("    loc:      " + block.pair.openLoc);
            System.err.println("    pres:     " + block.pair.pres);
            System.err.println("    posts:    " + block.pair.posts);
            System.err.println("    matches:  " + block.preMatchingPairs.size());
            System.err.println();
            */

            // Save the pre-post pair.
            //
            // Optimization: Don't save this pre-post pair if we have
            // already seen some number of matching pairs.
            if (block.preMatchingPairs.size() < kMaxMatchingPairsToSave) {
                writePair(block.pair);
            }

            // Make our block ID point to our parent block, so that all
            // threads spawned in this block will now be associated with
            // the enclosing block.
            blocks.put(block.id, blocks.get(block.parent));

            // Replace the current block with its parent.
            curBlock.set(block.parent);
        }
    }


    /**
     * Specifies precondition "preState.equals(preState')" for
     * the enclosing deterministic block.
     *
     * Parameter 'preState' must be serializable.
     */
    public static void requireDeterministic(Object preState) {
        if (!doCheck) {
            return;
        }
        requireDeterministic(preState, new Predicate.Equals());
    }


    /**
     * Specifies precondition "prePred.apply(preState, preState')" for
     * the enclosing deterministic block.
     *
     * Parameter 'preState' must be serializable.
     */
    public static void requireDeterministic(Object preState,
                                            Predicate prePred) {
        if (!doCheck) {
            return;
        }

        synchronized (Determinism.class) {

            // Get the current block.
            Block block = blocks.get(curBlock.get());
            if (block == null) {
                throw new IllegalStateException("No open block.");
            }

            block.pair.pres.add(serializedCopy((Serializable)preState));
            block.prePreds.add(prePred);
        }
    }


    /**
     * Asserts postcondition "postState.equals(postState')" for
     * the enclosing deterministic block.
     *
     * Parameter 'postState' must be serializable.
     */
    public static void assertDeterministic(Object postState) {
        if (!doCheck) {
            return;
        }
        assertDeterministic(postState, new Predicate.Equals());
    }


    /**
     * Asserts postcondition "postPred.apply(postState, postState')"
     * for the enclosing deterministic block.
     *
     * Parameter 'postState' must be serializable.
     */
    public static void assertDeterministic(Object postState, Predicate pred) {
        if (!doCheck) {
            return;
        }

        synchronized (Determinism.class) {

            // Get the current block.
            Block block = blocks.get(curBlock.get());
            if (block == null) {
                throw new IllegalStateException("No open block.");
            }

            // If we haven't done it yet, read all previous pre-post pairs
            // and find those with matching pre-states.
            if (block.pair.posts.size() == 0) {
                List<PrePostPair> pairs = readPairs();

                // Find matches.
                for (PrePostPair p : pairs) {
                    if (block.pair.preMatches(block.prePreds, p))
                        block.preMatchingPairs.add(p);
                }

                System.err.println("Found " + block.preMatchingPairs.size()
                                   + " matching pairs.");
                System.err.println(block.pair.openLoc);
            }

            // Check post-state against matching pairs.
            int i = block.pair.posts.size();
            for (PrePostPair p : block.preMatchingPairs) {
                if (p.posts.size() < i) {
                    System.err.println("Determinism violation:\n");
                    System.err.println(print(postState) + "\n");
                    System.err.println("[NO MATCHING POST-STATE]\n");
                    Thread.dumpStack();
                    System.exit(1);
                } else if (!pred.apply(postState, p.posts.get(i))) {
                    System.err.println("Determinism violation:\n");
                    System.err.println(print(postState) + "\n");
                    System.err.println(print(p.posts.get(i)) + "\n");
                    Thread.dumpStack();
                    System.exit(1);
                }
            }

            // Save the post-state.
            block.pair.posts.add(serializedCopy((Serializable)postState));
        }
    }


    /**
     * A Location object encapsulate a location in a source file
     * (e.g. of the opening of a deterministic block).
     *
     * Location objects are immutable.
     */
    static class Location implements Serializable {
        public String fileName;
        public int lineNumber;

        public Location(String fileName, int lineNumber) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public int hashCode() {
            return 1231*fileName.hashCode() + 1877*lineNumber;
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;

            if ((o == null) || (o.getClass() != getClass()))
                return false;

            Location ol = (Location)o;
            return (fileName.equals(ol.fileName)
                    && (lineNumber == ol.lineNumber));
        }

        public String toString() {
            return fileName + "#" + lineNumber;
        }
    }


    /**
     * A PrePostPair object encapsulates the pre- and post-states
     * associated with an executed deterministic block.
     */
    static class PrePostPair implements Serializable {

        /** The source location at which the block was opened. */
        public Location openLoc;

        /**
         * The serialized pre-state projections passed to
         * requireDeterministic.
         */
        public List<Serializable> pres;

        /**
         * The serialized post-state projections passed to
         * assertDeterministic.
         */
        public List<Serializable> posts;

        public PrePostPair(Location openLoc) {
            this.openLoc = openLoc;
            this.pres = new ArrayList<Serializable>();
            this.posts = new ArrayList<Serializable>();
        }

        /**
         * Determines whether the given bridge predicates are
         * satisfied by the pre-states of 'this' and PrePostPair 'o'.
         *
         * The predicates in argument 'preds' should be thos from the
         * corresponding calls to requireDeterministic in the relevant
         * deterministic block.
         */
        public boolean preMatches(List<Predicate> preds, PrePostPair o) {
            if (!openLoc.equals(o.openLoc))
                return false;

            if (o.pres.size() != pres.size())
                return false;

            if (preds.size() != pres.size())
                return false;

            for (int i = 0; i < preds.size(); i++) {
                if (!preds.get(i).apply(pres.get(i), o.pres.get(i)))
                    return false;
            }

            return true;
        }
    }


    /**
     * A Block object holds all of the associated with
     * currently-executing deterministic block.
     */
    static class Block {
        /** The id number of the block. */
        public final int id;

        /**
         * The id number of the parent of this block, or 0 if there is
         * no parent.
         */
        public final int parent;

        /**
         * Recorded pre- and post-states of the block.
         */
        public final PrePostPair pair;

        /**
         * Bridge predicates passed to requireDeterministic() calls in
         * the block.
         */
        public final List<Predicate> prePreds;

        /**
         * Pre- and post-states from previously executed deterministic
         * blocks, where the pre-states match this objects pre-state.
         *
         * Populated lazily on the first call to assertDeterministic.
         */
        public final List<PrePostPair> preMatchingPairs;

        public Block(int id, int parent, PrePostPair pair) {
            this.id = id;
            this.parent = parent;
            this.pair = pair;
            this.prePreds = new ArrayList<Predicate>();
            this.preMatchingPairs = new ArrayList<PrePostPair>();
        }
    }


    /*
     * Returns the source location of the nearest enclosing call from
     * outside of the Determinism class.
     *
     * Must be called from a static Determinism method which has been
     * called from application code (i.e. code in another class).
     */
    private static Location getLocation() {
        String thisClassName = Determinism.class.getName();

        // Terrible hack here to find right entry in the stack trace.
        StackTraceElement st[] = Thread.currentThread().getStackTrace();
        int i = st.length - 1;
        while (st[i].getClassName().equals(thisClassName)
               || st[i].getClassName().startsWith("java.lang.")) {
            --i;
        }

        return new Location(st[i].getFileName(), st[i].getLineNumber());
    }


    private static int numBlocks = 0;

    private static Map<Integer,Block> blocks = new HashMap<Integer,Block>();

    private static InheritableThreadLocal<Integer> curBlock =
        new InheritableThreadLocal<Integer>() {
          @Override protected Integer initialValue() {
                return 0;
            }
        };


    private static List<PrePostPair> readPairs() {
        List<PrePostPair> ret = new ArrayList<PrePostPair>();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(pairsFileName);
            while (true) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                ret.add((PrePostPair)ois.readObject());
            }
        } catch (IOException e) {
            // Fine? (No way to detect EOF above.)
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to read old pre- and post-states: " + e);
            System.exit(1);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) { }
            }
        }

        return ret;
    }


    private static void writePair(PrePostPair pair) {
        try {
            ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(pairsFileName, true));
            oos.writeObject(pair);
            oos.close();
        } catch (IOException e) {
            System.err.println("Error logging pre- and post-state: " + e);
            System.exit(1);
        }
    }


    private static Serializable serializedCopy(Serializable o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            byte[] serialized = baos.toByteArray();

            ObjectInputStream ois =
                new ObjectInputStream(new ByteArrayInputStream(serialized));
            try {
                return (Serializable)ois.readObject();
            } catch (ClassNotFoundException e) {
                System.err.println("Impossible: " + e);
                System.exit(1);
            }

        } catch (IOException e) {
            System.err.println("Failed to serialize pre-state: " + e);
            System.exit(1);
        }

        System.err.println("Impossible.");
        System.exit(1);
        return null;
    }

    private static String print(Object o) {
        // This is stupid, but easier than using reflection to decide
        // which of Arrays.toString() or Arrays.deepToString() should
        // be called directly on 'o'.
        return Arrays.deepToString(new Object[] {o});
    }
}
