/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: EstimatorFactory.java,v 1.1 2001/03/16 18:15:21 praun Exp $
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
public class EstimatorFactory {
    
    private static final String ESF01_ = "ESF01 - could not state data directory '%1', will act as ordinary factory";
    private static final String ESF02_ = "ESF02 - failed to read object from file %1 (%2)";
    private static final String ESF03_ = "ESF03 - failed to write object to file %1 (%2)";
    private static final String ESF_FILETAG_ = ".est";
    
    private static EstimatorFactory uniqueInstance_;
    private Hashtable currentEstimators_ = new Hashtable(); 
    /**
     * Path to a directory on the local machine weher estimators are stored.
     * We will not use a URL here as different machines will probably need different 
     * Estimators. It's also acquard to save Estimators back via URL's
     */
    private String dataPath_ = null;

    public Estimator newEstimator(String name, double lower_x, double upper_x) {
	Estimator ret = (Estimator) currentEstimators_.get(name);
	// create it 
	if (ret == null) {
	    // try to read it from a file
	    String filename = dataPath_ + File.separator + name + ESF_FILETAG_;
	    File f = new File(filename);
	    if (f.exists()) {
		try {
		    FileInputStream fis = new FileInputStream(f);
		    ObjectInputStream in = new ObjectInputStream(fis); 
		    ret = (Estimator) in.readObject();
		    in.close();
		} catch (Exception e) {
		    Messages.warn(0, ESF02_, filename, e.getMessage());
		    ret = null;
		}
	    }
	    if (ret == null) {
		// create a new one
		ret = new Estimator(name, lower_x, upper_x);
	    }
	    currentEstimators_.put(name, ret);
	} 
	return ret;
    }

    private EstimatorFactory() {
	dataPath_ = System.getProperty("ethz.util.EstimatorFactory.VAR");
	if (dataPath_ != null && !(new File(dataPath_)).exists()) {
	    dataPath_ = null;
	}
	if (dataPath_ == null) 
	    Messages.warn(0, ESF01_, dataPath_);
    }

    protected void finalize() {
	sync();
    }

    public void sync() {
	Messages.debug(2, "EstimatorFactory: sync to " + dataPath_);
	if (dataPath_ != null) {
	    for (Enumeration en = currentEstimators_.elements(); en.hasMoreElements(); ) {
		String filename = null;
		try {
		    Estimator est = (Estimator) en.nextElement();
		    filename = dataPath_ + File.separator + est.getName() + ESF_FILETAG_;
		    FileOutputStream fos = new FileOutputStream(filename);
		    ObjectOutputStream out = new ObjectOutputStream(fos);
		    out.writeObject(est);
		    out.close();
		} catch (Exception e) {
		    Messages.warn(0, ESF03_, filename, e.getMessage());
		}
	    }
	}
    }

    public static EstimatorFactory getUniqueInstance() {
	if (uniqueInstance_ == null)
	    uniqueInstance_ = new EstimatorFactory();
	return  uniqueInstance_;
    }
}
