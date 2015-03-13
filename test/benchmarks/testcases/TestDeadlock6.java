package benchmarks.testcases;

class ThreadL1 extends Thread {
    Object l1;
    Object l2;

    public ThreadL1(Object o1, Object o2) {
        l1 = o1;
        l2 = o2;
    }

    public void run() {
        synchronized (l1) {
            synchronized (l2) {

            }
        }
    }
}

class ThreadL2 extends Thread {
    Object l2;
    Object l3;
    Object l4;

    public ThreadL2(Object o2, Object o3, Object o4) {
        l2 = o2;
        l3 = o3;
        l4 = o4;
    }

    public void run() {

        foo();

        synchronized (l2) {
            foo();
        }
    }

    public void foo() {
        synchronized (l3) {
            synchronized (l4) {

            }

        }
    }
}

class ThreadL3 extends Thread {
    Object l4;
    Object l5;

    public ThreadL3(Object o4, Object o5) {
        l4 = o4;
        l5 = o5;
    }

    public void run() {
        synchronized (l4) {
            synchronized (l5) {

            }
        }
    }

}

class ThreadL4 extends Thread {
    Object l5;
    Object l6;
    Object l7;
    Object l1;

    public ThreadL4(Object o5, Object o6, Object o7, Object o1) {
        l5 = o5;
        l6 = o6;
        l7 = o7;
        l1 = o1;
    }

    public void run() {
        synchronized (l5) {
            synchronized (l6) {
                synchronized (l7) {
                    synchronized (l1) {

                    }
                }
            }
        }
    }
}

public class TestDeadlock6 {
    public static void main(String[] args) {
        Object[] objArr1 = new Object[2];
        for (int i = 0; i < 2; i++) {
            objArr1[i] = new Object();
        }

        Object[] objArr2 = new Object[5];
        for (int j = 0; j < 5; j++) {
            objArr2[j] = new Object();
        }

        ThreadL1 t1 = new ThreadL1(objArr1[0], objArr1[1]);
        ThreadL2 t2 = new ThreadL2(objArr1[1], objArr2[0], objArr2[1]);
        ThreadL3 t3 = new ThreadL3(objArr2[1], objArr2[2]);
        ThreadL4 t4 = new ThreadL4(objArr2[2], objArr2[3], objArr2[4], objArr1[0]);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

    }
}
