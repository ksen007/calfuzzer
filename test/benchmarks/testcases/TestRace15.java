package benchmarks.testcases;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Copyright (c) 2006-2009,
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
public class TestRace15 {
    static float x = 23.45f;
    public static int N = 1;

    public synchronized float getX() {
        return x;
    }

    public synchronized void setX(float x) {
        TestRace15.x = x;
    }

    private void mult() {
        //System.out.print("*");
        setX(getX()*1.52f);
    }

    private void add() {
        //System.out.print("+");
        setX(getX()+12.36f);
    }

    private void div() {
        //System.out.print("/");
        setX(getX()/1.21f);
    }

    public void test2 () throws InterruptedException {


        Thread t1 = new Thread("Star1") {
            public void run() {
                for (int i=0; i<N; i++) {
                    mult();
                }
                System.out.println("end Star1");
            }
        };
        Thread t2 = new Thread("Star2") {
            public void run() {
                for (int i=0; i<N; i++) {
                    div();
                }
                System.out.println("end Star2");
            }
        };
        t1.start();

        t2.start();
        for (int i=0; i<N; i++) {
            add();
        }
        System.out.println("before join 2");
        t2.join();
        System.out.println("before join 1");
        t1.join();
        System.out.println("");
        System.out.println(x);

    }

    public static void main(String[] args) throws InterruptedException, IOException {
        TestRace15.N = 3;
        (new TestRace15()).test2();
        PrintWriter pw = new PrintWriter(new FileWriter("error.stat",true));
        pw.println(TestRace15.x);
        pw.close();
    }
}
