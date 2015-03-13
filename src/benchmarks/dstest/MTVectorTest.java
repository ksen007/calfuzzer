package benchmarks.dstest;

/**
 * Created by IntelliJ IDEA.
 * User: Koushik Sen (ksen@cs.uiuc.edu)
 * Date: Dec 26, 2005
 * Time: 5:05:28 PM
 */
public class MTVectorTest extends Thread {
    Vector v1;
    int c;

    public MTVectorTest(Vector v1, int c) {
        this.v1 = v1;
        this.c = c;
    }

    public void run() {
        Object o1 = new Object();
        switch (c) {
            case 0:
                v1.addElement(o1);
                break;
            case 1:
                v1.capacity();
                break;
            case 2:
                v1.clone();
                break;
            case 3:
                v1.elements();
                break;
            case 4:
                v1.indexOf(o1);
                break;
            case 5:
                v1.insertElementAt(o1, 0);
                break;
            case 6:
                v1.isEmpty();
                break;
            case 7:
                v1.lastIndexOf(o1);
                break;
            case 8:
                v1.removeAllElements();
                break;
            default:
                v1.size();
                break;
        }
    }

    public static void main(String[] args) {
        Vector v1 = new Vector();
        v1.addElement(new Object());
        v1.addElement(new Object());
        for (int i = 0; i < 10; i++) {
            (new MTVectorTest(v1, i)).start();
        }
    }
}
