/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * $Id: SmartRef.java,v 1.1 2001/03/16 18:15:21 praun Exp $
 * 
 * 03/02/99  cvp
 *
 */

package benchmarks.hedc.ethz.util;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Smart reference to local file or URL
 */
public class SmartRef implements Serializable {

    private static final int SRF_NOT_CHECKED_ = 0;
    private static final int SRF_NONEXISTENT_ = 1;
    private static final int SRF_EXISTS_ = 2;
    private final static String SRF_ID_ = "$Id: SmartRef.java,v 1.1 2001/03/16 18:15:21 praun Exp $";
    private final static String SRF01_ = "SRF01 - could not associate file '%1' with a vaild URL '%2'";
    private final static String SRF02_ = "SRF02 - failed to establish smart reference for \"%1\"";
    private final static String SRF03_ = "SRF03 - failed to determine local hostname";
    private final static String SRF04_ = "SRF04 - hit non-existent URI (%1)";
    private final static String SRF05_ = "SRF05 - hit non-existent Http URI (%1, %2)";
    private final static String SRF06_ = "SRF06 - exception in initializer (%1)";
    private final static String SRF07_ = "SRF07 - failed to create input stream for target %1 / %2";
    private final static String SRF08_ = "SRF08 - failed to remove local file %1";

    private static int SRF_LOCAL_PORT_ = -1;
    private static String SRF_LOCAL_DIR_ROOT_ =  null;
    private static String SRF_LOCAL_URL_PREFIX_ = null;
    private static String SRF_LOCAL_URL_FILE_ = null;
    private static String SRF_LOCAL_TMP_PREFIX_ = null;

    static {
	try {
	    SystemProperties p = SystemProperties.getUniqueInstance();
	    SRF_LOCAL_DIR_ROOT_ = p.getProperty("ethz.util.SmartRef.ROOT", "");
	    SRF_LOCAL_URL_PREFIX_ = p.getProperty("ethz.util.SmartRef.URL", "");
	    SRF_LOCAL_TMP_PREFIX_ = p.getProperty("ethz.util.SmartRef.TMP");
	    URL tmp = new URL(SRF_LOCAL_URL_PREFIX_);
	    SRF_LOCAL_URL_FILE_ = tmp.getFile();
	    SRF_LOCAL_PORT_ = extractPortNo_(SRF_LOCAL_URL_PREFIX_);
	} catch(Exception e) {
	    Messages.error(SRF06_, e);
	}
    }

    private long length_ = -1;
    private volatile int exists_ = 0;
    // the name of the host where this data originated from
    private String hostname_ = null;
    // the port of the service where this data originated from
    private int port_ = -1;
    // can change if the object is transferred over the net, starts with a '/'
    private String localPath_ = null;
    private String urlString_= null;
    private URL url_ = null;
    // the absolute path of this data on the original machine
    // when retrieving an item, we may check first if the item can be found here
    // (that is the sender and receiver of the smart item could be on the same 
    // machine.
    private File file_ = null;
    // the file name that it is given in case of a retrieve
    private String suggestedFilename_ = null; 

