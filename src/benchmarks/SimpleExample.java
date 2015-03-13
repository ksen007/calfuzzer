package benchmarks;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: Jun 2, 2007
 * Time: 12:23:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleExample extends Thread {
    Object x, y;
    public static int a, b;

    public SimpleExample(Object x, Object y) {
        this.x = x;
        this.y = y;
    }

    public void run() {
        synchronized(x){
            a = 1;
        }
        synchronized(y){
            b = 2;
        }
    }

    public static void main(String[] args) {
        Object x = new Object();
        Object y = new Object();
        (new SimpleExample(x,y)).start();
        (new SimpleExample(y,x)).start();
    }
}
