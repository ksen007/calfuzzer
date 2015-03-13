//******************************************************************************
//
// File:    pjrun.java
// Package: ---
// Unit:    Class pjrun
//
// This Java source file is copyright (C) 2008 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java Library ("PJ"). PJ is free
// software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// PJ is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package benchmarks.determinism.pj;

import benchmarks.determinism.pj.edu.ritpj.PJProperties;

import benchmarks.determinism.pj.edu.ritpj.cluster.JobSchedulerException;
import benchmarks.determinism.pj.edu.ritpj.cluster.NonPjJobFrontend;

/**
 * Class pjrun is a main program that runs a program, other than a Parallel Java
 * (PJ) program, on a cluster parallel computer using the PJ job queue. (PJ
 * programs interact with the PJ job queue directly and do not need to use the
 * <TT>pjrun</TT> program.)
 * <P>
 * <B>Reserving Nodes on the Cluster</B>
 * <P>
 * Usage: java -Dpj.np=<I>K</I> pjrun
 * <BR><I>K</I> = Number of nodes
 * <P>
 * The <TT>pjrun</TT> program contacts the PJ Job Scheduler Daemon and requests
 * a job running on <I>K</I> nodes of the cluster. The job goes into the job
 * queue and may sit in the job queue for some time until <I>K</I> nodes are
 * available. Once <I>K</I> nodes are available, the <TT>pjrun</TT> program
 * prints their names on the standard output. For example:
 * <PRE>
 *     $ java -Dpj.np=4 pjrun
 *     thug01
 *     thug02
 *     thug03
 *     thug04
 * </PRE>
 * You can then do whatever you want with those nodes, such as log into them and
 * run programs on them. Other PJ jobs will not be assigned those nodes as long
 * as the <TT>pjrun</TT> program runs. The <TT>pjrun</TT> program continues to
 * run until killed externally. To release the assigned nodes, kill the
 * <TT>pjrun</TT> program, e.g. by typing CTRL-C.
 * <P>
 * You can put a time limit on the <TT>pjrun</TT> program this way:
 * <P>
 * Usage: java -Dpj.np=<I>K</I> -Dpj.jobtime=<I>T</I> pjrun
 * <BR><I>K</I> = Number of nodes
 * <BR><I>T</I> = Job time (seconds)
 * <P>
 * In this case the <TT>pjrun</TT> program will terminate itself automatically
 * after <I>T</I> seconds. You can also kill the <TT>pjrun</TT> program
 * manually.
 * <P>
 * <B>Example</B>
 * <P>
 * For example, here's how to run an MPI program via the PJ job queue in the
 * author's installation. Type this command in one shell:
 * <PRE>
 *     $ java -Dpj.np=4 pjrun
 *     thug01
 *     thug02
 *     thug03
 *     thug04
 * </PRE>
 * Then type this command in another shell:
 * <PRE>
 *     $ mprun -np 4 -l "thug01,thug02,thug03,thug04" foo ...
 * </PRE>
 * <TT>mprun</TT> is the MPI launcher program. The <TT>-np</TT> option tells the
 * MPI launcher to use 4 nodes, the number requested from the PJ job queue. The
 * <TT>-l</TT> option tells the MPI launcher to use the specific cluster nodes
 * assigned by the PJ job queue. <TT>foo</TT> is the MPI program to run,
 * followed by its command line arguments.
 * <P>
 * As long as the <TT>pjrun</TT> program remains running, the cluster nodes
 * remain reserved for use by MPI (or anything else). MPI jobs running on those
 * nodes will not interfere with PJ jobs running on other nodes, and vice versa.
 * When the MPI jobs are finished, kill the <TT>pjrun</TT> program.
 *
 * @author  Alan Kaminsky
 * @version 19-Jan-2008
 */
public class pjrun
	{

// Prevent construction.

	private pjrun()
		{
		}

// Main program.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Create non-PJ job frontend object.
		String username = System.getProperty ("user.name");
		int Kp = PJProperties.getPjNp();
		NonPjJobFrontend frontend = null;
		try
			{
			frontend = new NonPjJobFrontend (username, Kp);
			}
		catch (JobSchedulerException exc)
			{
			// We were not able to contact the Job Scheduler.
			System.err.println
				("No Job Scheduler at " +
				 PJProperties.getPjHost() + ":" +
				 PJProperties.getPjPort());
			System.exit (1);
			}

		// Run job frontend in a separate thread.
		new Thread (frontend) .start();

		// Wait for backend nodes to be assigned; print them out.
		for (String name : frontend.getBackendNames())
			{
			System.out.println (name);
			}

		// The main thread terminates, but the non-daemon job frontend thread
		// keeps running until the program is killed externally.
		}

	}
