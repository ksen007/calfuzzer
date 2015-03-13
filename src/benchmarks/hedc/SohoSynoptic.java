package benchmarks.hedc;

/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 *
 * @version $Id: SohoSynoptic.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun
 */

import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;

import benchmarks.hedc.regexp.*;
import benchmarks.hedc.ethz.util.EtcUtil;

public class SohoSynoptic extends MetaSearchResult {
    
    private static final String NAME_ = "SOHO Synoptic Data Archive";
    private static final String SOH01_ = "SOH01 - failed to obtain attributes from URL '%1'";
    private static final String SOH02_ = "SOH02 - success obtaining attributes from URL '%1'";
    private static final String ADDRESS_ = "http://sohowww.nascom.nasa.gov/data/synoptic/gif/";
    private static final int BUF_SIZE_ = 16384; 
    private static final DateFormatter dateFormatter_ = new DateFormatter();
    private static final DateFormatter dateFormatter2_ = new DateFormatter();
    private static final Hashtable INSTITUTE_CODES_ = new Hashtable();
    private static final Hashtable TYPE_CODES_ = new Hashtable();
    private static final Hashtable INFO_CODES_ = new Hashtable();
    private static final Hashtable FORMAT_CODES_ = new Hashtable();

    static { 
	dateFormatter_.applyPattern("yyMMdd");
	dateFormatter2_.applyPattern("yyyyMMddHHmm");

	INSTITUTE_CODES_.put("kbou", "Space Env. Lab, Boulder, Colorado");
	INSTITUTE_CODES_.put("khmn", "Holloman AFB, New Mexico");
	INSTITUTE_CODES_.put("htpr", "Haute-Provence");
	INSTITUTE_CODES_.put("lear", "Learmonth");
	INSTITUTE_CODES_.put("meud", "Meudon");
	INSTITUTE_CODES_.put("mitk", "Mitaka");
	INSTITUTE_CODES_.put("nobe", "Nobeyama");
	INSTITUTE_CODES_.put("ondr", "Ondrejov");
	INSTITUTE_CODES_.put("kanz", "Kanzelhoehe Solar Observatory");
	INSTITUTE_CODES_.put("ksac", "Sacramento Peak, New Mexico");
	INSTITUTE_CODES_.put("bbso", "Big Bear Solar Observatory");
	INSTITUTE_CODES_.put("kpno", "Kitt Peak National Observatory");
	INSTITUTE_CODES_.put("mees", "Mees Solar Observatory");
	INSTITUTE_CODES_.put("mwno", "Mt. Wilson National Observatory");
	INSTITUTE_CODES_.put("mwso", "Mt. Wilson Solar Observatory");
	INSTITUTE_CODES_.put("yohk", "Yohkoh Soft X-ray Telescope");
	INSTITUTE_CODES_.put("scds", "SOHO CDS");
	INSTITUTE_CODES_.put("seit", "SOHO EIT");
	INSTITUTE_CODES_.put("ssum", "SOHO SUMER");
	INSTITUTE_CODES_.put("suvc", "SOHO UVCS");
	INSTITUTE_CODES_.put("slas", "SOHO LASCO");
	INSTITUTE_CODES_.put("smdi", "SOHO MDI");
	INSTITUTE_CODES_.put("scel", "SOHO CELIAS");
	INSTITUTE_CODES_.put("svir", "SOHO VIRGO");
	INSTITUTE_CODES_.put("sswa", "SOHO SWAN");
	INSTITUTE_CODES_.put("sgof", "SOHO GOLF");
	INSTITUTE_CODES_.put("scos", "SOHO COSTEP");
	INSTITUTE_CODES_.put("sern", "SOHO ERNE");
	INSTITUTE_CODES_.put("nanc", "Nancay Radioheliograph");
	INSTITUTE_CODES_.put("stra", "Transition Region and Coronal Explorer (TRACE)");
	INSTITUTE_CODES_.put("mlso", "Mauna Loa Solar Obs. at HAO");
	INSTITUTE_CODES_.put("pdmo", "Pic du Midi Observatory");
	INSTITUTE_CODES_.put("kisf", "Kiepenheuer Inst. for Solar Phys.");
	
	TYPE_CODES_.put("bband", "Boradband");
	TYPE_CODES_.put("caiik", "Ca II K3 line");
	TYPE_CODES_.put("cak3p", "Ca II K3 line long exp. for promi.");
	TYPE_CODES_.put("cak3l", "Ca II K line");
	TYPE_CODES_.put("cak1l", "Ca II  K1v line");
	TYPE_CODES_.put("caxvm", "Ca XV synoptic coronal map");
	TYPE_CODES_.put("cogha", "H-alpha Coronagraph");
	TYPE_CODES_.put("cogmk", "MK3 Coronameter");
	TYPE_CODES_.put("doppl", "Dopplergram");
	TYPE_CODES_.put("halph", "H alpha 6563 A");
	TYPE_CODES_.put("lalph", "Lyman-alpha 1216 A");
	TYPE_CODES_.put("heimp", "He I 10830, synoptic map");
	TYPE_CODES_.put("magfe", "Magnetogram, Fe 5250 A");
	TYPE_CODES_.put("magna", "Magnetogram, Na 5896 A");
	TYPE_CODES_.put("magmp", "Magnetogram, synoptic map");
	TYPE_CODES_.put("maglc", "Magnetogram, longi. comp.");
	TYPE_CODES_.put("igram", "Intensitygram");
	TYPE_CODES_.put("radio", "Radio");
	TYPE_CODES_.put("vmgav", "Vectomagnetogram, average");
	TYPE_CODES_.put("vmgci", "Vectomagnetogram, component I");
	TYPE_CODES_.put("vmgcq", "Vectomagnetogram, component Q");
	TYPE_CODES_.put("vmgcu", "Vectomagnetogram, component U");
	TYPE_CODES_.put("vmgcv", "Vectomagnetogram, component V");
	TYPE_CODES_.put("vmgtf", "Vectomagnetogram, transverse field");
	TYPE_CODES_.put("white", "White light");
	TYPE_CODES_.put("softx", "Soft X rays");
	TYPE_CODES_.put("hardx", "Hard X rays");
	TYPE_CODES_.put("164Mz", "164 MHz Radio");
	TYPE_CODES_.put("327Mz", "327 MHz Radio");
	TYPE_CODES_.put("00171", "Fe IX/X line 171 A");
	TYPE_CODES_.put("00195", "Fe XII line 195 A");
	TYPE_CODES_.put("00284", "Fe XV line 284 A");
	TYPE_CODES_.put("00302", "He II line 304 A");
	TYPE_CODES_.put("10830", "He I line 10830 A");
	TYPE_CODES_.put("01216", "1216 A");
	TYPE_CODES_.put("01550", "1550 A");
	TYPE_CODES_.put("01600", "1600 A");
	TYPE_CODES_.put("01700", "1700 A");
	TYPE_CODES_.put("000WL", "White light");
	TYPE_CODES_.put("00171", "Fe IX/X line, 171 A");
	TYPE_CODES_.put("00195", "Fe XII line, 195 A");
	TYPE_CODES_.put("00284", "Fe XV line, 284 A");
	TYPE_CODES_.put("00304", "He II line, 304 A");
	//------------------------------------------------------------------
	//              CDS images
	//------------------------------------------------------------------
	TYPE_CODES_.put("00361", "Fe XVI line, 361 A");
	TYPE_CODES_.put("00368", "Mg IX line, 368 A");
	TYPE_CODES_.put("00584", "He I line, 584 A");
	TYPE_CODES_.put("00630", "O V line, 630 A");
	//------------------------------------------------------------------
	//              LASCO images
	//------------------------------------------------------------------
	TYPE_CODES_.put("c1wlc", "C1 telescope 1.1-3.0 Rs");
	TYPE_CODES_.put("c2wlc", "C2 telescope 2.0-6.0 Rs");
	TYPE_CODES_.put("c3wlc", "C3 telescope 4.0-30.0 Rs");
	//------------------------------------------------------------------
	//              UVCS images
	//------------------------------------------------------------------
	TYPE_CODES_.put("01032", "O VI 1032 A");
	TYPE_CODES_.put("01206", "Si III, 1206 A");
	TYPE_CODES_.put("lalph", "Lyman Alpha 1216 A");
	TYPE_CODES_.put("lbeta", "Lyman Beta 1025 A");
	TYPE_CODES_.put("01238", "N V, 1238  A");
	TYPE_CODES_.put("01242", "Fe XII, 1242 A");
	//------------------------------------------------------------------
	//              SUMER images
	//------------------------------------------------------------------
	TYPE_CODES_.put("00770", "Ne VIII, 770 A");
	TYPE_CODES_.put("00933", "S VI line, 933 A");
	TYPE_CODES_.put("00937", "Lyman Epsilon, 937 A");
	TYPE_CODES_.put("00944", "S VI line, 944 A");
	TYPE_CODES_.put("00977", "C III line, 977 A");
	TYPE_CODES_.put("01548", "C IV line, 1584 A");
	//------------------------------------------------------------------
	//		UVCS-EIT composites
	//------------------------------------------------------------------
	TYPE_CODES_.put("lc171", "LyA 1216 A / Fe IX/X 171 A");
	TYPE_CODES_.put("lc195", "LyA 1216 A / Fe XII 195 A");
	TYPE_CODES_.put("lc284", "LyA 1216 A / Fe XV 284 A");
	TYPE_CODES_.put("lc304", "LyA 1216 A / He II 304 A");
	TYPE_CODES_.put("oc171", "O VI 1032 A / Fe IX/X 171 A");
	TYPE_CODES_.put("oc195", "O VI 1032 A / Fe XII 195 A");
	TYPE_CODES_.put("oc284", "O VI 1032 A / Fe XV 284 A");
	TYPE_CODES_.put("oc304", "O VI 1032 A / He II 304 A");


	INFO_CODES_.put("fd", "Full Disk");
	INFO_CODES_.put("re", "Region of the Sun");
	INFO_CODES_.put("re", "Solar Limb Image");

	FORMAT_CODES_.put("cdf", "Common Data Format");
	FORMAT_CODES_.put("fts", "FITS");
	FORMAT_CODES_.put("gif", "GIF");
	FORMAT_CODES_.put("jpg", "JPEG");
	FORMAT_CODES_.put("mpg", "MPEG-1/2");
	FORMAT_CODES_.put("ppm", "Portable Pixmap");
	FORMAT_CODES_.put("yoh", "Yohkoh format");
    }

