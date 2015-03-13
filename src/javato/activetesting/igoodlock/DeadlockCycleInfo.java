package javato.activetesting.igoodlock;

import javato.activetesting.common.Parameters;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
public class DeadlockCycleInfo implements Serializable {
    // list of cycles
    // cycle is a list of Node
    List<List<Node>> cycles;
    private List<Node> current;

    public DeadlockCycleInfo(int nCycles) {
        cycles = new ArrayList<List<Node>>(nCycles);
    }

    public static DeadlockCycleInfo read() {
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(Parameters.ERROR_LOG_FILE)));
            DeadlockCycleInfo cycle = (DeadlockCycleInfo) in.readObject();
            in.close();
            return cycle;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int write() {
        ObjectOutputStream out;
        System.out.println("# of deadlocks detected " + cycles.size());
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(Parameters.ERROR_LOG_FILE)));
            out.writeObject(this);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cycles.size();
    }

    // parkcs: renaming addANode to addALockNode for compatibility with other types of "lock nodes"
    public void addALockNode(LockNode old) {
        Node tmp = new Node(old.getContext());
        current.add(tmp);
    }

    // parkcs: addANode adds a javato.deadlockCommon.Node now
    public void addANode(Node node) {
        current.add(node);
    }

    public void addACycle() {
        current = new LinkedList<Node>();
        cycles.add(current);
    }

    public List<List<Node>> getCycles() {
        return cycles;
    }
}
