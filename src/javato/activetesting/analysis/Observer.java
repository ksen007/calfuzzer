package javato.activetesting.analysis;

import javato.activetesting.common.Parameters;
import javato.activetesting.common.WeakIdentityHashMap;

import java.io.*;
import java.util.ArrayList;

/**
 * Copyright (c) 2007-2008,
 * Koushik Sen    <ksen@cs.berkeley.edu>
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
public class Observer {

    private static WeakIdentityHashMap objectMap = new WeakIdentityHashMap(3511);
    private static int currentId = readInteger(Parameters.usedObjectId, 1);
    private static ArrayList<String> iidToLineMap = null;

    public static Long idInt(int f, int s) {
        long l = f;
        l = l << 32;
        l += s;
        return l;
    }

    public static String getIidToLine(Integer iid) {
        ObjectInputStream in;
        if (iidToLineMap != null) {
            return iidToLineMap.get(iid).replaceAll(".html#", "#");
        } else {
            try {
                in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(Parameters.iidToLineMapFile)));
                iidToLineMap = (ArrayList<String>) in.readObject();
                in.close();
                return iidToLineMap.get(iid).replaceAll(".html#", "#");
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    synchronized public static Integer uniqueId(Object o) {
        Object val = objectMap.get(o);
        if (val == null) {
            val = currentId++;
            objectMap.put(o, val);
        }
        return (Integer) val;
    }

    synchronized public static Object idToObject(int id) {
        for (Object ret : objectMap.keySet()) {
            Integer val = (Integer) objectMap.get(ret);
            if (val != null) {
                if (val == id)
                    return ret;
            }
        }
        return "Unknown Object";
    }

    public static Long id(Object o, int x) {
        return idInt(uniqueId(o), x);
    }

    static public int readInteger(String filename, int defaultVal) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            int ret = Integer.parseInt(in.readLine());
            in.close();
            return ret;
        } catch (Exception e) {
        }
        return 0;
    }

    public static void writeIntegerList(String file, int val) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(file));
            for (int i = 1; i < val; i++) {
                pw.print(i + ",");
            }
            if (val > 0)
                pw.println(val);
            else
                pw.println();
            pw.close();
        } catch (IOException e) {
            System.err.println("Error while writing to " + file);
            System.exit(1);
        }

    }
}
