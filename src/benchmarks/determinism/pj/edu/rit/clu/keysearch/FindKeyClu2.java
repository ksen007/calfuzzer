//******************************************************************************
//
// File:    FindKeyClu2.java
// Package: benchmarks.determinism.pj.edu.ritclu.keysearch
// Unit:    Class benchmarks.determinism.pj.edu.ritclu.keysearch.FindKeyClu2
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

package benchmarks.determinism.pj.edu.ritclu.keysearch;

import benchmarks.determinism.pj.edu.ritcrypto.blockcipher.AES256Cipher;

import benchmarks.determinism.pj.edu.ritmp.IntegerBuf;

import benchmarks.determinism.pj.edu.ritpj.Comm;
import benchmarks.determinism.pj.edu.ritpj.CommRequest;

import benchmarks.determinism.pj.edu.ritutil.Hex;
import benchmarks.determinism.pj.edu.ritutil.Range;

/**
 * Class FindKeyClu2 is a cluster parallel program that solves an AES partial
 * key search problem. The program's command line arguments are the plaintext
 * (128-bit hexadecimal number), the ciphertext (128-bit hexadecimal number),
 * the partial key with the <I>n</I> least significant bits set to 0 (256-bit
 * hexadecimal number), and <I>n</I>, the number of key bits to search for. The
 * ciphertext was created by encrypting the plaintext with the key; however, not
 * all bits of the key are provided. The problem is to find the complete key.
 * The program performs an exhaustive search, trying all possible values for the
 * missing key bits until it finds the key that reproduces the given ciphertext
 * from the given plaintext.
 * <P>
 * Usage: java -Dpj.np=<I>K</I> benchmarks.determinism.pj.edu.ritclu.keysearch.FindKeyClu2
 * <I>plaintext</I> <I>ciphertext</I> <I>partialkey</I> <I>n</I>
 * <BR><I>K</I> = Number of parallel processes
 * <BR><I>plaintext</I> = Plaintext (128-bit hexadecimal number)
 * <BR><I>ciphertext</I> = Ciphertext (128-bit hexadecimal number)
 * <BR><I>partialkey</I> = Partial key (256-bit hexadecimal number)
 * <BR><I>n</I> = Number of key bits to search for
 * <P>
 * Whereas class {@linkplain FindKeyClu} always tests all possible keys, class
 * FindKeyClu2 stops as soon as it finds the correct key.
 *
 * @author  Alan Kaminsky
 * @version 18-Sep-2007
 */
public class FindKeyClu2
	{

// Prevent construction.

	private FindKeyClu2()
		{
		}

// Shared variables.

	// Command line arguments.
	static byte[] plaintext;
	static byte[] ciphertext;
	static byte[] partialkey;
	static int n;

	// Variables for doing trial encryptions.
	static int keylsbs;
	static int maxcounter;
	static byte[] foundkey;
	static byte[] trialkey;
	static byte[] trialciphertext;
	static AES256Cipher cipher;

// Main program.

	/**
	 * AES partial key search main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Start timing.
		long t1 = System.currentTimeMillis();

		// Initialize PJ middleware.
		Comm.init (args);
		Comm world = Comm.world();
		int size = world.size();
		int rank = world.rank();

		// Parse command line arguments.
		if (args.length != 4) usage();
		plaintext = Hex.toByteArray (args[0]);
		ciphertext = Hex.toByteArray (args[1]);
		partialkey = Hex.toByteArray (args[2]);
		n = Integer.parseInt (args[3]);

		// Make sure n is not too small or too large.
		if (n < 0)
			{
			System.err.println ("n = " + n + " is too small");
			System.exit (1);
			}
		if (n > 30)
			{
			System.err.println ("n = " + n + " is too large");
			System.exit (1);
			}

		// Set up variables for doing trial encryptions.
		keylsbs =
			((partialkey[28] & 0xFF) << 24) |
			((partialkey[29] & 0xFF) << 16) |
			((partialkey[30] & 0xFF) <<  8) |
			((partialkey[31] & 0xFF)      );
		maxcounter = 1 << n;
		trialkey = new byte [32];
		System.arraycopy (partialkey, 0, trialkey, 0, 32);
		trialciphertext = new byte [16];
		cipher = new AES256Cipher (trialkey);

		// Determine which chunk of the search space this process will do.
		Range chunk = new Range (0, maxcounter-1) .subrange (size, rank);
		int lb = chunk.lb();
		int ub = chunk.ub();

		// Set up to receive a notification when any process finds the key.
		CommRequest req = new CommRequest();
		world.floodReceive (IntegerBuf.emptyBuffer(), req);

		// Try every possible combination of low-order key bits.
		for (int counter = lb; counter <= ub; ++ counter)
			{
			// Fill in low-order key bits.
			int lsbs = keylsbs | counter;
			trialkey[28] = (byte) (lsbs >>> 24);
			trialkey[29] = (byte) (lsbs >>> 16);
			trialkey[30] = (byte) (lsbs >>>  8);
			trialkey[31] = (byte) (lsbs       );

			// Try the key.
			cipher.setKey (trialkey);
			cipher.encrypt (plaintext, trialciphertext);

			// If the result equals the ciphertext, we found the key. Send a
			// notification to all processes.
			if (match (ciphertext, trialciphertext))
				{
				foundkey = new byte [32];
				System.arraycopy (trialkey, 0, foundkey, 0, 32);
				world.floodSend (IntegerBuf.emptyBuffer());
				}

			// If key was found, exit loop.
			if (req.isFinished()) break;
			}

		// If we found the key, print it.
		if (foundkey != null)
			{
			System.out.println (Hex.toString (foundkey));
			}

		// Stop timing.
		long t2 = System.currentTimeMillis();
		System.out.println ((t2-t1) + " msec (" + rank + ")");
		}

// Hidden operations.

	/**
	 * Returns true if the two byte arrays match.
	 */
	private static boolean match
		(byte[] a,
		 byte[] b)
		{
		boolean matchsofar = true;
		int n = a.length;
		for (int i = 0; i < n; ++ i)
			{
			matchsofar = matchsofar && a[i] == b[i];
			}
		return matchsofar;
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java -Dpj.np=<K> benchmarks.determinism.pj.edu.ritclu.keysearch.FindKeyClu2 <plaintext> <ciphertext> <partialkey> <n>");
		System.err.println ("<K> = Number of parallel processes");
		System.err.println ("<plaintext> = Plaintext (128-bit hexadecimal number)");
		System.err.println ("<ciphertext> = Ciphertext (128-bit hexadecimal number)");
		System.err.println ("<partialkey> = Partial key (256-bit hexadecimal number)");
		System.err.println ("<n> = Number of key bits to search for");
		System.exit (1);
		}

	}
