//******************************************************************************
//
// File:    TimeFit.java
// Package: ---
// Unit:    Class TimeFit
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

package benchmarks.determinism.pj;

import benchmarks.determinism.pj.edu.ritnumeric.AggregateXYSeries;
import benchmarks.determinism.pj.edu.ritnumeric.ListSeries;
import benchmarks.determinism.pj.edu.ritnumeric.NonNegativeLeastSquares;

import benchmarks.determinism.pj.edu.ritnumeric.plot.Dots;
import benchmarks.determinism.pj.edu.ritnumeric.plot.Plot;

import java.awt.Color;

import java.io.File;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Map;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class TimeFit is a main program that analyzes running time measurements and
 * fits them to a running time model for a parallel program.
 * <P>
 * Usage: java TimeFit <I>inputfile</I> <I>nplot</I> <I>f1</I>
 * [ <I>f2</I> . . . ]
 * <P>
 * The input file, a plain text file, is the same as for class {@linkplain
 * Speedup}. For a detailed description of the input file format, see class
 * {@linkplain Speedup}.
 * <P>
 * The input file begins with running time data. Each line contains an <I>n</I>
 * value, a <I>K</I> value, and one or more <I>T</I> values. The <I>n</I> value,
 * an integer, is the problem size parameter. The <I>K</I> value, an integer
 * &gt;= 0, is the number of parallel processors; <I>K</I> = 0 signifies the
 * running times are for a sequential version of the program, <I>K</I> &gt; 0
 * signifies the running times are for a parallel version of the program. Each
 * <I>T</I> value, a long integer, is a running time measurement in
 * milliseconds. Each line of running time data, then, contains one experimental
 * data point with two independent variables, <I>n</I> and <I>K</I>, and one
 * dependent variable, <I>T</I> (the smallest of the running time values).
 * <P>
 * The <I>nplot</I> command line argument specifies one of the <I>n</I> values
 * in the running time data section of the input file. The TimeFit program will
 * generate plots of the data and the model for this value of <I>n</I>, as
 * described below.
 * <P>
 * The running time model is composed of <I>P</I> basis functions,
 * <I>f</I><SUB>1</SUB>(<I>n,K</I>), <I>f</I><SUB>2</SUB>(<I>n,K</I>), . . . ,
 * <I>f</I><SUB><I>P</I></SUB>(<I>n,K</I>). For each basis function there is
 * also a model parameter <I>c</I><SUB>1</SUB>, <I>c</I><SUB>2</SUB>, . . . ,
 * <I>c</I><SUB><I>P</I></SUB>. The model is:
 * <P>
 * <TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0>
 * <TR>
 * <TD ALIGN="center" VALIGN="center">&nbsp;</TD>
 * <TD ALIGN="center" VALIGN="center"><FONT SIZE="-1"><I>P</I></FONT></TD>
 * <TD ALIGN="center" VALIGN="center">&nbsp;</TD>
 * </TR>
 * <TR>
 * <TD ALIGN="center" VALIGN="center"><I>T</I>(<I>n,K</I>)&nbsp;&nbsp;=&nbsp;&nbsp;</TD>
 * <TD ALIGN="center" VALIGN="center"><FONT SIZE="+3">&Sigma;</FONT></TD>
 * <TD ALIGN="center" VALIGN="center">&nbsp;<I>c</I><SUB><I>i</I></SUB>&nbsp;<I>f</I><SUB><I>i</I></SUB>(<I>n,K</I>)</TD>
 * </TR>
 * <TR>
 * <TD ALIGN="center" VALIGN="center">&nbsp;</TD>
 * <TD ALIGN="center" VALIGN="center"><FONT SIZE="-1"><I>i</I>=1</FONT></TD>
 * <TD ALIGN="center" VALIGN="center">&nbsp;</TD>
 * </TR>
 * </TABLE>
 * <P>
 * The basis functions are specified on the command line in symbolic form. Each
 * basis function consists of either an <I>n</I> term, a <I>K</I> term, or an
 * <I>n</I> term followed by a <I>K</I> term. The <I>n</I> term may be one of
 * the following:
 * <BR><TT>&nbsp;&nbsp;&nbsp;&nbsp;1</TT>
 * <BR><TT>&nbsp;&nbsp;&nbsp;&nbsp;n</TT>
 * <BR><TT>&nbsp;&nbsp;&nbsp;&nbsp;n^<I>a</I></TT>&nbsp;&nbsp;&nbsp;&nbsp;(<TT><I>a</I></TT> is an
 * integer exponent &gt;= 0)
 * <BR><TT>&nbsp;&nbsp;&nbsp;&nbsp;lgn</TT>&nbsp;&nbsp;&nbsp;&nbsp;(base-2 logarithm of <I>n</I>)
 * <BR><TT>&nbsp;&nbsp;&nbsp;&nbsp;2^n</TT>
 * <P>
 * The <I>K</I> term may be one of the following:
 * <BR><TT>&nbsp;&nbsp;&nbsp;&nbsp;K</TT>
 * <BR><TT>&nbsp;&nbsp;&nbsp;&nbsp;/K</TT>
 * <P>
 * For example, the command:
 * <P>
 * <TT>java TimeFit <I>inputfile</I> <I>nplot</I> 1 n nK</TT>
 * <P>
 * specifies the model:
 * <P>
 * <I>T</I>(<I>n,K</I>) = <I>c</I><SUB>1</SUB> + <I>c</I><SUB>2</SUB><I>n</I> +
 * <I>c</I><SUB>3</SUB><I>nK</I>
 * <P>
 * The command (note that quotes may be necessary):
 * <P>
 * <TT>java TimeFit <I>inputfile</I> <I>nplot</I> 1 n K 'n^2/K' 'n^3/K'</TT>
 * <P>
 * specifies the model:
 * <P>
 * <I>T</I>(<I>n,K</I>) = <I>c</I><SUB>1</SUB> + <I>c</I><SUB>2</SUB><I>n</I> +
 * <I>c</I><SUB>3</SUB><I>K</I> +
 * <I>c</I><SUB>4</SUB><I>n</I><SUP>2</SUP>/<I>K</I> +
 * <I>c</I><SUB>5</SUB><I>n</I><SUP>3</SUP>/<I>K</I>
 * <P>
 * The TimeFit program finds the model parameter values that are nonnegative and
 * that minimize &chi;<SUP>2</SUP>, the sum of the squares of the differences
 * between the measured <I>T</I> values and the model <I>T</I> values. That is,
 * the TimeFit program does a nonnegative least squares curve fit. When fitting
 * the model, the TimeFit program uses only the running time measurements for
 * the parallel version of the program (<I>K</I> &gt;= 1). The TimeFit program
 * prints out the fitted model parameter values and the value of
 * &chi;<SUP>2</SUP>.
 * <P>
 * The TimeFit program prints, for the problem size <I>nplot</I> specified on
 * the command line, a table of the measured <I>n</I>, <I>K</I>, <I>T</I>,
 * <I>Speedup</I>, <I>Eff</I>, and <I>EDSF</I> values as well as a table of
 * those values calculated from the fitted model. The speedup and efficiency are
 * calculated with respect to the parallel version of the program on one
 * processor.
 * <P>
 * The TimeFit program displays, for the problem size <I>nplot</I> specified on
 * the command line, plots of the running time, speedup, efficiency, and EDSF
 * series, each in its own window. The measured data is plotted in black, the
 * data from the fitted model is plotted in red. Each plot window has menu
 * options for saving the plot to a PNG image file, saving the plot to a
 * PostScript file, and zooming the display.
 * <P>
 * Here is an example of an input file:
 * <A HREF="doc-files/times.txt">times.txt</A>
 * <FONT SIZE="-1">
 * <PRE>
 * 20 0 5718 5773 5777 5822 5864 5886 5891
 * 20 1 5798 5809 5817 5823 5849 5865 5897
 * 20 2 2971 2986 2988 3003 3014 3017 3034
 * 20 3 2056 2077 2092 2112 2114 2135 2165
 * 20 4 1579 1591 1595 1603 1604 1605 1610
 * 20 5 1302 1304 1307 1311 1314 1316 1319
 * 20 6 1091 1099 1110 1112 1114 1135 1145
 * 20 7 973 974 979 1007 1027 1027 1105
 * 20 8 867 870 881 883 895 913 1019
 * 21 0 11169 11176 11180 11298 11408 11416 11576
 * 21 1 11334 11390 11415 11430 11432 11445 11458
 * 21 2 5753 5758 5801 5802 5812 5838 5866
 * 21 3 3921 3933 3981 3993 4002 4056 4086
 * 21 4 3006 3011 3012 3016 3033 3052 3058
 * 21 5 2393 2425 2426 2449 2452 2458 2490
 * 21 6 2059 2064 2076 2080 2081 2442 2733
 * 21 7 1776 1781 1783 1785 1799 1802 1825
 * 21 8 1611 1616 1619 1625 1642 1643 1771
 * 22 0 22138 22279 22282 22376 22416 22506 22670
 * 22 1 22216 22290 22316 22549 22575 22637 22662
 * 22 2 11220 11359 11361 11369 11385 11387 11426
 * 22 3 7604 7631 7633 7771 7808 7905 7914
 * 22 4 5780 5814 5817 5863 5886 5886 6807
 * 22 5 4704 4704 4713 4725 4727 4753 4757
 * 22 6 3930 3944 3953 3956 3967 3972 3984
 * 22 7 3405 3426 3431 3444 3446 3455 3687
 * 22 8 3023 3041 3042 3048 3068 3091 3123
 * 23 0 43995 44306 44441 44543 44718 44847 45234
 * 23 1 44170 44209 44331 44345 44364 44702 44767
 * 23 2 22430 22444 22460 22558 22560 22942 22983
 * 23 3 15190 15213 15396 15516 15683 15689 15910
 * 23 4 11325 11552 11558 11560 11579 11595 11610
 * 23 5 9281 9289 9289 9309 9361 9418 9423
 * 23 6 7730 7773 7775 7830 7877 7955 7956
 * 23 7 6675 6700 6705 6706 6715 6727 6737
 * 23 8 5961 5972 5989 6003 6016 6094 6609
 * 24 0 88101 88457 89067 89250 89323 89917 90187
 * 24 1 87694 87924 88072 88353 88466 88540 88894
 * 24 2 44196 44681 44857 44887 44939 45039 45166
 * 24 3 29859 30159 30270 30484 30486 30775 30814
 * 24 4 22551 22670 22724 22794 23156 23262 25503
 * 24 5 18063 18331 18477 18592 18767 19364 19453
 * 24 6 15179 15340 15445 15498 15678 16906 18453
 * 24 7 13180 13249 13260 13314 13327 13406 13904
 * 24 8 11716 11770 11805 11934 11951 12059 12991
 * 25 0 175108 175302 175680 176496 177071 178365 181651
 * 25 1 175452 175623 175670 175926 176410 176607 178560
 * 25 2 88185 89112 89178 89241 89432 89777 89921
 * 25 3 60031 60145 60161 61135 61647 61659 61727
 * 25 4 44941 45368 45618 45751 45934 46339 53115
 * 25 5 36443 36481 36586 36588 36671 36772 36775
 * 25 6 30270 30461 30518 30688 30720 30993 31282
 * 25 7 26076 26368 26405 26556 26616 26721 26770
 * 25 8 23287 23288 23350 23441 23616 23827 23965
 * 26 0 351583 357215 357360 357711 358596 362653 363086
 * 26 1 348706 351180 351643 351936 351987 352264 352354
 * 26 2 176925 179100 179388 179638 181486 182045 196086
 * 26 3 119701 119715 120342 120793 121305 122482 122774
 * 26 4 89095 89182 90051 90113 90240 92107 98899
 * 26 5 71888 72280 72356 72611 73266 73785 74389
 * 26 6 61077 61097 61108 61156 61382 61396 61620
 * 26 7 51485 52184 52282 52733 52906 53168 53238
 * 26 8 46279 46543 46683 46851 48792 49126 51304
 * 27 0 705776 706487 713025 713236 713634 714578 716011
 * 27 1 696826 700121 703033 705225 709731 712779 713126
 * 27 2 351242 354433 354523 355175 355433 355831 357476
 * 27 3 237912 238336 238485 238900 239785 241458 241914
 * 27 4 178166 178444 178677 178688 181724 182888 183997
 * 27 5 144231 145245 146223 146378 146829 147605 147956
 * 27 6 120829 120894 121618 121637 121664 121835 122048
 * 27 7 103471 103706 104441 104913 105210 106320 113690
 * 27 8 92259 93030 93253 94102 94126 94317 103790
 * n 20 1048576 "N = 1M"
 * n 21 2097152 "N = 2M"
 * n 22 4194304 "N = 4M"
 * n 23 8388608 "N = 8M"
 * n 24 16777216 "N = 16M"
 * n 25 33554432 "N = 32M"
 * n 26 67108864 "N = 64M"
 * n 27 134217728 "N = 128M"
 * time rightMargin 54
 * speedup rightMargin 54
 * speedup xAxisEnd 8
 * speedup xAxisMajorDivisions 8
 * speedup yAxisEnd 8
 * speedup yAxisMajorDivisions 8
 * eff rightMargin 54
 * eff xAxisEnd 8
 * eff xAxisMajorDivisions 8
 * eff yAxisEnd 1.1
 * eff yAxisMajorDivisions 11
 * edsf rightMargin 54
 * edsf xAxisEnd 8
 * edsf xAxisMajorDivisions 8
 * edsf yAxisEnd 0.05
 * edsf yAxisMajorDivisions 10
 * </PRE>
 * </FONT>
 * <P>
 * Here is the output the TimeFit program printed for the above input file:
 * <A HREF="doc-files/timefit.txt">timefit.txt</A>
 * <FONT SIZE="-1">
 * <PRE>
 * $ java TimeFit times.txt 23 1 '2^n' '2^nK' '2^n/K'
 * Actual
 * n	K	T	Spdup	Effic	EDSF
 * 23	1	44170	1.000	1.000
 * 23	2	22430	1.969	0.985	0.016
 * 23	3	15190	2.908	0.969	0.016
 * 23	4	11325	3.900	0.975	0.009
 * 23	5	9281	4.759	0.952	0.013
 * 23	6	7730	5.714	0.952	0.010
 * 23	7	6675	6.617	0.945	0.010
 * 23	8	5961	7.410	0.926	0.011
 * Model
 * n	K	T	Spdup	Effic	EDSF
 * 23	1	43864	1.000	1.000
 * 23	2	22244	1.972	0.986	0.014
 * 23	3	15037	2.917	0.972	0.014
 * 23	4	11434	3.836	0.959	0.014
 * 23	5	9272	4.731	0.946	0.014
 * 23	6	7830	5.602	0.934	0.014
 * 23	7	6801	6.450	0.921	0.014
 * 23	8	6029	7.276	0.909	0.014
 * T(n,K) = 286.3702556157946 + 4.0206527835534166E-5 (2^n) + 0.0 (2^n) K + 0.0051546938471946505 (2^n) / K
 * chi^2 = 1.0941942674402043E7
 * </PRE>
 * </FONT>
 * <P>
 * Rounding the model parameters to three significant figures, the fitted model
 * is as follows. Note that the coefficient for the basis function
 * 2<SUP><I>n</I></SUP><I>K</I> is 0, meaning that the model does not include
 * that basis function.
 * <P>
 * <I>T</I>(<I>n,K</I>) = 286 + 4.02x10<SUP>-5</SUP> 2<SUP><I>n</I></SUP> +
 * 5.15x10<SUP>-3</SUP> 2<SUP><I>n</I></SUP>/<I>K</I> msec
 * <P>
 * Here are the plots the TimeFit program generated for the above input file:
 * <BR><IMG SRC="doc-files/plot_e.png"> <IMG SRC="doc-files/plot_h.png">
 * <BR><IMG SRC="doc-files/plot_f.png"> <IMG SRC="doc-files/plot_g.png">
 * <P>
 * The TimeFit program's error handling is rudimentary. The first error in the
 * input file terminates the program. The error may cause an exception stack
 * trace to be printed.
 *
 * @author  Alan Kaminsky
 * @version 06-Aug-2008
 */
