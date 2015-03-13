/*
 * (c) COPYRIGHT MIT and INRIA, 1996.
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: ObservableProperties.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 28/03/99  cvp 
 *
 */

package benchmarks.hedc.ethz.util;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * An enhanced property class that provides support to monitor changes.
 * This class extends the basic properties class of Java, by providing
 * monitoring support. It also provides more type conversion.
 * @see PropertyMonitoring
 */

public class ObservableProperties extends BaseProperties {

    /** 
     * An array of observers. The size is adapted automatically
     * if necessary
     * @serial
     */
    private PropertyMonitoring observers_[] = new PropertyMonitoring[5];

    /**
     * The number of current observers
     * @serial
     */
    private int observersCount_ = 0;
    private final static String OPR_ID_ = "$Id: ObservableProperties.java,v 1.1 2001/03/16 18:15:21 praun Exp $";
    
    public ObservableProperties() {
	super();	
    }
    
    public ObservableProperties(Properties p) {
	super(p);	
    }


    /**
     * Subscribe for property monitoring.
     * @param observer The object that handles the PropertyMonitoring 
     *    interface.
     */
    public synchronized void registerObserver (PropertyMonitoring o) {
	// Try looking for an empty slot:
	for (int i = 0 ; i < observers_.length ; i++) {
	    if ( observers_[i] == null ) {
		observers_[i] = o;
		return;
	    }
	}
	// Add the observer to the registered oned, resizing array if needed
	if ( observersCount_ + 1 >= observers_.length ) {
	    PropertyMonitoring m[]=new PropertyMonitoring[observers_.length*2];
	    System.arraycopy (observers_, 0, m, 0, observers_.length) ;
	    observers_ = m ;
	}
	observers_[observersCount_++] = o;
    }

    /**
     * Unsubscribe this object from the observers list.
     * @param observer The observer to unsubscribe.
     * @return A boolean <strong>true</strong> if object was succesfully 
     *     unsubscribed, <strong>false</strong> otherwise.
     */

    public synchronized boolean unregisterObserver (PropertyMonitoring o) {
	for (int i = 0 ; i < observers_.length ; i++) {
	    if ( observers_[i] == o ) {
		observers_[i] = null ;
		return true ;
	    }
	}
	return false ;
    }
		
    /**
     * Update a property value.
     * Assign a value to a property. If the property value has really changed
     * notify our observers of the change.
     * @param name The name of the property to assign.
     * @param value The new value for this property, or <strong>null</strong>
     *    if the property setting is to be cancelled.
     * @return A boolean <strong>true</strong> if change was accepted by 
     *    our observers, <strong>false</strong> otherwise.
     */
    
    public synchronized boolean putValue (String name, String value) {
	boolean ret = false;
	boolean isNew = false;
	Messages.debug(3, "ObservableProperties: put "+name+"=["+value+"]");
	// if null value, remove the prop definition, 
	// do not notify any registeres observers
	if ( value == null ) {
	    super.remove(name);
	    return true;
	}

	// otherwise, proceed
	String old = (String) get(name);
	if (old == null) 
	    isNew = true;
	if ((old == null) || (! old.equals(value))) {
	    super.put(name, value);
	    for (int i = 0 ; i < observers_.length ; i++) {
		if (observers_[i] != null) {
		    Messages.debug(3, "ObservableProperties: notifying " +  observers_[i]);
		    ret |= observers_[i].propertyChanged(name);
		}
	    }
	    if (ret == false) {
		if (!isNew)
		    // restore old value, new value did not take any effect
		    super.put (name, old);
		else 
		    // remove new value again, no component took notice of it
		    super.remove(name);
	    }
	}
	return ret;
    }
}

 
