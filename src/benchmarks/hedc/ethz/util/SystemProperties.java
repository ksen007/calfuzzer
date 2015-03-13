/*
 * (c) COPYRIGHT MIT and INRIA, 1996.
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: SystemProperties.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 28/03/99  cvp 
 *
 */

package benchmarks.hedc.ethz.util;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Features also the Java built in system properties.
 */

public class SystemProperties extends ObservableProperties {

    private final static String SPR01_ = "SPR01 - did not initialize properties from file %1";
    private final static String SPR02_ = "SPR02 - exception during the initialization (%1)";
    private final static String SPR03_ = "SPR03 - reloaded properties from file %1";
    /**
     * Reference to the unique instance of this class.
     */
    private static SystemProperties uniqueInstance_ = null;
    private static String source_ = null;

    private SystemProperties() {
	super(System.getProperties());
	Messages.debug(3, "SystemProperties are %1", this);
    }

    /**
     * Singleton pattern: returns the unique instance of this class;
     * lazy initialization.
     *
     * @return The unique instance.
     */
    public static SystemProperties getUniqueInstance() {
	try {
	    if (uniqueInstance_ == null) {
		if (source_ == null)
		    setSource(System.getProperty("ethz.util.SystemProperties.SOURCE"));
		uniqueInstance_ = new SystemProperties();
		uniqueInstance_.load(source_);
		System.setProperties(uniqueInstance_);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    Messages.error(SPR02_, e.toString());
	}
	return uniqueInstance_;
    }

    public static void setSource(String source) {
	if ( source != null && !source.equals(source_) &&
	     (new File(source)).isFile()) {
	    source_ = source;
	    if (uniqueInstance_ != null)
		Messages.warn(0, SPR03_, source);
	    uniqueInstance_ = null;
	} else
	    Messages.warn(0, SPR01_, source);
    }
}

 
