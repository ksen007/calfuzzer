//******************************************************************************
//
// File:    Program1Seq.java
// Package: ---
// Unit:    Class Program1Seq
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

/**
 * Class Program1Seq is the sequential version of the first program illustrating
 * parallel computing.
 * <P>
 * Usage: java Program1Seq <I>x1</I> [ <I>x2</I> . . . ]
 * <BR><I>x1</I> = Number to be tested for primality
 *
 * @author  Alan Kaminsky
 * @version 05-Aug-2008
 */
public class Program1Seq
	{

// Prevent construction.

	private Program1Seq()
		{
		}

// Global variables.

	static int n;
	static long[] x;
	static long t1, t2[], t3[];

// Exported operations.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		t1 = System.currentTimeMillis();
		n = args.length;
		x = new long [n];
		for (int i = 0; i < n; ++ i) x[i] = Long.parseLong (args[i]);
		t2 = new long [n];
		t3 = new long [n];
		for (int i = 0; i < n; ++ i)
			{
			t2[i] = System.currentTimeMillis();
			isPrime (x[i]);
			t3[i] = System.currentTimeMillis();
			}
		for (int i = 0; i < n; ++ i)
			{
			System.out.println
				("i = "+i+" call start = "+(t2[i]-t1)+" msec");
			System.out.println
				("i = "+i+" call finish = "+(t3[i]-t1)+" msec");
			}
		}

// Hidden operations.

	/**
	 * Subroutine for one computation. Returns true if x is prime, false
	 * otherwise.
	 */
	private static boolean isPrime
		(long x)
		{
		if (x % 2 == 0) return false;
		long p = 3;
		long psqr = p*p;
		while (psqr <= x)
			{
			if (x % p == 0) return false;
			p += 2;
			psqr = p*p;
			}
		return true;
		}

	}