    private char buffer_[] = null;
    private int charsread_ = 0;
    private String urlString_ = null;

    private class SohoIterator extends MetaSearchResultIterator {
	
	SohoIterator() {
	    h_.put("ARCHIVE", NAME_);
	    h_.put("U_ARCHIVE", EtcUtil.makeURLish(NAME_));
	}

	public Object next() {
	    if (resultIterator_ != null) {
		try {
		    //parse date and further information from result
		    String urlString = (String) resultIterator_.next();
		    h_.put("URL", urlString);
		    determineAttributes_(h_, urlString);
		} catch (Exception e) {
		    Messages.warn(-1, "Soho::exception during parsing of (%2)", e);
		    // e.printStackTrace();
		}
	    }
	    return h_;
	}
    }

    /*  Broken:
     *  http://sohowww.nascom.nasa.gov/data/synoptic/gif/000123/nanc_164Mz_fd_2023_1046.gif
     *  http://sohowww.nascom.nasa.gov/data/synoptic/gif/000123/nanc_164Mz_fd_2023_0916.gif
     *  http://sohowww.nascom.nasa.gov/data/synoptic/gif/000123/meud_halph_fd_2023_1356.gif
     *  http://sohowww.nascom.nasa.gov/data/synoptic/gif/000213/nanc_164Mz_fd_2113_0919.gif
     *
     *  Ok:
     *  http://sohowww.nascom.nasa.gov/data/synoptic/gif/000212/pdmo_cogha_fd_20000212_0814.gif
     *  http://sohowww.nascom.nasa.gov/data/synoptic/gif/000212/pdmo_cogha_fd_20000212_0945.gif
     */
    private static void determineAttributes_(Hashtable h, String url) {
	int idxl = url.lastIndexOf('/') + 1;
	int idxr = url.indexOf('_'); 
	String tmp = null;
	try {
	    // Messages.debug(2, "Soho:DeterminAttrib1 idxl=%1 idxr=%2 url=%3", String.valueOf(idxl), String.valueOf(idxr), url);
	    tmp = (String) INSTITUTE_CODES_.get(url.substring(idxl, idxr));
	    h.put("U_INSTITUTE_CODE", (tmp == null) ? "unknown" :  EtcUtil.makeURLish(tmp));
	    h.put("INSTITUTE_CODE", tmp);

	    idxl = idxr + 1;
	    idxr = url.indexOf('_', idxl);
	    // Messages.debug(2, "Soho:DeterminAttrib2 idxl=%1 idxr=%2", String.valueOf(idxl), String.valueOf(idxr));
	    tmp = (String) TYPE_CODES_.get(url.substring(idxl, idxr));
	    h.put("U_TYPE_CODE", (tmp == null) ? "unknown" :  EtcUtil.makeURLish(tmp));
	    h.put("TYPE_CODE", tmp);

	    idxl = idxr + 1;
	    idxr = url.indexOf('_', idxl);
	    // Messages.debug(2, "Soho:DeterminAttrib3 idxl=%1 idxr=%2", String.valueOf(idxl), String.valueOf(idxr));
	    tmp = (String) INFO_CODES_.get(url.substring(idxl, idxr));
	    h.put("U_INFO_CODE", (tmp == null) ? "unknown" :  EtcUtil.makeURLish(tmp));
	    
	    idxl = idxr + 1;
	    idxr = url.indexOf('_', idxl);
	    // Messages.debug(2, "Soho:DeterminAttrib4 idxl=%1 idxr=%2", String.valueOf(idxl), String.valueOf(idxr));
	    tmp = url.substring(idxl, idxr);
	    idxl = idxr + 1;
	    idxr = url.indexOf('.', idxl);
	    // Messages.debug(2, "Soho:DeterminAttrib5 idxl=%1 idxr=%2", String.valueOf(idxl), String.valueOf(idxr));
	    tmp += url.substring(idxl, idxr);
	    Date d = null;
	    if (tmp.length() == 12) {
		// Messages.debug(-1, "Soho::determineAttributes before parse");
		d = dateFormatter2_.parse(tmp);
		tmp = RandomDate.format(d);
	    } else if (tmp.length() == 8)
		tmp = "date not parsed";
	    h.put("U_DATETIME",  EtcUtil.makeURLish(tmp));
	    h.put("DATETIME", tmp);
	    
	    idxl =  url.lastIndexOf(".");
	    // Messages.debug(2, "Soho:DeterminAttrib6 idxl=%1", String.valueOf(idxl));
	    tmp = (String) FORMAT_CODES_.get(url.substring(idxl+1));
	    // Messages.debug(-1, "Soho::determineAttributes formatcode=%1", tmp);
	    h.put("U_FORMAT_CODE", (tmp == null) ? "unknown" :  EtcUtil.makeURLish(tmp));
	    // Messages.debug(3, SOH02_, url);
	} catch (Exception e) {
	    Messages.warn(0, SOH01_, url);
	    // Messages.debug(-1, "Soho::exception during parsing (%1)", e);
	    e.printStackTrace();
	    if (h.get("INSTITUTE_CODE") == null)
		h.put("INSTITUTE_CODE", "unknown");
	    if (h.get("U_INSTITUTE_CODE") == null)
		h.put("U_INSTITUTE_CODE", "unknown");
	    if (h.get("TYPE_CODE") == null)
		h.put("TYPE_CODE", "unknown");
	    if (h.get("U_TYPE_CODE") == null)
		h.put("U_TYPE_CODE", "unknown");
	    if (h.get("U_INFO_CODE") == null)
		h.put("U_INFO_CODE", "unknown");
	    if (h.get("DATE_TIME") == null)
		h.put("DATE_TIME", "unknown");
	    if (h.get("U_DATE_TIME") == null)
		h.put("U_DATE_TIME", "unknown");
	    if (h.get("U_FORMAT_CODE") == null)
		h.put("U_FORMAT_CODE", "unknown");
	} 
    }

