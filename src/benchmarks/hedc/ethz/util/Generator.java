/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: Generator.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 05/01/99  cvp 
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;
import java.util.*;

/*
 * This class performs a substitution of macros with actual values
 * in an input stream or file. The funcionality is useful e.g. for 
 * the generation of html files from templates where contents has 
 * to be filled in when the page is delivered. The macros are delimited 
 * by '@' characters that enclose the name of he macro. The '@@' escapes
 * the macro delimiter and yields an ordinary single '@' character
 * in the output.
 */
public class Generator {

    private static final String GEN01_ = "GEN01 - file %1 does not exist, is not a file or cannot be read";
    private static final String GEN02_ = "GEN02 - %1 is a directory, you cannot write fo it";
    private static final String GEN03_ = "GEN03 - cannot serve request - max page size exceeded";
    private static final int GEN_MAX_PAGE_SIZE_ = 32552;
    private static final int GEN_MAX_TAG_SIZE_ = 32;

    /*
     * The number of threads that can efficiently operate concurrently
     */
    private static int GEN_POOL_CAPACITY_ = 0;
    private static int GEN_POOL_CAPACITY_NAMED_ = 0;

    /* There are unnamed and named buffers - the latter contain 
     * prefetched contents
     * The maximum number of unnamed buffers is GEN_POOL_CAPACITY_
     * The number of named buffers is GEN_POOL_CAPACITY_NAMED_
     */
    private static final Vector bufferPool_ = new Vector();
    private static final TreeMap namedBufferPool_ = new TreeMap();
    
    static {
	SystemProperties sp = SystemProperties.getUniqueInstance();
	GEN_POOL_CAPACITY_ = sp.getInteger("ethz.util.Generator.POOL_CAPACITY", 4);
	GEN_POOL_CAPACITY_NAMED_ = sp.getInteger("ethz.util.Generator.NAMED_POOL_CAPACITY", 8);
    }
    
    /**
     * To alleviate the work of the garbage collector. 
     * We allocate large chungks of mem here!
     */
    private static char[] getBuffer_() {
	char[] ret = null;
	synchronized (bufferPool_) {
	    if (ret == null) {
		// take unnamed buffer
		if (bufferPool_.size() > 0) {
		    ret = (char[]) bufferPool_.elementAt(0);
		    bufferPool_.removeElementAt(0);
		} else
		    ret = new char[GEN_MAX_PAGE_SIZE_];
	    } 
	}
	return ret;
    }
    
    private static void putBuffer_(char[] b) {
	if (GEN_POOL_CAPACITY_ > 0) {
	    synchronized (bufferPool_) {
		if (bufferPool_.size() < GEN_POOL_CAPACITY_)
		    bufferPool_.addElement(b);
	    }
	}
    }

        /**
     * To alleviate the work of the garbage collector. 
     * We allocate large chungks of mem here!
     */
    private static char[] getBuffer_(String name) {
	char[] ret = null;
	if (GEN_POOL_CAPACITY_NAMED_ > 0){
	    synchronized (namedBufferPool_) {
		ret = (char[]) namedBufferPool_.get(name);
	    }
	}
	return ret;
    }
    
    private static synchronized void putBuffer_(String name, char[] b) {
	if (GEN_POOL_CAPACITY_NAMED_ > 0){
	    synchronized (namedBufferPool_) {
		if (namedBufferPool_.size() >= GEN_POOL_CAPACITY_NAMED_)
		    namedBufferPool_.remove(namedBufferPool_.lastKey());
		namedBufferPool_.put(name, b);
	    }
	}
    }
	
    /**
     * side effect: puts buffer in the cache
     */ 
    private static char[] openTemplateFile_(String templateFile) throws IOException {
	// look if the file is in the named buffer pool
	char[] ret = getBuffer_(templateFile);
	Messages.debug(3, "Generator::openBuffer() ret = %1", ret);
	if (ret == null) {
	    // open the file and read it into a buffer
	    File f = new File(templateFile);
	    Messages.debug(3, "Generator::openBuffer() read from file %1", templateFile);
	    if (!f.exists() || !f.isFile() || !f.canRead())
		Messages.error(GEN01_, templateFile);
	    // allocate buffer and read it in
	    FileReader fist = new FileReader(f); 
	    ret = new char[(int)f.length()];
	    int i=0, j;
	    while ((j = fist.read(ret, i, ret.length - i)) > 0) {
		Messages.debug(3, "Generator:::openBuffer read %1 bytes", String.valueOf(j));
		i+=j;
	    }
	    fist.close();
	    putBuffer_(templateFile, ret);
	}
	return ret;
    }
    
     private Generator() {}
    
