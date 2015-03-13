/*
 * Copyright (C) 1998 by CERN/IT/PDP/IP
 * All rights reserved
 * 
 * $Id: Messages.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 *
 * 03/02/99 cvp
 */


package benchmarks.hedc.ethz.util;

import java.io.*;
import java.util.*;
import java.text.*;


/**
 * The class/module serves to issue messages to stdout. The
 * messages are categorized into debug, warnings and errors, 
 * debug and warning can be further characterized by a level.
 * The output of the latter two categories of messages is 
 * performed only if the level is lower or equal than the 
 * values set in the system attributes DEBUG_LEVEL and 
 * WARN_LEVEL. If these attributes are not set, level 0 is 
 * assumed.
 */
public class Messages {
    
    private static SimpleDateFormat sdf_ = new SimpleDateFormat();
    static {
	sdf_.applyPattern("dd/MMM/yyyy, HH:mm:ss.SSS");
    }
  
  /**
   * debug level
   */
    private static int dlevel_ = -1;
  
  /**
   * warn level (if negative, it is read from
   * the system properties, if those are not set, 
   * 0 is assumed
   */
    private static int wlevel_ = -1;

  /**
     * The PrintStream the output is written to.
     * Default is System.out but it can be changed with 
     * method setPrintStream().
     *
     * @see setPrintStream
     */
    private static PrintStream out = System.out;
    
    /**
     * All methods are static, we don't ant this class to be 
     * instanced or subclassed.
     */
    private Messages() {};
    
    /**
     * Issue an error message.
     * 
     * @param message
     *   The error message.
     */
    public static void error(String message) {
	output_(message, "e");
	throw (new RuntimeException(message));
    }
    
    /**
     * Issue an error message with one parameter.
     * 
     * @param message
     *   The error message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     */
    public static void error(String message, Object arg1) {
	String s = format(message, arg1);
	output_(s, "e");
	throw (new RuntimeException(s));
    }
    
    /**
     * Issue an error message with two parameters.
     * 
     * @param message
     *   The error message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     * @param arg2
     *   The value that is substituted for '%2' in message.
     */
    public static void error(String message, Object arg1, Object arg2) {
	String s = format(message, arg1, arg2);
	output_(s, "e");
	throw (new RuntimeException(s));
    }

    /**
     * Issue an error message with two parameters.
     * 
     * @param message
     *   The error message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     * @param arg2
     *   The value that is substituted for '%2' in message.
     @param arg3
     *   The value that is substituted for '%3' in message.
     */
    public static void error(String message, Object arg1, Object arg2, Object arg3) {
	String s = format(message, arg1, arg2, arg3);
	output_(s, "e");
	throw (new RuntimeException(s));
    }
    
    /**
     * Issue a warning.
     * 
     * @param level 
     *   The warn level.
     * @param message
     *   The message.
     */
    public static void warn(int level, String message) {
	if (level <= wlevel()) {
	    output_(message, "w"+level);
	}
    }
    
    public static String format(String message, Object arg1) {
	String mess;
	int index = 0;
	try {
	    while((index = message.indexOf("%1")) != -1) {
		mess = message.substring(0, index) + arg1 + message.substring(index + 2);
		message = mess;			
	    }
	} catch (Exception e) {
	    message = message + message.substring(index + 2);
	}
	return message;
    }

    public static String format(String message, Object arg1, Object arg2) {
	String mess;
	int index = 0;
	try {
	    while((index = message.indexOf("%1")) != -1) {
		mess = message.substring(0, index) + arg1 + message.substring(index + 2);
		message = mess;			
	    } 
	    while((index = message.indexOf("%2")) != -1) {
		mess = message.substring(0, index) + arg2 + message.substring(index + 2);
		message = mess;
	    }
	} catch(Exception e) {
	    message = message + message.substring(index + 2);
	}
	return message;
    }

    public static String format(String message, Object arg1, Object arg2, Object arg3) {
	String mess;
	int index = 0;
	try {
	    while((index = message.indexOf("%1")) != -1) {
		mess = message.substring(0, index) + arg1 + message.substring(index + 2);
		message = mess;			
	    }
	    
	    while((index = message.indexOf("%2")) != -1) {
		mess = message.substring(0, index) + arg2 + message.substring(index + 2);
		message = mess;
	    }
	    
	    while((index = message.indexOf("%3")) != -1) {
		mess = message.substring(0, index) + arg3 + message.substring(index + 2);
		message = mess;
	    }
	} catch(Exception e) {
	    message = message + message.substring(index + 2);
	}
	return message;
    }

    public static String format(String message, Object arg1, Object arg2, Object arg3, Object arg4) {
	String mess;
	int index = 0;
	try {
	    while((index = message.indexOf("%1")) != -1) {
		mess = message.substring(0, index) + arg1 + message.substring(index + 2);
		message = mess;			
	    }
	    
	    while((index = message.indexOf("%2")) != -1) {
		mess = message.substring(0, index) + arg2 + message.substring(index + 2);
		message = mess;
	    }
	    
	    while((index = message.indexOf("%3")) != -1) {
		mess = message.substring(0, index) + arg3 + message.substring(index + 2);
		message = mess;
	    }
	    
	    while((index = message.indexOf("%4")) != -1) {
		mess = message.substring(0, index) + arg4 + message.substring(index + 2);
		message = mess;
	    }
	} catch (Exception e) {
	    message = message + message.substring(index + 2);
	}
	return message;
    }