public class TimeFit
	{

// Prevent construction.

	private TimeFit()
		{
		}

// Hidden helper classes.

	/**
	 * Class Data is a record of data associated with a certain size parameter
	 * n.
	 *
	 * @author  Alan Kaminsky
	 * @version 15-Jun-2007
	 */
	private static class Data
		{
		// Size parameter n.
		public int n;

		// Smallest running time for sequential version, or 0.0 if not
		// specified.
		public double T_seq;

		// Largest running time for sequential version, or 0.0 if not specified.
		public double T_max_seq;

		// T deviation value for sequential version, or 0.0 if not specified.
		public double Dev_seq;

		// Smallest running time for parallel version K=1, or 0.0 if not
		// specified.
		public double T_par_1;

		// Series of K values, K >= 1 (K = number of processors).
		public ListSeries K_series = new ListSeries();

		// Series of T values, K >= 1 (T = smallest running time measurement).
		public ListSeries T_series = new ListSeries();

		// Series of T_max values, K >= 1 (T_max = largest running time
		// measurement).
		public ListSeries T_max_series = new ListSeries();

		// Series of Speedup values, K >= 1.
		public ListSeries Speedup_series = new ListSeries();

		// Series of Eff values, K >= 1.
		public ListSeries Eff_series = new ListSeries();

		// Series of T deviation values, K >= 1.
		public ListSeries Dev_series = new ListSeries();

		// Series of K values, K >= 2 (K = number of processors).
		public ListSeries K_series_2 = new ListSeries();

		// Series of EDSF values, K >= 2 (K = number of processors).
		public ListSeries EDSF_series_2 = new ListSeries();

		// Problem size N.
		public double N;

		// Label text.
		public String labelText;

		// Constructor.
		public Data
			(int n)
			{
			this.n = n;
			this.N = n;
			this.labelText = "N = " + n;
			}
		}

	/**
	 * Class BasisFunction is the abstract base class for a basis function.
	 *
	 * @author  Alan Kaminsky
	 * @version 15-Jun-2007
	 */
	private static abstract class BasisFunction
		{
		public abstract double f (double n, double K);

		public abstract String toString (double c);
		}

// Global variables.

	// For parsing basis functions.
	private static Pattern N_POWER_PATTERN =
		Pattern.compile ("n\\^([0-9]+)");
	private static Pattern N_POWER_TIMES_K_PATTERN =
		Pattern.compile ("n\\^([0-9]+)K");
	private static Pattern N_POWER_OVER_K_PATTERN =
		Pattern.compile ("n\\^([0-9]+)/K");

	// For parsing input file.
	private static Pattern QUOTED_STRING_PATTERN =
		Pattern.compile ("\"[^\"]*\"");

	// For printing metrics.
	private static DecimalFormat FMT_0  = new DecimalFormat ("0");
	private static DecimalFormat FMT_0E = new DecimalFormat ("0E0");
	private static DecimalFormat FMT_1  = new DecimalFormat ("0.0");
	private static DecimalFormat FMT_2  = new DecimalFormat ("0.00");
	private static DecimalFormat FMT_3  = new DecimalFormat ("0.000");

	// Mapping from size parameter n (type Integer) to data for n (type Data).
	private static Map<Integer,Data> dataMap = new TreeMap<Integer,Data>();

	// Maximum K value.
	private static double K_max = Double.NEGATIVE_INFINITY;

	// Plot objects.
	private static Plot T_plot = new Plot();
	private static Plot Speedup_plot = new Plot();
	private static Plot Eff_plot = new Plot();
	private static Plot EDSF_plot = new Plot();

	// Command line arguments.
	private static File inputfile;
	private static int nplot;
	private static ArrayList<BasisFunction> basis =
		new ArrayList<BasisFunction>();

// Main program.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Parse command line arguments.
		if (args.length < 3) usage();
		inputfile = new File (args[0]);
		nplot = Integer.parseInt (args[1]);
		for (int i = 2; i < args.length; ++ i)
			{
			basis.add (parseBasisFunction (args[i]));
			}

		// Set up plots with default attributes.
		T_plot
			.plotTitle ("n = " + nplot)
			.rightMargin (72)
			.minorGridLines (true)
			.xAxisKind (Plot.LOGARITHMIC)
			.xAxisMinorDivisions (10)
			.xAxisTitle ("Processors, K")
			.yAxisKind (Plot.LOGARITHMIC)
			.yAxisMinorDivisions (10)
			.yAxisTickFormat (FMT_0E)
			.yAxisTickScale (1000)
			.yAxisTitle ("Running Time, T (sec)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		Speedup_plot
			.plotTitle ("n = " + nplot)
			.rightMargin (72)
			.xAxisStart (0)
			.xAxisTitle ("Processors, K")
			.yAxisStart (0)
			.yAxisTitle ("Speedup")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		Eff_plot
			.plotTitle ("n = " + nplot)
			.rightMargin (72)
			.xAxisStart (0)
			.xAxisTitle ("Processors, K")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_1)
			.yAxisTitle ("Efficiency")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		EDSF_plot
			.plotTitle ("n = " + nplot)
			.rightMargin (72)
			.xAxisStart (0)
			.xAxisTitle ("Processors, K")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_0)
			.yAxisTickScale (0.001)
			.yAxisTitle ("Sequential Fraction, F (/1000)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);

		// Parse the input file.
		Scanner scanner = new Scanner (inputfile);
		int linenum = 1;
		while (scanner.hasNextLine())
			{
			String line = scanner.nextLine();
			int i = line.indexOf ('#');
			if (i >= 0) line = line.substring (0, i);
			line = line.trim();
			if (line.length() > 0) parseLine (line, linenum);
			++ linenum;
			}

		// Validate data.
		for (Data data: dataMap.values())
			{
			validateData (data);
			}

		// Set up and solve nonnegative least squares problem.
		ListSeries n_series = new ListSeries();
		ListSeries K_series = new ListSeries();
		ListSeries T_series = new ListSeries();
		for (Data data : dataMap.values())
			{
			double n = data.n;
			for (int i = 0; i < data.K_series.length(); ++ i)
				{
				double K = data.K_series.x(i);
				double T = data.T_series.x(i);
				if (K >= 1)
					{
					n_series.add (n);
					K_series.add (K);
					T_series.add (T);
					}
				}
			}
		int M = n_series.length();
		int P = basis.size();
		NonNegativeLeastSquares nnls = new NonNegativeLeastSquares (M, P);
		for (int i = 0; i < M; ++ i)
			{
			double n = n_series.x(i);
			double K = K_series.x(i);
			double T = T_series.x(i);
			for (int j = 0; j < P; ++ j)
				{
				nnls.a[i][j] = basis.get(j).f (n, K);
				}
			nnls.b[i] = T;
			}
		nnls.solve();

		// Get actual data for n = nplot.
		Data actualData = dataMap.get (nplot);
		if (actualData == null)
			{
			System.err.println ("No data for n = " + nplot);
			System.exit (1);
			}

		// Set up model data for n = nplot.
		Data modelData = new Data (nplot);
		for (int i = 0; i < actualData.K_series.length(); ++ i)
			{
			double K = actualData.K_series.x(i);
			double T = modelFunction (nplot, K, nnls.x);
			if (K == 1) modelData.T_par_1 = T;
			double Speedup = modelData.T_par_1 / T;
			double Eff = Speedup / K;
			modelData.K_series.add (K);
			modelData.T_series.add (T);
			modelData.Speedup_series.add (Speedup);
			modelData.Eff_series.add (Eff);
			if (K >= 2)
				{
				double EDSF =
					(K * T - modelData.T_par_1) / modelData.T_par_1 / (K-1);
				modelData.K_series_2.add (K);
				modelData.EDSF_series_2.add (EDSF);
				}
			}

		// Print data.
		System.out.println ("Actual");
		printData (actualData);
		System.out.println ("Model");
		printData (modelData);

		// Print model function and chi^2.
		System.out.print ("T(n,K)");
		for (int i = 0; i < P; ++ i)
			{
			System.out.print (i == 0 ? " = " : " + ");
			System.out.print (basis.get(i).toString (nnls.x[i]));
			}
		System.out.println();
		System.out.println ("chi^2 = " + nnls.normsqr);

		// Add ideal performance lines to plots.
		Speedup_plot
			.seriesDots (null)
			.seriesColor (new Color (0.7f, 0.7f, 0.7f))
			.xySeries (new double[] {0, K_max}, new double[] {0, K_max});
		Eff_plot
			.seriesDots (null)
			.seriesColor (new Color (0.7f, 0.7f, 0.7f))
			.xySeries (new double[] {0, K_max}, new double[] {1, 1});

		// Add model data to plots.
		T_plot
			.seriesDots (null)
			.seriesColor (Color.RED)
			.labelColor (Color.RED);
		Speedup_plot
			.seriesDots (null)
			.seriesColor (Color.RED)
			.labelColor (Color.RED);
		Eff_plot
			.seriesDots (null)
			.seriesColor (Color.RED)
			.labelColor (Color.RED);
		EDSF_plot
			.seriesDots (null)
			.seriesColor (Color.RED)
			.labelColor (Color.RED);
		plotData (modelData, "Model");

		// Add actual data to plots.
		T_plot
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.BLACK)
			.labelColor (Color.BLACK);
		Speedup_plot
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.BLACK)
			.labelColor (Color.BLACK);
		Eff_plot
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.BLACK)
			.labelColor (Color.BLACK);
		EDSF_plot
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.BLACK)
			.labelColor (Color.BLACK);
		plotData (actualData, "Actual");

		// Display plots.
		T_plot.getFrame().setVisible (true);
		Speedup_plot.getFrame().setVisible (true);
		Eff_plot.getFrame().setVisible (true);
		EDSF_plot.getFrame().setVisible (true);
		}

