/*
 * (c) COPYRIGHT MIT and INRIA, 1996.
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: BaseProperties.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 28/03/99  cvp 
 *
 */

package benchmarks.hedc.ethz.util;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Reads properties form a file and converts them into Java objects
 */
public class BaseProperties extends Properties {

    /** 
     * An array of observers. The size is adapted automatically
     * if necessary
     */
    private final static String BPR03_ = "BPR03 - could not load properties from file %1 - ignored";
    private final static String BPR04_ = "BPR04 - failed to retrive property %1 - using default ";
    private final static String BPR_ID_ = "$Id: BaseProperties.java,v 1.1 2001/03/16 18:15:21 praun Exp $";

    /**
     * @serial
     */
    private String source_ = null;

    /**
     * Get this property value, as a boolean.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return A Boolean instance.
     */
    public boolean getBoolean(String name, boolean def) {
	return Boolean.getBoolean(name);
    }

    /**
     * Get this property value, as a String.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An instance of String.
     */
    public String getString(String name, String def) {
	String v = getProperty (name, null);
	if ( v != null )
	    return v ;
	else {
	    Messages.warn(0, BPR04_ + def, name);
	    return def;
	}
    }

    /**
     * Get this property value, as an InetAddress
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An instance of InetAddress or null.
     */
    public InetAddress getInetAddress(String name, InetAddress def) {
	String v = getProperty (name, null);
	InetAddress ret = def;
	if ( v != null ) {
	    try {
		ret = InetAddress.getByName(v);
	    } catch (Exception e) {}
	}
	if (ret == def)
	    Messages.warn(0, BPR04_ + def, name);
	return ret;
    }

    /**
     * Get this property value, as an InetAddress
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An instance of InetAddress or null.
     */
    public URL getURL(String name, URL def) {
	String v = getProperty (name, null);
	URL ret = def;
	try {
	    if ( v != null && v.indexOf(":/") > -1)
		ret = new URL(v);
	    else if (v != null && v.startsWith("/")) {
		ret = new URL("file", null, v);
		Messages.debug(0, "BaseProperties::getURL from %1 (%2)", v, ret);
	    }
	} catch (Exception e) {}
	if (ret == def)
	    Messages.warn(0, BPR04_ + def, name);
	return ret;
    }


    /**
     * Get this property as a String array;
     * by convention, properties that are get as string arrays should be
     * encoded as a list of Strings separated by a space character.
     * @param name The property's name.
     * @param def The default value (if undefined).
     * @return A String array, or <strong>null</strong> if the property
     * is undefined.
     */
    public String[] getStringArray(String name, String def[]) {
	String v = getProperty(name, null);
	if ( v == null ) {
	    Messages.warn(0, BPR04_ + def, name);
	    return def;
	}
	// Parse the property value:
	StringTokenizer st    = new StringTokenizer(v);
	int             len   = st.countTokens();
	String          ret[] = new String[len];
	for (int i = 0 ; i < ret.length ; i++) {
	    ret[i] = st.nextToken();
	}
	return ret;
    }

    /**
     * Get this property as an array of ints.
     * By convention, properties that are get as string arrays should be
     * encoded as a list of Integers each separated by a space character.
     * @param name The property's name.
     * @param def The default value (if undefined).
     * @return An integer array, or <strong>null</strong> if the property
     * is undefined.
     */
    public int[] getIntegerArray(String name, int def[]) {
	String v = getProperty(name, null);
	if ( v == null ) {
	    Messages.warn(0, BPR04_ + def, name);
	    return def;
	}
	// Parse the property value:
	StringTokenizer st    = new StringTokenizer(v);
	int             len   = st.countTokens();
	int          ret[] = new int[len];
	for (int i = 0 ; i < ret.length ; i++) {
	    try {
		ret[i] = Integer.parseInt(st.nextToken());
	    } catch (Exception e) {
		ret[i] = 0;
	    }
	}
	return ret;
    }



    /**
     * Get this property value, as an integer.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An integer value.
     */
    public int getInteger(String name, int def) {
	Integer tmp = Integer.getInteger(name);
	if (tmp == null)
	    return def;
	else 
	    return tmp.intValue();
    }

    public long getLong(String name, long def) {
	Long tmp = Long.getLong(name);
	if (tmp == null)
	    return def;
	else 
	    return tmp.longValue();
    }

    /**
     * Get this property value, as a double.
     * @param name The name of the property.
     * @param def The default value if undefined.
     * @return A double value.
     */
    public double getDouble(String name, double def) {
	String v = getProperty(name, null);
	if ( v != null ) {
	    try {
		return Double.valueOf(v).doubleValue();
	    } catch (NumberFormatException ex) {
		Messages.warn(0, BPR04_ + def, name);	
	    }
	} else {
	    Messages.warn(0, BPR04_ + def, name);
	}
	return def;
    }

    /**
     * Get this property as a File.
     * @param name The name of the property to be fetched.
     * @param def The default value, if the property isn't defined.
     * @return An instance of File.
     */
    public File getFile(String name, File def) {
	String v = getProperty(name, null);
	if ( v != null )
	    return new File (v);
	else {
	    Messages.warn(0, BPR04_ + def, name);
	    return def;
	}
    }

    public BaseProperties() {
	super();	
    }
    
    public BaseProperties(Properties p) {
	super(p);	
    }
    
    /**
     * Initializes the properties from a file or URL
     * @param source A valid file or URL
     */
    public BaseProperties(String file) {
	load(file);
    }
    
    protected void reload() {
	if (source_ != null)
	    load(source_);
    }

    public void load(String source) {
	Messages.debug(4, "BaseProperties: initializing from \"%1\"", source);
	if (source != null) {
	    source_ = source;
	    try {
		// check if source is a URL
		URL u = new URL(source);
		InputStream ist = u.openStream();
		load(u.openStream());
		ist.close();
	    } catch (Exception eouter) {
		// it was probably a file
		try {
		    File f = new File(source);
		    load(new FileInputStream(f));
		} catch(IOException einner) {
		    // neither file nor url
		    einner.printStackTrace();
		    Messages.error(BPR03_, source);
		}
	    }
	}
    }
}

 
