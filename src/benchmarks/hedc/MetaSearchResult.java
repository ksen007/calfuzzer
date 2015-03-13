package benchmarks.hedc;

/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 *
 * @version $Id: MetaSearchResult.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun 
 */

import java.util.*;

public abstract class MetaSearchResult extends Task implements Cloneable {
    public Date date = null;
    public List results = null;
    public MetaSearchRequest request = null;
    private boolean completed_ = false;
    
    protected abstract class MetaSearchResultIterator implements Iterator {
	protected Hashtable h_ = new Hashtable();
	protected Iterator resultIterator_ = null;
	protected boolean firstSeen_ = false;
	
	protected MetaSearchResultIterator () {
	    resultIterator_ = (results == null || results.size() == 0) ? null : results.iterator();
	}

	public boolean hasNext() {
	    boolean ret = false;
	    if (resultIterator_ != null) 
		ret = resultIterator_.hasNext();
	    else if (firstSeen_ == false) {
		if (!completed_) // item was canceled
		    h_.put("MESSAGE", "timed out");
		else 
		    h_.put("MESSAGE", "no results");
		firstSeen_= ret = true;
	    }
	    return ret;
	}
	
	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }

    public abstract Iterator getInfo();

    public synchronized void cancel() {
	if (thread_ != null) {
	    thread_.interrupt();
	}
	request = null;
	valid = false;
    }

    public void run() {
	super.run();
	completed_ = true;
	request.countDownInterrupt();
    }

    public static MetaSearchResult cloneTask(MetaSearchResult t) {
	MetaSearchResult ret = null;
	if (t != null && t instanceof Task) 
	    try { 
		ret = (MetaSearchResult) t.clone();
	    } catch (CloneNotSupportedException e) {
		Messages.debug(-1, "MetaSearchResult::ConleNotSupported exception=%1", e);
		// e.printStackTrace();
	    }
	return ret;
    }
    
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("##");
	for (Iterator it = getInfo(); it.hasNext(); ) {
	    Hashtable h = (Hashtable) it.next();
	    sb.append(h);
	    sb.append("#");
	}
	sb.append("#");
	return sb.toString();
    }
}







