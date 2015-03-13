package benchmarks.dstest;

import benchmarks.instrumented.java15.util.Collections;
import benchmarks.instrumented.java15.util.LinkedList;
import benchmarks.instrumented.java15.util.List;
import benchmarks.jpf_test_cases.MyRandom;

/**
 * Created by IntelliJ IDEA.
 * User: Koushik Sen (ksen@cs.uiuc.edu)
 * Date: Dec 26, 2005
 * Time: 9:46:07 AM
 */
public class MTListTest extends Thread {
    List al1, al2;
    int c;

    public MTListTest(List al1, List al2, int c) {
        this.al1 = al1;
        this.al2 = al2;
        this.c = c;
    }

    public void run() {
        SimpleObject o1 = new SimpleObject(MyRandom.nextInt(3));
        switch (c) {
            case 0:
                al1.add(o1);
                break;
            case 1:
                al1.toArray();
                break;
            case 2:
                al1.clear();
                break;
            case 3:
                al1.contains(o1);
                break;
            case 4:
                al1.size();
                break;
            case 5:
                al1.remove(o1);
                break;
            case 6:
                al1.listIterator();
                break;
            case 7:
                al1.indexOf(o1);
                break;
            case 8:
                al1.isEmpty();
                break;
            case 9:
                al1.iterator();
                break;
            case 10:
                al1.lastIndexOf(o1);
                break;
            case 11:
                al1.equals(al2);
                break;
            case 12:
                al1.containsAll(al2);
                break;
            case 13:
                al1.addAll(al2);
                break;
            default:
                al1.removeAll(al2);
                break;
        }
    }

    public static void main(String args[]) {
        List al1 = Collections.synchronizedList(new LinkedList());
        List al2 = Collections.synchronizedList(new LinkedList());
        al1.add(new SimpleObject(MyRandom.nextInt(3)));
        al1.add(new SimpleObject(MyRandom.nextInt(3)));
        al2.add(new SimpleObject(MyRandom.nextInt(3)));
        al2.add(new SimpleObject(MyRandom.nextInt(3)));
        for (int i = 14; i >= 0; i--) {
            (new MTListTest(al1, al2, i)).start();
        }
        for (int i = 10; i >= 0; i--) {
            (new MTListTest(al2, al1, i)).start();
        }
    }
}
