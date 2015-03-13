/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: Logger.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 28/03/99  cvp 
 *
 */
 
package benchmarks.hedc.ethz.util;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/*
 *
 */
public abstract class Logger {
    
    protected static final String monthnames[] = {
	"Jan", "Feb", "Mar", "Apr", "May", "Jun",
	"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private static final String LOG01_ = "LOG01 - unable to open log file (%1) - not logging";
    private static final String LOG02_ = "LOG02 - error occurred during logging (%1)";
    private static final String LOG_ID_ = "$Id: Logger.java,v 1.1 2001/03/16 18:15:21 praun Exp $";
    private static int LOG_BUFFER_DEFAULT_SIZE_ = 512;

    private static Vector allLoggerInstances_ = new Vector();
    protected RandomAccessFile log_ = null;
    protected StringBuffer buffer_ = null;
    private int logBuffer_ = LOG_BUFFER_DEFAULT_SIZE_;
    private String filename_ = null;

    /**
     * Flushes buffers of all logs, writes the epilogue and closes the file. 
     * Calls to log(...) on any instance of type Logger after this call. Typically called
     * before System.exit();
     */
    public static void closeAllLogs() {
	for (Enumeration e = allLoggerInstances_.elements(); e.hasMoreElements();) {
	    Logger l = (Logger) e.nextElement();
	    l.sync();
	    l.closeLog_();
	}
	allLoggerInstances_ = new Vector();
    }

    /**
     * @return the absolute path of the file where this log goes
     */
    public String getFilename() {
        return filename_;
    }

    protected final void openLog (String filename, int bufsize) {
	try {
	    filename_ = filename;
	    logBuffer_ = (bufsize < 0) ? LOG_BUFFER_DEFAULT_SIZE_ : bufsize;
	    buffer_ = new StringBuffer(logBuffer_);
	    log_ = new RandomAccessFile(filename_, "rw") ;
	    log_.seek (log_.length());
	    writePreamble_();
	} catch (Exception e) {
	    Messages.error(LOG01_, filename);
	    log_ = null;
	}
    }

    protected Logger() {
	allLoggerInstances_.addElement(this);
    }

    protected abstract void writePreamble_();
    protected abstract void writeEpilogue_();

    protected synchronized void appendLogBuffer()
	throws IOException
    {
	if (buffer_.length() >= logBuffer_ ) {
	    // flush the buffer:
	    if (log_ != null) {
		log_.writeBytes(buffer_.toString());
	    }
	    // abandon the old buffer
	    buffer_ = new StringBuffer(logBuffer_);
	}
    }

    private synchronized void closeLog_() {
	try {
	    writeEpilogue_();
	    log_.close();
	} catch (IOException e) {
	    Messages.warn(0, LOG02_, e.getMessage());
	} finally {
	    log_ = null;
	}
    }

    /**
     * Force to write buffered data to stable storage.
     */
    public synchronized void sync() {
	if (log_ != null) {
	    try {
		log_.writeBytes(buffer_.toString());
		buffer_ = new StringBuffer(logBuffer_);
	    } catch (IOException e) {
		Messages.warn(0, LOG02_, e.getMessage());
	    }
	}
    }
}

