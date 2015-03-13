//******************************************************************************
//
// File:    Test05.java
// Package: benchmarks.detinfer.pj.edu.ritpj.test
// Unit:    Class benchmarks.detinfer.pj.edu.ritpj.test.Test05
//
// This Java source file is copyright (C) 2005 by Alan Kaminsky. All rights
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

package benchmarks.detinfer.pj.edu.ritpj.test;

import benchmarks.detinfer.pj.edu.ritpj.IntegerForLoop;
import benchmarks.detinfer.pj.edu.ritpj.ParallelRegion;
import benchmarks.detinfer.pj.edu.ritpj.ParallelTeam;

/**
 * Class Test05 is a unit test main program for classes {@linkplain
 * benchmarks.detinfer.pj.edu.ritpj.ParallelTeam ParallelTeam}, {@linkplain benchmarks.determinism.pj.edu.ritpj.ParallelRegion
 * ParallelRegion}, and {@linkplain benchmarks.detinfer.pj.edu.ritpj.IntegerForLoop IntegerForLoop}.
 * A parallel for loop iterates over a given range of indexes. Each iteration
 * prints the loop index and the thread index executing that loop index. If the
 * loop index exceeds 10, an exception is thrown.
 * <P>
 * Usage: java -Dpj.nt=<I>K</I> benchmarks.detinfer.pj.edu.ritpj.test.Test05 <I>lb</I> <I>ub</I>
 * <BR><I>K</I> = Number of parallel threads
 * <BR><I>lb</I> = Loop index lower bound, inclusive
 * <BR><I>ub</I> = Loop index upper bound, inclusive
 *
 * @author  Alan Kaminsky
 * @version 31-Aug-2005
 */
public class Test05
	{

// Prevent construction.

	private Test05()
		{
		}

// Main program.

	/**
	 * Unit test main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		if (args.length != 2) usage();
		final int lb = Integer.parseInt (args[0]);
		final int ub = Integer.parseInt (args[1]);

		new ParallelTeam().execute (new ParallelRegion()
			{
			public void run() throws Exception
				{
				System.out.println ("Begin thread " + getThreadIndex());
				execute (lb, ub, new IntegerForLoop()
					{
					public void run (int first, int last) throws Exception
						{
						for (int i = first; i <= last; ++ i)
							{
							System.out.println
								("i = " + i + ", thread = " + getThreadIndex());
							if (i > 10)
								{
								throw new Exception ("i = " + i + " too large");
								}
							}
						}
					});
				System.out.println ("End thread " + getThreadIndex());
				}
			});
		}

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java -Dpj.nt=<K> benchmarks.detinfer.pj.edu.ritpj.test.Test05 <lb> <ub>");
		System.err.println ("<K> = Number of parallel threads");
		System.err.println ("<lb> = Loop index lower bound, inclusive");
		System.err.println ("<ub> = Loop index upper bound, inclusive");
		System.exit (1);
		}

	}
