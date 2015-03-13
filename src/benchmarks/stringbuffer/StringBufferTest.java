package benchmarks.stringbuffer;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: Jun 2, 2007
 * Time: 2:08:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringBufferTest extends Thread {
    StringBuffer al1, al2;
    int choice;

    public StringBufferTest(StringBuffer al1, StringBuffer al2, int choice) {
        this.al1 = al1;
        this.al2 = al2;
        this.choice = choice;
    }

    public void run() {
        System.out.println("started " + Thread.currentThread());
        System.out.flush();
        switch (choice) {
            case 0:
                al1.append(al2);
                break;
            case 1:
                al1.delete(0, al1.length());
                break;
        }
    }

    public static void main(String args[]) {
        StringBuffer al1 = new benchmarks.stringbuffer.StringBuffer("Hello");
        StringBuffer al2 = new benchmarks.stringbuffer.StringBuffer("World");
        (new StringBufferTest(al1, al2, 0)).start();
        (new StringBufferTest(al2, al1, 1)).start();
    }
}
