//******************************************************************************
//
// File:    MSHistogramClu.java
// Package: benchmarks.detinfer.pj.edu.ritclu.fractal
// Unit:    Class benchmarks.detinfer.pj.edu.ritclu.fractal.MSHistogramClu
//
// This Java source file is copyright (C) 2007 by Alan Kaminsky. All rights
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

package benchmarks.detinfer.pj.edu.ritclu.fractal;

import benchmarks.detinfer.pj.edu.ritmp.IntegerBuf;
import benchmarks.detinfer.pj.edu.ritmp.ObjectBuf;

import benchmarks.detinfer.pj.edu.ritmp.buf.ObjectItemBuf;

import benchmarks.detinfer.pj.edu.ritpj.Comm;
import benchmarks.detinfer.pj.edu.ritpj.CommStatus;
import benchmarks.detinfer.pj.edu.ritpj.IntegerSchedule;
import benchmarks.detinfer.pj.edu.ritpj.ParallelRegion;
import benchmarks.detinfer.pj.edu.ritpj.ParallelSection;
import benchmarks.detinfer.pj.edu.ritpj.ParallelTeam;

import benchmarks.detinfer.pj.edu.ritpj.reduction.IntegerOp;

import benchmarks.detinfer.pj.edu.ritutil.Range;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class MSHistogramClu is a cluster parallel program that calculates a
 * histogram of the Mandelbrot Set.
 * <P>
 * Usage: java -Dpj.np=<I>K</I> [-Dpj.schedule=<I>schedule</I>]
 * benchmarks.detinfer.pj.edu.ritclu.fractal.MSHistogramClu <I>width</I> <I>height</I> <I>xcenter</I>
 * <I>ycenter</I> <I>resolution</I> <I>maxiter</I> <I>outfile</I>
 * <BR><I>K</I> = Number of parallel processes
 * <BR><I>schedule</I> = Load balancing schedule
 * <BR><I>width</I> = Image width (pixels)
 * <BR><I>height</I> = Image height (pixels)
 * <BR><I>xcenter</I> = X coordinate of center point
 * <BR><I>ycenter</I> = Y coordinate of center point
 * <BR><I>resolution</I> = Pixels per unit
 * <BR><I>maxiter</I> = Maximum number of iterations
 * <BR><I>outfile</I> = Output file name
 * <P>
 * The program calculates an iteration count <I>i</I> for each pixel in the same
 * way as the {@linkplain MandelbrotSetSeq} program, with 0 &lt;= <I>i</I> &lt;=
 * <I>maxiter</I>. The program prints into the output file a histogram of the
 * <I>i</I> values; that is, for each value of <I>i</I>, how many times that
 * value occurred in the image.
 * <P>
 * The computation is performed in parallel in multiple processors. The program
 * measures the computation's running time, including the time to print the
 * output.
 *
 * @author  Alan Kaminsky
 * @version 02-Feb-2008
 */
