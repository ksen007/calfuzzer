package benchmarks.hedc;

/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 *
 * @version $Id: Rag.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun 
 */

import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;

import benchmarks.hedc.regexp.*;
import benchmarks.hedc.ethz.util.EtcUtil;

public class Rag extends MetaSearchResult {
    
    private static final String NAME_ = "RAPP Archive ETH Zurich";
    private static final String ADDRESS_ = "http://www.astro.phys.ethz.ch/cgi-bin/showdir?dir=observations/";
    private static final String RESULT_ADDRESS_ = "http://www.astro.phys.ethz.ch/";
    private static final int BUF_SIZE_ = 16384; // actually 32k bytes
    private static DateFormatter dateFormatter1_ = new DateFormatter();
    private static DateFormatter dateFormatter2_ = new DateFormatter();
    private static DateFormatter dateFormatter3_ = new DateFormatter();
    private class RagIterator extends MetaSearchResultIterator {
	
	RagIterator() {
	    h_.put("ARCHIVE", NAME_);
	    h_.put("U_ARCHIVE", EtcUtil.makeURLish(NAME_));
	    h_.put("INSTITUTE_CODE", "Phoenix-2 Spectrometer");
	    h_.put("U_INSTITUTE_CODE", "Phoenix-2%20Spectrometer");
	    h_.put("TYPE_CODE", "Radio");
	    h_.put("U_TYPE_CODE", "Radio");
	    h_.put("U_INFO_CODE", "Full%20Disk");
	    h_.put("U_FORMAT_CODE", "FITS");
	}

	public Object next() {
	    if (resultIterator_ != null) {
		String tmp = null;
		try {
		    //parse date and further information from result
		    tmp = (String) resultIterator_.next();
		    // Messages.debug(-1, "Rag::next before parse");
		    Date d = dateFormatter3_.parse(tmp.substring(59, 73));
		    // Messages.debug(-1, "Rag::next after parse");
		    h_.put("URL", tmp);
		    tmp = RandomDate.format(d);
		    h_.put("U_DATETIME", EtcUtil.makeURLish(tmp));
		    h_.put("DATETIME", tmp);
		} catch (Exception e) {
		    Messages.warn(-1, "Rag::exception during parsing of s=%1 (%2)", tmp.substring(59, 73), e);
		    // e.printStackTrace();
		}
	    }
	    return h_;
	}
    }

    static { 
	dateFormatter1_.applyPattern("yyyy/MM/dd");
	dateFormatter2_.applyPattern("yyyyMMdd");
	dateFormatter3_.applyPattern("yyyyMMddHHmmss");
    }

    private char buffer_[] = null;
    private int charsread_ = 0;

    public Iterator getInfo() {
	return new RagIterator();
    }
    
    public void runImpl() throws Exception {
	// create the URL corresponding to the date
	
	URL u = new URL(ADDRESS_ + dateFormatter1_.format(date));
	Messages.debug(3, "Rag::runImpl - reading data from URL=%1", u);
	// fetch the page into the buffer
	buffer_ = new char[BUF_SIZE_];
	Reader r = new InputStreamReader(u.openStream());
	int size = 1;
	charsread_ = 0;
	while (charsread_ < BUF_SIZE_ && size > 0) {
	    size = r.read(buffer_, 0, BUF_SIZE_ - charsread_);
	    if (size != -1) 
		charsread_ += size;
	    // Messages.debug(3, "Rag::runImpl - read %1 chars", String.valueOf(size));
	    if (!r.ready())
		break;
	}
	r.close();
	// Messages.debug(3, "Rag::runImpl - buffer=\"%1\"XXXXXXX", new String(buffer_, 0, charsread_));
	// create the results
	createResults_();
    }

    private void createResults_() {
	results = new LinkedList();
	String dateTime = dateFormatter2_.format(date);
	// Messages.debug(3, "Rag::createResults_ dateTime=%1", dateTime);
	String regExp = "HREF=\\/(rapp[\\/a-zA-Z0-9]*" + dateTime + "[a-zA-Z0-9_\\.]*)>";
	Messages.debug(3, "Rag::createResults_ regExp=%1", regExp);
	Regexp reg = Regexp.compile(regExp);
	// Messages.debug(3, "Rag::createResults_ compiled regexp");
	Result result;
	int pos = 0;
	while ((result = reg.searchForward(buffer_, pos, charsread_ - pos)) != null) {
	    pos = result.getMatchEnd() + 1;
	    // Messages.debug(3, "Rag:: gotResult:" + result.getMatch(1));
	    results.add(RESULT_ADDRESS_ + result.getMatch(1));
	}
    }
    
    public static void main(String[] args) {
	System.out.println("http://www.astro.phys.ethz.ch/rapp/observations/1999/08/31/19990831104500p.fit.gz".substring(59, 75));
    }
    

}

