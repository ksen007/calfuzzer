/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: DoubleBufferCopy.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 05/01/99  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;

/**
 */
public class DoubleBufferCopy {

    private static final int DBC_BUF_SIZE_ = 32768;
    private static String DBC01_ = "DBC01 - copy failed with an IOException";
    private static String DBC02_ = "DBC02 - copy failed, thread was interrupted before";
    private CopyThread read_;
    private CopyThread write_;
    private InputStream ist_;
    private OutputStream ost_;
    private Buffer buffer1_;
    private Buffer buffer2_;
    private static final int DBC_WRITE = 1;
    private static final int DBC_READ = 2;
    
    private class Buffer {
	private final int BUF_WRITE = 1;
	private final int BUF_READ = 2;
	
	Buffer(int buffersize) {
	    buffer_ = new byte[buffersize];
	}
	
	synchronized boolean accessBuffer(int mode) throws IOException {
	    boolean ret = true;
	    switch (mode) {
	    case BUF_WRITE: {
		if (bufferFill_ > 0) {
		    ost_.write(buffer_, 0, bufferFill_);
		    ost_.flush();
		    bufferFill_ = 0;  // reset bufferFill_
		}
		else if (bufferFill_ < 0) // in case bufferFill_ == 0 we do not do anything and hope that soon something is read
		    ret = false;
		break;
	    }
	    case BUF_READ: {
		if (bufferFill_== 0) { // if bufferFill_ was reset
		    bufferFill_ = ist_.read(buffer_, 0, buffer_.length);
		    if (bufferFill_ < 0) {
			ret = false;
		    }
		}
		break;
	    }
	    }
	    return ret;
	}
    	
	private byte[] buffer_;
	private int bufferFill_ = 0;
    }

	
    private class CopyThread extends Thread {
	CopyThread(int type) {
	    type_ = type;
	}
	
	public void run() {
	    try {
		boolean stop = false;
		while (!stop) {
		    if (buffer1_.accessBuffer(type_))
			stop = !buffer2_.accessBuffer(type_);
		    else 
			stop = true;
		}
	    } catch (IOException e) {
		Messages.error(DBC01_);
		e.printStackTrace();
	    }
	}
	
	private int type_;
    }

    /**
     * @param ist 
     *   The input stream to copy.
     */
    public DoubleBufferCopy(OutputStream ost, InputStream ist, int buffersize) {
	ist_ = ist;
	ost_ = ost;
	buffer1_ = new Buffer((buffersize > 0) ? buffersize : DBC_BUF_SIZE_);
	buffer2_ = new Buffer((buffersize > 0) ? buffersize : DBC_BUF_SIZE_);
	read_= new CopyThread(DBC_READ);
	write_= new CopyThread(DBC_WRITE);
    }

    public DoubleBufferCopy(OutputStream ost, InputStream ist) {
	this(ost, ist, DBC_BUF_SIZE_);
    }
    
    /**
     * Invoked when the thread is started. The 
     * Thread stops executing when ist.read() delivers -1 or
     * an IOException occurs.
     */
    public void copy() throws IOException {
	read_.start();
	write_.start();
	try {
	    write_.join();
	} catch (InterruptedException e) {
	    Messages.error(DBC02_);
	}
    }
    
}
