package benchmarks.testcases;

class ThreadB1 extends Thread {
    Object l1;
    Object l2;
    Object l3;
    Object l4;

    public ThreadB1(Object o1, Object o2, Object o3, Object o4) {
        l1 = o1;
        l2 = o2;
        l3 = o3;
        l4 = o4;
    }

    public void run() {
        Thread t4 = new ThreadB4(l4, l3);
        t4.start();

        synchronized (l1) {
            synchronized (l2) {

            }
        }

        synchronized (l3) {
            synchronized (l4) {

            }
        }

        try {
            t4.join();
        }
        catch (Exception e) {
            System.err.println("Exception caused while waiting for thread t4");
        }
    }
}

class ThreadB2 extends Thread {
    Object l2;
    Object l3;

    public ThreadB2(Object o2, Object o3) {
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

class ThreadB3 extends Thread {
    Object l4;
    Object l1;

    public ThreadB3(Object o4, Object o1) {
        l4 = o4;
        l1 = o1;
    }

    public void run() {
        synchronized (l4) {
            synchronized (l1) {

            }
        }
    }
}

class ThreadB4 extends Thread {
    Object l4;
    Object l3;

    public ThreadB4(Object o4, Object o3) {
        l4 = o4;
        l3 = o3;
    }

    public void run() {
        synchronized (l4) {
            synchronized (l3) {

            }
        }
    }
}

public class TestDeadlock4 {

    static Object o1 = new Object();
    static Object o2 = new Object();
    static Object o3 = new Object();
    static Object o4 = new Object();


    public static void main(String[] args) {
        Thread t1 = new ThreadB1(o1, o2, o3, o4);
        //Thread t2 = new ThreadB2(o2, o3);
        //Thread t3 = new ThreadB3(o4, o1);

        t1.start();
        //t2.start();
        //t3.start();

        try {
            t1.join();
            //t2.join();
            //t3.join();
        }
        catch (Exception e) {
            System.err.println("Exception occurred while waiting for threads " + e.toString());
        }

    }

}