    /**
     * Issue a warning with one parameter.
     * 
     * @param level 
     *   The warn level.
     * @param message
     *   The message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     */
    public static void warn(int level, String message, Object arg1) {
	if (level <= wlevel())
	    output_(format(message, arg1), "w"+level);
    }
    
    /**
     * Issue a warning with two parameters.
     * 
     * @param level 
     *   The warn level.
     * @param message
     *   The message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     * @param arg2
     *   The value that is substituted for '%2' in message.
     */
    public static void warn(int level, String message, Object arg1, Object arg2) {
	if (level <= wlevel())
	    output_(format(message, arg1, arg2), "w"+level);
    }

    /**
     * Issue a warning with two parameters.
     * 
     * @param level 
     *   The warn level.
     * @param message
     *   The message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     * @param arg2
     *   The value that is substituted for '%2' in message.
     * @param arg3
     *   The value that is substituted for '%3' in message.
     */
    public static void warn(int level, String message, Object arg1, Object arg2, Object arg3) {
	if (level <= wlevel())
	    output_(format(message, arg1, arg2, arg3), "w"+level);
    }
    
    /**
     * Issue a warning with two parameters.
     * 
     * @param level 
     *   The warn level.
     * @param message
     *   The message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     * @param arg2
     *   The value that is substituted for '%2' in message.
     * @param arg3
     *   The value that is substituted for '%3' in message.
     * @param arg4
     *   The value that is substituted for '%4' in message.
     */
    public static void warn(int level, String message, Object arg1, Object arg2, Object arg3, Object arg4) {
	if (level <= wlevel())
	    output_(format(message, arg1, arg2, arg3, arg4), "w"+level);
    }

     /**
     * Issue a debug message.
     * 
     * @param level 
     *   The debug level.
     * @param message
     *   The message.
     */
    public static void debug(int level, String message) {
	if (level <= dlevel())
	    output_(message, "d"+level);
    }
    
    /**
     * Issue a debug message with one parameter.
     * 
     * @param level 
     *   The debug level.
     * @param message
     *   The message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     */
    public static void debug(int level, String message, Object arg1) {
	if (level <= dlevel())
	    output_(format(message, arg1), "d"+level);
    }
     
    /**
     * Issue a debug message with two parameters.
     * 
     * @param level 
     *   The debug level.
     * @param message
     *   The message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     * @param arg2
     *   The value that is substituted for '%2' in message.
     */
    public static void debug(int level, String message, Object arg1, Object arg2) {
	if (level <= dlevel())
	    output_(format(message, arg1, arg2), "d"+level);
    }
    
    /**
     * Issue a debug message with two parameters.
     * 
     * @param level 
     *   The debug level.
     * @param message
     *   The message.
     * @param arg1
     *   The value that is substituted for '%1' in message.
     * @param arg2
     *   The value that is substituted for '%2' in message.
     * @param arg3
     *   The value that is substituted for '%3' in message.
     */
    public static void debug(int level, String message, Object arg1, Object arg2, Object arg3) {
	if (level <= dlevel())
	    output_(format(message, arg1, arg2, arg3), "d"+level);
    }


  


    /**
     * We retrieve the system property repeatedly
     * the latter are not available during static initialization 
     * of servlets.
     *
     * @return The debug level.
     */
    public static int dlevel() {
	  int retval = (dlevel_ > 0) ? dlevel_ : 0;
	  if (dlevel_ < 0) {
		try {
		  String s = System.getProperty("ethz.util.Messages.DEBUG_LEVEL");
		  if (s != null)
			retval = (new Integer(s)).intValue();
		} catch (SecurityException e) {} // we want to safely run in an Applet context
		}
	  return retval;
	}
    
  public static void dlevel(int l) {
	dlevel_ = l;
  }

  public static void wlevel(int l) {
	wlevel_ = l;
  }
  
    /**
     * We retrieve the system property repeatedly
     * the latter are not available during static initialization 
     * of servlets.
     *
     * @return The warn level.
     */
    public static int wlevel() {
	  int retval = (wlevel_ > 0) ? wlevel_ : 0;
	  if (wlevel_ < 0) {
		try {
		  String s = System.getProperty("ethz.util.Messages.WARN_LEVEL");
		  if (s != null)
			retval = (new Integer(s)).intValue();
		}
		catch (SecurityException e) {} // we want to safely run in an Applet context
	  }
	  return retval;
    }

    /**
     * Redirects the stream where the messages should go.
     * The stream is not changed if the checkError() method of the 
     * stream yield true.
     *
     * @param ps 
     *   The new print stream.
     */
    public static void setPrintStream(PrintStream ps) {
	if (!ps.checkError())
	    out = ps;
    }
    
    private static void output_(String message, String kind) {
	Date mydate = new Date();
	StringBuffer s = new StringBuffer();
	s = sdf_.format(mydate, s, new FieldPosition(0));

	out.println("[" + s.toString() + ", " + kind +"] " + message);
    }
}
