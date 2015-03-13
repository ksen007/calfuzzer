//******************************************************************************
//
// File:    Speedup.java
// Package: ---
// Unit:    Class Speedup
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

import benchmarks.determinism.pj.edu.ritnumeric.AggregateXYSeries;
import benchmarks.determinism.pj.edu.ritnumeric.ListSeries;

import benchmarks.determinism.pj.edu.ritnumeric.plot.Dots;
import benchmarks.determinism.pj.edu.ritnumeric.plot.Plot;

import java.awt.Color;

import java.io.File;

import java.text.DecimalFormat;

import java.util.TreeMap;
import java.util.Map;
import java.util.Scanner;

import java.util.regex.Pattern;

/**
 * Class Speedup is a main program that analyzes running time measurements and
 * reports speedup metrics for a parallel program.
 * <P>
 * Usage: java Speedup <I>inputfile</I>
 * <P>
 * The input file, a plain text file, is formatted as follows. Blank lines are
 * ignored. A pound sign (<TT>#</TT>) and everything after it on a line is
 * ignored.
 * <P>
 * The input file begins with running time data. Each line contains an <I>n</I>
 * value, a <I>K</I> value, and one or more <I>T</I> values. The <I>n</I> value,
 * an integer, is the size parameter; it is either the problem size itself or a
 * quantity from which the problem size can be derived. The <I>K</I> value, an
 * integer &gt;= 0, is the number of parallel processors; <I>K</I> = 0 signifies
 * the running times are for a sequential version of the program, <I>K</I> &gt;
 * 0 signifies the running times are for a parallel version of the program. Each
 * <I>T</I> value, a long integer, is a running time measurement in
 * milliseconds. For each <I>n</I> value, the running time data lines must
 * appear in ascending order of the <I>K</I> values. For each <I>n</I> value,
 * the value <I>K</I> = 1 is mandatory; other <I>K</I> values are optional.
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
 * <BR><TT>time xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processors, K"</TT>)
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
 * <BR><TT>speedup xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processors, K"</TT>)
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
 * <BR><TT>eff xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processors, K"</TT>)
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
 * <BR><TT>edsf xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processors, K"</TT>)
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
 * following data series: running time versus <I>K</I> for <I>K</I> &gt;= 1;
 * speedup versus <I>K</I> for <I>K</I> &gt;= 1 (speedup relative to the
 * sequential version if input data for <I>K</I> = 0 is present, otherwise
 * speedup relative to the parallel version for <I>K</I> = 1); efficiency versus
 * <I>K</I> for <I>K</I> &gt;= 1 (efficiency = speedup/<I>K</I>); EDSF versus
 * <I>K</I> for <I>K</I> &gt;= 2 (EDSF relative to the parallel version for
 * <I>K</I> = 1); and running time deviation versus <I>K</I> (deviation =
 * (maximum <I>T</I> - minimum <I>T</I>) / (minimum <I>T</I>)).
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
 * Here is the output the Speedup program printed for the above input file:
 * <A HREF="doc-files/speedup.txt">speedup.txt</A>
 * <FONT SIZE="-1">
 * <PRE>
 * $ java Speedup times.txt
 * N	K	T	Spdup	Effic	EDSF	Devi
 *
 * 1M	seq	5718				3%
 * 1M	1	5798	0.986	0.986		2%
 * 1M	2	2971	1.925	0.962	0.025	2%
 * 1M	3	2056	2.781	0.927	0.032	5%
 * 1M	4	1579	3.621	0.905	0.030	2%
 * 1M	5	1302	4.392	0.878	0.031	1%
 * 1M	6	1091	5.241	0.874	0.026	5%
 * 1M	7	973	5.877	0.840	0.029	14%
 * 1M	8	867	6.595	0.824	0.028	18%
 *
 * 2M	seq	11169				4%
 * 2M	1	11334	0.985	0.985		1%
 * 2M	2	5753	1.941	0.971	0.015	2%
 * 2M	3	3921	2.849	0.950	0.019	4%
 * 2M	4	3006	3.716	0.929	0.020	2%
 * 2M	5	2393	4.667	0.933	0.014	4%
 * 2M	6	2059	5.424	0.904	0.018	33%
 * 2M	7	1776	6.289	0.898	0.016	3%
 * 2M	8	1611	6.933	0.867	0.020	10%
 *
 * 4M	seq	22138				2%
 * 4M	1	22216	0.996	0.996		2%
 * 4M	2	11220	1.973	0.987	0.010	2%
 * 4M	3	7604	2.911	0.970	0.013	4%
 * 4M	4	5780	3.830	0.958	0.014	18%
 * 4M	5	4704	4.706	0.941	0.015	1%
 * 4M	6	3930	5.633	0.939	0.012	1%
 * 4M	7	3405	6.502	0.929	0.012	8%
 * 4M	8	3023	7.323	0.915	0.013	3%
 *
 * 8M	seq	43995				3%
 * 8M	1	44170	0.996	0.996		1%
 * 8M	2	22430	1.961	0.981	0.016	2%
 * 8M	3	15190	2.896	0.965	0.016	5%
 * 8M	4	11325	3.885	0.971	0.009	3%
 * 8M	5	9281	4.740	0.948	0.013	2%
 * 8M	6	7730	5.691	0.949	0.010	3%
 * 8M	7	6675	6.591	0.942	0.010	1%
 * 8M	8	5961	7.380	0.923	0.011	11%
 *
 * 16M	seq	88101				2%
 * 16M	1	87694	1.005	1.005		1%
 * 16M	2	44196	1.993	0.997	0.008	2%
 * 16M	3	29859	2.951	0.984	0.011	3%
 * 16M	4	22551	3.907	0.977	0.010	13%
 * 16M	5	18063	4.877	0.975	0.007	8%
 * 16M	6	15179	5.804	0.967	0.008	22%
 * 16M	7	13180	6.684	0.955	0.009	5%
 * 16M	8	11716	7.520	0.940	0.010	11%
 *
 * 32M	seq	175108				4%
 * 32M	1	175452	0.998	0.998		2%
 * 32M	2	88185	1.986	0.993	0.005	2%
 * 32M	3	60031	2.917	0.972	0.013	3%
 * 32M	4	44941	3.896	0.974	0.008	18%
 * 32M	5	36443	4.805	0.961	0.010	1%
 * 32M	6	30270	5.785	0.964	0.007	3%
 * 32M	7	26076	6.715	0.959	0.007	3%
 * 32M	8	23287	7.520	0.940	0.009	3%
 *
 * 64M	seq	351583				3%
 * 64M	1	348706	1.008	1.008		1%
 * 64M	2	176925	1.987	0.994	0.015	11%
 * 64M	3	119701	2.937	0.979	0.015	3%
 * 64M	4	89095	3.946	0.987	0.007	11%
 * 64M	5	71888	4.891	0.978	0.008	3%
 * 64M	6	61077	5.756	0.959	0.010	1%
 * 64M	7	51485	6.829	0.976	0.006	3%
 * 64M	8	46279	7.597	0.950	0.009	11%
 *
 * 128M	seq	705776				1%
 * 128M	1	696826	1.013	1.013		2%
 * 128M	2	351242	2.009	1.005	0.008	2%
 * 128M	3	237912	2.967	0.989	0.012	2%
 * 128M	4	178166	3.961	0.990	0.008	3%
 * 128M	5	144231	4.893	0.979	0.009	3%
 * 128M	6	120829	5.841	0.974	0.008	1%
 * 128M	7	103471	6.821	0.974	0.007	10%
 * 128M	8	92259	7.650	0.956	0.008	12%
 * </PRE>
 * </FONT>
 * <P>
 * Here are the plots the Speedup program generated for the above input file:
 * <BR><IMG SRC="doc-files/plot_a.png"> <IMG SRC="doc-files/plot_d.png">
 * <BR><IMG SRC="doc-files/plot_b.png"> <IMG SRC="doc-files/plot_c.png">
 * <P>
 * The Speedup program's error handling is rudimentary. The first error in the
 * input file terminates the program. The error may cause an exception stack
 * trace to be printed.
 *
 * @author  Alan Kaminsky
 * @version 04-Aug-2008
 */
