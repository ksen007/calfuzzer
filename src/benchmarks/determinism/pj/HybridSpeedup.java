//******************************************************************************
//
// File:    HybridSpeedup.java
// Package: ---
// Unit:    Class HybridSpeedup
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

import benchmarks.determinism.pj.edu.ritnumeric.ListXYSeries;

import benchmarks.determinism.pj.edu.ritnumeric.plot.Dots;
import benchmarks.determinism.pj.edu.ritnumeric.plot.Plot;

import java.awt.Color;

import java.io.File;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import java.util.regex.Pattern;

/**
 * Class HybridSpeedup is a main program that analyzes running time measurements
 * and reports speedup metrics for a hybrid parallel program.
 * <P>
 * Usage: java HybridSpeedup <I>inputfile</I>
 * <P>
 * The input file, a plain text file, is formatted as follows. Blank lines are
 * ignored. A pound sign (<TT>#</TT>) and everything after it on a line is
 * ignored.
 * <P>
 * The input file begins with running time data. Each line contains an <I>n</I>
 * value, a <I>Kp</I> value, a <I>Kt</I> value, and one or more <I>T</I> values.
 * The <I>n</I> value, an integer, is the size parameter; it is either the
 * problem size itself or a quantity from which the problem size can be derived.
 * The <I>Kp</I> value, an integer &gt;= 0, is the number of parallel processes;
 * <I>Kp</I> = 0 signifies the running times are for a sequential version of the
 * program, <I>Kp</I> &gt; 0 signifies the running times are for a parallel
 * version of the program. The <I>Kt</I> value, an integer &gt;= 0, is the
 * number of parallel threads; <I>Kt</I> = 0 signifies the running times are for
 * a sequential version of the program, <I>Kt</I> &gt; 0 signifies the running
 * times are for a parallel version of the program. Each <I>T</I> value, a long
 * integer, is a running time measurement in milliseconds.
 * <P>
 * After the running time data come the problem size specifications. Each line
 * begins with the literal character <TT>n</TT> followed by an <I>n</I> value,
 * an <I>N</I> value, and a label text enclosed in quotation marks. The <I>n</I>
 * value, an integer, is one of the size parameter values appearing in the
 * running time data section. The <I>N</I> value, a floating point number, is
 * the actual problem size corresponding to that size parameter. The label text
 * is placed next to the curve for that size parameter on the plots. If no
 * problem size specification line appears for a certain size parameter in the
 * running time data, then the problem size is taken to be the same as the size
 * parameter, and the label text is taken to be <TT>"N = <I>n</I>"</TT>.
 * <P>
 * After the problem size specifications (if any) come the plot specifications.
 * Each of the following plot specifications is optional. The plot specification
 * keywords are the same as the methods of class {@linkplain
 * benchmarks.determinism.pj.edu.ritnumeric.plot.Plot Plot}; see that class for further information.
 * <P>
 * The following plot specifications control the running time plot:
 * <BR><TT>time frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>time plotTitle "<I>theTitle</I>"</TT> (default: <TT>"Running Time vs.
 * Processors"</TT>)
 * <BR><TT>time margins <I>theMargin</I></TT>
 * <BR><TT>time leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>time topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>time rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>time bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>time xAxisStart <I>theStart</I></TT> (default: automatic*)
 * <BR><TT>time xAxisEnd <I>theEnd</I></TT> (default: automatic*)
 * <BR><TT>time xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>time xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processes, Kp"</TT>)
 * <BR><TT>time yAxisStart <I>theStart</I></TT> (default: automatic*)
 * <BR><TT>time yAxisEnd <I>theEnd</I></TT> (default: automatic*)
 * <BR><TT>time yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0E0"</TT>)
 * <BR><TT>time yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>time yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Running Time, T (sec)"</TT>)
 * <BR><TT>time yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <BR>*For the running time plot, which is a log-log plot, the specified X and
 * Y axis starting and ending values are the base-10 logarithms of the actual
 * values.
 * <P>
 * The following plot specifications control the speedup plot:
 * <BR><TT>time frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>speedup plotTitle "<I>theTitle</I>"</TT> (default: <TT>"Speedup vs.
 * Processors"</TT>)
 * <BR><TT>speedup margins <I>theMargin</I></TT>
 * <BR><TT>speedup leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>speedup topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>speedup rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>speedup bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>speedup xAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>speedup xAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>speedup xAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>speedup xAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>speedup xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>speedup xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processes, Kp"</TT>)
 * <BR><TT>speedup yAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>speedup yAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>speedup yAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>speedup yAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>speedup yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0"</TT>)
 * <BR><TT>speedup yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>speedup yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Speedup"</TT>)
 * <BR><TT>speedup yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <P>
 * The following plot specifications control the efficiency plot:
 * <BR><TT>time frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>eff plotTitle "<I>theTitle</I>"</TT> (default: <TT>"Efficiency vs.
 * Processors"</TT>)
 * <BR><TT>eff margins <I>theMargin</I></TT>
 * <BR><TT>eff leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>eff topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>eff rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>eff bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>eff xAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>eff xAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>eff xAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>eff xAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>eff xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>eff xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processes, Kp"</TT>)
 * <BR><TT>eff yAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>eff yAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>eff yAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>eff yAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>eff yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0.0"</TT>)
 * <BR><TT>eff yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>eff yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Efficiency"</TT>)
 * <BR><TT>eff yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <P>
 * The following plot specifications control the experimentally determined
 * sequential fraction (EDSF) plot:
 * <BR><TT>time frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>edsf plotTitle "<I>theTitle</I>"</TT> (default: <TT>"EDSF vs.
 * Processors"</TT>)
 * <BR><TT>edsf margins <I>theMargin</I></TT>
 * <BR><TT>edsf leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>edsf topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>edsf rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>edsf bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>edsf xAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>edsf xAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>edsf xAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>edsf xAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>edsf xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>edsf xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processes, Kp"</TT>)
 * <BR><TT>edsf yAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>edsf yAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>edsf yAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>edsf yAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>edsf yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0"</TT>)
 * <BR><TT>edsf yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>edsf yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Sequential Fraction, F"</TT>)
 * <BR><TT>edsf yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <P>
 * For each line in the running time data section, the program takes the
 * smallest <I>T</I> value as the running time for the given <I>n</I> and
 * <I>K</I> values. For each value of <I>n</I>, the program calculates the
 * following data series: running time versus <I>Kp</I> and <I>Kt</I> for
 * <I>Kp</I> and <I>Kt</I> &gt;= 1; speedup versus <I>Kp</I> and <I>Kt</I> for
 * <I>Kp</I> and <I>Kt</I> &gt;= 1 (speedup relative to the sequential version
 * if input data for <I>Kp</I> = 0 is present, otherwise speedup relative to the
 * parallel version for <I>Kp</I> = <I>Kt</I> = 1); efficiency versus <I>Kp</I>
 * and <I>Kt</I> for <I>Kp</I> and <I>Kt</I> &gt;= 1 (efficiency =
 * speedup/(<I>Kp</I>*<I>Kt</I>)); EDSF versus <I>Kp</I> and <I>Kt</I> for
 * <I>Kp</I> or <I>Kt</I> &gt;= 2 (EDSF relative to the parallel version for
 * <I>Kp</I> = <I>Kt</I> = 1); and running time deviation versus <I>Kp</I> and
 * <I>Kt</I> (deviation = (maximum <I>T</I> - minimum <I>T</I>) / (minimum
 * <I>T</I>)).
 * <P>
 * The program prints the running time, speedup, efficiency, EDSF, and running
 * time deviation series on the standard output.
 * <P>
 * The program displays plots of the running time, speedup, efficiency, and EDSF
 * series, each in its own window. Each plot window has menu options for saving
 * the plot to a PNG image file, saving the plot to a PostScript file, and
 * zooming the display.
 * <P>
 * Here is an example of an input file:
 * <A HREF="doc-files/hybtimes.txt">hybtimes.txt</A>
 * <FONT SIZE="-1">
 * <PRE>
 * 27 0 0 223352 226590 225559 225460 224333 227854 225836
 * 27 1 1 229705 221275 229412 220199 220695 229741 229090
 * 27 1 2 111249 111490 111091 113569 111738 113729 110768
 * 27 1 3 76009 76823 80661 76172 75743 76768 74518
 * 27 1 4 61777 62049 58180 58522 58422 58435 61869
 * 27 2 1 114810 110901 112733 110364 112385 111219 115009
 * 27 2 2 56217 56528 55906 57538 57095 57004 57919
 * 27 2 3 40389 40603 38273 37536 40468 40730 38890
 * 27 2 4 30410 30353 30810 30696 28219 30495 29067
 * 27 3 1 74752 74293 73706 74143 75357 74704 74282
 * 27 3 2 37764 40142 38299 38578 38400 38010 38126
 * 27 3 3 27025 26351 24973 27455 26037 26481 26914
 * 27 3 4 20120 19845 20637 19761 21096 19932 20572
 * 27 4 1 55892 55696 55573 57322 57674 57156 55289
 * 27 4 2 29755 30762 28718 28828 28255 28898 28975
 * 27 4 3 19912 19793 20400 21538 19782 20286 20173
 * 27 4 4 15080 15493 15487 14653 15245 15438 15091
 * 27 5 1 45384 46273 45739 45427 45193 46216 45633
 * 27 5 2 23366 23312 24347 23460 23165 22652 22974
 * 27 5 3 15820 15957 16038 16048 16045 16322 15742
 * 27 5 4 11929 12154 12341 12358 12174 12426 12612
 * 27 6 1 37838 37322 38540 38585 37277 38182 37462
 * 27 6 2 19250 19164 19847 20005 19136 19299 20340
 * 27 6 3 13697 13654 13651 13716 13204 13646 13772
 * 27 6 4 10027 10214 10462 10404 10351 10134 10229
 * 27 7 1 32938 33099 33075 32421 33072 33079 33145
 * 27 7 2 17071 16713 16286 17110 16725 16483 16473
 * 27 7 3 11443 11459 11472 11524 11361 11829 11494
 * 27 7 4 8930 8611 8955 8912 8955 8871 8885
 * 27 8 1 28984 28967 28838 28620 28878 28861 28849
 * 27 8 2 14402 14361 15064 15406 14123 14218 14578
 * 27 8 3 10214 10300 10232 10216 10308 10801 10163
 * 27 8 4 7807 7895 7751 7866 7890 7871 7788
 * 27 9 1 25765 25637 25711 25757 25694 25589 25704
 * 27 9 2 12586 12792 12794 13269 12963 13066 13206
 * 27 9 3 9169 9131 9255 9024 10131 9199 9098
 * 27 9 4 6977 7009 6995 6948 6987 7263 7035
 * 27 10 1 23150 23185 23082 23200 23212 23036 23260
 * 27 10 2 11588 11947 11570 11306 11694 11402 11747
 * 27 10 3 8302 8237 8309 8247 8206 8615 8260
 * 27 10 4 6260 6352 6241 5992 6316 6247 6226
 * n 27 134217728 "<I>N</I> = 128M"
 * time yAxisStart 3
 * time yAxisEnd 7
 * speedup yAxisEnd 40
 * speedup yAxisMajorDivisions 8
 * eff yAxisEnd 1.1
 * eff yAxisMajorDivisions 11
 * </PRE>
 * </FONT>
 * <P>
 * Here is the output the HybridSpeedup program printed for the above input
 * file: <A HREF="doc-files/hybspeedup.txt">hybspeedup.txt</A>
 * <FONT SIZE="-1">
 * <PRE>
 * $ java HybridSpeedup hybtimes.txt
 * N	Kp	Kt	T	Spdup	Effic	EDSF	Devi
 *
 * 128M	seq	seq	223352				2%
 * 128M	1	1	220199	1.014	1.014		4%
 * 128M	1	2	110768	2.016	1.008	0.006	3%
 * 128M	1	3	74518	2.997	0.999	0.008	8%
 * 128M	1	4	58180	3.839	0.960	0.019	7%
 * 128M	2	1	110364	2.024	1.012	0.002	4%
 * 128M	2	2	55906	3.995	0.999	0.005	4%
 * 128M	2	3	37536	5.950	0.992	0.005	9%
 * 128M	2	4	28219	7.915	0.989	0.004	9%
 * 128M	3	1	73706	3.030	1.010	0.002	2%
 * 128M	3	2	37764	5.914	0.986	0.006	6%
 * 128M	3	3	24973	8.944	0.994	0.003	10%
 * 128M	3	4	19761	11.303	0.942	0.007	7%
 * 128M	4	1	55289	4.040	1.010	0.001	4%
 * 128M	4	2	28255	7.905	0.988	0.004	9%
 * 128M	4	3	19782	11.291	0.941	0.007	9%
 * 128M	4	4	14653	15.243	0.953	0.004	6%
 * 128M	5	1	45193	4.942	0.988	0.007	2%
 * 128M	5	2	22652	9.860	0.986	0.003	7%
 * 128M	5	3	15742	14.188	0.946	0.005	4%
 * 128M	5	4	11929	18.723	0.936	0.004	6%
 * 128M	6	1	37277	5.992	0.999	0.003	4%
 * 128M	6	2	19136	11.672	0.973	0.004	6%
 * 128M	6	3	13204	16.915	0.940	0.005	4%
 * 128M	6	4	10027	22.275	0.928	0.004	4%
 * 128M	7	1	32421	6.889	0.984	0.005	2%
 * 128M	7	2	16286	13.714	0.980	0.003	5%
 * 128M	7	3	11361	19.660	0.936	0.004	4%
 * 128M	7	4	8611	25.938	0.926	0.004	4%
 * 128M	8	1	28620	7.804	0.976	0.006	1%
 * 128M	8	2	14123	15.815	0.988	0.002	9%
 * 128M	8	3	10163	21.977	0.916	0.005	6%
 * 128M	8	4	7751	28.816	0.900	0.004	2%
 * 128M	9	1	25589	8.728	0.970	0.006	1%
 * 128M	9	2	12586	17.746	0.986	0.002	5%
 * 128M	9	3	9024	24.751	0.917	0.004	12%
 * 128M	9	4	6948	32.146	0.893	0.004	5%
 * 128M	10	1	23036	9.696	0.970	0.005	1%
 * 128M	10	2	11306	19.755	0.988	0.001	6%
 * 128M	10	3	8206	27.218	0.907	0.004	5%
 * 128M	10	4	5992	37.275	0.932	0.002	6%
 * </PRE>
 * </FONT>
 * <P>
 * Here are the plots the HybridSpeedup program generated for the above input
 * file:
 * <P><IMG SRC="doc-files/plot_m.png"> <IMG SRC="doc-files/plot_n.png">
 * <BR><IMG SRC="doc-files/plot_o.png"> <IMG SRC="doc-files/plot_p.png">
 * <P>
 * The HybridSpeedup program's error handling is rudimentary. The first error in
 * the input file terminates the program. The error may cause an exception stack
 * trace to be printed.
 *
 * @author  Alan Kaminsky
 * @version 06-Aug-2008
 */
