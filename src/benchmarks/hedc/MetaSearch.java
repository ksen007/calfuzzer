package benchmarks.hedc;


/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 * 
 * @version $Id: MetaSearch.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun
 */

import java.util.Hashtable;
import java.util.List;

public interface MetaSearch {
    /* returns a list of MetaSearchResults */
    List search(Hashtable parameters, MetaSearchRequest r);
}
