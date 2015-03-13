package javato.activetesting.threadrepro;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

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
public class RaceBreakpointTest {
    public int x;
    public int y;
    public Object lock;
    public boolean cond;

    @Before
    public void setUp() {
        x = 0;
        y = 100;
        lock = new Object();
        cond = true;
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void race1() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                synchronized (lock) {
                    lock.notify();
                    cond = false;
                }
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(1);
                int t = x;
                tc.tick();
                tc.waitForTick(3);
                t++;
                x = t;
                tc.tick();
            }
        };
        t1.start();
        Thread.sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(2);
        int t = x;
        tc.tick();
        t++;
        x = t;
        synchronized (lock) {
            if (cond) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        t1.join();
        assertTrue("value of x ", x==1);
    }

    @Test
    public void simpleRace() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                int t = x;
                t++;
                x = t;
            }
        };
        t1.start();
        sleep(10);
        int t = x;
        t++;
        x=t;
        t1.join();
        assertTrue("value of x ",x==2);
    }


    @Test
    public void simpleRaceGuided3() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(2);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                x = t;
                tc.tick();
            }
        };
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(1);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(3);
        x=t;
        tc.tick();
        t1.join();
        assertTrue("value of x ",x==5);
    }

    @Test
    public void simpleRaceGuided4() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(1);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(3);
                x = t;
                tc.tick();
            }
        };
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(2);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(4);
        x=t;
        tc.tick();
        t1.join();
        assertTrue("value of x "+x,x==1);
    }

    @Test
    public void simpleRaceGuided0() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(false);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false);
                x = t;
                tc.tick();
            }
        };
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(true);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false);
        x=t;
        tc.tick();
        t1.join();
        assertTrue("value of x "+x,x==5);
    }

    @Test
    public void simpleRaceGuided1() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(false,0);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false,0);
                x = t;
                tc.tick();
            }
        };
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(true,0);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false,0);
        x=t;
        tc.tick();
        t1.join();
        assertTrue("value of x "+x,x==5);
    }

    @Test
    public void simpleRaceGuided2() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(true);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false);
                x = t;
                tc.tick();
            }
        };
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(false);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false);
        x=t;
        tc.tick();
        t1.join();
        assertTrue("value of x "+x,x==1);
    }

    @Test
    public void simpleRaceGuided5() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(false);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false);
                x = t;
                tc.tick();
            }
        };
        t1.start();
        sleep(2000);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(true);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false);
        x=t;
        tc.tick();
        t1.join();
        assertTrue("value of x "+x,x==6);
    }

    @Test
    public void simpleRaceGuided33() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(2);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                x = t;
                tc.tick();
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(2);
                int t = y;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                y = t;
                tc.tick();
            }
        };
        Thread t3 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(1);
                int t = y;
                t += 12;
                tc.tick();
                tc.waitForTick(3);
                y = t;
                tc.tick();
            }
        };
        t2.start();
        t3.start();

        t1.start();
        //sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(1);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(3);
        x=t;
        tc.tick();
        t1.join();

//        t2.join();
//        t3.join();
//        assertTrue("value of y ",y==105);

        assertTrue("value of x "+x,x==5);
    }

    @Test
    public void simpleRaceGuided34() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(1);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(3);
                x = t;
                tc.tick();
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(2);
                int t = y;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                y = t;
                tc.tick();
            }
        };
        Thread t3 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(1);
                int t = y;
                t += 12;
                tc.tick();
                tc.waitForTick(3);
                y = t;
                tc.tick();
            }
        };
        t2.start();
        t3.start();
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(2);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(4);
        x=t;
        tc.tick();
        t1.join();
        t2.join();
        t3.join();
        assertTrue("value of y ",y==105);
        assertTrue("value of x "+x,x==1);
    }

    @Test
    public void simpleRaceGuided30() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(false);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false);
                x = t;
                tc.tick();
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(2);
                int t = y;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                y = t;
                tc.tick();
            }
        };
        Thread t3 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(1);
                int t = y;
                t += 12;
                tc.tick();
                tc.waitForTick(3);
                y = t;
                tc.tick();
            }
        };
        t2.start();
        t3.start();
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(true);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false);
        x=t;
        tc.tick();
        t1.join();
        t2.join();
        t3.join();
        assertTrue("value of y ",y==105);
        assertTrue("value of x "+x,x==5);
    }

    @Test
    public void simpleRaceGuided31() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(false,0);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false,0);
                x = t;
                tc.tick();
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(2);
                int t = y;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                y = t;
                tc.tick();
            }
        };
        Thread t3 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(1);
                int t = y;
                t += 12;
                tc.tick();
                tc.waitForTick(3);
                y = t;
                tc.tick();
            }
        };
        t2.start();
        t3.start();
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(true,0);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false,0);
        x=t;
        tc.tick();
        t1.join();
        t2.join();
        t3.join();
        assertTrue("value of y ",y==105);
        assertTrue("value of x "+x,x==5);
    }

    @Test
    public void simpleRaceGuided32() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(true);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false);
                x = t;
                tc.tick();
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(2);
                int t = y;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                y = t;
                tc.tick();
            }
        };
        Thread t3 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(1);
                int t = y;
                t += 12;
                tc.tick();
                tc.waitForTick(3);
                y = t;
                tc.tick();
            }
        };
        t2.start();
        t3.start();
        t1.start();
        sleep(10);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(false);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false);
        x=t;
        tc.tick();
        t1.join();
        t2.join();
        t3.join();
        assertTrue("value of y ",y==105);
        assertTrue("value of x "+x,x==1);
    }

    @Test
    public void simpleRaceGuided35() throws InterruptedException {
        Thread t1 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
                tc.waitForTick(false);
                int t = x;
                t += 5;
                tc.tick();
                tc.waitForTick(false);
                x = t;
                tc.tick();
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(2);
                int t = y;
                t += 5;
                tc.tick();
                tc.waitForTick(4);
                y = t;
                tc.tick();
            }
        };
        Thread t3 = new Thread() {
            public void run() {
                RaceBreakpoint tc = new RaceBreakpoint(lock);
                tc.waitForTick(1);
                int t = y;
                t += 12;
                tc.tick();
                tc.waitForTick(3);
                y = t;
                tc.tick();
            }
        };
        t2.start();
        t3.start();
        t1.start();
        sleep(2000);
        RaceBreakpoint tc = new RaceBreakpoint(RaceBreakpointTest.class);
        tc.waitForTick(true);
        int t = x;
        t++;
        tc.tick();
        tc.waitForTick(false);
        x=t;
        tc.tick();
        t1.join();
        t2.join();
        t3.join();
        assertTrue("value of y ",y==105);
        assertTrue("value of x "+x,x==6);
    }

}