public class HybridSpeedup
	{

// Prevent construction.

	private HybridSpeedup()
		{
		}

// Hidden helper classes.

	/**
	 * Class DataEntry is a record of data associated with a certain number of
	 * processes Kp and a certain number of threads Kt.
	 *
	 * @author  Alan Kaminsky
	 * @version 22-May-2008
	 */
	private static class DataEntry
		implements Comparable<DataEntry>
		{
		// Number of processes.
		public int Kp;

		// Number of threads.
		public int Kt;

		// Smallest running time.
		public double T;

		// Largest running time.
		public double T_max;

		// Construct a new data entry.
		public DataEntry
			(int Kp,
			 int Kt,
			 double T,
			 double T_max)
			{
			this.Kp = Kp;
			this.Kt = Kt;
			this.T = T;
			this.T_max = T_max;
			}

		// Equality test.
		public boolean equals
			(Object obj)
			{
			return
				(obj instanceof DataEntry) &&
				(((DataEntry) obj).Kp == this.Kp) &&
				(((DataEntry) obj).Kt == this.Kt);
			}

		// Hash code.
		public int hashCode()
			{
			return (Kp << 16) | Kt;
			}

		// Comparison test.
		public int compareTo
			(DataEntry that)
			{
			return this.Kp != that.Kp ? this.Kp - that.Kp : this.Kt - that.Kt;
			}
		}

	/**
	 * Class Data is a record of data associated with a certain size parameter
	 * n.
	 *
	 * @author  Alan Kaminsky
	 * @version 22-May-2008
	 */
	private static class Data
		{
		// Size parameter n.
		public int n;

		// Problem size N.
		public double N;

		// Label text.
		public String labelText;

		// List of data entries.
		public ArrayList<DataEntry> entries = new ArrayList<DataEntry>();

		// Constructor.
		public Data
			(int n)
			{
			this.n = n;
			this.N = n;
			this.labelText = "N = " + n;
			}

		// Returns true if there is an entry for Kp = Kt = 1, false otherwise.
		public boolean validate()
			{
			return entries.contains (new DataEntry (1, 1, 0, 0));
			}

		// Returns a data series for running time versus Kp for the given Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries TVsKp
			(int Kt)
			{
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kt == Kt)
					{
					series.add (entry.Kp, entry.T);
					}
				}
			return series;
			}

		// Returns a data series for running time for the given Kp over all Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries TOverKt
			(int Kp)
			{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kp == Kp)
					{
					min = Math.min (min, entry.T);
					max = Math.max (max, entry.T);
					}
				}
			series.add (Kp, min);
			series.add (Kp, max);
			return series;
			}

		// Returns a data series for speedup versus Kp for the given Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries SpeedupVsKp
			(int Kt)
			{
			double T_seq = 0.0;
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kp == 0)
					{
					T_seq = entry.T;
					}
				else if (T_seq == 0.0 && entry.Kp == 1 && entry.Kt == 1)
					{
					T_seq = entry.T;
					}
				if (entry.Kt == Kt)
					{
					double speedup = T_seq / entry.T;
					series.add (entry.Kp, speedup);
					}
				}
			return series;
			}

		// Returns a data series for speedup for the given Kp over all Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries SpeedupOverKt
			(int Kp)
			{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			double T_seq = 0.0;
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kp == 0)
					{
					T_seq = entry.T;
					}
				else if (T_seq == 0.0 && entry.Kp == 1 && entry.Kt == 1)
					{
					T_seq = entry.T;
					}
				if (entry.Kp == Kp)
					{
					double speedup = T_seq / entry.T;
					min = Math.min (min, speedup);
					max = Math.max (max, speedup);
					}
				}
			series.add (Kp, min);
			series.add (Kp, max);
			return series;
			}

		// Returns a data series for efficiency versus Kp for the given Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries EffVsKp
			(int Kt)
			{
			double T_seq = 0.0;
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kp == 0)
					{
					T_seq = entry.T;
					}
				else if (T_seq == 0.0 && entry.Kp == 1 && entry.Kt == 1)
					{
					T_seq = entry.T;
					}
				if (entry.Kt == Kt)
					{
					double speedup = T_seq / entry.T;
					double eff = speedup / entry.Kp / entry.Kt;
					series.add (entry.Kp, eff);
					}
				}
			return series;
			}

		// Returns a data series for efficiency for the given Kp over all Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries EffOverKt
			(int Kp)
			{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			double T_seq = 0.0;
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kp == 0)
					{
					T_seq = entry.T;
					}
				else if (T_seq == 0.0 && entry.Kp == 1 && entry.Kt == 1)
					{
					T_seq = entry.T;
					}
				if (entry.Kp == Kp)
					{
					double speedup = T_seq / entry.T;
					double eff = speedup / entry.Kp / entry.Kt;
					min = Math.min (min, eff);
					max = Math.max (max, eff);
					}
				}
			series.add (Kp, min);
			series.add (Kp, max);
			return series;
			}

		// Returns a data series for EDSF versus Kp for the given Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries EdsfVsKp
			(int Kt)
			{
			double T_par_1 = 0.0;
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kp == 1 && entry.Kt == 1)
					{
					T_par_1 = entry.T;
					}
				int K = entry.Kp * entry.Kt;
				if (K > 1 && entry.Kt == Kt)
					{
					double edsf = (entry.T * K - T_par_1) / T_par_1 / (K-1);
					series.add (entry.Kp, edsf);
					}
				}
			return series;
			}

		// Returns a data series for EDSF for the given Kp over all Kt.
		// Assumes the entry list is sorted.
		public ListXYSeries EdsfOverKt
			(int Kp)
			{
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			double T_par_1 = 0.0;
			ListXYSeries series = new ListXYSeries();
			for (DataEntry entry : entries)
				{
				if (entry.Kp == 1 && entry.Kt == 1)
					{
					T_par_1 = entry.T;
					}
				int K = entry.Kp * entry.Kt;
				if (K > 1 && entry.Kp == Kp)
					{
					double edsf = (entry.T * K - T_par_1) / T_par_1 / (K-1);
					min = Math.min (min, edsf);
					max = Math.max (max, edsf);
					}
				}
			series.add (Kp, min);
			series.add (Kp, max);
			return series;
			}
		}

