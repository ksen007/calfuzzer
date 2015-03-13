//******************************************************************************
//
// File:    XYZSeries.java
// Package: benchmarks.detinfer.pj.edu.ritnumeric
// Unit:    Class benchmarks.detinfer.pj.edu.ritnumeric.XYZSeries
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

package benchmarks.detinfer.pj.edu.ritnumeric;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Class XYZSeries is the abstract base class for a series of (<I>x,y,z</I>)
 * triples of real values (type <TT>double</TT>).
 *
 * @author  Alan Kaminsky
 * @version 13-Oct-2007
 */
public abstract class XYZSeries
	{

// Exported helper classes.

	/**
	 * Class XYZSeries.Stats holds the means, variances, and standard deviations
	 * of an {@linkplain XYZSeries}.
	 *
	 * @author  Alan Kaminsky
	 * @version 12-Jun-2007
	 */
	public static class Stats
		{
		/**
		 * Mean of the series' X values.
		 */
		public final double meanX;

		/**
		 * Variance of the series' X values.
		 */
		public final double varX;

		/**
		 * Standard deviation of the series' X values.
		 */
		public final double stddevX;

		/**
		 * Mean of the series' Y values.
		 */
		public final double meanY;

		/**
		 * Variance of the series' Y values.
		 */
		public final double varY;

		/**
		 * Standard deviation of the series' Y values.
		 */
		public final double stddevY;

		/**
		 * Mean of the series' Z values.
		 */
		public final double meanZ;

		/**
		 * Variance of the series' Z values.
		 */
		public final double varZ;

		/**
		 * Standard deviation of the series' Z values.
		 */
		public final double stddevZ;

		/**
		 * Construct a new XYZSeries.Stats object.
		 */
		private Stats
			(double meanX,
			 double varX,
			 double stddevX,
			 double meanY,
			 double varY,
			 double stddevY,
			 double meanZ,
			 double varZ,
			 double stddevZ)
			{
			this.meanX = meanX;
			this.varX = varX;
			this.stddevX = stddevX;
			this.meanY = meanY;
			this.varY = varY;
			this.stddevY = stddevY;
			this.meanZ = meanZ;
			this.varZ = varZ;
			this.stddevZ = stddevZ;
			}
		}

	/**
	 * Class XYZSeries.XSeriesView provides a series view of the X values in an
	 * XYZ series.
	 *
	 * @author  Alan Kaminsky
	 * @version 12-Jun-2007
	 */
	private static class XSeriesView
		extends Series
		{
		private XYZSeries outer;

		public XSeriesView
			(XYZSeries outer)
			{
			this.outer = outer;
			}

		public int length()
			{
			return outer.length();
			}

		public double x
			(int i)
			{
			return outer.x (i);
			}
		}

	/**
	 * Class XYZSeries.YSeriesView provides a series view of the Y values in an
	 * XYZ series.
	 *
	 * @author  Alan Kaminsky
	 * @version 12-Jun-2007
	 */
	private static class YSeriesView
		extends Series
		{
		private XYZSeries outer;

		public YSeriesView
			(XYZSeries outer)
			{
			this.outer = outer;
			}

		public int length()
			{
			return outer.length();
			}

		public double x
			(int i)
			{
			return outer.y (i);
			}
		}

	/**
	 * Class XYZSeries.ZSeriesView provides a series view of the Z values in an
	 * XYZ series.
	 *
	 * @author  Alan Kaminsky
	 * @version 12-Jun-2007
	 */
	private static class ZSeriesView
		extends Series
		{
		private XYZSeries outer;

		public ZSeriesView
			(XYZSeries outer)
			{
			this.outer = outer;
			}

		public int length()
			{
			return outer.length();
			}

		public double x
			(int i)
			{
			return outer.z (i);
			}
		}

	/**
	 * Class XYZSeries.XYSeriesView provides an XY series view of the X and Y
	 * values in an XYZ series.
	 *
	 * @author  Alan Kaminsky
	 * @version 13-Oct-2007
	 */
	private static class XYSeriesView
		extends XYSeries
		{
		private XYZSeries outer;

		public XYSeriesView
			(XYZSeries outer)
			{
			this.outer = outer;
			}

		public int length()
			{
			return outer.length();
			}

		public double x
			(int i)
			{
			return outer.x (i);
			}

		public double y
			(int i)
			{
			return outer.y (i);
			}
		}

// Exported constructors.

	/**
	 * Construct a new XYZ series.
	 */
	public XYZSeries()
		{
		}

// Exported operations.

	/**
	 * Returns the number of values in this series.
	 *
	 * @return  Length.
	 */
	public abstract int length();

	/**
	 * Determine if this series is empty.
	 *
	 * @return  True if this series is empty (length = 0), false otherwise.
	 */
	public boolean isEmpty()
		{
		return length() == 0;
		}

	/**
	 * Returns the given X value in this series.
	 *
	 * @param  i  Index.
	 *
	 * @return  The X value in this series at index <TT>i</TT>.
	 *
	 * @exception  ArrayIndexOutOfBoundsException
	 *     (unchecked exception) Thrown if <TT>i</TT> is not in the range
	 *     <TT>0</TT> .. <TT>length()-1</TT>.
	 */
	public abstract double x
		(int i);

	/**
	 * Returns the given Y value in this series.
	 *
	 * @param  i  Index.
	 *
	 * @return  The Y value in this series at index <TT>i</TT>.
	 *
	 * @exception  ArrayIndexOutOfBoundsException
	 *     (unchecked exception) Thrown if <TT>i</TT> is not in the range
	 *     <TT>0</TT> .. <TT>length()-1</TT>.
	 */
	public abstract double y
		(int i);

	/**
	 * Returns the given Z value in this series.
	 *
	 * @param  i  Index.
	 *
	 * @return  The Z value in this series at index <TT>i</TT>.
	 *
	 * @exception  ArrayIndexOutOfBoundsException
	 *     (unchecked exception) Thrown if <TT>i</TT> is not in the range
	 *     <TT>0</TT> .. <TT>length()-1</TT>.
	 */
	public abstract double z
		(int i);

	/**
	 * Returns the minimum X value in this series.
	 *
	 * @return  Minimum X value.
	 */
	public double minX()
		{
		int n = length();
		double result = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; ++ i)
			{
			result = Math.min (result, x(i));
			}
		return result;
		}

	/**
	 * Returns the maximum X value in this series.
	 *
	 * @return  Maximum X value.
	 */
	public double maxX()
		{
		int n = length();
		double result = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < n; ++ i)
			{
			result = Math.max (result, x(i));
			}
		return result;
		}

	/**
	 * Returns the minimum Y value in this series.
	 *
	 * @return  Minimum Y value.
	 */
	public double minY()
		{
		int n = length();
		double result = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; ++ i)
			{
			result = Math.min (result, y(i));
			}
		return result;
		}

	/**
	 * Returns the maximum Y value in this series.
	 *
	 * @return  Maximum Y value.
	 */
	public double maxY()
		{
		int n = length();
		double result = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < n; ++ i)
			{
			result = Math.max (result, y(i));
			}
		return result;
		}

	/**
	 * Returns the minimum Z value in this series.
	 *
	 * @return  Minimum Z value.
	 */
	public double minZ()
		{
		int n = length();
		double result = Double.POSITIVE_INFINITY;
		for (int i = 0; i < n; ++ i)
			{
			result = Math.min (result, z(i));
			}
		return result;
		}

	/**
	 * Returns the maximum Z value in this series.
	 *
	 * @return  Maximum Z value.
	 */
	public double maxZ()
		{
		int n = length();
		double result = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < n; ++ i)
			{
			result = Math.max (result, z(i));
			}
		return result;
		}

	/**
	 * Returns a {@linkplain Stats Stats} object containing statistics of this
	 * series.
	 *
	 * @return  Statistics.
	 */
	public Stats stats()
		{
		int n = length();
		double sumX = 0.0;
		double sumY = 0.0;
		double sumZ = 0.0;
		for (int i = 0; i < n; ++ i)
			{
			sumX += x(i);
			sumY += y(i);
			sumZ += z(i);
			}
		double meanX = sumX / n;
		double sumdevX = 0.0;
		double sumdevsqrX = 0.0;
		double meanY = sumY / n;
		double sumdevY = 0.0;
		double sumdevsqrY = 0.0;
		double meanZ = sumZ / n;
		double sumdevZ = 0.0;
		double sumdevsqrZ = 0.0;
		for (int i = 0; i < n; ++ i)
			{
			double devX = x(i) - meanX;
			double devsqrX = devX * devX;
			sumdevX += devX;
			sumdevsqrX += devsqrX;
			double devY = y(i) - meanY;
			double devsqrY = devY * devY;
			sumdevY += devY;
			sumdevsqrY += devsqrY;
			double devZ = z(i) - meanZ;
			double devsqrZ = devZ * devZ;
			sumdevZ += devZ;
			sumdevsqrZ += devsqrZ;
			}
		double varX = (sumdevsqrX - sumdevX * sumdevX / n) / (n - 1);
		double stddevX = Math.sqrt (varX);
		double varY = (sumdevsqrY - sumdevY * sumdevY / n) / (n - 1);
		double stddevY = Math.sqrt (varY);
		double varZ = (sumdevsqrZ - sumdevZ * sumdevZ / n) / (n - 1);
		double stddevZ = Math.sqrt (varZ);
		return new Stats
			(meanX, varX, stddevX, meanY, varY, stddevY, meanZ, varZ, stddevZ);
		}

	/**
	 * Returns a {@linkplain Series} view of the X values in this XYZ series.
	 * <P>
	 * <I>Note:</I> The returned Series object is backed by this XYZ series
	 * object. Changing the contents of this XYZ series object will change the
	 * contents of the returned Series object.
	 *
	 * @return  Series of X values.
	 */
	public Series xSeries()
		{
		return new XSeriesView (this);
		}

	/**
	 * Returns a {@linkplain Series} view of the Y values in this XYZ series.
	 * <P>
	 * <I>Note:</I> The returned Series object is backed by this XYZ series
	 * object. Changing the contents of this XYZ series object will change the
	 * contents of the returned Series object.
	 *
	 * @return  Series of Y values.
	 */
	public Series ySeries()
		{
		return new YSeriesView (this);
		}

	/**
	 * Returns a {@linkplain Series} view of the Z values in this XYZ series.
	 * <P>
	 * <I>Note:</I> The returned Series object is backed by this XYZ series
	 * object. Changing the contents of this XYZ series object will change the
	 * contents of the returned Series object.
	 *
	 * @return  Series of Z values.
	 */
	public Series zSeries()
		{
		return new ZSeriesView (this);
		}

	/**
	 * Returns an {@linkplain XYSeries} view of the X and Y values in this XYZ
	 * series.
	 * <P>
	 * <I>Note:</I> The returned XYSeries object is backed by this XYZ series
	 * object. Changing the contents of this XYZ series object will change the
	 * contents of the returned Series object.
	 *
	 * @return  Series of X and Y values.
	 */
	public XYSeries xySeries()
		{
		return new XYSeriesView (this);
		}

	/**
	 * Print this XYZ series on the standard output. Each line of output
	 * consists of the index, the <I>x</I> value, the <I>y</I> value, and the
	 * <I>z</I> value, separated by tabs.
	 */
	public void print()
		{
		print (System.out);
		}

	/**
	 * Print this XYZ series on the given print stream. Each line of output
	 * consists of the index, the <I>x</I> value, the <I>y</I> value, and the
	 * <I>z</I> value, separated by tabs.
	 *
	 * @param  theStream  Print stream.
	 */
	public void print
		(PrintStream theStream)
		{
		int n = length();
		for (int i = 0; i < n; ++ i)
			{
			theStream.print (i);
			theStream.print ('\t');
			theStream.print (x(i));
			theStream.print ('\t');
			theStream.print (y(i));
			theStream.print ('\t');
			theStream.println (z(i));
			}
		}

	/**
	 * Print this XYZ series on the given print writer. Each line of output
	 * consists of the index, the <I>x</I> value, the <I>y</I> value, and the
	 * <I>z</I> value, separated by tabs.
	 *
	 * @param  theWriter  Print writer.
	 */
	public void print
		(PrintWriter theWriter)
		{
		int n = length();
		for (int i = 0; i < n; ++ i)
			{
			theWriter.print (i);
			theWriter.print ('\t');
			theWriter.print (x(i));
			theWriter.print ('\t');
			theWriter.print (y(i));
			theWriter.print ('\t');
			theWriter.println (z(i));
			}
		}

	}
