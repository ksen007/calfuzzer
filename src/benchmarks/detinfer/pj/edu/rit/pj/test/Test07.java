//******************************************************************************
//
// File:    Test07.java
// Package: benchmarks.detinfer.pj.edu.ritpj.test
// Unit:    Class benchmarks.detinfer.pj.edu.ritpj.test.Test07
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

package benchmarks.detinfer.pj.edu.ritpj.test;

import benchmarks.detinfer.pj.edu.ritpj.BarrierAction;
import benchmarks.detinfer.pj.edu.ritpj.ParallelRegion;
import benchmarks.detinfer.pj.edu.ritpj.ParallelSection;
import benchmarks.detinfer.pj.edu.ritpj.ParallelTeam;

/**
 * Class Test07 is a unit test main program for classes {@linkplain
 * benchmarks.detinfer.pj.edu.ritpj.ParallelTeam ParallelTeam}, {@linkplain benchmarks.determinism.pj.edu.ritpj.ParallelRegion
 * ParallelRegion}, and {@linkplain benchmarks.detinfer.pj.edu.ritpj.ParallelSection ParallelSection}.
 * A parallel section is executed by one parallel team thread, first with a
 * wait, then without a wait.
 * <P>
 * Usage: java -Dpj.nt=<I>K</I> benchmarks.detinfer.pj.edu.ritpj.test.Test07
 * <BR><I>K</I> = Number of parallel threads
 *
 * @author  Alan Kaminsky
 * @version 11-Nov-2007
 */
public class Test07
	{

// Prevent construction.

	private Test07()
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
		new ParallelTeam().execute (new ParallelRegion()
			{
			public void run() throws Exception
				{
				System.out.println
					("Thread " + getThreadIndex() + " calls execute()");
				execute (new ParallelSection()
					{
					public void run() throws Exception
						{
						System.out.println
							("Thread " + getThreadIndex() +
							 " calls parallel section 1 run()");
						Thread.sleep (2000L);
						System.out.println
							("Thread " + getThreadIndex() +
							 " returns from parallel section 1 run()");
						}
					});
				System.out.println
					("Thread " + getThreadIndex() + " returns from execute()");
				System.out.println
					("Thread " + getThreadIndex() +
					 " calls execute() with no wait");
				execute (new ParallelSection()
					{
					public void run() throws Exception
						{
						System.out.println
							("Thread " + getThreadIndex() +
							 " calls parallel section 2 run()");
						Thread.sleep (2000L);
						System.out.println
							("Thread " + getThreadIndex() +
							 " returns from parallel section 2 run()");
						}
					},
					BarrierAction.NO_WAIT);
				System.out.println
					("Thread " + getThreadIndex() +
					 " returns from execute() with no wait");
				System.out.println
					("Thread " + getThreadIndex() + " calls execute()");
				execute (new ParallelSection()
					{
					public void run() throws Exception
						{
						System.out.println
							("Thread " + getThreadIndex() +
							 " calls parallel section 3 run()");
						Thread.sleep (2000L);
						System.out.println
							("Thread " + getThreadIndex() +
							 " returns from parallel section 3 run()");
						}
					});
				System.out.println
					("Thread " + getThreadIndex() + " returns from execute()");
				}
			});
		}

	}