    /**
     * An object of this type can be transferred dynamically, the 
     * methods must hide this transparently.
     *
     * @param: location can be a relative path the SRF_LOCAL_DIR_ROOT_ (without leading '/' then
     *         can be an absolute path where the path prefix must be SRF_LOCAL_DIR_ROOT_, 
     *         can be any URL
     */
    public SmartRef(String location) {
	if (location != null) {
	    try {
		// check if source is a URL
		url_ = new URL(location);
		// look if it is local and if so translate it to a local file reference
		hostname_ = url_.getHost();
		port_ = url_.getPort();
		if (hostname_.equals("")) {
		    // pseudo URL file:/ is local and indicates and absolute filename
		    String urlfile = url_.getFile();
		    if (urlfile.startsWith(SRF_LOCAL_DIR_ROOT_)) {
			// localPath start with a '/'
			localPath_ = url_.getFile().substring(SRF_LOCAL_DIR_ROOT_.length());
			file_ = new File(SRF_LOCAL_DIR_ROOT_ + localPath_);
			port_ = SRF_LOCAL_PORT_;
			hostname_ = getLocalHostname_();
			urlString_ = SRF_LOCAL_URL_PREFIX_ + localPath_;
			Messages.debug(5, "SmartRef: case file://: " + urlString_);
		    } else {
			File tmp = new File(urlfile);
			if (tmp.exists()) {
			    //copy it into the tmp dir
			    localPath_ = File.separator + 
				SRF_LOCAL_TMP_PREFIX_ +
				File.separator + tmp.getName();
			    file_ = new File(SRF_LOCAL_DIR_ROOT_ + localPath_);
			    hostname_ = getLocalHostname_();
			    port_ = SRF_LOCAL_PORT_;
			    urlString_ = SRF_LOCAL_URL_PREFIX_ + localPath_;
			    if (!EtcUtil.copyFile(tmp, file_))
				Messages.error(SRF02_, location);
			} else
			    Messages.error(SRF02_, location);
		    }
		} 
		else if (EtcUtil.isLocalHost(hostname_) && port_ == SRF_LOCAL_PORT_) {
		    // pseudo URL http://<localhost> ...
		    localPath_ = url_.getFile();
		    // strip off the http://host/xx/localPath_
		    //                          ^^^^
		    Messages.debug(5, "SmartRef: SRF_LOCAL_URL_FILE_  is " + SRF_LOCAL_URL_FILE_);                  
		    if (SRF_LOCAL_URL_FILE_ != null && !SRF_LOCAL_URL_FILE_.equals("") &&
			localPath_.startsWith(SRF_LOCAL_URL_FILE_)) {
			localPath_ = localPath_.substring(SRF_LOCAL_URL_FILE_.length());
			urlString_ = SRF_LOCAL_URL_PREFIX_ + localPath_;
		    } else 
			// file is served by the local webserver but outside the SRF_LOCAL_URL_FILE_ subdir
			// treat it like a file from outside (else case)
			localPath_ = File.separator + SRF_LOCAL_TMP_PREFIX_ + localPath_.substring(localPath_.lastIndexOf('/'));
			urlString_ = location;
		    Messages.debug(5, "SmartRef: case localhost:localport://: " + SRF_LOCAL_URL_PREFIX_ + "@" + localPath_ );
		}
		else {
		    // real URL (can also be localhost but another port!)
		    localPath_ = url_.getFile(); // we would put it here if we retrieved it
		    // getFilereturns the whole directory string after the hostname:port.... - strip 
		    // strip the directory but keep the leading '/'
		    localPath_ = File.separator + SRF_LOCAL_TMP_PREFIX_ + localPath_.substring(localPath_.lastIndexOf('/'));
		    urlString_ = location;
		    Messages.debug(5, "SmartRef: case http://: " + urlString_);
		}
		file_ = new File(SRF_LOCAL_DIR_ROOT_ + localPath_);
	    } catch (MalformedURLException e) {
		// is probably a file
		hostname_ = getLocalHostname_();
		port_ = SRF_LOCAL_PORT_;
		// check if the path is absolute or relative to the SRF_LOCAL_DIR_ROOT_
		if (location.startsWith(SRF_LOCAL_DIR_ROOT_)) 
		    // absolute path that has the prefix SRF_LOCAL_DIR_ROOT_
		    localPath_ = location.substring(SRF_LOCAL_DIR_ROOT_.length());
		else if (!location.startsWith(File.separator)) 
		    // relative path to SRF_LOCAL_DIR_ROOT_
		    localPath_ = File.separator + location;
		else { 
		    File tmp = new File(location);
		    if (tmp.exists()) {
			//copy it into the tmp dir
			localPath_ = File.separator + 
			    SRF_LOCAL_TMP_PREFIX_ +
			    File.separator + tmp.getName();
			file_ = new File(SRF_LOCAL_DIR_ROOT_ + localPath_);
			if (!EtcUtil.copyFile(tmp, file_))
			    Messages.error(SRF02_, location);
		    } else
			Messages.error(SRF02_, location);
		}
		urlString_ = SRF_LOCAL_URL_PREFIX_ + localPath_;
		file_ = new File(SRF_LOCAL_DIR_ROOT_ + localPath_);
		try {
		    url_ = new URL(urlString_);
		    Messages.debug(5, "SmartRef: assigned url as: " + urlString_);
		} 
		catch (MalformedURLException ex) {
		    Messages.error(SRF01_, SRF_LOCAL_DIR_ROOT_ + localPath_, urlString_);
		}
	    }
	} 
    }
    
