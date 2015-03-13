package benchmarks.dstest;

import benchmarks.jpf_test_cases.MyRandom;

/**
 * Created by IntelliJ IDEA.
 * User: ksen
 * Date: Jun 10, 2007
 * Time: 12:21:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class MTSBTest extends Thread {
    benchmarks.stringbuffer.StringBuffer v1;
    benchmarks.stringbuffer.StringBuffer v2;
    int c;

    public MTSBTest(benchmarks.stringbuffer.StringBuffer v1, benchmarks.stringbuffer.StringBuffer v2,int c) {
        this.v1 = v1;
        this.v2 = v2;
        this.c = c;
    }

    public void run() {
        char c1 = (char)MyRandom.nextInt(128);
        char c2 = (char)MyRandom.nextInt(128);
        switch(c){
            case 0:
                v1.append(c1);
                break;
            case 1:
                v1.append(v2);
                break;
            case 2:
                v1.charAt(0);
                break;
            case 3:
                v2.append(v1);
                break;
            case 4:
                v1.deleteCharAt(0);
                break;
        }
    }

    public static void main(String[] args) {
        benchmarks.stringbuffer.StringBuffer v1 = new benchmarks.stringbuffer.StringBuffer();
        benchmarks.stringbuffer.StringBuffer v2 = new benchmarks.stringbuffer.StringBuffer();
        v1.append("Hello");
        v2.append("World");

        (new MTSBTest(v2,v1,0)).start();
        (new MTSBTest(v1,v2,1)).start();
        (new MTSBTest(v2,v1,2)).start();
        (new MTSBTest(v1,v2,3)).start();
        (new MTSBTest(v2,v1,4)).start();
    }
}