// Hidden operations.

	/**
	 * Parse the given basis function.
	 */
	private static BasisFunction parseBasisFunction
		(String function)
		{
		Matcher matcher;
		if (function.equals ("1"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return 1.0;
					}
				public String toString (double c)
					{
					return c + "";
					}
				};
			}
		else if (function.equals ("K"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return K;
					}
				public String toString (double c)
					{
					return c + " K";
					}
				};
			}
		else if (function.equals ("1/K"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return 1.0 / K;
					}
				public String toString (double c)
					{
					return c + " / K";
					}
				};
			}
		else if (function.equals ("n"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return n;
					}
				public String toString (double c)
					{
					return c + " n";
					}
				};
			}
		else if (function.equals ("nK"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return n * K;
					}
				public String toString (double c)
					{
					return c + " n K";
					}
				};
			}
		else if (function.equals ("n/K"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return n / K;
					}
				public String toString (double c)
					{
					return c + " n / K";
					}
				};
			}
		else if ((matcher = N_POWER_PATTERN.matcher (function))
					.matches())
			{
			final int exp = Integer.parseInt (matcher.group (1));
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.pow (n, exp);
					}
				public String toString (double c)
					{
					return c + " n^" + exp;
					}
				};
			}
		else if ((matcher = N_POWER_TIMES_K_PATTERN.matcher (function))
					.matches())
			{
			final int exp = Integer.parseInt (matcher.group (1));
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.pow (n, exp) * K;
					}
				public String toString (double c)
					{
					return c + " n^" + exp + " K";
					}
				};
			}
		else if ((matcher = N_POWER_OVER_K_PATTERN.matcher (function))
					.matches())
			{
			final int exp = Integer.parseInt (matcher.group (1));
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.pow (n, exp) / K;
					}
				public String toString (double c)
					{
					return c + " n^" + exp + " / K";
					}
				};
			}
		else if (function.equals ("lgn"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.log (n) / LOG_2;
					}
				public String toString (double c)
					{
					return c + " lg n";
					}
				};
			}
		else if (function.equals ("lgnK"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.log (n) / LOG_2 * K;
					}
				public String toString (double c)
					{
					return c + " (lg n) K";
					}
				};
			}
		else if (function.equals ("lgn/K"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.log (n) / LOG_2 / K;
					}
				public String toString (double c)
					{
					return c + " (lg n) / K";
					}
				};
			}
		else if (function.equals ("2^n"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.pow (2.0, n);
					}
				public String toString (double c)
					{
					return c + " (2^n)";
					}
				};
			}
		else if (function.equals ("2^nK"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.pow (2.0, n) * K;
					}
				public String toString (double c)
					{
					return c + " (2^n) K";
					}
				};
			}
		else if (function.equals ("2^n/K"))
			{
			return new BasisFunction()
				{
				public double f (double n, double K)
					{
					return Math.pow (2.0, n) / K;
					}
				public String toString (double c)
					{
					return c + " (2^n) / K";
					}
				};
			}
		else
			{
			System.err.println ("Illegal basis function: " + function);
			System.exit (1);
			return null;
			}
		}

	private static final double LOG_2 = Math.log (2.0);

	/**
	 * Parse the given line of input.
	 */
	private static void parseLine
		(String line,
		 int linenum)
		{
		Scanner scanner = new Scanner (line);
		if (scanner.hasNextInt())
			{
			// A running time data line. Parse contents.
			int n = scanner.nextInt();
			if (! scanner.hasNextInt()) error ("K invalid", linenum);
			int K = scanner.nextInt();
			if (K < 0) error ("K < 0", linenum);
			if (K == 0) return; // Ignore T_seq
			K_max = Math.max (K_max, K);
			double T_min = Double.POSITIVE_INFINITY;
			double T_max = Double.NEGATIVE_INFINITY;
			while (scanner.hasNextLong())
				{
				double T = (double) scanner.nextLong();
				if (T <= 0.0) error ("T invalid", linenum);
				T_min = Math.min (T_min, T);
				T_max = Math.max (T_max, T);
				}
			if (T_min == Double.POSITIVE_INFINITY)
				{
				error ("T values missing", linenum);
				}

			// Record data.
			Data data = getData (n);
			if (K == 0)
				{
				data.T_seq = T_min;
				data.T_max_seq = T_max;
				data.Dev_seq = (T_max - T_min) / T_min;
				}
			else if (K == 1)
				{
				double Speedup =
					data.T_seq == 0.0 ?
						1.0 :
						data.T_seq / T_min;
				double Eff = Speedup;
				double Dev = (T_max - T_min) / T_min;
				data.T_par_1 = T_min;
				data.K_series.add (K);
				data.T_series.add (T_min);
				data.T_max_series.add (T_max);
				data.Speedup_series.add (Speedup);
				data.Eff_series.add (Eff);
				data.Dev_series.add (Dev);
				}
			else
				{
				double Speedup =
					data.T_seq == 0.0 ?
						data.T_par_1 / T_min :
						data.T_seq / T_min;
				double Eff = Speedup / K;
				double Dev = (T_max - T_min) / T_min;
				double EDSF = (K*T_min - data.T_par_1) / data.T_par_1 / (K-1);
				data.K_series.add (K);
				data.T_series.add (T_min);
				data.T_max_series.add (T_max);
				data.Speedup_series.add (Speedup);
				data.Eff_series.add (Eff);
				data.Dev_series.add (Dev);
				data.K_series_2.add (K);
				data.EDSF_series_2.add (EDSF);
				}
			}
		else
			{
			// A problem size specification line or plot specification line.
			String keyword = scanner.next();
			if (keyword.equals ("n"))
				{
				// A problem size specification line. Parse contents.
				if (! scanner.hasNextInt())
					error ("Missing n value", linenum);
				int n = scanner.nextInt();
				if (! scanner.hasNextDouble())
					error ("Missing N value", linenum);
				double N = scanner.nextDouble();
				String labelText = scanner.findInLine (QUOTED_STRING_PATTERN);
				if (labelText == null)
					error ("Missing quoted label text", linenum);
				labelText = labelText.substring (1, labelText.length()-1);

				// Record contents.
				Data data = getData (n);
				data.N = N;
				data.labelText = labelText;
				}
			else if (keyword.equals ("time"))
				{
				parsePlotSpecification (T_plot, scanner, linenum);
				}
			else if (keyword.equals ("speedup"))
				{
				parsePlotSpecification (Speedup_plot, scanner, linenum);
				}
			else if (keyword.equals ("eff"))
				{
				parsePlotSpecification (Eff_plot, scanner, linenum);
				}
			else if (keyword.equals ("edsf"))
				{
				parsePlotSpecification (EDSF_plot, scanner, linenum);
				}
			else
				{
				error ("Unknown command", linenum);
				}
			}
		}

	/**
	 * Parse a plot specification line.
	 */
	private static void parsePlotSpecification
		(Plot plot,
		 Scanner scanner,
		 int linenum)
		{
		if (! scanner.hasNext()) error ("Missing plot attribute", linenum);
		String attribute = scanner.next();
		if (attribute.equals ("frameTitle"))
			{
			String theTitle = scanner.findInLine (QUOTED_STRING_PATTERN);
			if (theTitle == null)
				error ("Missing quoted frame title", linenum);
			theTitle = theTitle.substring (1, theTitle.length()-1);
			plot.frameTitle (theTitle + " (n = " + nplot + ")");
			}
		else if (attribute.equals ("plotTitle"))
			{
			String theTitle = scanner.findInLine (QUOTED_STRING_PATTERN);
			if (theTitle == null)
				error ("Missing quoted plot title", linenum);
			theTitle = theTitle.substring (1, theTitle.length()-1);
			plot.plotTitle (theTitle + " (n = " + nplot + ")");
			}
		else if (attribute.equals ("margins"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing margins value", linenum);
			double theMargin = scanner.nextDouble();
			plot.margins (theMargin);
			}
		else if (attribute.equals ("leftMargin"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing left margin value", linenum);
			double theMargin = scanner.nextDouble();
			plot.leftMargin (theMargin);
			}
		else if (attribute.equals ("topMargin"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing top margin value", linenum);
			double theMargin = scanner.nextDouble();
			plot.topMargin (theMargin);
			}
		else if (attribute.equals ("rightMargin"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing right margin value", linenum);
			double theMargin = scanner.nextDouble();
			plot.rightMargin (theMargin);
			}
		else if (attribute.equals ("bottomMargin"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing bottom margin value", linenum);
			double theMargin = scanner.nextDouble();
			plot.bottomMargin (theMargin);
			}
		else if (attribute.equals ("xAxisStart"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing X axis start value", linenum);
			double theStart = scanner.nextDouble();
			plot.xAxisStart (theStart);
			}
		else if (attribute.equals ("xAxisEnd"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing X axis end value", linenum);
			double theStart = scanner.nextDouble();
			plot.xAxisEnd (theStart);
			}
		else if (attribute.equals ("xAxisMajorDivisions"))
			{
			if (! scanner.hasNextInt())
				error ("Missing X axis major divisions value", linenum);
			int theMajorDivisions = scanner.nextInt();
			plot.xAxisMajorDivisions (theMajorDivisions);
			}
		else if (attribute.equals ("xAxisMinorDivisions"))
			{
			if (! scanner.hasNextInt())
				error ("Missing X axis minor divisions value", linenum);
			int theMinorDivisions = scanner.nextInt();
			plot.xAxisMinorDivisions (theMinorDivisions);
			}
		else if (attribute.equals ("xAxisLength"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing X axis length value", linenum);
			double theLength = scanner.nextDouble();
			plot.xAxisLength (theLength);
			}
		else if (attribute.equals ("xAxisTitle"))
			{
			String theTitle = scanner.findInLine (QUOTED_STRING_PATTERN);
			if (theTitle == null)
				error ("Missing quoted X axis title", linenum);
			theTitle = theTitle.substring (1, theTitle.length()-1);
			plot.xAxisTitle (theTitle);
			}
		else if (attribute.equals ("yAxisStart"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing Y axis start value", linenum);
			double theStart = scanner.nextDouble();
			plot.yAxisStart (theStart);
			}
		else if (attribute.equals ("yAxisEnd"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing Y axis end value", linenum);
			double theStart = scanner.nextDouble();
			plot.yAxisEnd (theStart);
			}
		else if (attribute.equals ("yAxisMajorDivisions"))
			{
			if (! scanner.hasNextInt())
				error ("Missing Y axis major divisions value", linenum);
			int theMajorDivisions = scanner.nextInt();
			plot.yAxisMajorDivisions (theMajorDivisions);
			}
		else if (attribute.equals ("yAxisMinorDivisions"))
			{
			if (! scanner.hasNextInt())
				error ("Missing Y axis minor divisions value", linenum);
			int theMinorDivisions = scanner.nextInt();
			plot.yAxisMinorDivisions (theMinorDivisions);
			}
		else if (attribute.equals ("yAxisTickFormat"))
			{
			String theFormat = scanner.findInLine (QUOTED_STRING_PATTERN);
			if (theFormat == null)
				error ("Missing quoted Y axis tick format", linenum);
			theFormat = theFormat.substring (1, theFormat.length()-1);
			plot.yAxisTickFormat (new DecimalFormat (theFormat));
			}
		else if (attribute.equals ("yAxisLength"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing Y axis length value", linenum);
			double theLength = scanner.nextDouble();
			plot.yAxisLength (theLength);
			}
		else if (attribute.equals ("yAxisTitle"))
			{
			String theTitle = scanner.findInLine (QUOTED_STRING_PATTERN);
			if (theTitle == null)
				error ("Missing quoted Y axis title", linenum);
			theTitle = theTitle.substring (1, theTitle.length()-1);
			plot.yAxisTitle (theTitle);
			}
		else if (attribute.equals ("yAxisTitleOffset"))
			{
			if (! scanner.hasNextDouble())
				error ("Missing Y axis title offset value", linenum);
			double theTitleOffset = scanner.nextDouble();
			plot.yAxisTitleOffset (theTitleOffset);
			}
		else
			{
			error ("Unknown plot attribute", linenum);
			}
		}

	/**
	 * Get the data record for the given n value. Create it if necessary.
	 */
	private static Data getData
		(int n)
		{
		Data data = dataMap.get (n);
		if (data == null)
			{
			data = new Data (n);
			dataMap.put (n, data);
			}
		return data;
		}

	/**
	 * Validate the given data record.
	 */
	private static void validateData
		(Data data)
		{
		// Skip if the data series are empty.
		if (data.K_series.isEmpty()) return;

		// Make sure data for K=1 was specified.
		if (data.T_par_1 == 0.0)
			{
			System.err.println ("Error: n = " + data.n + ": No data for K = 1");
			System.exit (1);
			}

		// Make sure K values appear in ascending order.
		double K_prev = Double.NEGATIVE_INFINITY;
		for (double K : data.K_series)
			{
			if (K <= K_prev)
				{
				System.err.println
					("Error: n = " + data.n +
					 ": K values not in ascending order");
				System.exit (1);
				}
			K_prev = K;
			}
		}

	/**
	 * Compute the model function.
	 */
	private static double modelFunction
		(double n,
		 double K,
		 double[] c)
		{
		double result = 0.0;
		for (int i = 0; i < c.length; ++ i)
			{
			result += c[i] * basis.get(i).f (n, K);
			}
		return result;
		}

	/**
	 * Print a table of metrics for the given data.
	 */
	private static void printData
		(Data data)
		{
		System.out.println ("n\tK\tT\tSpdup\tEffic\tEDSF");
		System.out.println
			(data.n + "\t" +
			 FMT_0.format (data.K_series.x(0)) + "\t" +
			 FMT_0.format (data.T_series.x(0)) + "\t" +
			 FMT_3.format (data.Speedup_series.x(0)) + "\t" +
			 FMT_3.format (data.Eff_series.x(0)));
		for (int i = 1; i < data.K_series.length(); ++ i)
			{
			System.out.println
				(data.n + "\t" +
				 FMT_0.format (data.K_series.x(i)) + "\t" +
				 FMT_0.format (data.T_series.x(i)) + "\t" +
				 FMT_3.format (data.Speedup_series.x(i)) + "\t" +
				 FMT_3.format (data.Eff_series.x(i)) + "\t" +
				 FMT_3.format (data.EDSF_series_2.x(i-1)));
			}
		}

	/**
	 * Plot the given data.
	 */
	private static void plotData
		(Data data,
		 String label)
		{
		int len = data.K_series.length();
		T_plot
			.xySeries
				(new AggregateXYSeries
					(data.K_series, data.T_series))
			.label
				(label,
				 data.K_series.x (len-1),
				 data.T_series.x (len-1));
		Speedup_plot
			.xySeries
				(new AggregateXYSeries
					(data.K_series, data.Speedup_series))
			.label
				(label,
				 data.K_series.x (len-1),
				 data.Speedup_series.x (len-1));
		Eff_plot
			.xySeries
				(new AggregateXYSeries
					(data.K_series, data.Eff_series))
			.label
				(label,
				 data.K_series.x (len-1),
				 data.Eff_series.x (len-1));
		EDSF_plot
			.xySeries
				(new AggregateXYSeries
					(data.K_series_2, data.EDSF_series_2))
			.label
				(label,
				 data.K_series_2.x (len-2),
				 data.EDSF_series_2.x (len-2));
		}

	/**
	 * Print an error message and exit.
	 */
	private static void error
		(String msg,
		 int linenum)
		{
		System.err.println ("Error: line " + linenum + ": " + msg);
		System.exit (1);
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java TimeFit <inputfile> <nplot> <f1> [<f2> ...]");
		System.exit (1);
		}

	}
