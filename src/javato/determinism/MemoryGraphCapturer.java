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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

import static javato.determinism.MemoryGraph.Node;

public class MemoryGraphCapturer {

    public MemoryGraphCapturer() {
        G = null;
        objNodes = new IdentityHashMap<Object,Node>();
        Q = new LinkedList<Node>();
    }

    public MemoryGraph capture(Set<Map.Entry<String,Object>> localRoots) {
        System.out.println(localRoots);

        if (G != null)
            return G;
        G = new MemoryGraph();

        // Initialize root set and work queue with local variables.
        for (Map.Entry<String,Object> e : localRoots) {
            if (e.getValue() != null) {
                Node n = add(e.getValue());
                G.root.fields.put(e.getKey(), n);
            }
        }

        // Also add static fields of app classes to root set / work queue.
        for (Class<?> c : getLoadedClasses()) {
            if (!isApplicationClass(c))
                continue;

            // Add class object to the root set.
            Node cn = add(c);
            G.root.fields.put(c.getName(), cn);

            // Add edges from class object for its static fields.
            // (It's safe to use getDeclaredFields() here because we
            // only want static fields, which cannot be inherited.)
            for (Field f : c.getDeclaredFields()) {
                if (isStaticField(f)) {
                    Object obj = getStaticFieldValue(f);
                    if (obj != null) {
                        Node n = add(obj);
                        cn.fields.put(f.getName(), n);
                    }
                }
            }
        }

        // Breadth-first search through the memory graph.
        while (!isQueueEmpty()) {
            Node n = popQueue();
            Class<?> cls = n.obj.getClass();

            if (n.obj.getClass().isArray()) {
                // Special handling for arrays.
                //
                // NOTE: We skip large arrays, as it is wasteful and
                // typically not productive to check for equality
                // individual pairs of elements from huge data arrays.
                int len = Array.getLength(n.obj);
                if (len <= 10) {
                    for (int i = 0; i < len; i++) {
                        Object obj = Array.get(n.obj, i);
                        if (obj != null) {
                            Node m = add(obj);
                            n.fields.put("[" + i + "]", m);
                        }
                    }
                }

                // Add array length as a field.
                n.fields.put("length", add(len));

            } else if (!isApplicationClass(cls)) {
                // Skip normal processing for non-application classes.

                // Special handling of AtomicX classes -- treat get()
                // method as a field.
                if (n.obj instanceof AtomicInteger) {
                    n.fields.put("get()", add(((AtomicInteger)n.obj).get()));
                } else if (n.obj instanceof AtomicLong) {
                    n.fields.put("get()", add(((AtomicLong)n.obj).get()));
                } else if (n.obj instanceof AtomicBoolean) {
                    n.fields.put("get()", add(((AtomicBoolean)n.obj).get()));
                } else if (n.obj instanceof AtomicReference) {
                    Object obj = ((AtomicReference)n.obj).get();
                    if (obj != null) {
                        n.fields.put("get()", add(obj));
                    }
                } else if (n.obj instanceof AtomicMarkableReference) {
                    Object obj = ((AtomicMarkableReference)n.obj).getReference();
                    if (obj != null) {
                        n.fields.put("get()", add(obj));
                    }
                } else if (n.obj instanceof AtomicStampedReference) {
                    Object obj = ((AtomicStampedReference)n.obj).getReference();
                    if (obj != null) {
                        n.fields.put("getReference()", add(obj));
                    }
                }

            } else {
                // Add edges for each field.
                for (Field f : getFields(cls)) {
                    if (!isStaticField(f)) {
                        Object obj = getInstanceFieldValue(f, n.obj);
                        if (obj != null) {
                            Node m = add(obj);
                            n.fields.put(f.getName(), m);
                        }
                    }
                }
            }
        }

        removeNonSerializableRefs(G);
        return G;
    }

    private void removeNonSerializableRefs(MemoryGraph G) {
        for (Node n : objNodes.values()) {
            if (n.obj != null) {
                if (!MemoryUtils.isSerializable(n.obj)) {
                    n.obj = null;
                }
            }
        }
    }

    private boolean isQueueEmpty() {
        return Q.isEmpty();
    }

    private Node popQueue() {
        return Q.remove();
    }

    private Node add(Object o) {
        Node n = objNodes.get(o);

        if (n == null) {
            n = new Node(o);
            objNodes.put(o, n);
            Q.add(n);
        }

        return n;
    }

    private Iterable<Field> getFields(Class<?> cls) {
        HashSet<Field> fields = new HashSet<Field>();
        Collections.addAll(fields, cls.getFields());
        while ((cls != Object.class) && (cls != null)) {
            Collections.addAll(fields, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }
        return fields;
    }

    private Object getStaticFieldValue(Field f) {
        try {
            f.setAccessible(true);
            return f.get(null);
        } catch (SecurityException e) {
            System.err.println("Failed to access static field "
                               + f.getName() + " of "
                               + f.getDeclaringClass().getName());
        } catch (IllegalAccessException e) {
            System.err.println("Impossible!");
            e.printStackTrace();
        }
        return null;
    }

    private Object getInstanceFieldValue(Field f, Object o) {
        try {
            f.setAccessible(true);
            return f.get(o);
        } catch (SecurityException e) {
            System.err.println("Failed to access instance field "
                               + f.getName() + " of " + o
                               + " of type " + o.getClass().getName());
        } catch (IllegalAccessException e) {
            System.err.println("Impossible!");
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isApplicationClass(Class<?> c) {
        String name = c.getName();
        return !(name.startsWith("java.") || name.startsWith("javax.")
                 || name.startsWith("javato.") || name.startsWith("org.junit.")
                 || name.startsWith("junit.") || name.startsWith("sun.")
                 || name.startsWith("edu.berkeley.cs.detcheck."));
    }

    private static boolean isStaticField(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }

    private static Iterable<Class<?>> getLoadedClasses() {
        // Hacky magic, from: Ted Neward, "Know What You're Executing:
        // Finding a list of of All Loaded Classes".
        //
        // Note: This is totally not thread-safe.
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Field f = java.lang.ClassLoader.class.getDeclaredField("classes");
            f.setAccessible(true);
            return new ArrayList<Class<?>>((Vector)f.get(cl));
        } catch (SecurityException e) {
            System.err.println("Failed to generate list of loaded classes.");
        } catch (NoSuchFieldException e) {
            System.err.println("Could not find 'classes' field in ClassLoader.");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            System.err.println("Impossible.");
            e.printStackTrace();
        }
        System.exit(1);
        return Collections.emptyList();
    }

    private MemoryGraph G;
    private IdentityHashMap<Object,Node> objNodes;
    private Queue<Node> Q;
}
