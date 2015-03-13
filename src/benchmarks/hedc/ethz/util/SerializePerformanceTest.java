/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: SerializePerformanceTest.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 27/08/99  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class provides a simple test the performance of the java object 
 * serizlization.
 */
public class SerializePerformanceTest {

    private static Object[] rows_;
    
    /** 
     * make 
     */
    private static void makeData_(int n) {
	rows_ = new Object[n];
	for (int i = 0; i < n; ++ i) {
	    Object[] oneRow = new Object[6];
	    int[] someArray = {i, 2, 3};
	    try {
		oneRow[0] = new URL("http://www.cern.ch" + i);
	    } catch (Exception e) {
		throw new RuntimeException(e.toString());
	    }
	    oneRow[1] = new String("this is an test" + i);
	    oneRow[2] = new String("this is another test" + i);
	    oneRow[3] = new Double(1.23456);
	    oneRow[4] = new Vector();
	    oneRow[5] = someArray;
	    rows_[i] = oneRow;
	}
    }
    
    private static long writeData_(String file) {
	try {
	    FileOutputStream fos = new FileOutputStream(file);
	    ObjectOutputStream out = new ObjectOutputStream(fos);
	    out.writeObject(rows_);
	    out.flush();
	    out.close();
	} catch (IOException ex) {
	    System.err.println("Save NOT successful: " + ex);
	}
	return 0;
    }

    public static void main (String[] args) {
	if (args.length < 2) {
	    printUsage_();
	    System.exit(1);
	}
	
	int n = Integer.parseInt(args[0]);
	makeData_(n);
	Date start = new Date();
	long size = writeData_(args[1]);
	double duration = (double)((new Date()).getTime() - start.getTime()) / (double) 1000;
	System.out.println("Took " + duration + " seconds to serialize the data.");
    }
    
    private static void printUsage_() {
	System.out.println("java SerializePerformance <number> <file>");
	System.out.println("The amount of serialized data is linear to <number>.");
	System.out.println("Try small numbers first. Serialize large numbers to /dev/null");
    }

}
