package benchmarks.hedc;

/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 *
 * @version $Id: Rag.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun
 */

import java.util.*;
import java.text.*;

public class RandomDate { 
    private static SimpleDateFormat format_ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static String format(Date d) {
	Messages.assertErr(d != null);
	return format_.format(d);
    }
    public static Date parse(String s) throws ParseException {
	Messages.assertErr(s != null);
	return format_.parse(s);
    }

    private Random random_ = null;
    private long start_ = 0;
    private long end_ = 0;
    
    RandomDate(Date start, Date end) {
	Messages.assertErr(start != null && end != null);
	start_ = start.getTime();
	end_ = end.getTime();
	Messages.assertErr(start_ < end_);
	random_ = new Random();
    }

    RandomDate(String start, String end) throws ParseException {
	Messages.assertErr(start != null && end != null);
	// Messages.debug(-1, "RandomDate::<init1> before parse s=%1", start);
	Date tmp = parse(start); start_ = tmp.getTime();
	// Messages.debug(-1, "RandomDate::<init1> after parse");
	// Messages.debug(-1, "RandomDate::<init2> before parse");
	tmp = parse(end); end_ = tmp.getTime();
	// Messages.debug(-1, "RandomDate::<init2> after parse");
	Messages.assertErr(start_ < end_);
	random_ = new Random();
    }
    
    public Date nextDate() {
	long d = (long) (random_.nextDouble() * (end_-start_));
	Date ret = new Date(start_ + d);
	// Messages.debug(3, "RandomDate::nextDate ret=%1", ret);
	return ret;
    }
    
    public String nextString() {
	String ret = null;
	try {
	    ret = format(nextDate());
	} catch (Exception _) {
	    Messages.assertErr(false);
	}
	return ret;
    }

    /* for testing */
    public static void main(String ars[]) {
	try {
	    RandomDate rd = 
		new RandomDate("2000/01/20 00:00:00", 
			       "2000/02/20 23:00:00");
	    for (int i = 0; i < 10; ++i)
		System.out.println(rd.nextString());
	} catch (Exception e) {
	    Messages.error(e.toString());
	}
    }
}