    public boolean equals(Object o) {
	boolean ret = false;
	if (o != null && o instanceof SmartRef) {
	    SmartRef rhs = (SmartRef) o;
	    ret = urlString_. equals(rhs.urlString_);
	}
	return ret;
    }

    private String getLocalHostname_() {
	String ret = null;
	try {
	    ret = InetAddress.getLocalHost().getHostName();
	} catch (UnknownHostException e) {
	    Messages.error(SRF03_);
	}
	return ret;
    }
    
    public SmartRef(String location, String suggestedFilename) {
	this(location);
	suggestedFilename_ = suggestedFilename;
    }
    
    public long length() {
	if (length_ <= 0) {
	    if (isLocal()) {
		length_ = file_.length();
		exists_ = SRF_EXISTS_;
	    }
	    else if (url_ != null) {
		try {
		    URLConnection c = url_.openConnection();
		    // c is connected
		    length_ = c.getContentLength();
		    exists_ = SRF_EXISTS_;
		    // abort the conncetion
		    c.getInputStream().close();
		} 
		catch (IOException e) { /* not available, we return -1 */ }
	    }
	}
	return length_;
    }

    /**
     * prefer reading it locally
     */
    public InputStream getInputStream() {
	InputStream ret = null;
	try {
	    if (isLocal()) {
		ret = new FileInputStream(file_);
	    } else if (url_ != null) {
		ret = url_.openStream();
	    }
	} catch (Exception e) {
	    Messages.warn(0, SRF07_, url_, file_);
	}
	return ret;
    }

    /**
     * prefer reading it locally
     */
    public Reader getReader() throws IOException {
	Reader ret = null;
	if (isLocal()) {
	    ret = new FileReader(file_);
	} else if (url_ != null) {
	    ret = new InputStreamReader(url_.openStream());
	}
	return ret;
    }
    
    /** 
     * check first local then remote
     */
    public boolean exists() {
	if (exists_ == SRF_NOT_CHECKED_) {
	    if (isLocal()) {
		exists_ = (file_.exists()) ? SRF_EXISTS_ : SRF_NONEXISTENT_;
	    } else if (url_ != null) {
		try {
		    URLConnection c = url_.openConnection();
		    if (c instanceof HttpURLConnection) {
			HttpURLConnection hc = (HttpURLConnection) c;
			c.connect();
			String response = hc.getResponseMessage();
			if (response != null) {
			    response = response.toLowerCase();
			    // HttpURLConnection.getResponseCode does not work properly for all servers!
			    exists_ = ((response.indexOf("ok") != -1) || (response.indexOf("200") != -1)) ? SRF_EXISTS_ : SRF_NONEXISTENT_;
			}
			if (exists_ == SRF_NONEXISTENT_) {
			    Messages.warn(4, SRF05_, url_.toString(), hc.getResponseMessage());
			}
			hc.disconnect();
		    }
		    else {
			exists_ = SRF_EXISTS_;
			c.getInputStream().close();
		    }
		} 
		catch (IOException e) { 
		    Messages.warn(4, SRF04_, url_.toString());
		    /* not available, we return false */ 
		}
	    }
	}
	Messages.debug(5, "SmartRef::exists() yield " + exists_ + " on " + url_);
	return (exists_ == SRF_EXISTS_);
    }

