/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: Estimator.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 03/02/99  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;
import java.util.*;

/**
 * Abstract base class of all typed items in our 
 * information space. A typed item can stand for some data that is already 
 * materialized in the warehouse, some data that has to be etablished,
 * some dynamic data retrieved from the meta-data database or simply
 * a directory listing.
 */
public class Estimator implements Serializable {

    private static int EST_MAX_COLUMNS = 100;
    private static String EST01_ = "EST01 - lower bound (%1) must be below upper bound (%2) in initializer";
    private static String EST02_ = "EST02 - estimation failed by %1 % (%2)";

    // the columns belong linearly to slots between xMin_ and yMax_
    private double xMin_;
    private double xMax_;
    private String name_;
    
    double ys[] = new double [EST_MAX_COLUMNS];

    public Estimator(String name, double lower_x, double upper_x) {
	
	if (lower_x >= upper_x) {
	    throw new RuntimeException(Messages.format(EST01_, String.valueOf(lower_x), String.valueOf(upper_x)));
	}
	    
	name_ = name;
	xMin_ = lower_x;
	xMax_ = upper_x;
	
	for (int i=0; i < EST_MAX_COLUMNS; i++)
	    ys[i] = 0.0;
    }
    
    private int calcIndex(double x) {
	if (x >= xMax_)
	    return EST_MAX_COLUMNS -1;
	else if (x <= xMin_)
	    return 0;
	else 
	    return (int)Math.round(((x - xMin_)/(xMax_ - xMin_)) * EST_MAX_COLUMNS);
    }

    public void sample (double x, double y) {
	int index = calcIndex(x);
	if (ys[index] != 0.0) {
	    logDeviation_(ys[index], y);
	    ys[index] = (ys[index] + y) / 2; // the latest samples will always count half, 
	// the secondlatest a quarter ..
	} else
	    ys[index] = y;
	// Messages.debug(3, "Estimator(%1)::sample (%2, %3)", name_, ""+x, ""+y);
    }

    public String getName() {
	return name_;
    }
    
    public double estimate (double x) {
	double ret = 0.0;
	int index = calcIndex(x);
	if (ys[index] != 0.0)
	    ret = ys[index];
	else { // try to find the nearest enclosing value
	    int left = index - 1, right = index + 1;
	    boolean go;
	    while (ret == 0.0 && (left >= 0 || right < EST_MAX_COLUMNS)) {
		// do right first, we tend to overestimate
		if (right < EST_MAX_COLUMNS && ys[right] != 0.0) 
		    ret = ys[right];  
		if (ret == 0.0 && left >= 0 && ys[index] != 0.0) 
		    ret = ys[left];
		right ++; left --;
	    }
	}
	// Messages.debug(3, "Estimator(%1)::estimating (%2, %3)", name_, ""+x, ""+ret);
	return ret;
    }

    private void logDeviation_(double predicted, double actual) {
	double deviation = ((predicted - actual) / actual) * 100.0;
	Messages.warn(0, EST02_, String.valueOf(deviation), name_);
    }
    
    public static void main(String[] args) {
	Estimator e = new Estimator("test", 0.0, 100.0);
	int i = 0;
	for (i=0; i<100; i+=10) {
	    System.out.println("Estimation is " + e.estimate(i));
	}

	for (int j=1; j < 3; j++)
	    for (i=0; i<100; i++) 
		e.sample(i, i*Math.random());

	for (i=0; i<100; i+=10) {
	    System.out.println("Estimation is " + e.estimate(i));
	}
    }
    
}
