package benchmarks;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: May 30, 2007
 * Time: 11:01:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestMe {
    public static int f(int x) {
        return 2 * x;
    }

    public static void main(String[] args) {
        byte l = 8;
        l++;
        TestMe.f(l);
    }
}
