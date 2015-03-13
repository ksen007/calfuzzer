package benchmarks.dstest;

import benchmarks.instrumented.java15.util.Collections;
import benchmarks.instrumented.java15.util.LinkedHashSet;
import benchmarks.instrumented.java15.util.Set;
import benchmarks.jpf_test_cases.MyRandom;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: Jun 7, 2007
 * Time: 11:55:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class MTLinkedHashSetTest {
    public static void main(String[] args) {
        Set s1 = Collections.synchronizedSet(new LinkedHashSet());
        Set s2 = Collections.synchronizedSet(new LinkedHashSet());
        s1.add(new SimpleObject(MyRandom.nextInt(3)));
        s1.add(new SimpleObject(MyRandom.nextInt(3)));
        s2.add(new SimpleObject(MyRandom.nextInt(3)));
        s2.add(new SimpleObject(MyRandom.nextInt(3)));
        for (int i = 12; i >= 0; i--) {
            (new MTSetTest(s1, s2, i)).start();
        }
        for (int i = 7; i >= 0; i--) {
            (new MTSetTest(s2, s1, i)).start();
        }
    }

}
