package benchmarks.testcases;

class Thread1 extends Thread {
    Object l1;
    Object l2;
    Object l3;
    Object l4;

    public Thread1(Object o1, Object o2, Object o3, Object o4) {
        l1 = o1;
        l2 = o2;
        l3 = o3;
        l4 = o4;
    }

    public void run() {
        synchronized (l1) {
            synchronized (l2) {

            }
        }
        try {
            sleep(1);
        }
        catch (Exception e) {
            System.err.println("Error while sleeping in Thread1");
        }

        synchronized (l3) {
            synchronized (l4) {

            }
        }
    }
}

class Thread2 extends Thread {
    Object l2;
    Object l3;

    public Thread2(Object o2, Object o3) {
        l2 = o2;
        l3 = o3;
    }

    public void run() {
        synchronized (l2) {
            synchronized (l3) {

            }
        }
    }
}

public class TestDeadlock1 {

    static Object o1 = new Object();
    static Object o2 = new Object();
    static Object o3 = new Object();
    static Object o4 = new Object();


    public static void main(String[] args) {
        Thread t1 = new Thread1(o1, o2, o3, o4);
        //Thread t2 = new Thread2(o2, o3);
        //Thread t3 = new Thread2(o4, o1);
        Thread t4 = new Thread2(o4, o3);

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
