package benchmarks.hedc;

/*
* Copyright (C) 1998 by ETHZ/INF/CS
* All rights reserved
*
* @version $Id: MetaSearchImpl.java,v 1.1 2001/03/16 17:55:07 praun Exp $
* @author Christoph von Praun
*/

import benchmarks.EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import benchmarks.hedc.ethz.util.Generator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * An object of type RequestDispatchServlet is the entry point for http requests to the HEDC system.
 * The communication with the WWW server is done through the Servlet API.
 */
public class MetaSearchImpl implements MetaSearch {

    private static final String TFA01_ = "TFA01 - failed to parse date from string '%1'";
    private static final String TFA02_ = "TFA02 - failed to create uniqueInstance (%1)";
    private static final int MSR_DEFAULT_DURATION_ = 5000;
    private static final int MSR_MAX_THREADS_ = 50;
    private static String MSR_TEMPLATE_LOCATION_ = null;
    private static String MSR_FRAME_TEMPLATE_ = "hedc_synoptic_frame";
    private static char[] MSR_ROW_HEADER_TEMPLATE_ = null;
    private static char[] MSR_ROW_LINE_TEMPLATE_ = null;
    private static char[] MSR_ROW_EMPTY_LINE_TEMPLATE_ = null;
    private static MetaSearchImpl uniqueInstance_ = null;
    private TaskFactory taskFac_ = null;
    private PooledExecutorWithInvalidate executor_ = null;

    public static MetaSearchImpl getUniqueInstance() {
        if (uniqueInstance_ == null)
            try {
                uniqueInstance_ = new MetaSearchImpl();
            } catch (Exception e) {
                Messages.error(TFA02_, e);
            }
        return uniqueInstance_;
    }

    private MetaSearchImpl() {
        taskFac_ = new TaskFactory();
        executor_ = new PooledExecutorWithInvalidate(new LinkedQueue(),
                MSR_MAX_THREADS_);
        executor_.setKeepAliveTime(-1); // threads live forever
        MSR_ROW_HEADER_TEMPLATE_ = FormFiller.internalize("hedc_synoptic_row_header");
        MSR_ROW_LINE_TEMPLATE_ = FormFiller.internalize("hedc_synoptic_row_body");
        MSR_ROW_EMPTY_LINE_TEMPLATE_ = FormFiller.internalize("hedc_synoptic_row_empty_body");
        Messages.debug(0, "MetaSearchImpl:: constructor done");
    }

    public long search(Hashtable h, Writer wrt, MetaSearchRequest r) throws IOException {
        List results = search(h, r);
        return writeResults_(results, wrt);
    }

    public List search(Hashtable h, MetaSearchRequest r) {
        // create tasks
        List taskList = null;
        String dateString = (String) h.get("DATETIME");
        Date date = null;
        long waitTime = MSR_DEFAULT_DURATION_;
        try {
            waitTime = Long.valueOf((String) h.get("WAIT_TIME")).longValue() * 1000;
        } catch (Exception e) {
        }
        if (dateString != null)
            try {
                Messages.debug(-1, "MetaSearch::search before parse");
                date = RandomDate.parse(dateString);
                Messages.debug(-1, "MetaSearch::search after parse date=%1", date);
            } catch (Exception e) {
                Messages.error(TFA01_, dateString);
            }
        else
            Messages.error(TFA01_, dateString);

        if (date != null) {
            Thread t = Thread.currentThread();
            taskList = taskFac_.makeTasks(h, date, r);
            // take precaution that the issueing Thread is interrupted when all tasks are done
            r.registerInterrupt(t, taskList.size());
            try {
                for (Iterator e = taskList.iterator(); e.hasNext();)
                    executor_.execute((Task) e.next());
                // sleep
                t.sleep(waitTime);
            } catch (InterruptedException e) {
                // if all tasks have been done, before waitTime was over
            }

            // invalidate all tasks and interrupt the corresponding threads
            for (Iterator e = taskList.iterator(); e.hasNext();)
                ((Task) e.next()).cancel();
        }
        return taskList;
    }


    /**
     * Input is a list of tasks
     */
    private long writeResults_(List l, Writer w) throws IOException {
        long ret = -1;
        StringWriter sw = new StringWriter();
        Hashtable h = new Hashtable();
        for (Iterator e = l.iterator(); e.hasNext();) {
            MetaSearchResult r = (MetaSearchResult) e.next();
            Iterator i = r.getInfo();
            if (i.hasNext()) {
                h = (Hashtable) i.next();
                if (h.get("URL") != null) {
                    do {
                        Generator.generate(sw, h, MSR_ROW_LINE_TEMPLATE_);
                    } while (i.hasNext() && (h = (Hashtable) i.next()) != null);
                } else
                    Generator.generate(sw, h, MSR_ROW_EMPTY_LINE_TEMPLATE_);
            }
        }

        h.put("ROWS", sw.getBuffer().toString());
        FormFiller f = new FormFiller(w, h, MSR_FRAME_TEMPLATE_);
        f.fillForm();
        return ret;
    }

    private final void printResults_(List l) {
        for (Iterator e = l.iterator(); e.hasNext();) {
            MetaSearchResult t = (MetaSearchResult) e.next();
            System.out.println(t.getInfo());
            System.out.println(t.results);
        }
    }

    private static final String RDI01_ = "RDI01 - an error occurred while opening your session (%1)";
    private static final String RDI02_ = "RDI02 - no session with ID %1 found";
    private static final String RDI03_ = "RDI03 - the RequestDispatch service is implicitly started through the server RequestDispatchServlet";
    private static final String RDI04_ = "RDI04 - session id %1 was not opened through this API";
}
