/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: CopyStream.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 05/01/99  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;

/**
 * The class is useful to redirect an input stream to stdout.
 * It helps e.g. to redirect the output of one or multiple child 
 * threads into the console of the parent thread.
 * The routine must not be used for copying binary 
 * contents!
 */
public class CopyStream extends Thread {
    
    /**
     * @param ist 
     *   The input stream to copy.
     */
    public CopyStream(InputStream ist, Writer wrt) {
	ist_ = ist;
	wrt_ = wrt;
    }

    /**
     * @param ist 
     *   The input stream to copy.
     */
    public CopyStream(InputStream ist, OutputStream ost) {
	ist_ = ist;
	ost_ = ost;
    }
    
    /**
     * Invoked when the thread is started. The 
     * Thread stops executing when ist.read() delivers -1 or
     * an IOException occurs.
     */
    public void run() {
	int c;
	try {
	    if (ost_!= null) 
		while ((c = ist_.read()) != -1)
		    ost_.write(c);
	    else if (wrt_ != null)
		while ((c = ist_.read()) != -1)
		    wrt_.write(c);
		
	} catch (IOException e) {};
    }
    
    public static StringBuffer copyStreamIntoBuffer(InputStream is, int len) {
	int c;
	int i = 0;
	StringBuffer ret = null;
	StringWriter sw = new StringWriter();
	try {
	    if (is != null) 
		while (i < len && (c = is.read()) != -1) {
		    sw.write(c);
		    ++ i;
		}
	    ret = sw.getBuffer();
	    sw.close();
	} catch (IOException e) {}
	return ret;
    }
    
    /*
     * The input stream that should be redirected to stdout.
     */
    private InputStream ist_;
    private OutputStream ost_;
    private Writer wrt_;
}
