package benchmarks.philo;

/**
 * Created by IntelliJ IDEA.
 * User: Koushik Sen (ksen@cs.uiuc.edu)
 * Date: May 19, 2007
 * Time: 3:03:32 PM
 */
public class StartError extends Thread {
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) {
        StartError t1 = new StartError();
        t1.start();
        t1.start();
    }
}
