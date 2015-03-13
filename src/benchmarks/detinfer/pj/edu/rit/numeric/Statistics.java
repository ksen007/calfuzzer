//******************************************************************************
//
// File:    Statistics.java
// Package: benchmarks.detinfer.pj.edu.ritnumeric
// Unit:    Class benchmarks.detinfer.pj.edu.ritnumeric.Statistics
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

package benchmarks.detinfer.pj.edu.ritnumeric;

// For unit test main program.
import benchmarks.detinfer.pj.edu.ritutil.Random;
import java.util.Arrays;

/**
 * Class Statistics provides static methods for doing statistical tests.
 * <P>
 * For each statistical test, there is a method that returns the "p-value" of
 * the test statistic. This is the probability that the test statistic would
 * have a value greater than or equal to the observed value if the null
 * hypothesis is true.
 *
 * @author  Alan Kaminsky
 * @version 25-Apr-2008
 */
public class Statistics
	{

// Prevent construction.

	private Statistics()
		{
		}

// Exported operations.

	/**
	 * Do a Kolmogorov-Smirnov (K-S) test on the given data. The null hypothesis
	 * is that the data was drawn from a uniform distribution between 0.0 and
	 * 1.0.
	 * <P>
	 * The values in the <TT>data</TT> array must all be in the range 0.0
	 * through 1.0 and must be in ascending numerical order. The
	 * <TT>ksTest()</TT> method does not sort the data itself because the
	 * process that produced the data might already have sorted the data. If
	 * necessary, call <TT>Arrays.sort(data)</TT> before calling
	 * <TT>ksTest(data)</TT>.
	 *
	 * @param  data  Data array.
	 *
	 * @return  K-S statistic.
	 */
	public static double ksTest
		(double[] data)
		{
		int M = data.length;
		double N = M;
		double D = 0.0;
		double F_lower = 0.0;
		double F_upper;
		for (int i = 0; i < N; ++ i)
			{
			F_upper = (i+1) / N;
			D = Math.max (D, Math.abs (data[i] - F_lower));
			D = Math.max (D, Math.abs (data[i] - F_upper));
			F_lower = F_upper;
			}
		return D;
		}

	/**
	 * Returns the p-value of a K-S statistic.
	 *
	 * @param  N  Number of data points.
	 * @param  D  K-S statistic.
	 *
	 * @return  P-value.
	 */
	public static double ksPvalue
		(double N,
		 double D)
		{
		double sqrt_N = Math.sqrt(N);
		double x = (sqrt_N + 0.12 + 0.11/sqrt_N) * D;
		x = -2.0*x*x;
		double a = 2.0;
		double sum = 0.0;
		double term;
		double absterm;
		double prevterm = 0.0;
		for (int j = 1; j <= 100; ++ j)
			{
			term = a * Math.exp (x*j*j);
			sum += term;
			absterm = Math.abs(term);
			if (absterm <= 1.0e-6*prevterm || absterm <= 1.0e-12*sum)
				{
				return sum;
				}
			a = -a;
			prevterm = absterm;
			}
		return 1.0; // Failed to converge
		}

// Unit test main program.

	/**
	 * Unit test main program. Does a K-S test on N random doubles, prints the
	 * K-S statistic, and prints the p-value.
	 * <P>
	 * Usage: java benchmarks.detinfer.pj.edu.ritnumeric.Statistics <I>seed</I> <I>N</I>
	 * <BR><I>seed</I> = Random seed
	 * <BR><I>N</I> = Number of data points
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		if (args.length != 2) usage();
		long seed = Long.parseLong (args[0]);
		int N = Integer.parseInt (args[1]);
		Random prng = Random.getInstance (seed);
		double[] data = new double [N];
		for (int i = 0; i < N; ++ i)
			{
			data[i] = prng.nextDouble();
			}
		Arrays.sort (data);
		double D = ksTest (data);
		System.out.println ("D = " + D);
		System.out.println ("p = " + ksPvalue (N, D));
		}

	private static void usage()
		{
		System.err.println ("Usage: java benchmarks.detinfer.pj.edu.ritnumeric.Statistics <seed> <N>");
		System.err.println ("<seed> = Random seed");
		System.err.println ("<N> = Number of data points");
		System.exit (1);
		}

	}
