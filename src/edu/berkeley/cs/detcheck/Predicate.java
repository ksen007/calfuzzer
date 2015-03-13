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

import java.util.Arrays;
import java.util.Iterator;

public interface Predicate {
    public boolean apply(Object a, Object b);

    public static final class Equals implements Predicate {
        public boolean apply(Object a, Object b) {
            return a.equals(b);
        }
    }

    public static final class ArrayEquals implements Predicate {
        public boolean apply(Object a, Object b) {
            // Force Arrays.deepEquals to do all of the heavy lifting.
            return Arrays.deepEquals(new Object[] {a}, new Object[] {b});
        }
    }

    /**
     * Works for a variety of types:
     *  - Float and Double
     *  - float[] and float[][]
     *  - double[] and double[][]
     *  - Iterable<Double>
     *
     * TODO: Make the code more generic, so that it works for
     * arbitrary arrays and Iterables.
     */
    public static final class ApproxEquals implements Predicate {
        private final double tol;

        public ApproxEquals(double tolerance) {
            tol = tolerance;
        }

        public boolean apply(Object a, Object b) {
            if ((a instanceof Double) && (b instanceof Double)) {
                return (Math.abs((Double)a - (Double)b) < tol);
            }

            if ((a instanceof Float) && (b instanceof Float)) {
                return (Math.abs((Float)a - (Float)b) < tol);
            }

            if ((a instanceof Iterable) && (b instanceof Iterable)) {
                Iterator<Double> i = ((Iterable<Double>)a).iterator();
                Iterator<Double> j = ((Iterable<Double>)b).iterator();
                while (i.hasNext() && j.hasNext()) {
                    if (Math.abs(i.next() - j.next()) >= tol)
                        return false;
                }
                return (!i.hasNext() && !j.hasNext());
            }

            if ((a instanceof float[][]) && (b instanceof float[][])) {
                float[][] aa = (float[][])a;
                float[][] bb = (float[][])b;
                if (aa.length != bb.length)
                    return false;
                for (int i = 0; i < aa.length; i++) {
                    if (aa[i].length != bb[i].length)
                        return false;
                    for (int j = 0; j < aa[i].length; j++) {
                        if (Math.abs(aa[i][j] - bb[i][j]) >= tol)
                            return false;
                    }
                }
                return true;
            }

            if ((a instanceof double[]) && (b instanceof double[])) {
                double[] aa = (double[])a;
                double[] bb = (double[])b;
                if (aa.length != bb.length)
                    return false;
                for (int i = 0; i < aa.length; i++) {
                    if (Math.abs(aa[i] - bb[i]) >= tol)
                        return false;
                    }
                return true;
            }

            if ((a instanceof double[][]) && (b instanceof double[][])) {
                double[][] aa = (double[][])a;
                double[][] bb = (double[][])b;
                if (aa.length != bb.length)
                    return false;
                for (int i = 0; i < aa.length; i++) {
                    if (aa[i].length != bb[i].length)
                        return false;
                    for (int j = 0; j < aa[i].length; j++) {
                        if (Math.abs(aa[i][j] - bb[i][j]) >= tol)
                            return false;
                    }
                }
                return true;
            }

            return false;
        }
    }
}