// Global variables.

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
	private static TreeMap<Integer,Data> dataMap = new TreeMap<Integer,Data>();

	// Sets of Kp and Kt values.
	private static TreeSet<Integer> Kp_set = new TreeSet<Integer>();
	private static TreeSet<Integer> Kt_set = new TreeSet<Integer>();

	// Maximum Kp and Kt values.
	private static double Kp_max = Double.NEGATIVE_INFINITY;
	private static double Kt_max = Double.NEGATIVE_INFINITY;

	// Plot objects.
	private static Plot T_plot = new Plot();
	private static Plot Speedup_plot = new Plot();
	private static Plot Eff_plot = new Plot();
	private static Plot EDSF_plot = new Plot();

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
		File inputfile = new File (args[0]);

		// Set up plots with default attributes.
		T_plot
			.plotTitle ("Running Time vs. Processors")
			.rightMargin (54)
			.minorGridLines (true)
			.xAxisKind (Plot.LOGARITHMIC)
			.xAxisMinorDivisions (10)
			.xAxisTitle ("Processes, <I>Kp</I>")
			.yAxisKind (Plot.LOGARITHMIC)
			.yAxisMinorDivisions (10)
			.yAxisTickFormat (FMT_0E)
			.yAxisTickScale (1000)
			.yAxisTitle ("<I>T</I> (<I>N,K</I>) (sec)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		Speedup_plot
			.plotTitle ("Speedup vs. Processors")
			.rightMargin (54)
			.xAxisStart (0)
			.xAxisTitle ("Processes, <I>Kp</I>")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_0)
			.yAxisTitle ("<I>Speedup</I> (<I>N,K</I>)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		Eff_plot
			.plotTitle ("Efficiency vs. Processors")
			.rightMargin (54)
			.xAxisStart (0)
			.xAxisTitle ("Processes, <I>Kp</I>")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_1)
			.yAxisTitle ("<I>Eff</I> (<I>N,K</I>)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		EDSF_plot
			.plotTitle ("EDSF vs. Processors")
			.rightMargin (54)
			.xAxisStart (0)
			.xAxisTitle ("Processes, <I>Kp</I>")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_0)
			.yAxisTickScale (0.001)
			.yAxisTitle ("<I>EDSF</I> (<I>N,K</I>) (/1000)")
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
		for (Data data : dataMap.values())
			{
			if (! data.validate())
				{
				error ("No data for n="+data.n+", Kp=1, Kt=1", linenum);
				}
			}

		// Add ideal performance lines to plots.
		for (int Kt : Kt_set)
			{
			Speedup_plot
				.seriesDots (null)
				.seriesColor (new Color (0.7f, 0.7f, 0.7f))
				.xySeries (0, 0, Kp_max, Kp_max*Kt);
			}
		Eff_plot
			.seriesDots (null)
			.seriesColor (new Color (0.7f, 0.7f, 0.7f))
			.xySeries (0, 1, Kp_max, 1);

		// Process data.
		System.out.println ("N\tKp\tKt\tT\tSpdup\tEffic\tEDSF\tDevi");
		for (Data data : dataMap.values())
			{
			// Print metrics.
			System.out.println();
			String labelText = data.labelText;
			if (labelText.startsWith ("N = "))
				{
				labelText = labelText.substring (4);
				}
			else if (labelText.startsWith ("N="))
				{
				labelText = labelText.substring (2);
				}
			else if (labelText.startsWith ("<I>N</I> = "))
				{
				labelText = labelText.substring (11);
				}
			else if (labelText.startsWith ("<I>N</I>="))
				{
				labelText = labelText.substring (9);
				}
			double T_seq = 0.0;
			double T_par_1 = 0.0;
			Collections.sort (data.entries);
			for (DataEntry entry : data.entries)
				{
				double dev = 100.0 * (entry.T_max - entry.T) / entry.T;
				if (entry.Kp == 0)
					{
					T_seq = entry.T;
					System.out.println
						(labelText + "\tseq\tseq\t" +
						 FMT_0.format (entry.T) + "\t\t\t\t" +
						 FMT_0.format (dev) + "%");
					}
				else if (entry.Kp == 1 && entry.Kt == 1)
					{
					if (T_seq == 0.0) T_seq = entry.T;
					T_par_1 = entry.T;
					double speedup = T_seq / entry.T;
					double eff = speedup;
					System.out.println
						(labelText + "\t1\t1\t" +
						 FMT_0.format (entry.T) + "\t" +
						 FMT_3.format (speedup) + "\t" +
						 FMT_3.format (eff) + "\t\t" +
						 FMT_0.format (dev) + "%");
					}
				else
					{
					int K = entry.Kp * entry.Kt;
					double speedup = T_seq / entry.T;
					double eff = speedup / K;
					double edsf = (entry.T * K - T_par_1) / T_par_1 / (K-1);
					System.out.println
						(labelText + "\t" +
						 entry.Kp + "\t" +
						 entry.Kt + "\t" +
						 FMT_0.format (entry.T) + "\t" +
						 FMT_3.format (speedup) + "\t" +
						 FMT_3.format (eff) + "\t" +
						 FMT_3.format (edsf) + "\t" +
						 FMT_0.format (dev) + "%");
					}
				}

			// Add data series to plots.
			for (int Kt : Kt_set)
				{
				ListXYSeries series;
				int len;
				series = data.TVsKp (Kt);
				len = series.length();
				if (len > 0)
					{
					T_plot
						.seriesDots (Dots.circle (5))
						.seriesColor (Color.black)
						.xySeries (series)
						.label
							("<I>Kt</I> = "+Kt,
							 series.x(len-1), series.y(len-1));
					}
				series = data.SpeedupVsKp (Kt);
				len = series.length();
				if (len > 0)
					{
					Speedup_plot
						.seriesDots (Dots.circle (5))
						.seriesColor (Color.black)
						.xySeries (series)
						.label
							("<I>Kt</I> = "+Kt,
							 series.x(len-1), series.y(len-1));
					}
				series = data.EffVsKp (Kt);
				len = series.length();
				if (len > 0)
					{
					Eff_plot
						.seriesDots (Dots.circle (5))
						.seriesColor (Color.black)
						.xySeries (series)
						.label
							("<I>Kt</I> = "+Kt,
							 series.x(len-1), series.y(len-1));
					}
				series = data.EdsfVsKp (Kt);
				len = series.length();
				if (len > 0)
					{
					EDSF_plot
						.seriesDots (Dots.circle (5))
						.seriesColor (Color.black)
						.xySeries (series)
						.label
							("<I>Kt</I> = "+Kt,
							 series.x(len-1), series.y(len-1));
					}
				}
			for (int Kp : Kp_set)
				{
				ListXYSeries series;
				int len;
				series = data.TOverKt (Kp);
				len = series.length();
				if (len > 0)
					{
					T_plot
						.seriesDots (null)
						.seriesColor (Color.black)
						.xySeries (series);
					}
				series = data.SpeedupOverKt (Kp);
				len = series.length();
				if (len > 0)
					{
					Speedup_plot
						.seriesDots (null)
						.seriesColor (Color.black)
						.xySeries (series);
					}
				series = data.EffOverKt (Kp);
				len = series.length();
				if (len > 0)
					{
					Eff_plot
						.seriesDots (null)
						.seriesColor (Color.black)
						.xySeries (series);
					}
				series = data.EdsfOverKt (Kp);
				len = series.length();
				if (len > 0)
					{
					EDSF_plot
						.seriesDots (null)
						.seriesColor (Color.black)
						.xySeries (series);
					}
				}
			}

		// Display plots.
		T_plot.getFrame().setVisible (true);
		Speedup_plot.getFrame().setVisible (true);
		Eff_plot.getFrame().setVisible (true);
		EDSF_plot.getFrame().setVisible (true);
		}

// Hidden operations.

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
			if (! scanner.hasNextInt()) error ("Kp invalid", linenum);
			int Kp = scanner.nextInt();
			if (Kp < 0) error ("Kp < 0", linenum);
			if (Kp > 0) Kp_set.add (Kp);
			Kp_max = Math.max (Kp_max, Kp);
			if (! scanner.hasNextInt()) error ("Kt invalid", linenum);
			int Kt = scanner.nextInt();
			if (Kt < 0) error ("Kt < 0", linenum);
			if (Kt > 0) Kt_set.add (Kt);
			Kt_max = Math.max (Kt_max, Kt);
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
			data.entries.add (new DataEntry (Kp, Kt, T_min, T_max));
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
			plot.frameTitle (theTitle);
			}
		else if (attribute.equals ("plotTitle"))
			{
			String theTitle = scanner.findInLine (QUOTED_STRING_PATTERN);
			if (theTitle == null)
				error ("Missing quoted plot title", linenum);
			theTitle = theTitle.substring (1, theTitle.length()-1);
			plot.plotTitle (theTitle);
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
		System.err.println ("Usage: java Speedup <inputfile>");
		System.exit (1);
		}

	}
