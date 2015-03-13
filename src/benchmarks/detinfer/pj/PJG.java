//******************************************************************************
//
// File:    PJG.java
// Package: ---
// Unit:    Class PJG
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

package benchmarks.detinfer.pj;

import benchmarks.detinfer.pj.edu.ritimage.PJGImage;

import benchmarks.detinfer.pj.edu.ritswing.DisplayableFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Class PJG is a program that displays a Parallel Java Graphics (PJG) image
 * file. For further information about the PJG image file format, see class
 * {@linkplain benchmarks.detinfer.pj.edu.ritimage.PJGImage}.
 * <P>
 * If multiple file names are specified, the PJG program combines the contents
 * of all the PJG files into a single image. All the PJG files must contain an
 * image with the same height and width. The PJG program has a menu item for
 * saving the image in a single PJG file. Each process of a cluster parallel
 * program can write its own separate PJG file with a portion of the image, then
 * afterwards the PJG program can combine the files. The PJG program also has
 * menu items for saving the image in a PNG file or a PostScript file.
 * <P>
 * Usage: java PJG <I>filename</I> [ <I>filename</I> . . . ]
 *
 * @author  Alan Kaminsky
 * @version 21-Dec-2007
 */
public class PJG
	{

// Prevent construction.

	private PJG()
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
		String filename = "";

		try
			{
			// Parse command line arguments.
			if (args.length < 1) usage();

			// Read first PJG image file and create image.
			filename = args[0];
			final PJGImage image =
				PJGImage.readFromStream
					(new BufferedInputStream
						(new FileInputStream (filename)));

			// Read additional PJG image files.
			for (int i = 1; i < args.length; ++ i)
				{
				filename = args[i];
				PJGImage.Reader reader =
					image.prepareToRead
						(new BufferedInputStream
							(new FileInputStream (filename)));
				reader.read();
				reader.close();
				}

			// Set up frame to display image.
			final DisplayableFrame frame =
				new DisplayableFrame
					(args[0], image.getDisplayable(), JFrame.EXIT_ON_CLOSE);
			if (image.getWidth() > 800 || image.getHeight() > 800)
				{
				frame.setSize (800, 800);
				}

			// Add a menu item to the File menu.
			JMenuBar menubar = frame.getJMenuBar();
			JMenu fileMenu = menubar.getMenu (0);
			JMenuItem saveAsPjgItem =
				new JMenuItem ("Save as PJG...", KeyEvent.VK_A);
			saveAsPjgItem.setAccelerator
				(KeyStroke.getKeyStroke (KeyEvent.VK_A, InputEvent.CTRL_MASK));
			saveAsPjgItem.addActionListener
				(new ActionListener()
					{
					public void actionPerformed
						(ActionEvent e)
						{
						doSaveAsPjg (frame, image);
						}
					});
			fileMenu.insert (saveAsPjgItem, 0);

			// Display image.
			frame.setVisible (true);
			}

		catch (IOException exc)
			{
			System.err.println
				("PJG: Error reading file \"" + filename + "\"");
			exc.printStackTrace (System.err);
			System.exit (1);
			}
		}

// Hidden operations.

	/**
	 * Save the image in a PJG file.
	 */
	private static void doSaveAsPjg
		(final DisplayableFrame frame,
		 final PJGImage image)
		{
		frame.saveFile
			(".pjg",
			 "PJG image files",
			 new DisplayableFrame.FileSaver()
				{
				public void saveFile (File file) throws IOException
					{
					PJGImage.Writer writer =
						image.prepareToWrite
							(new BufferedOutputStream
								(new FileOutputStream (file)));
					writer.write();
					writer.close();
					}
				});
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java PJG <filename>");
		System.exit (1);
		}

	}