    public Iterator getInfo() {
	return new SohoIterator();
    }

    public void runImpl() throws Exception {
	// create the URL corresponding to the date
	Messages.debug(-1, "SohoSynoptic::runImpl - date=%1", date);
	urlString_ = ADDRESS_ + dateFormatter_.format(date) + '/';
	URL u = new URL(urlString_);
	System.out.println("urlString_ is"+urlString_);
	// Messages.debug(3, "SohoSynoptic::runImpl - reading data from URL=%1", u);
	// fetch the page into the buffer
	buffer_ = new char[BUF_SIZE_];
	Reader r = new InputStreamReader(u.openStream());
	int size = 1;
	charsread_ = 0;
	while (charsread_ < BUF_SIZE_ && size > 0) {
	    size = r.read(buffer_, 0, BUF_SIZE_ - charsread_);
	    if (size != -1) 
		charsread_ += size;
	    // Messages.debug(3, "Soho::runImpl - read %1 chars", String.valueOf(size));
	    if (!r.ready())
		break;
	}
	r.close();
	// Messages.debug(3, "Soho::runImpl - buffer=\"%1\"XXXXXXX", new String(buffer_, 0, charsread_));
	// create the results
	createResults_();
    }

    private void createResults_() {
	results = new LinkedList();
	Regexp reg = Regexp.compile("<A HREF=\\\"([a-zA-Z0-9_\\.]*)\\\">");
	Result result;
	int pos = 0;
	while ((result = reg.searchForward(buffer_, pos, charsread_ - pos)) != null) {
	    pos = result.getMatchEnd() + 1;
	    results.add(urlString_ + result.getMatch(1));
	}
    }
    
    /* for test */
    public static void main (String args[]) {
	Hashtable h = new Hashtable();
	determineAttributes_(h, "http://sohowww.nascom.nasa.gov/data/synoptic/gif/990831/kanz_halph_fd_19990831_1050.gif");
	System.out.println(h);
    }

}

