//******************************************************************************
//
// File:    mprun.java
// Package: ---
// Unit:    Class mprun
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

package benchmarks.detinfer.pj;

import benchmarks.detinfer.pj.edu.ritpj.PJProperties;

import benchmarks.detinfer.pj.edu.ritpj.cluster.JobSchedulerException;
import benchmarks.detinfer.pj.edu.ritpj.cluster.NonPjJobFrontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.LinkedList;

/**
 * Class mprun is a main program that runs an MPI job on a cluster parallel
 * computer using the Parallel Java job queue.
 * <P>
 * Usage: java mprun -np <I>K</I> <I>command</I> <I>args</I> . . .
 * <BR><I>K</I> = Number of nodes (default 1)
 * <BR><I>command</I> = MPI program to run
 * <BR><I>args</I> = MPI program command line arguments
 * <P>
 * The <TT>mprun</TT> program contacts the PJ Job Scheduler Daemon and requests
 * a job running on <I>K</I> nodes of the cluster. The job goes into the job
 * queue and may sit in the job queue for some time until <I>K</I> nodes are
 * available. Once <I>K</I> nodes are available, the <TT>mprun</TT> program
 * prints their names on the standard error along with the job number. For
 * example:
 * <PRE>
 *     $ java mprun -np 4 . . .
 *     Job 42, thug01, thug02, thug03, thug04
 * </PRE>
 * Standard input, standard output, and standard error go to the MPI program as
 * usual.
 * <P>
 * You can put a time limit on the <TT>mprun</TT> program this way:
 * <P>
 * Usage: java -Dpj.jobtime=<I>T</I> mprun -np <I>K</I> <I>command</I>
 * <I>args</I> . . .
 * <BR><I>T</I> = Job time (seconds)
 * <BR><I>K</I> = Number of nodes (default 1)
 * <BR><I>command</I> = MPI program to run
 * <BR><I>args</I> = MPI program command line arguments
 * <P>
 * In this case the <TT>mprun</TT> program will terminate itself automatically
 * after <I>T</I> seconds. You can also kill the <TT>mprun</TT> program
 * manually.
 * <P>
 * <I>Note:</I> The Java <TT>mprun</TT> program spawns a separate process to run
 * the actual MPI job. This separate process executes the Sun Microsystems
 * <TT>mprun</TT> command. If the Parallel Java Library is installed on a
 * non-Sun system, the source code of class mprun will have to be changed to
 * issue the correct command to run an MPI job.
 *
 * @author  Alan Kaminsky
 * @version 24-Apr-2008
 */
public class mprun
	{

// Prevent construction.

	private mprun()
		{
		}

// Global variables.

	// mprun process.
	private static Process proc;

// Main program.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Build list of arguments for mprun command. Strip out any -np and -l
		// arguments.
		int nargs = args.length;
		int Kp = 1;
		LinkedList<String> mprunCommand = new LinkedList<String>();
		for (int i = 0; i < nargs; ++ i)
			{
			String arg = args[i];
			if (arg.equals ("-np"))
				{
				++ i;
				if (i < nargs)
					{
					arg = args[i];
					try
						{
						Kp = Integer.parseInt (arg);
						if (Kp < 1) usage();
						}
					catch (NumberFormatException exc)
						{
						usage();
						}
					}
				else
					{
					usage();
					}
				}
			else if (arg.equals ("-l"))
				{
				++ i;
				if (i < nargs)
					{
					}
				else
					{
					usage();
					}
				}
			else
				{
				mprunCommand.add (arg);
				}
			}
		if (mprunCommand.size() == 0) usage();

		// Create non-PJ job frontend object.
		String username = System.getProperty ("user.name");
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

		// Wait for job number and backend nodes to be assigned; print them out.
		System.err.print ("Job " + frontend.getJobNumber());
		for (String name : frontend.getBackendNames())
			{
			System.err.print (", ");
			System.err.print (name);
			}
		System.err.println();

		// Set up mprun command.
		mprunCommand.add (0, "/opt/SUNWhpc/bin/mprun");
		mprunCommand.add (1, "-np");
		mprunCommand.add (2, ""+Kp);
		mprunCommand.add (3, "-l");
		StringBuilder b = new StringBuilder();
		for (String name : frontend.getBackendNames())
			{
			if (b.length() > 0) b.append (',');
			b.append (name);
			}
		mprunCommand.add (4, b.toString());

		// Install a shutdown hook to kill the mprun process if this process
		// terminates.
		Runtime.getRuntime().addShutdownHook (new Thread()
			{
			public void run()
				{
				if (proc != null) proc.destroy();
				}
			});

		// Spawn a separate process to execute the mprun command.
		ProcessBuilder pb = new ProcessBuilder (mprunCommand);
		try
			{
			proc = pb.start();
			}
		catch (Throwable exc)
			{
			System.err.println ("Cannot execute mprun command:");
			for (String s : mprunCommand)
				{
				System.err.print (s);
				System.err.print (' ');
				}
			System.err.println();
			exc.printStackTrace (System.err);
			System.exit (1);
			}

		// Spawn threads to relay standard input, standard output, and standard
		// error from mprun process to this process.
		Relay stdinRelay =
			new Relay (System.in, proc.getOutputStream(), "input");
		Relay stdoutRelay =
			new Relay (proc.getInputStream(), System.out, "output");
		Relay stderrRelay =
			new Relay (proc.getErrorStream(), System.err, "error");

		// Wait for the mprun process to terminate, then exit.
		try
			{
			frontend.terminateJobFinished (proc.waitFor());
			}
		catch (Throwable exc)
			{
			System.err.println ("Cannot wait for mprun command");
			exc.printStackTrace (System.err);
			System.exit (1);
			}
		}

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java mprun -np <K> <command> <args> ...");
		System.err.println ("<K> = Number of nodes (default 1)");
		System.err.println ("<command> = MPI program to run");
		System.err.println ("<args> = MPI program command line arguments");
		System.exit (1);
		}

// Hidden helper classes.

	/**
	 * Class Relay is a thread that relays an input stream to an output stream,
	 * byte by byte.
	 *
	 * @author  Alan Kaminsky
	 * @version 19-Mar-2008
	 */
	private static class Relay
		extends Thread
		{
		private InputStream fromStream;
		private OutputStream toStream;
		private String name;

		public Relay
			(InputStream fromStream,
			 OutputStream toStream,
			 String name)
			{
			this.fromStream = fromStream;
			this.toStream = toStream;
			this.name = name;
			setDaemon (true);
			start();
			}

		public void run()
			{
			try
				{
				int c;
				while ((c = fromStream.read()) != -1)
					{
					toStream.write (c);
					if (c == '\n') toStream.flush();
					}
				try { toStream.close(); } catch (IOException exc) {}
				}
			catch (Throwable exc)
				{
				System.err.println
					("Error relaying standard " + name + " stream");
				exc.printStackTrace (System.err);
				}
			}
		}

	}