    /**
     * Does the actual substitution.
     *
     * @return A string that contains the character sequence 
     *   of the template but ith substituted macros.
     * @param subst
     *   A hashtable that contains macroname / substitution pairs. 
     * @exception IOException
     *   Thrown if the input stream cannot be read.
     */
    public static String generateString(Reader r, Hashtable subst) throws IOException {
	StringWriter sw = new StringWriter();
	generateStream(r, sw, subst);
	return sw.toString();
    };
    

    /**
     * Does the actual substitution and yields the result in a file.
     *
     * @exception IOException
     *   Raised if the output file cannot be written or the input file 
     *   cannot be read.
     * @param outilename 
     *   The absolute path of the output file.
     * @param subst
     *   A hashtable that contains macroname / substitution pairs. 
     */
    public static long generateFile (Reader r, String outfilename, Hashtable subst)  throws IOException {
	File outfile = new File(outfilename);
	if (outfile.isDirectory())
	    Messages.error(GEN02_, outfilename);
	FileWriter fost = new FileWriter(outfile);
	long ret = generateStream (r, fost, subst);
	fost.close();
	return ret;
    }

    /**
     * Does the actual substitution and writes the result to a character 
     * stream (Reader).
     *
     * @param fost
     *   The output stream that absorbs the result of the substitution.
     * @param subst
     *   The macroname / substitution pairs.
     * @exception IOException
     *   Raised if the output stream is invalid or cannot be written or if the 
     *   template input stream is broken.
     */
    public static long generateStream (Reader fist, Writer fost, Hashtable subst) throws IOException {
	long cnt = 0;
	int c = 0;
	while (c != -1 && (c = fist.read()) != -1) {
	    if (c != '@') {
		fost.write(c);
		cnt ++;
	    }
	    /* 
	     * collect the string up to the next '@' into a String and compate it to 
	     * "Model". If so, replace the while by the fullModuleName and 
	     * continue in the outer while loop
	     */
	    else {
		StringWriter sw = new StringWriter();
		while ((c = fist.read()) != -1 && c != '@')
		    sw.write(c);
		// check if you find such item in the lookuptable
		if (sw.toString().equals("")) {
		    // two consecutive @@ should be escaped and yield one @ in the 
		    // in the target stream
		    fost.write('@');
		    cnt ++;
		}
		else {
		    Object repl = subst.get(sw.toString());
		    if (repl != null) {
			String tmp = repl.toString();
			fost.write(tmp);
			cnt += tmp.length();
		    }
		}
		// otherwise we just leave it blank
	    }
	}
	return cnt;
    }

    /**
     * Does the actual substitution and writes the result to a binary 
     * stream (OutputStream). 
     *
     * @param fost
     *   The output stream that absorbs the result of the substitution.
     * @param subst
     *   The macroname / substitution pairs.
     * @exception IOException
     *   Raised if the output stream is invalid or cannot be written or if the 
     *   template input stream is broken.
     */
    public static long generateStream (Reader fist, OutputStream fost, Hashtable subst) throws IOException {
	 long cnt = 0;
	 int c = 0;
	 while (c != -1 && (c = fist.read()) != -1) {
	     if (c != '@') {
		 fost.write(c);
		 cnt ++;
	     }
	     
	     /* 
	      * Collect the string up to the next '@' into a String and compare it to 
	      * the macro name. If so, replace the while by the fullModuleName and 
	      * continue in the outer while loop
	      */
	     else {
		 StringWriter sw = new StringWriter();
		 while ((c = fist.read()) != -1 && c != '@')
		     sw.write(c);
		 // check if you find such item in the lookuptable
		 if (sw.toString().equals("")) {
		     // two consecutive @@ should be escaped and yield one @ in the 
		     // in the target stream
		     fost.write('@');
		     cnt ++;
		 }
		 else {
		     Object repl = subst.get(sw.toString());
		     if (repl != null) {
			 String tmp = repl.toString();
			 fost.write(tmp.getBytes());
			 cnt += tmp.length();
		     }
		 }
		 // otherwise we just leave it blank
	     }
	 }
	 return cnt;
    }

