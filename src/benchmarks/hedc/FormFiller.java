package benchmarks.hedc;


/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: FormFiller.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun
 */

import java.io.*;
import java.util.*;
import benchmarks.hedc.ethz.util.Generator;
import benchmarks.hedc.ethz.util.SystemProperties;

/**
 *
 */
public class FormFiller { 
    private static final String FFS_FILETAG_ = ".ffs";
    private static final String FFS_ID_ = "$Id: FormFiller.java,v 1.1 2001/03/16 17:55:07 praun Exp $";
    private static final String FFS_MIME_ = "text/html";
    private static final String FFS01_ = "FFS01 - failed to store profile for user %1 in file %2 (%3)";
    private static final String FFS02_ = "FFS02 - failed to read profile from file %1 (%2)";
    private static final String FFS03_ = "FFS03 - failed to read template '%1'";
    private static final String FFS04_ = "FFS04 - IOException while interalizing template (%1)";
    private static String FFS_PATH_ = null;
    private static String FFS_SERVER_ = null;
    private static String FFS_BROWSE_SERVER_ = null;
    private static String FFS_WWW_ = null;
    private static String FFS_TEMPLATE_PATH_ = null;
    private static String FFS_META_SEARCH_SERVER_ = null;
    private static Hashtable usersLastParameters_ = null;
    
    static {
	usersLastParameters_ = new Hashtable();
	SystemProperties sp = SystemProperties.getUniqueInstance();
	FFS_SERVER_ = sp.getProperty("ethz.hedc.SERVER");
	FFS_BROWSE_SERVER_ = sp.getProperty("ethz.hedc.BROWSE_SERVER");
	FFS_WWW_ = sp.getProperty("ethz.hedc.WWW");
	FFS_PATH_ = sp.getProperty("ethz.hedc.ROOT") + File.separator + sp.getProperty("ethz.hedc.VAR");
	FFS_TEMPLATE_PATH_ = sp.getProperty("ethz.hedc.ui.FormFiller.TEMPLATE_PATH") + File.separator;
	System.out.println("FFS_TEMPLATE_PATH_"+FFS_TEMPLATE_PATH_);
	FFS_META_SEARCH_SERVER_ = sp.getProperty("ethz.hedc.META_SEARCH_SERVER");
    }

    private Hashtable parameters_ = null;
    private OutputStream ost_;
    private Writer wrt_;
    private long size_ = 0;
    private String form_ = null;

    public FormFiller(OutputStream ost, Hashtable parameters, String form) {
	this(ost, parameters);
	form_ = form;
    }
    
    public FormFiller(Writer wrt, Hashtable parameters, String form) {
	this(wrt, parameters);
	form_ = form;
    }
    
    public FormFiller(OutputStream ost, Hashtable parameters) {
	parameters_ = parameters;
	ost_ = ost;
    }

    public FormFiller(Writer wrt, Hashtable parameters) {
	parameters_ = parameters;
	wrt_ = wrt;
    }
    
    public void go() throws IOException {
	String form = determineForm_();
	size_ = sendResult_(parameters_, form);
    }

    public long fillForm() throws IOException {
	go();
	return size_;
    }

    /**
     * Reads template from file into memory and applies standard 
     * substitutions. 
     * @return null if name cannot be matched to a template file
     * @param name The name of file or name of a form
     */
    public static char[] internalize(String name) {
	char[] buf = null;
	Messages.assertErr(name != null && !name.equals(""));
	try {
	    // first try the name (e.g. name is an absolute pathname)
	    File f = new File(name);
	    if (!f.exists() || !f.isFile() || !f.canRead()) {
		// try name relative to FFS_TEMPLATE_PATH_
		f = new File(FFS_TEMPLATE_PATH_ + name);
		if (!f.exists() || !f.isFile() || !f.canRead()) {
		    // try name relative to FFS_TEMPLATE_PATH_ + .html
		    f = new File(FFS_TEMPLATE_PATH_ + name + ".html");
		    if (!f.exists() || !f.isFile() || !f.canRead()) 
			Messages.error(FFS03_, name);
		}
	    }
	    // at this moment, we should have a proper file hande
	    // create the hashtable for the substitutions
	    Hashtable subst = new Hashtable();
	    Reader r = new FileReader(f);
	    String tmp = Generator.generateString(r, subst);
	    r.close();
	    buf = new char[(int)tmp.length()];
	    tmp.getChars(0, tmp.length(), buf, 0);
	} catch (IOException e) {
	    Messages.error(FFS04_, e);
	}
	return buf;
    }

    private static class Filter implements FilenameFilter {
	public boolean accept(File dir, String name) {
	    boolean ret = false;
	    if (name.endsWith(FFS_FILETAG_)) 
		ret = true;
	    return ret;
	}
    }
    
    private long sendResult_(Hashtable h, String form) throws IOException {
	long ret = 0;
	if (ost_ != null) { // this is unfortunatly not very efficient
	    FileReader fist = new FileReader(form);
	    ret = Generator.generateStream(fist, ost_, h);
	    fist.close();
	}
	else if (wrt_ != null) // much faster, as caching is used
	    ret = Generator.generateThroughBuffer(form, wrt_, h);
	return ret;
    }

    private String determineForm_() {
	String form = (form_ != null) ? form_ : (String) parameters_.get("FORM");
	File f = new File(form);
	if (!f.exists() || !f.isFile() || !f.canRead()) {
	    // try name relative to FFS_TEMPLATE_PATH_
	    f = new File(form = FFS_TEMPLATE_PATH_ + form);
	    if (!f.exists() || !f.isFile() || !f.canRead()) {
		// try name relative to FFS_TEMPLATE_PATH_ + .html
		f = new File(form = form + ".html");
		if (!f.exists() || !f.isFile() || !f.canRead()) 
		    Messages.error(FFS03_, form_);
	    }
	}
	return form;
    }
}
