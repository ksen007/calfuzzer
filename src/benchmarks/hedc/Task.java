package benchmarks.hedc;

/*
 * Copyright (C) 1998 by ETHZ/INF/CS
 * All rights reserved
 *
 * @version $Id: Task.java,v 1.1 2001/03/16 17:55:07 praun Exp $
 * @author Christoph von Praun
 */



public abstract class Task implements Cloneable, Runnable {

    /**
     * the thread that handles this task.
     */
    protected Thread thread_ = null;

    /** 
     * Item will not be processed by the Worker 
     * executor if set
     */ 
    public boolean valid = true;

    /**
     *  The request thread that indirectly issued this task
     */
    public synchronized void setThread(Thread t) {
	if (!valid)
	    thread_.interrupt();
	else
	    thread_ = t;
    }
    
    public abstract void cancel();
    
    public void run() {
	try {
	    runImpl();
	} catch(Exception e) {
	    Messages.warn(-1, "Task::run exception=%1", e);
	    e.printStackTrace();
	}
	thread_ = null;
    }

    public abstract void runImpl() throws Exception;
}