    /**
     * Inputbuffers are named buffers, outputbuffers are unnamed
     */
    public static long generateThroughBuffer(String filename, Writer fost, Hashtable subst) throws IOException {
	// allocate buffer
	CharArrayWriter sw = new CharArrayWriter(GEN_MAX_TAG_SIZE_);
	char[] inputBuf = openTemplateFile_(filename);
	char[] outputBuf = getBuffer_();
	int  cnt = 0, i = 0, j = 0, k = 0;

	for (j=0; j < inputBuf.length; ++j) {
	    if (inputBuf[j] != '@')
		outputBuf[cnt++] = inputBuf[j];
	    
	    /* 
	     * Collect the string up to the next '@' into a String and compare it to 
	     * the macro name. If so, replace the while by the fullModuleName and 
	     * continue in the outer while loop
	     */
	    else {
		sw.reset();
		while (j < inputBuf.length - 1 && inputBuf[++j] != '@')
		    sw.write(inputBuf[j]);
		// check if you find such item in the lookuptable
		if ("".equals(sw.toString())) {
		    // two consecutive @@ should be escaped and yield one @ in the 
		    // in the target stream
		    outputBuf[cnt++] = '@';
		    cnt ++;
		}
		else {
		    Object repl = subst.get(sw.toString());
		    if (repl != null) {
			String tmp = repl.toString();
			int length = tmp.length();
			for (k = 0; k < length; ++k) 
			    outputBuf[cnt++] = tmp.charAt(k);
		    }
		}
		// otherwise we just leave it blank
	    }
	}

	// copy buf to fost in one go!
	fost.write(outputBuf, 0, cnt);
	//for (j=0; j < outputBuf.length; j++)
	//    System.out.print(outputBuf[j]);
	putBuffer_(outputBuf);
	return (long)cnt;
    }

    /**
     * Through unnamed buffers
     */
    public static long generateThroughBuffer(Reader fist, Writer fost, Hashtable subst) throws IOException {
	// allocate buffer
	CharArrayWriter sw = new CharArrayWriter(GEN_MAX_TAG_SIZE_);
	char[] inputBuf = getBuffer_();
	char[] outputBuf = getBuffer_();
	int  cnt = 0, i = 0, j = 0, k = 0;
	
	while (i < GEN_MAX_PAGE_SIZE_ && (j = fist.read(inputBuf, i, GEN_MAX_PAGE_SIZE_ - i)) > 0)
	    i+=j;
	if (i >= GEN_MAX_PAGE_SIZE_)
	    Messages.error(GEN03_);

	for (j=0; j < i; ++j) {
	    if (inputBuf[j] != '@')
		outputBuf[cnt++] = inputBuf[j];
	    
	    /* 
	     * Collect the string up to the next '@' into a String and compare it to 
	     * the macro name. If so, replace the while by the fullModuleName and 
	     * continue in the outer while loop
	     */
	    else {
		sw.reset();
		while (j < i && inputBuf[++j] != '@')
		    sw.write(inputBuf[j]);
		// check if you find such item in the lookuptable
		if ("".equals(sw.toString())) {
		    // two consecutive @@ should be escaped and yield one @ in the 
		    // in the target stream
		    outputBuf[cnt++] = '@';
		    cnt ++;
		}
		else {
		    Object repl = subst.get(sw.toString());
		    if (repl != null) {
			String tmp = repl.toString();
			int length = tmp.length();
			for (k = 0; k < length; ++k) 
			    outputBuf[cnt++] = tmp.charAt(k);
		    }
		}
		// otherwise we just leave it blank
	    }
	}

	putBuffer_(inputBuf);
	// copy buf to fost in one go!
	fost.write(outputBuf, 0, cnt);
	putBuffer_(outputBuf);
	return (long)cnt;
    }


    /** 
     * Template is given as an argument , user manages the input buffers 
     */
    public static long generate(Writer w, Hashtable subst, char[] inputBuf) throws IOException {
	char[] outputBuf = getBuffer_();
	CharArrayWriter sw = new CharArrayWriter(GEN_MAX_TAG_SIZE_);
	int  cnt = 0, j = 0, k = 0;
	
	for (j=0; j < inputBuf.length; ++j) {
	    if (inputBuf[j] != '@')
		outputBuf[cnt++] = inputBuf[j];
	    
	    /* 
	     * Collect the string up to the next '@' into a String and compare it to 
	     * the macro name. If so, replace the while by the fullModuleName and 
	     * continue in the outer while loop
	     */
	    else {
		sw.reset();
		while (j < inputBuf.length && inputBuf[++j] != '@')
		    sw.write(inputBuf[j]);
		// check if you find such item in the lookuptable
		if ("".equals(sw.toString())) {
		    // two consecutive @@ should be escaped and yield one @ in the 
		    // in the target stream
		    outputBuf[cnt++] = '@';
		    cnt ++;
		}
		else {
		    Object repl = subst.get(sw.toString());
		    if (repl != null) {
			String tmp = repl.toString();
			int length = tmp.length();
			for (k = 0; k < length; ++k) 
			    outputBuf[cnt++] = tmp.charAt(k);
		    }
		}
		// otherwise we just leave it blank
	    }
	}
	
	w.write(outputBuf, 0, cnt);
	putBuffer_(outputBuf);
	return (long)cnt;
    }
}
