//******************************************************************************
//
// File:    Test02.java
// Package: benchmarks.detinfer.pj.edu.ritpj.cluster.test
// Unit:    Class benchmarks.detinfer.pj.edu.ritpj.cluster.test.Test02
//
// This Java source file is copyright (C) 2006 by Alan Kaminsky. All rights
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

package benchmarks.detinfer.pj.edu.ritpj.cluster.test;

import benchmarks.detinfer.pj.edu.ritpj.Comm;

/**
 * Class Test02 is a unit test main program for class {@linkplain
 * benchmarks.detinfer.pj.edu.ritpj.Comm}. It prints out a "Hello, world" message and echoes the
 * command line arguments.
 * <P>
 * Usage: java -Dpj.np=<I>K</I> benchmarks.detinfer.pj.edu.ritpj.cluster.test.Test02 [ <I>args</I> ]
 *
 * @author  Alan Kaminsky
 * @version 30-Oct-2006
 */
public class Test02
	{

// Prevent construction.

	private Test02()
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
		Comm.init (args);
		Comm world = Comm.world();
		int size = world.size();
		int rank = world.rank();
		System.out.print ("Hello, world from process ");
		System.out.print (rank);
		System.out.print (" of ");
		System.out.print (size);
		System.out.print ("!");
		for (String arg : args)
			{
			System.out.print (" ");
			System.out.print (arg);
			}
		System.out.println();
		}

	}
