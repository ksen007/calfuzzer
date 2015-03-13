//******************************************************************************
//
// File:    View.java
// Package: ---
// Unit:    Class View
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

package benchmarks.determinism.pj;

import benchmarks.determinism.pj.edu.ritswing.DisplayableFrame;
import benchmarks.determinism.pj.edu.ritswing.Viewable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JFrame;

/**
 * Class View is a main program for displaying a {@linkplain
 * benchmarks.determinism.pj.edu.ritswing.Viewable Viewable} object on the screen. The object is read in
 * from a file containing a {@linkplain benchmarks.determinism.pj.edu.ritswing.Viewable Viewable} object
 * in serialized form. The program polls the file periodically, and if the file
 * has changed, the program re-reads and re-displays the object.
 * <P>
 * The View program includes a "File" menu with menu items for saving the
 * drawing in a PNG file, saving the drawing in a PostScript file, and quitting
 * the program.
 * <P>
 * The View program can display instances of the following classes:
 * <UL>
 * <LI>{@linkplain benchmarks.determinism.pj.edu.ritdraw.Drawing}
 * <LI>{@linkplain benchmarks.determinism.pj.edu.ritnumeric.plot.Plot}
 * </UL>
 * <P>
 * Usage: java View <I>file</I>
 *
 * @author  Alan Kaminsky
 * @version 11-Dec-2007
 */
public class View
	{

// Prevent construction.

	private View()
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
		// Parse command line arguments.
		if (args.length != 1) usage();
		File drawingfile = new File (args[0]);

		// Read in drawing file and record last modification time.
		Viewable drawing = readfile (drawingfile);
		if (drawing == null)
			{
			System.err.println
				("View: Cannot read file \"" + drawingfile + "\"");
			System.exit (1);
			}
		long oldmodtime = drawingfile.lastModified();

		// Display drawing.
		DisplayableFrame frame = drawing.getFrame();
		String title = drawing.getTitle();
		frame.setTitle (title != null ? title : drawingfile.toString());
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setVisible (true);

		// Whenever drawing file changes, redisplay it.
		for (;;)
			{
			Thread.sleep (1000L);
			long newmodtime = drawingfile.lastModified();
			if (newmodtime != oldmodtime)
				{
				drawing = readfile (drawingfile);
				if (drawing != null)
					{
					title = drawing.getTitle();
					frame.setTitle
						(title != null ? title : drawingfile.toString());
					frame.display (drawing);
					oldmodtime = newmodtime;
					}
				}
			}
		}

// Hidden operations.

	/**
	 * Read the drawing file.
	 *
	 * @param  drawingfile  Drawing file.
	 *
	 * @return  Viewable object, or null if file could not be read.
	 */
	private static Viewable readfile
		(File drawingfile)
		{
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		ObjectInputStream ois = null;

		try
			{
			fis = new FileInputStream (drawingfile);
			bis = new BufferedInputStream (fis);
			ois = new ObjectInputStream (bis);
			Viewable drawing = (Viewable) ois.readObject();
			ois.close();
			return drawing;
			}

		catch (Exception exc)
			{
			if (ois != null)
				{
				try { ois.close(); } catch (IOException exc2) {}
				}
			if (bis != null)
				{
				try { bis.close(); } catch (IOException exc2) {}
				}
			if (fis != null)
				{
				try { fis.close(); } catch (IOException exc2) {}
				}
			return null;
			}
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java benchmarks.determinism.pj.edu.ritdraw.View <drawingfile>");
		System.exit (1);
		}

	}
