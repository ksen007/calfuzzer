/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: EtcUtil.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 03/02/99  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;
import java.net.*;
import java.util.Date;

public class EtcUtil {
    
    private static final String ETC01_ = "ETC01 - unable to determine hostname from URI %1";
    private static final String ETC02_ = "ETC02 - failed to determine protocol from URI %1";
    private static final String ETC03_ = "ETC03 - failed to determine file from URI %1";
    private static final String ETC04_ = "ETC04 - failed to copy src file (%1) to dest (%2) - %3 bytes copied";

    private static String localHostName_ = null;

    public static boolean copyFile(File src, File dst) {
	boolean ret = false;
	long size = -1;
	try {
	    Messages.debug(3, "EtcUtil::copy file %1 to %2", src, dst);
	    if (src != null && dst != null && src.exists()) {
		FileInputStream fist = new FileInputStream(src);
		FileOutputStream fost = new FileOutputStream(dst);
		size = (new BufferedCopy(fost, fist)).copy();
		ret = (size == src.length());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	if (ret == false)
	    Messages.warn(0, ETC04_, src, dst, String.valueOf(size));
	return ret;
    }

    public static String getTmpFilename() {
	return (new Date()).getTime() + ".tmp";
    }

    public static String changeFileTag(String filename, String newTag) {
	String ret = null;
	if (filename != null) {
	    int idx = filename.lastIndexOf('.');
	    if (idx >= 0) {
		ret = filename.substring(0, idx);
		if (newTag != null)
		    ret += newTag;
	    }
	}
	return ret;
    }

    public static String getLocalHostname() {
	if (localHostName_ == null)
	    try {
		localHostName_ = InetAddress.getLocalHost().getHostName();
	    } catch (UnknownHostException e) {}
	return localHostName_;
    }

    public static boolean isLocalHost(String h1) {
	boolean ret = false;
	try {
	    InetAddress a1 = InetAddress.getByName(h1);
	    if (a1 != null)
		ret = InetAddress.getLocalHost().getHostAddress().equals(a1.getHostAddress());
	} catch (UnknownHostException e) {}
	return ret;
    }

    /**
     * A hack because the following does not work for URLS like rmi://XX in jdk 1.1
     * URL url = new URL(uri);
     */
    public static String getHostNameFromURI(String uri) throws Exception {
	String ret = null;
	int idxLeft = uri.indexOf("://");
	int idxRight = -1;
	if (idxLeft > 0) {
	    idxRight = uri.indexOf(":", idxLeft + 3);
	    if (idxRight < 0) 
		idxRight = uri.indexOf("/", idxLeft + 3);
	    if (idxRight < 0) 
		idxRight = uri.length();
	}
	if (idxLeft > 0 && idxRight > 0)
	    ret = uri.substring(idxLeft + 3, idxRight);
	if (ret == null)
	    Messages.error(ETC01_, uri);
	Messages.debug(3, "ServiceAdmin::getHostNameFromURI yield %1", ret);
	return ret;
    }
    
    /**
     * A hack because the following does not work for URLS like rmi://XX in jdk 1.1
     * URL url = new URL(uri);
     */
    public static String getProtocolFromURI(String uri) throws Exception {
	String ret = null;
	int idxRight = uri.indexOf("://");
	if (idxRight > 0)
	    ret = uri.substring(0, idxRight);
	else
	    Messages.error(ETC02_, uri);
	Messages.debug(3, "ServiceAdmin::getProtocolFromURI yield %1", ret);
	return ret;
    }

    /**
     * A hack because the following does not work for URLS like rmi://XX in jdk 1.1
     * URL url = new URL(uri);
     */
    public static String getFileFromURI(String uri) throws Exception {
	String ret = null;
	int idxLeft = uri.indexOf("://");
	if (idxLeft > 0) {
	    idxLeft = uri.indexOf('/', idxLeft + 4);
	}
	if (idxLeft > 0)
	    ret = uri.substring(idxLeft +1);
	else
	    Messages.error(ETC03_, uri);
	Messages.debug(3, "ServiceAdmin::getFileFromURI yield %1", ret);
	return ret;
    }

    /**
     * A hack because the following does not work for URLS like rmi://XX in jdk 1.1
     * URL url = new URL(uri);
     */
    public static int getPortFromURI(String uri) throws Exception {
	int ret = -1;
	int idxLeft = uri.indexOf("://");
	int idxRight = -1;
	if (idxLeft > 0) {
	    idxLeft = uri.indexOf(':', idxLeft + 4);
	    if (idxLeft > 0)
		idxRight = uri.indexOf('/', idxLeft);
	}
	if (idxLeft > 0 && idxRight > 0)
	    ret = Integer.parseInt(uri.substring(idxLeft + 1, idxRight));
	Messages.debug(3, "ServiceAdmin::getPortFromURI yield %1", "" + ret);
	return ret;
    }

    public static String makeURLish(String in) {
	int length = in.length();
	StringBuffer sb = new StringBuffer(length);
	for (int i = 0; i < length; ++i) {
	    char c = in.charAt(i);
	    if (Character.isLetterOrDigit(c) || c == '.' || c == '/')
		sb.append(c);
	    else {
		sb.append('%');
		if (c < 16)
		    sb.append('0');
		sb.append(Integer.toHexString(c));
	    }
	}
	return sb.toString();
    }

    /*
     * for test
     */
    public static void main(String[] args) {
	System.out.println(makeURLish("Hello World"));
    }

}