    public boolean isLocal() {
	boolean ret = false;
	if (file_ != null) {
	    ret = file_.exists();
	    exists_ = (ret) ? SRF_EXISTS_ : SRF_NONEXISTENT_;
	}
	Messages.debug(5,"SmartRef: local (t/f): " + ret);
	return ret;
    }

    public void remove() {
	if (file_ != null && file_.exists())
	    if (!file_.delete())
		Messages.warn(0, SRF08_, file_.getAbsolutePath());
    }
    
    public String getFilename() {
	String ret = null;
	int idx = urlString_.lastIndexOf('/');
	if (idx != -1)
	    ret = urlString_.substring(idx+1);
	Messages.debug(5,"SmartRef: filename was " + ret);
	return ret;
    }
    
    // OK returns the absolute path of the filename
    // fname can be an absolute pathname but must start with SRF_LOCAL_DIR_ROOT_
    // otherwise, fname is assume to be a relative pathname (with leading '/' relative to SRF_LOCAL_DIR_ROOT_)
    private String retrieve_(String filename) throws IOException {
	String ret = null;
	if (isLocal()) {
	    ret = file_.getAbsolutePath();
	    Messages.debug(3, "SmartRef.retrieve: local file " + ret);
	}
	else {
	    // the reference may not be local according to the above conventions, 
	    // nevertheless the service where the reference originated may see the same 
	    // file tree
	    if (filename != null) {
		int idx = localPath_.lastIndexOf(File.separator);
		localPath_ = localPath_.substring(0,idx + 1) + filename;  
	    }
	    File f = new File(SRF_LOCAL_DIR_ROOT_ + localPath_);
	    if (url_ != null) {
		Messages.debug(3, "SmartRef.retrieve: remote file from " + this + " to be placed at " + SRF_LOCAL_DIR_ROOT_ + localPath_);
		URLConnection c = url_.openConnection();
		InputStream ist = c.getInputStream();
		FileOutputStream fost = new FileOutputStream(f);
		BufferedCopy bs = new BufferedCopy(fost, ist);
		bs.copy();
		fost.close();
		ist.close();
		file_ = f;
		ret = file_.getAbsolutePath();
	    }
	}
	Messages.debug(2, "SmartRef::retrieve yield " + ret +" for " + this);
	return ret;
    }

    public String retrieve() throws IOException {
	return retrieve_(suggestedFilename_);
    }


    public String toString() {
	if (isLocal())
	    return file_.getAbsolutePath();
	else 
	    return urlString_;
    }

    private static int extractPortNo_(String urlprefix) {
	int ret = -1;
	if (urlprefix != null) {
	    int start = urlprefix.indexOf(':');
	    int end = -1;
	    String rets = null;
	    if (start != -1)
		start = urlprefix.indexOf(':', start+1); // second colon
	    if (start != -1)
		end = urlprefix.indexOf('/', start+1);
	    if (end != -1)
		rets = urlprefix.substring(start+1, end);
	    else 
		rets = urlprefix.substring(start+1); // no trailling '/'
	    try {
		ret = (new Integer(rets)).intValue();
	    }
	    catch (Exception e) { /* we keep -1 then */ }
	}
	return ret;
    }
    
    public String getURLString() {
	return urlString_;
    }
    
    public URL getURL() {
	return url_;
    }
    
    public static void main(String args[]) throws Exception {
	SmartRef sr1 = new SmartRef("http://snake.inf.ethz.ch:8001/pl/foo2.fits");
	SmartRef sr2 = new SmartRef("http://snake.inf.ethz.ch:8001/pl/foo2.fits");
	sr2.retrieve();
	sr1.remove();
    }

}
