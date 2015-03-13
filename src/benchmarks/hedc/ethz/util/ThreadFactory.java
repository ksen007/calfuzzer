/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: ThreadFactory.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 03/02/99  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import java.util.Vector;
import java.util.Enumeration;

/*
 * not used at the moment
 */
public class ThreadFactory {
    
    private static ThreadFactory uniqueInstance_;
    private Vector currentThreads_ = new Vector(); 

    private ThreadFactory() {}

    public Thread newThread(Runnable r) {
	Thread ret = new Thread(r);
	currentThreads_.addElement(ret);
	return ret;
    }

    public void stopAll() {
	for (Enumeration en = currentThreads_.elements(); en.hasMoreElements(); ) {
	    Thread t = (Thread) en.nextElement();
	    t.stop();
	}
    }

    public static ThreadFactory getUniqueInstance() {
	if (uniqueInstance_ == null)
	    uniqueInstance_ = new ThreadFactory();
	return  uniqueInstance_;
    }
}
