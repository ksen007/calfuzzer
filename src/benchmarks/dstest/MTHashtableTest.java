package benchmarks.dstest;

import benchmarks.instrumented.java15.util.Hashtable;
import benchmarks.jpf_test_cases.MyRandom;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: Jun 10, 2007
 * Time: 12:21:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class MTHashtableTest extends Thread {
    Hashtable v1;
    Hashtable v2;
    int c;

    public MTHashtableTest(Hashtable v1, Hashtable v2,int c) {
        this.v1 = v1;
        this.v2 = v2;
        this.c = c;
    }

    public void run() {
        SimpleObject o1 = new SimpleObject(MyRandom.nextInt(3));
        SimpleObject o2 = new SimpleObject(MyRandom.nextInt(3));
        switch(c){
            case 0:
                v1.put(o1,o2);
                break;
            case 1:
                v1.putAll(v2);
                break;
            case 2:
                v1.clear();
                break;
            case 3:
                v1.contains(o1);
                break;
            case 4:
                v1.remove(o1);
                break;
        }
    }

    public static void main(String[] args) {
        Hashtable v1 = new Hashtable();
        Hashtable v2 = new Hashtable();
        v1.put(new SimpleObject(MyRandom.nextInt(3)),new SimpleObject(MyRandom.nextInt(3)));
        v1.put(new SimpleObject(MyRandom.nextInt(3)),new SimpleObject(MyRandom.nextInt(3)));
        v2.put(new SimpleObject(MyRandom.nextInt(3)),new SimpleObject(MyRandom.nextInt(3)));
        v2.put(new SimpleObject(MyRandom.nextInt(3)),new SimpleObject(MyRandom.nextInt(3)));

        (new MTHashtableTest(v1,v2,0)).start();
        (new MTHashtableTest(v2,v1,1)).start();
        (new MTHashtableTest(v1,v2,2)).start();
        (new MTHashtableTest(v2,v1,3)).start();
        (new MTHashtableTest(v1,v2,4)).start();
    }
}
