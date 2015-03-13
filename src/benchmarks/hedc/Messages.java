package benchmarks.hedc;

/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: Messages.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun
 */


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
 * WARN_LEVEL.  */
public class Messages {
    
    private static SimpleDateFormat sdf_ = new SimpleDateFormat();
    static {
	sdf_.applyPattern("dd/MMM/yyyy, HH:mm:ss.SSS");
    }
    
    /**
     * The PrintStream the output is written to.
     * Default is System.out but it can be changed with 
     * method setPrintStream().
     *
     * @see Messages#setPrintStream(PrintStream) setPrintStream
     */
    private static PrintStream out = System.out;
    
    /**
     * All methods are static, we don't ant this class to be 
     * instanced or subclassed.
     */
    private Messages() {};
    
    public static void assertErr(boolean b) {
	if (!b) 
	    error("assertion failed");
    }
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
    public static void assertErr(boolean b, String message) {
	if (!b) 
	    error(message);
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
    public static void assertErr(boolean b, String message, Object arg1) {
	if (!b) 
	    error(message, arg1);
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
    public static void assertErr(boolean b, String message, Object arg1, Object arg2) {
	if (!b) 
	    error(message, arg1, arg2);
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
    public static void assertErr(boolean b, String message, Object arg1, Object arg2, Object arg3) {
	if (!b) 
	    error(message, arg1, arg2, arg3);
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
    

    public static String format(String fmt, Object[] args) {
        StringBuffer sb = new StringBuffer();
        int i = 0, ni, argi = 0;

        while ((ni = fmt.indexOf("%", i)) != -1) {                              
            sb.append(fmt.substring(i, i = ni));            

            if (fmt.charAt(++i) >= '1' && fmt.charAt(i) <= '9') {                
                argi = fmt.charAt(i++) - '1';
		if (argi < args.length)
		    sb.append(args[argi]);
            } else {               
                switch (fmt.charAt(i++)) {    
                    case 's': sb.append((String)args[argi++]); break;  
                    case 'i':
                    case 'd': sb.append(((Number)args[argi++]).longValue()); break;
                    case 'f': sb.append(((Number)args[argi++]).doubleValue()); break;
                    case 'p':
                    case 'x': sb.append(Integer.toHexString(((Number)args[argi++]).intValue())); break;
                    case '%': sb.append('%');
                }                        
            }
        }
	sb.append(fmt.substring(i));
        return sb.toString();
    }

    public static String format(String fmt, Object arg) {
        Object[] args = {arg};
        return format(fmt, args);
    }
    
    public static String format(String fmt, Object arg1, Object arg2) {
        Object[] args = {arg1, arg2};
        return format(fmt,args);
    }

    public static String format(String fmt, Object arg1, Object arg2, Object arg3) {
        Object[] args = {arg1, arg2, arg3};
        return format(fmt, args);
    }
        
    public static String format(String fmt, Object arg1, Object arg2, Object arg3, Object arg4) {
        Object[] args = {arg1, arg2, arg3, arg4};
        return format(fmt, args);
    }

    public static String format(String fmt, int arg) {
        Object[] args = {new Integer(arg)};
        return format(fmt, args);
    }

    public static String format(String fmt, int arg1, int arg2) {
        Object[] args = {new Integer(arg1), new Integer(arg2)};
        return format(fmt, args);
    }

    public static String format(String fmt, double arg) {
        Object[] args = {new Double(arg)};
        return format(fmt, args);
    }



    public static String formatArray(int[] a) {
	String ret = null;
	if (a != null) {
	    StringBuffer sb = new StringBuffer();
	    int i = 0;
	    sb.append("[");
	    if (a.length > 0) {
		for (i = 0; i < a.length-1; ++i) 
		    sb.append(a[i] + ",");
		sb.append(a[i]);
	    }
	    sb.append("]");
	    ret = sb.toString();
	} 
	return ret;
    }

    public static String formatArray(Object[] a) {
	String ret = null;
	if (a != null) {
	    StringBuffer sb = new StringBuffer();
	    int i = 0;
	    sb.append("[");
	    if (a.length > 0) {
		for (i = 0; i < a.length-1; ++i) 
		    sb.append(a[i] + ",");
		sb.append(a[i]);
	    }
	    sb.append("]");
	    ret = sb.toString();
	}
	return ret;
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

    private static int dlevel_ = 0;
    /**
     * We retrieve the system property repeatedly
     * the latter are not available during static initialization 
     * of servlets.
     *
     * @return The debug level.
     */
    public static int dlevel() {
	int retval = dlevel_;
	if (retval != -1) {
	    try {
		String s = System.getProperty("Messages.DEBUG_LEVEL");
		if (s != null)
		    retval = (new Integer(s)).intValue();
	    } catch (SecurityException e) {} // we want to safely run in an Applet context
	}
	return retval;
    }
    public static void dlevel(int i) {
	dlevel_ = i;
    }
    
    private static int wlevel_ = 0; 
    /**
     * We retrieve the system property repeatedly
     * the latter are not available during static initialization 
     * of servlets.
     *
     * @return The warn level.
     */
    public static int wlevel() {
	int retval = wlevel_;
	if (retval != -1) {
	    try {
		String s = System.getProperty("Messages.WARN_LEVEL");
		if (s != null)
		    retval = (new Integer(s)).intValue();
	    }
	    catch (SecurityException e) {} // we want to safely run in an Applet context
	}
	return retval;
    }
    public static void wlevel(int i) {
	wlevel_ = i;
    }
    
    /**
     * Redirects the steam where the messages should go.
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

    /* for testing */
    public static void main(String args[]) {
        //System.out.println(Messages.format("subl $%d, %%esp\n", 4));
        //System.out.println(Messages.format("a string %1 %%", "String"));
	//System.out.println(Messages.format("a string %%, %1", "String"));
	//System.out.println(Messages.format("a string %s", "String"));
	//System.out.println(Messages.format("a double %f", new Double(12.34)));
	//System.out.println(Messages.format("an int %d", new Integer(12)));
	
	StringBuffer sb = new StringBuffer();
	sb.append("x");
	sb.append(231212);
	System.out.println("Hello");
	System.out.println(sb.toString());
	System.out.println(Messages.format("a hex %x", new Integer(63)));
    }
}








