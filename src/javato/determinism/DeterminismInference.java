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


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class DeterminismInference {

    public static void openDeterministicBlock(String[] localNames,
                                              Object[] localVals) {
        // Capture pre-state.
        captureAndSerializeMemoryGraph(localNames, localVals);
    }


    public static void closeDeterministicBlock(String[] localNames,
                                               Object[] localVals) {
        // Capture post-state.
        captureAndSerializeMemoryGraph(localNames, localVals);
    }


    private static void captureAndSerializeMemoryGraph(String[] localNames,
                                                       Object[] localVals) {

        TreeMap<String,Object> locals = new TreeMap<String,Object>();
        for (int i = 0; i < localNames.length; i++) {
            locals.put(localNames[i], localVals[i]);
        }

        MemoryGraphCapturer mgc = new MemoryGraphCapturer();
        MemoryGraph G = mgc.capture(locals.entrySet());
        try {
            ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream("execs", true));
            oos.writeObject(G);
            oos.close();
        } catch (IOException e) {
            System.err.println("Failed to write captured state.");
            e.printStackTrace();
        }
    }
}
