package benchmarks.dstest;

import benchmarks.instrumented.java15.util.ArrayList;
import benchmarks.instrumented.java15.util.Collections;
import benchmarks.instrumented.java15.util.List;
import benchmarks.jpf_test_cases.MyRandom;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: Jun 7, 2007
 * Time: 11:34:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class MTArrayListTest {
    public static void main(String args[]) {
        List al1 = Collections.synchronizedList(new ArrayList());
        List al2 = Collections.synchronizedList(new ArrayList());
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
