package benchmarks.testcases;

// same output as TestDeadlock2
class ThreadG1a extends Thread {
    Object l1;
    Object l2;
    Object l3;
    Object l4;
    Object l5;

    public ThreadG1a(Object o1, Object o2, Object o3, Object o4, Object o5) {
        l1 = o1;
        l2 = o2;
        l3 = o3;
        l4 = o4;
        l5 = o5;
    }

    public void run() {
        synchronized (l1) {
            synchronized (l2) {
                synchronized (l1) {
                    synchronized (l1) {
                        synchronized (l2) {
                        }
                    }
                }
            }
        }
        try {
            sleep(5);
        }
        catch (Exception e) {
            System.err.println("Error while sleeping in Thread1");
        }
        synchronized (l5) {
            synchronized (l3) {
                synchronized (l5) {
                    synchronized (l4) {
                        synchronized (l3) {
                        }
                    }
                }
            }
        }
    }
}

class ThreadG2a extends Thread {
    Object l2;
    Object l3;

    public ThreadG2a(Object o2, Object o3) {
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

class ThreadG3a extends Thread {
    Object l4;
    Object l1;

    public ThreadG3a(Object o4, Object o1) {
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

class ThreadG4a extends Thread {
    Object l4;
    Object l3;
    Object l5;

    public ThreadG4a(Object o4, Object o3, Object o5) {
        l4 = o4;
        l3 = o3;
        l5 = o5;
    }

    public void run() {
        synchronized (l5) {
            synchronized (l4) {
                synchronized (l3) {
                    synchronized (l5) {
                        synchronized (l3) {
                        }
                    }
                }
            }
        }
    }
}

public class TestDeadlock2a {

    static Object o1 = new Object();
    static Object o2 = new Object();
    static Object o3 = new Object();
    static Object o4 = new Object();
    static Object o5 = new Object();


    public static void main(String[] args) {
        Thread t1 = new ThreadG1a(o1, o2, o3, o4, o5);
        Thread t2 = new ThreadG2a(o2, o3);
        Thread t3 = new ThreadG3a(o4, o1);
        Thread t4 = new ThreadG4a(o4, o3, o5);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        }
        catch (Exception e) {
            System.err.println("Exception occurred while waiting for threads " + e.toString());
        }

    }

}
