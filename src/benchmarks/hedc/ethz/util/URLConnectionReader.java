/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: URLConnectionReader.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 05/01/99  cvp 
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;
import java.net.*;


/**
 * Reads a specified URL into a string
 */
public class  URLConnectionReader {
    
    public static void readURL(URL url, OutputStream ost) throws IOException {
	URLConnection c = url.openConnection();
	//System.out.println("Content-Length is " + c.getContentLength());
	InputStream isr = c.getInputStream();
	BufferedCopy bc = new BufferedCopy(ost, isr);
	bc.copy();
	isr.close();
    }
    
    public static int checkUrl (String urlString) throws Exception {
	int ret = 0;
	URL url =  new URL(urlString);
	URLConnection c = url.openConnection();
	if (c instanceof HttpURLConnection) {
	    HttpURLConnection hc = (HttpURLConnection) c;
	    hc.connect();
	    ret = hc.getResponseCode();
	    hc.disconnect();
	} else
	    Messages.error(UCR01_, urlString);
	return ret;
    }

    public static void main(String[] args) {
	if (args.length != 1)
	    printUsage_();
	else {
	    try {
		System.out.println(checkUrl(args[0]));
		System.exit(UCR_OK_EXIT_CODE_);
	    } catch (Exception e) {
		System.exit(UCR_ERROR_EXIT_CODE_);
	    }
	}
    }
    
    /**
       public static void main(String[] args) {
       int ret = 1;
       if (args.length != 1)
       printUsage_();
       else {
       try {
       URL url = new URL(args[0]);
       readURL(url, System.out);
       ret = 0;
       } catch (Exception e) {
       System.out.println(e.getMessage());
       }
       }
       System.exit(ret);
       }
    */
	
    private static void printUsage_() {
	System.out.println("Usage: java ethz.util.URLConnectionReader <url>");
    }

    private static final String UCR01_ = "UCR01 - could not establish a HTTP connection to %1"; 
    private static final int UCR_ERROR_EXIT_CODE_ = 1;
    private static final int UCR_OK_EXIT_CODE_ = 0;
}
	