public class MSHistogramClu
	{

// Prevent construction.

	private MSHistogramClu()
		{
		}

// Program shared variables.

	// Communicator.
	static Comm world;
	static int size;
	static int rank;

	// Command line arguments.
	static int width;
	static int height;
	static double xcenter;
	static double ycenter;
	static double resolution;
	static int maxiter;
	static File outfile;

	// Initial pixel offsets from center.
	static int xoffset;
	static int yoffset;

	// Histogram (array of counters indexed by pixel value).
	static int[] histogram;

	// Message tags.
	static final int WORKER_MSG = 0;
	static final int MASTER_MSG = 1;
	static final int HISTOGRAM_DATA_MSG = 2;

	// Number of chunks the worker computed.
	static int chunkCount;

// Main program.

	/**
	 * Mandelbrot Set main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Start timing.
		long t1 = System.currentTimeMillis();

		// Initialize middleware.
		Comm.init (args);
		world = Comm.world();
		size = world.size();
		rank = world.rank();

		// Validate command line arguments.
		if (args.length != 7) usage();
		width = Integer.parseInt (args[0]);
		height = Integer.parseInt (args[1]);
		xcenter = Double.parseDouble (args[2]);
		ycenter = Double.parseDouble (args[3]);
		resolution = Double.parseDouble (args[4]);
		maxiter = Integer.parseInt (args[5]);
		outfile = new File (args[6]);

		// Initial pixel offsets from center.
		xoffset = -(width - 1) / 2;
		yoffset = (height - 1) / 2;

		// Create histogram.
		histogram = new int [maxiter + 1];

		long t2 = System.currentTimeMillis();

		// In master process, run master section and worker section in parallel.
		if (rank == 0)
			{
			new ParallelTeam(2).execute (new ParallelRegion()
				{
				public void run() throws Exception
					{
					execute (new ParallelSection()
						{
						public void run() throws Exception
							{
							masterSection();
							}
						},
					new ParallelSection()
						{
						public void run() throws Exception
							{
							workerSection();
							}
						});
					}
				});
			}

		// In worker process, run only worker section.
		else
			{
			workerSection();
			}

		// Reduce histogram into process 0.
		world.reduce
			(0,
			 HISTOGRAM_DATA_MSG,
			 IntegerBuf.buffer (histogram),
			 IntegerOp.SUM);

		long t3 = System.currentTimeMillis();

		// Process 0 prints histogram.
		if (rank == 0)
			{
			PrintWriter out =
				new PrintWriter
					(new BufferedWriter
						(new FileWriter (outfile)));
			for (int i = 0; i <= maxiter; ++ i)
				{
				out.print (i);
				out.print ('\t');
				out.print (histogram[i]);
				out.println();
				}
			out.close();
			}

		// Stop timing.
		long t4 = System.currentTimeMillis();
		System.out.println (chunkCount + " chunks " + rank);
		System.out.println ((t2-t1) + " msec pre " + rank);
		System.out.println ((t3-t2) + " msec calc " + rank);
		System.out.println ((t4-t3) + " msec post " + rank);
		System.out.println ((t4-t1) + " msec total " + rank);
		}

// Hidden operations.

	/**
	 * Perform the master section.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	private static void masterSection()
		throws IOException
		{
		int worker;
		Range range;

		// Set up a schedule object to divide the row range into chunks.
		IntegerSchedule schedule = IntegerSchedule.runtime();
		schedule.start (size, new Range (0, height-1));

		// Send initial chunk range to each worker. If range is null, no more
		// work for that worker. Keep count of active workers.
		int activeWorkers = size;
		for (worker = 0; worker < size; ++ worker)
			{
			range = schedule.next (worker);
			world.send (worker, WORKER_MSG, ObjectBuf.buffer (range));
			if (range == null) -- activeWorkers;
			}

		// Repeat until all workers have finished.
		while (activeWorkers > 0)
			{
			// Receive an empty message from any worker.
			CommStatus status =
				world.receive (null, MASTER_MSG, IntegerBuf.emptyBuffer());
			worker = status.fromRank;

			// Send next chunk range to that specific worker. If null, no more
			// work.
			range = schedule.next (worker);
			world.send (worker, WORKER_MSG, ObjectBuf.buffer (range));
			if (range == null) -- activeWorkers;
			}
		}

	/**
	 * Perform the worker section.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	private static void workerSection()
		throws IOException
		{
		// Process chunks from master.
		for (;;)
			{
			// Receive chunk range from master. If null, no more work.
			ObjectItemBuf<Range> rangeBuf = ObjectBuf.buffer();
			world.receive (0, WORKER_MSG, rangeBuf);
			Range range = rangeBuf.item;
			if (range == null) break;
			int lb = range.lb();
			int ub = range.ub();
			++ chunkCount;

			// Compute all rows and columns in slice.
			for (int r = lb; r <= ub; ++ r)
				{
				double y = ycenter + (yoffset - r) / resolution;

				for (int c = 0; c < width; ++ c)
					{
					double x = xcenter + (xoffset + c) / resolution;

					// Iterate until convergence.
					int i = 0;
					double aold = 0.0;
					double bold = 0.0;
					double a = 0.0;
					double b = 0.0;
					double zmagsqr = 0.0;
					while (i < maxiter && zmagsqr <= 4.0)
						{
						++ i;
						a = aold*aold - bold*bold + x;
						b = 2.0*aold*bold + y;
						zmagsqr = a*a + b*b;
						aold = a;
						bold = b;
						}

					// Increment histogram counter for pixel value.
					++ histogram[i];
					}
				}

			// Report completion of slice to master.
			world.send (0, MASTER_MSG, IntegerBuf.emptyBuffer());
			}
		};

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java -Dpj.np=<K> [-Dpj.schedule=<schedule>] benchmarks.detinfer.pj.edu.ritclu.fractal.MSHistogramClu <width> <height> <xcenter> <ycenter> <resolution> <maxiter> <outfile>");
		System.err.println ("<K> = Number of parallel processes");
		System.err.println ("<schedule> = Load balancing schedule");
		System.err.println ("<width> = Image width (pixels)");
		System.err.println ("<height> = Image height (pixels)");
		System.err.println ("<xcenter> = X coordinate of center point");
		System.err.println ("<ycenter> = Y coordinate of center point");
		System.err.println ("<resolution> = Pixels per unit");
		System.err.println ("<maxiter> = Maximum number of iterations");
		System.err.println ("<outfile> = Output file name");
		System.exit (1);
		}

	}
