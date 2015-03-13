/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: BufferedCopy.java,v 1.1 2001/03/16 18:15:21 praun Exp $
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
 */
public class BufferedCopy {

    private int BCS_BUF_SIZE_ = 32768;

    /**
     * @param ist 
     *   The input stream to copy.
     */
    public BufferedCopy(OutputStream ost, InputStream ist, int buffersize) {
	ist_ = ist;
	ost_ = ost;
	buffer_ = new byte[(buffersize > 0) ? buffersize : BCS_BUF_SIZE_];
    }

    public BufferedCopy(OutputStream ost, InputStream ist) {
	ist_ = ist;
	ost_ = ost;
	buffer_ = new byte[BCS_BUF_SIZE_];
    }
    
    /**
     * Invoked when the thread is started. The 
     * Thread stops executing when ist.read() delivers -1 or
     * an IOException occurs.
     */
    public long copy() throws IOException {
	long retval = 0;
	int bytesread = 0;
	if (ist_ != null && ost_ != null) {
	    while ((bytesread = ist_.read(buffer_)) != -1) {
		ost_.write(buffer_, 0, bytesread);
		retval += bytesread;
	    }
	}
	return retval;
    }
    
    /**
     * The input stream that should be copied to ost
     */
    private InputStream ist_;
    private OutputStream ost_;
    private byte[] buffer_;
}
