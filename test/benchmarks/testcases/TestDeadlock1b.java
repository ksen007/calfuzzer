package benchmarks.testcases;

// same output as TestDeadlock1
class SyncObject {
    public synchronized void m1(SyncObject o) {
        o.m2();

    }

    public synchronized void m2() {
        System.out.print("a");
    }
}

class Thread1b extends Thread {
    SyncObject l1;
    SyncObject l2;
    SyncObject l3;
    SyncObject l4;

    public Thread1b(SyncObject o1, SyncObject o2, SyncObject o3, SyncObject o4) {
        l1 = o1;
        l2 = o2;
        l3 = o3;
        l4 = o4;
    }

    public void run() {
        l1.m1(l2);
        l3.m1(l4);
    }
}

class Thread2b extends Thread {
    SyncObject l2;
    SyncObject l3;

    public Thread2b(SyncObject o2, SyncObject o3) {
        l2 = o2;
        l3 = o3;
    }

    public void run() {
        l2.m1(l3);
    }
}

public class TestDeadlock1b {

    static SyncObject o1 = new SyncObject();
    static SyncObject o2 = new SyncObject();
    static SyncObject o3 = new SyncObject();
    static SyncObject o4 = new SyncObject();


    public static void main(String[] args) {
        Thread t1 = new Thread1b(o1, o2, o3, o4);
        Thread t4 = new Thread2b(o4, o3);
        //Thread t2 = new Thread2b(o2, o3);
        //Thread t3 = new Thread2b(o4, o1);

        t1.start();
        //t2.start();
        //t3.start();
        t4.start();

        try {
            t1.join();
            //t2.join();
            //t3.join();
            t4.join();
        }
        catch (Exception e) {
            System.err.println("Exception occurred while waiting for threads " + e.toString());
        }

    }

}
