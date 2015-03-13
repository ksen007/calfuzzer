package benchmarks.dstest;

import benchmarks.instrumented.java15.util.Collections;
import benchmarks.instrumented.java15.util.Set;
import benchmarks.instrumented.java15.util.TreeSet;
import benchmarks.jpf_test_cases.MyRandom;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: Jun 7, 2007
 * Time: 11:54:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class MTTreeSetTest {
    public static void main(String[] args) {
        Set s1 = Collections.synchronizedSet(new TreeSet());
        Set s2 = Collections.synchronizedSet(new TreeSet());
        s1.add(new SimpleObject(MyRandom.nextInt(3000)));
        s1.add(new SimpleObject(MyRandom.nextInt(3000)));
        s2.add(new SimpleObject(MyRandom.nextInt(3000)));
        s2.add(new SimpleObject(MyRandom.nextInt(3000)));
        for (int i = 12; i >=0 ; i--) {
            (new MTSetTest(s1, s2, i)).start();
        }
        for (int i = 7; i >= 0; i--) {
            (new MTSetTest(s2, s1, i)).start();
        }
    }

}
