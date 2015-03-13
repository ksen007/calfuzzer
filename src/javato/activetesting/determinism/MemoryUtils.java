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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class MemoryUtils {

    /**
     * Returns true if the given object can be serialized.
     *
     * For most objects, we simply check if the object implements the
     * Serializable interface.  However, arrays and
     * java.util.Collections implement Serializable but cannot always
     * actually be serialize.  We have to recursively check that the
     * component objects are themselves serializable.  (Or, we could
     * just serialize and then de-serialize the object and see what
     * happens.)
     */
    public static boolean isSerializable(Object obj) {
        if (obj == null)
            return true;

        return isSerializable(new IdentityHashMap<Object,Boolean>(), obj);
    }

    private static boolean isSerializable(IdentityHashMap<Object,Boolean> seen,
                                          Object obj) {
        // Cycles in the reference graph do not prevent serialization.
        if (seen.containsKey(obj) || (obj == null))
            return true;
        seen.put(obj, Boolean.TRUE);

        if (!(obj instanceof Serializable)) {
            return false;

        } else if (obj instanceof java.util.Collection) {
            // Many Collections implement Serializable, but
            // serialization will still fail unless all contained
            // objects are serializable, too.
            for (Object o : (Collection)obj) {
                if (!isSerializable(seen, o))
                    return false;
            }

        } else if (obj instanceof java.util.Map) {
            // Many Maps implement Serializable, but serialization
            // will still fail unless all contained keys and values
            // are serializable, too.
            for (Map.Entry<?,?> e : ((Map<?,?>)obj).entrySet()) {
                if (!isSerializable(seen, e.getKey())
                    || !isSerializable(seen, e.getValue()))
                    return false;
            }

        } else if (obj.getClass().isArray()) {
            // All arrays implement Serializable, but serialization
            // will still fail unless the array elements are
            // serializable, too.
            //
            // TODO: For speed, should maybe short-circuit here if the
            // component type is a primitive.
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (!isSerializable(seen, Array.get(obj, i)))
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns true if calling equals() on the given object will
     * perform a semantic equality check.  For arrays, returns true if
     * calling Arrays.deepEquals() checks semantic equality.
     *
     * NOTE: It is not possible in general to tell whether an equals()
     * method performs a semantic equality check.  All we check here
     * is that the object (and, in some circumstances, objects it
     * contains) override the default Object.equals() method.
     *
     * NOTE: It should typically be safe to, rather than calling this
     * method, just go ahead and call equals().  Any non-semantic
     * equals() methods will just return false on deserialized
     * instances.  (One exception is Collections which contain
     * themselves, which can cause equals() to throw a
     * StackOverflowError.)
     */
    public static boolean hasSemanticEquality(Object obj) {
        try {
            return hasSemanticEquality(new IdentityHashMap<Object,Boolean>(), obj);
        } catch (NoSuchMethodException e) {
            System.err.println("Impossible.");
            e.printStackTrace();
        } catch (SecurityException e) {
            System.err.println("Impossible.");
            e.printStackTrace();
        }

        return false;
    }

    private static boolean hasSemanticEquality(IdentityHashMap<Object,Boolean> seen,
                                               Object obj)
        throws NoSuchMethodException, SecurityException
    {
        // Typically, equals() makes no sense on cyclic references.
        if (seen.containsKey(obj))
            return false;
        seen.put(obj, Boolean.TRUE);

        // Grab equals method for checking later.
        Method equals = obj.getClass().getMethod("equals", Object.class);

        if (obj.getClass().isArray()) {
            // Arrays do not provide a semantic equality method.
            // However, we instead check here whether
            // Arrays.deepEquals provides semantic equality, by
            // recursive checking all contained objects.
            //
            // TODO: For speed, should maybe short-circuit here if the
            // component type is a primitive.
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                if (!hasSemanticEquality(seen, Array.get(obj, i)))
                    return false;
            }

        } else if (equals.getDeclaringClass() == Object.class) {
            // obj does not override Object.equals.
            //
            // TODO: Does this work in the presence of multiple
            // classloaders?
            return false;

        } else if (obj instanceof java.util.Collection) {
            // Many Collection overrides equals(), but only compute
            // semantic equality if all contained objects provide
            // a semantic equality check.
            for (Object o : (Collection)obj) {
                if (!hasSemanticEquality(seen, o))
                    return false;
            }

        } else if (obj instanceof java.util.Map) {
            // Many Maps overrides equals(), but only compute semantic
            // equality if all contained keys and values provide a
            // semantic equality check.
            for (Map.Entry<?,?> e : ((Map<?,?>)obj).entrySet()) {
                if (!hasSemanticEquality(seen, e.getKey())
                    || !hasSemanticEquality(seen, e.getValue()))
                    return false;
            }
        }

        return true;
    }
}
