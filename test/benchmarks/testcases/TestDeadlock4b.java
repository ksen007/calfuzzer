package benchmarks.testcases;

// same output as TestDeadlock4
class SyncObjecb {
    public synchronized void m1(SyncObjecb o) {
        o.m2();

    }

    public synchronized void m2() {
        System.out.print("a");
    }
}

class ThreadB1b extends Thread {
    SyncObjecb l1;
    SyncObjecb l2;
    SyncObjecb l3;
    SyncObjecb l4;

    public ThreadB1b(SyncObjecb o1, SyncObjecb o2, SyncObjecb o3, SyncObjecb o4) {
        l1 = o1;
        l2 = o2;
        l3 = o3;
        l4 = o4;
    }

    public void run() {
        Thread t4 = new ThreadB4(l4, l3);
        t4.start();

        l1.m1(l2);
        try {
            sleep(5);
        }
        catch (Exception e) {
            System.err.println("Error while sleeping in Thread1");
        }

        l3.m1(l4);

        try {
            t4.join();
        }
        catch (Exception e) {
            System.err.println("Exception caused while waiting for thread t4");
        }
    }
}

class ThreadB2b extends Thread {
    SyncObjecb l2;
    SyncObjecb l3;

    public ThreadB2b(SyncObjecb o2, SyncObjecb o3) {
        l2 = o2;
        l3 = o3;
    }

    public void run() {
        l2.m1(l3);
    }
}

class ThreadB3b extends Thread {
    SyncObjecb l4;
    SyncObjecb l1;

    public ThreadB3b(SyncObjecb o4, SyncObjecb o1) {
        l4 = o4;
        l1 = o1;
    }

    public void run() {
        l4.m1(l1);
    }
}

class ThreadB4b extends Thread {
    SyncObjecb l4;
    SyncObjecb l3;

    public ThreadB4b(SyncObjecb o4, SyncObjecb o3) {
        l4 = o4;
        l3 = o3;
    }

    public void run() {
        l4.m1(l3);
    }
}

public class TestDeadlock4b {

    static SyncObjecb o1 = new SyncObjecb();
    static SyncObjecb o2 = new SyncObjecb();
    static SyncObjecb o3 = new SyncObjecb();
    static SyncObjecb o4 = new SyncObjecb();


    public static void main(String[] args) {
        Thread t1 = new ThreadB1b(o1, o2, o3, o4);
        Thread t2 = new ThreadB2b(o2, o3);
        Thread t3 = new ThreadB3b(o4, o1);

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        }
        catch (Exception e) {
            System.err.println("Exception occurred while waiting for threads " + e.toString());
        }

    }

}