public class Speedup
	{

// Prevent construction.

	private Speedup()
		{
		}

// Hidden helper classes.

	/**
	 * Class Data is a record of data associated with a certain size parameter
	 * n.
	 *
	 * @author  Alan Kaminsky
	 * @version 12-Jun-2007
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
	private static Map<Integer,Data> dataMap = new TreeMap<Integer,Data>();

	// Maximum K value.
	private static double K_max = Double.NEGATIVE_INFINITY;

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
			.rightMargin (72)
			.minorGridLines (true)
			.xAxisKind (Plot.LOGARITHMIC)
			.xAxisMinorDivisions (10)
			.xAxisTitle ("Processors, <I>K</I>")
			.yAxisKind (Plot.LOGARITHMIC)
			.yAxisMinorDivisions (10)
			.yAxisTickFormat (FMT_0E)
			.yAxisTickScale (1000)
			.yAxisTitle ("<I>T</I> (<I>N,K</I>) (sec)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		Speedup_plot
			.plotTitle ("Speedup vs. Processors")
			.rightMargin (72)
			.xAxisStart (0)
			.xAxisTitle ("Processors, <I>K</I>")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_0)
			.yAxisTitle ("<I>Speedup</I> (<I>N,K</I>)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		Eff_plot
			.plotTitle ("Efficiency vs. Processors")
			.rightMargin (72)
			.xAxisStart (0)
			.xAxisTitle ("Processors, <I>K</I>")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_1)
			.yAxisTitle ("<I>Eff</I> (<I>N,K</I>)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		EDSF_plot
			.plotTitle ("EDSF vs. Processors")
			.rightMargin (72)
			.xAxisStart (0)
			.xAxisTitle ("Processors, <I>K</I>")
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

		// Validate data.
		for (Data data: dataMap.values())
			{
			validateData (data);
			}

		// Add ideal performance lines to plots.
		Speedup_plot
			.seriesDots (null)
			.seriesColor (new Color (0.7f, 0.7f, 0.7f))
			.xySeries (new double[] {0, K_max}, new double[] {0, K_max})
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.black);
		Eff_plot
			.seriesDots (null)
			.seriesColor (new Color (0.7f, 0.7f, 0.7f))
			.xySeries (new double[] {0, K_max}, new double[] {1, 1})
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.black);

		// Process data.
		System.out.println ("N\tK\tT\tSpdup\tEffic\tEDSF\tDevi");
		for (Data data : dataMap.values())
			{
			// Print metrics.
			if (data.K_series.isEmpty()) continue;
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
			if (data.T_seq != 0.0)
				{
				System.out.println
					(labelText + "\tseq\t" +
					 FMT_0.format (data.T_seq) + "\t\t\t\t" +
					 FMT_0.format (100 * data.Dev_seq) + "%");
				}
			int len = data.K_series.length();
			System.out.println
				(labelText + "\t" +
				 FMT_0.format (data.K_series.x(0)) + "\t" +
				 FMT_0.format (data.T_series.x(0)) + "\t" +
				 FMT_3.format (data.Speedup_series.x(0)) + "\t" +
				 FMT_3.format (data.Eff_series.x(0)) + "\t\t" +
				 FMT_0.format (100 * data.Dev_series.x(0)) + "%");
			for (int i = 1; i < len; ++ i)
				{
				System.out.println
					(labelText + "\t" +
					 FMT_0.format (data.K_series.x(i)) + "\t" +
					 FMT_0.format (data.T_series.x(i)) + "\t" +
					 FMT_3.format (data.Speedup_series.x(i)) + "\t" +
					 FMT_3.format (data.Eff_series.x(i)) + "\t" +
					 FMT_3.format (data.EDSF_series_2.x(i-1)) + "\t" +
					 FMT_0.format (100 * data.Dev_series.x(i)) + "%");
				}

			// Add data series to plots.
			T_plot
				.xySeries
					(new AggregateXYSeries
						(data.K_series, data.T_series))
				.label
					(data.labelText,
					 data.K_series.x (len-1),
					 data.T_series.x (len-1));
			Speedup_plot
				.xySeries
					(new AggregateXYSeries
						(data.K_series, data.Speedup_series))
				.label
					(data.labelText,
					 data.K_series.x (len-1),
					 data.Speedup_series.x (len-1));
			Eff_plot
				.xySeries
					(new AggregateXYSeries
						(data.K_series, data.Eff_series))
				.label
					(data.labelText,
					 data.K_series.x (len-1),
					 data.Eff_series.x (len-1));
			EDSF_plot
				.xySeries
					(new AggregateXYSeries
						(data.K_series_2, data.EDSF_series_2))
				.label
					(data.labelText,
					 data.K_series_2.x (len-2),
					 data.EDSF_series_2.x (len-2));
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
			if (! scanner.hasNextInt()) error ("K invalid", linenum);
			int K = scanner.nextInt();
			if (K < 0) error ("K < 0", linenum);
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
