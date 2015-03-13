//******************************************************************************
//
// File:    Sizeup.java
// Package: ---
// Unit:    Class Sizeup
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

import benchmarks.determinism.pj.edu.ritnumeric.Interpolation;
import benchmarks.determinism.pj.edu.ritnumeric.ListXYSeries;

import benchmarks.determinism.pj.edu.ritnumeric.plot.Dots;
import benchmarks.determinism.pj.edu.ritnumeric.plot.Plot;

import java.awt.Color;

import java.io.File;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import java.util.regex.Pattern;

/**
 * Class Sizeup is a main program that analyzes running time measurements and
 * reports sizeup metrics for a parallel program.
 * <P>
 * Usage: java Sizeup <I>inputfile</I>
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
 * milliseconds.
 * <P>
 * After the running time data come the problem size specifications. Each line
 * begins with the literal character <TT>n</TT> followed by an <I>n</I> value
 * and an <I>N</I> value. The <I>n</I> value, an integer, is one of the size
 * parameter values appearing in the running time data section. The <I>N</I>
 * value, a floating point number, is the actual problem size corresponding to
 * that size parameter. If no problem size specification line appears for a
 * certain size parameter in the running time data, then the problem size is
 * taken to be the same as the size parameter.
 * <P>
 * After the problem size specifications (if any) come the running time
 * specifications. Each line begins with the literal character <TT>T</TT>
 * followed by a <I>T</I> value in milliseconds. These <I>T</I> values are used
 * to construct the plots of problem size versus <I>K</I>, sizeup versus
 * <I>K</I>, and sizeup efficiency versus <I>K</I>.
 * <P>
 * After the running time specifications come the plot specifications. Each of
 * the following plot specifications is optional. The plot specification
 * keywords are the same as the methods of class {@linkplain
 * benchmarks.determinism.pj.edu.ritnumeric.plot.Plot Plot}; see that class for further information.
 * <P>
 * The following plot specifications control the <I>N</I> vs. <I>T</I> plot:
 * <BR><TT>nvst frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>nvst plotTitle "<I>theTitle</I>"</TT> (default: <TT>"Problem Size vs.
 * Running Time"</TT>)
 * <BR><TT>nvst margins <I>theMargin</I></TT>
 * <BR><TT>nvst leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>nvst topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>nvst rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>nvst bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>nvst xAxisStart <I>theStart</I></TT> (default: automatic*)
 * <BR><TT>nvst xAxisEnd <I>theEnd</I></TT> (default: automatic*)
 * <BR><TT>nvst xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>nvst xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Running Time, T (sec)"</TT>)
 * <BR><TT>nvst yAxisStart <I>theStart</I></TT> (default: automatic*)
 * <BR><TT>nvst yAxisEnd <I>theEnd</I></TT> (default: automatic*)
 * <BR><TT>nvst yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0E0"</TT>)
 * <BR><TT>nvst yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>nvst yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Problem Size, N"</TT>)
 * <BR><TT>nvst yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <BR>*For the <I>N</I> vs. <I>T</I> plot, which is a log-log plot, the
 * specified X and Y axis starting and ending values are the base-10 logarithms
 * of the actual values.
 * <P>
 * The following plot specifications control the problem size plot:
 * <BR><TT>size frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>size plotTitle "<I>theTitle</I>"</TT> (default: <TT>"Problem Size vs.
 * Processors"</TT>)
 * <BR><TT>size margins <I>theMargin</I></TT>
 * <BR><TT>size leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>size topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>size rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>size bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>size xAxisStart <I>theStart</I></TT> (default: automatic*)
 * <BR><TT>size xAxisEnd <I>theEnd</I></TT> (default: automatic*)
 * <BR><TT>size xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>size xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processors, K"</TT>)
 * <BR><TT>size yAxisStart <I>theStart</I></TT> (default: automatic*)
 * <BR><TT>size yAxisEnd <I>theEnd</I></TT> (default: automatic*)
 * <BR><TT>size yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0E0"</TT>)
 * <BR><TT>size yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>size yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"N(T,K)"</TT>)
 * <BR><TT>size yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <BR>*For the problem size plot, which is a log-log plot, the specified X and
 * Y axis starting and ending values are the base-10 logarithms of the actual
 * values.
 * <P>
 * The following plot specifications control the sizeup plot:
 * <BR><TT>sizeup frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>sizeup plotTitle "<I>theTitle</I>"</TT> (default: <TT>"Sizeup vs.
 * Processors"</TT>)
 * <BR><TT>sizeup margins <I>theMargin</I></TT>
 * <BR><TT>sizeup leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>sizeup topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>sizeup rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>sizeup bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>sizeup xAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>sizeup xAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>sizeup xAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>sizeup xAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>sizeup xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>sizeup xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processors, K"</TT>)
 * <BR><TT>sizeup yAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>sizeup yAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>sizeup yAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>sizeup yAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>sizeup yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0"</TT>)
 * <BR><TT>sizeup yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>sizeup yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Sizeup(T,K)"</TT>)
 * <BR><TT>sizeup yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <P>
 * The following plot specifications control the sizeup efficiency plot:
 * <BR><TT>sizeupeff frameTitle "<I>theTitle</I>"</TT> (default: no title)
 * <BR><TT>sizeupeff plotTitle "<I>theTitle</I>"</TT> (default: <TT>"Sizeup
 * Efficiency vs. Processors"</TT>)
 * <BR><TT>sizeupeff margins <I>theMargin</I></TT>
 * <BR><TT>sizeupeff leftMargin <I>theMargin</I></TT> (default: 42 points)
 * <BR><TT>sizeupeff topMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>sizeupeff rightMargin <I>theMargin</I></TT> (default: 18 points)
 * <BR><TT>sizeupeff bottomMargin <I>theMargin</I></TT> (default: 36 points)
 * <BR><TT>sizeupeff xAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>sizeupeff xAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>sizeupeff xAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>sizeupeff xAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>sizeupeff xAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>sizeupeff xAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"Processors, K"</TT>)
 * <BR><TT>sizeupeff yAxisStart <I>theStart</I></TT> (default: 0)
 * <BR><TT>sizeupeff yAxisEnd <I>theEnd</I></TT> (default: automatic)
 * <BR><TT>sizeupeff yAxisMajorDivisions <I>theMajorDivisions</I></TT> (default: 10)
 * <BR><TT>sizeupeff yAxisMinorDivisions <I>theMinorDivisions</I></TT> (default: 1)
 * <BR><TT>sizeupeff yAxisTickFormat "<I>theFormat</I>"</TT> (default: <TT>"0.0"</TT>)
 * <BR><TT>sizeupeff yAxisLength <I>theLength</I></TT> (default: 288 points)
 * <BR><TT>sizeupeff yAxisTitle "<I>theTitle</I>"</TT> (default: <TT>"SizeupEff(T,K)"</TT>)
 * <BR><TT>sizeupeff yAxisTitleOffset <I>theTitleOffset</I></TT> (default: 30 points)
 * <P>
 * For each line in the running time data section, the program takes the
 * smallest <I>T</I> value as the running time for the given <I>n</I> and
 * <I>K</I> values. For each value of <I>K</I>, the program calculates the
 * following data series: problem size versus running time. For each value of
 * <I>T</I> specified in the running time specifications section, the program
 * calculates the following data series: problem size versus <I>K</I> for
 * <I>K</I> &gt;= 1; sizeup versus <I>K</I> for <I>K</I> &gt;= 1 (sizeup
 * relative to the sequential version if input data for <I>K</I> = 0 is present,
 * otherwise sizeup relative to the parallel version for <I>K</I> = 1); and
 * sizeup efficiency versus <I>K</I> for <I>K</I> &gt;= 1 (sizeup efficiency =
 * sizeup/<I>K</I>).
 * <P>
 * The program prints the <I>N</I> vs. <I>T</I>, problem size, sizeup, and
 * sizeup efficiency series on the standard output.
 * <P>
 * The program displays plots of the <I>N</I> vs. <I>T</I>, problem size,
 * sizeup, and sizeup efficiency series, each in its own window. Each plot
 * window has menu options for saving the plot to a PNG image file, saving the
 * plot to a PostScript file, and zooming the display.
 * <P>
 * Here is an example of an input file:
 * <A HREF="doc-files/sizes.txt">sizes.txt</A>
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
 * n 20 1048576
 * n 21 2097152
 * n 22 4194304
 * n 23 8388608
 * n 24 16777216
 * n 25 33554432
 * n 26 67108864
 * n 27 134217728
 * T 6000
 * T 10000
 * T 15000
 * T 30000
 * T 60000
 * T 100000
 * nvst rightMargin 60
 * size rightMargin 60
 * sizeup rightMargin 60
 * sizeup xAxisEnd 8
 * sizeup xAxisMajorDivisions 8
 * sizeup yAxisEnd 8
 * sizeup yAxisMajorDivisions 8
 * sizeupeff rightMargin 60
 * sizeupeff xAxisEnd 8
 * sizeupeff xAxisMajorDivisions 8
 * sizeupeff yAxisStart 0.95
 * sizeupeff yAxisEnd 1.01
 * sizeupeff yAxisMajorDivisions 6
 * sizeupeff yAxisTickFormat "0.00"
 * </PRE>
 * </FONT>
 * <P>
 * Here is the output the Sizeup program printed for the above input file:
 * <A HREF="doc-files/sizeup.txt">sizeup.txt</A>
 * <FONT SIZE="-1">
 * <PRE>
 * $ java Sizeup sizes.txt
 * K	T	N
 *
 * seq	5718	1048576
 * seq	11169	2097152
 * seq	22138	4194304
 * seq	43995	8388608
 * seq	88101	16777216
 * seq	175108	33554432
 * seq	351583	67108864
 * seq	705776	134217728
 *
 * 1	5798	1048576
 * 1	11334	2097152
 * 1	22216	4194304
 * 1	44170	8388608
 * 1	87694	16777216
 * 1	175452	33554432
 * 1	348706	67108864
 * 1	696826	134217728
 *
 * 2	2971	1048576
 * 2	5753	2097152
 * 2	11220	4194304
 * 2	22430	8388608
 * 2	44196	16777216
 * 2	88185	33554432
 * 2	176925	67108864
 * 2	351242	134217728
 *
 * 3	2056	1048576
 * 3	3921	2097152
 * 3	7604	4194304
 * 3	15190	8388608
 * 3	29859	16777216
 * 3	60031	33554432
 * 3	119701	67108864
 * 3	237912	134217728
 *
 * 4	1579	1048576
 * 4	3006	2097152
 * 4	5780	4194304
 * 4	11325	8388608
 * 4	22551	16777216
 * 4	44941	33554432
 * 4	89095	67108864
 * 4	178166	134217728
 *
 * 5	1302	1048576
 * 5	2393	2097152
 * 5	4704	4194304
 * 5	9281	8388608
 * 5	18063	16777216
 * 5	36443	33554432
 * 5	71888	67108864
 * 5	144231	134217728
 *
 * 6	1091	1048576
 * 6	2059	2097152
 * 6	3930	4194304
 * 6	7730	8388608
 * 6	15179	16777216
 * 6	30270	33554432
 * 6	61077	67108864
 * 6	120829	134217728
 *
 * 7	973	1048576
 * 7	1776	2097152
 * 7	3405	4194304
 * 7	6675	8388608
 * 7	13180	16777216
 * 7	26076	33554432
 * 7	51485	67108864
 * 7	103471	134217728
 *
 * 8	867	1048576
 * 8	1611	2097152
 * 8	3023	4194304
 * 8	5961	8388608
 * 8	11716	16777216
 * 8	23287	33554432
 * 8	46279	67108864
 * 8	92259	134217728
 *
 * T	K	N	Sizeup	SzEff
 *
 * 6000	0	1102823
 * 6000	1	1086837	0.986	0.986
 * 6000	2	2191902	1.988	0.994
 * 6000	3	3280964	2.975	0.992
 * 6000	4	4360715	3.954	0.989
 * 6000	5	5381942	4.880	0.976
 * 6000	6	6479096	5.875	0.979
 * 6000	7	7522811	6.821	0.974
 * 6000	8	8445455	7.658	0.957
 *
 * 10000	0	1872279
 * 10000	1	1844479	0.985	0.985
 * 10000	2	3726310	1.990	0.995
 * 10000	3	5519054	2.948	0.983
 * 10000	4	7386362	3.945	0.986
 * 10000	5	9075400	4.847	0.969
 * 10000	6	10944943	5.846	0.974
 * 10000	7	12676405	6.771	0.967
 * 10000	8	14275939	7.625	0.953
 *
 * 15000	0	2829597
 * 15000	1	2803654	0.991	0.991
 * 15000	2	5608619	1.982	0.991
 * 15000	3	8283557	2.927	0.976
 * 15000	4	11134745	3.935	0.984
 * 15000	5	13851424	4.895	0.979
 * 15000	6	16575637	5.858	0.976
 * 15000	7	19144968	6.766	0.967
 * 15000	8	21538808	7.612	0.951
 *
 * 30000	0	5703002
 * 30000	1	5681434	0.996	0.996
 * 30000	2	11306083	1.982	0.991
 * 30000	3	16855619	2.956	0.985
 * 30000	4	22358881	3.921	0.980
 * 30000	5	27673278	4.852	0.970
 * 30000	6	33254263	5.831	0.972
 * 30000	7	38736359	6.792	0.970
 * 30000	8	43351357	7.601	0.950
 *
 * 60000	0	11432631
 * 60000	1	11439607	1.001	1.001
 * 60000	2	22804794	1.995	0.997
 * 60000	3	33537194	2.933	0.978
 * 60000	4	44998383	3.936	0.984
 * 60000	5	55854947	4.886	0.977
 * 60000	6	65935815	5.767	0.961
 * 60000	7	78100900	6.831	0.976
 * 60000	8	87134978	7.622	0.953
 *
 * 100000	0	19071653
 * 100000	1	19129827	1.003	1.003
 * 100000	2	38021928	1.994	0.997
 * 100000	3	56030334	2.938	0.979
 * 100000	4	75325030	3.950	0.987
 * 100000	5	93186914	4.886	0.977
 * 100000	6	110824193	5.811	0.968
 * 100000	7	129737005	6.803	0.972
 * 100000	8	145515895	7.630	0.954
 * </PRE>
 * </FONT>
 * <P>
 * Here are the plots the Sizeup program generated for the above input file:
 * <BR><IMG SRC="doc-files/plot_i.png"> <IMG SRC="doc-files/plot_j.png">
 * <BR><IMG SRC="doc-files/plot_k.png"> <IMG SRC="doc-files/plot_l.png">
 * <P>
 * The Sizeup program's error handling is rudimentary. The first error in the
 * input file terminates the program. The error may cause an exception stack
 * trace to be printed.
 *
 * @author  Alan Kaminsky
 * @version 20-Sep-2008
 */
public class Sizeup
	{

// Prevent construction.

	private Sizeup()
		{
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

	// Mapping from number of processors K (type Integer) to problem size vs.
	// running time data (type ListXYSeries). The X values are the running
	// times. The Y values are the problem sizes.
	private static Map<Integer,ListXYSeries> nvstMap =
		new TreeMap<Integer,ListXYSeries>();

	// Mapping from number of processors K (type Integer) to problem size vs.
	// running time interpolation object (type Interpolation). The X values are
	// the running times. The Y values are the problem sizes.
	private static Map<Integer,Interpolation> interpolationMap =
		new TreeMap<Integer,Interpolation>();

	// Mapping from problem size specification n (type Integer) to problem size
	// N (type Double).
	private static Map<Integer,Double> ntoNMap =
		new TreeMap<Integer,Double>();

	// List of T values (msec) (type Double).
	private static List<Double> tList = new ArrayList<Double>();

	// Maximum K value.
	private static int K_max = Integer.MIN_VALUE;

	// Plot objects.
	private static Plot NvsT_plot = new Plot();
	private static Plot N_plot = new Plot();
	private static Plot Sizeup_plot = new Plot();
	private static Plot SizeupEff_plot = new Plot();

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
		NvsT_plot
			.plotTitle ("Problem Size vs. Running Time")
			.rightMargin (84)
			.minorGridLines (true)
			.xAxisKind (Plot.LOGARITHMIC)
			.xAxisMinorDivisions (10)
			.xAxisTickScale (1000)
			.xAxisTickFormat (FMT_0E)
			.xAxisTitle ("Running time, <I>T</I> (sec)")
			.yAxisKind (Plot.LOGARITHMIC)
			.yAxisMinorDivisions (10)
			.yAxisTickFormat (FMT_0E)
			.yAxisTitle ("Problem size, <I>N</I>")
			.labelPosition (Plot.ABOVE+Plot.ROTATE_LEFT)
			.labelOffset (6);
		N_plot
			.plotTitle ("Problem Size vs. Processors")
			.rightMargin (84)
			.minorGridLines (true)
			.xAxisKind (Plot.LOGARITHMIC)
			.xAxisMinorDivisions (10)
			.xAxisTitle ("Processors, <I>K</I>")
			.yAxisKind (Plot.LOGARITHMIC)
			.yAxisMinorDivisions (10)
			.yAxisTickFormat (FMT_0E)
			.yAxisTitle ("<I>N</I> (<I>T,K</I>)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		Sizeup_plot
			.plotTitle ("Sizeup vs. Processors")
			.rightMargin (84)
			.xAxisStart (0)
			.xAxisTitle ("Processors, <I>K</I>")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_0)
			.yAxisTitle ("<I>Sizeup</I> (<I>T,K</I>)")
			.labelPosition (Plot.RIGHT)
			.labelOffset (6);
		SizeupEff_plot
			.plotTitle ("Sizeup Efficiency vs. Processors")
			.rightMargin (84)
			.xAxisStart (0)
			.xAxisTitle ("Processors, <I>K</I>")
			.yAxisStart (0)
			.yAxisTickFormat (FMT_1)
			.yAxisTitle ("<I>SizeupEff</I> (<I>T,K</I>)")
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

		// Add ideal performance lines to plots.
		Sizeup_plot
			.seriesDots (null)
			.seriesColor (new Color (0.7f, 0.7f, 0.7f))
			.xySeries (new double[] {0, K_max}, new double[] {0, K_max})
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.black);
		SizeupEff_plot
			.seriesDots (null)
			.seriesColor (new Color (0.7f, 0.7f, 0.7f))
			.xySeries (new double[] {0, K_max}, new double[] {1, 1})
			.seriesDots (Dots.circle (5))
			.seriesColor (Color.black);

		// Process data for each K value.
		System.out.println ("K\tT\tN");
		for (Map.Entry<Integer,ListXYSeries> entry : nvstMap.entrySet())
			{
			System.out.println();
			Integer K = entry.getKey();
			String Kstring = K == 0 ? "seq" : K.toString();
			ListXYSeries nvst = entry.getValue();

			// Convert n vs. T to N vs. T.
			ListXYSeries Nvst = new ListXYSeries();
			for (int i = 0; i < nvst.length(); ++ i)
				{
				double T = nvst.x(i);
				int n = (int) nvst.y(i);
				Double N = ntoNMap.get (n);
				if (N == null)
					{
					Nvst.add (T, n);
					System.out.println
						(Kstring + "\t" +
						 FMT_0.format (T) + "\t" +
						 n);
					}
				else
					{
					Nvst.add (T, N);
					System.out.println
						(Kstring + "\t" +
						 FMT_0.format (T) + "\t" +
						 FMT_0.format (N));
					}
				}

			// Add N vs. T series to plot.
			if (K >= 1)
				{
				NvsT_plot
					.xySeries (Nvst)
					.label
						("<I>K</I> = " + K,
						 Nvst.x (Nvst.length()-1),
						 Nvst.y (Nvst.length()-1));
				}

			// Set up interpolation objects.
			interpolationMap.put (K, new Interpolation (Nvst));
			}

		// Process data for each T value.
		System.out.println();
		System.out.println ("T\tK\tN\tSizeup\tSzEff");
		for (double T : tList)
			{
			System.out.println();

			// Calculate and print metrics.
			double N = 0.0;
			double N_seq = 0.0;
			double sizeup = 0.0;
			double sizeupeff = 0.0;
			ListXYSeries nvskSeries = new ListXYSeries();
			ListXYSeries sizeupvskSeries = new ListXYSeries();
			ListXYSeries sizeupeffvskSeries = new ListXYSeries();
			for (int K = 0; K <= K_max; ++ K)
				{
				Interpolation interp = interpolationMap.get (K);
				if (interp == null)
					{
					}
				else if (K == 0)
					{
					N = interp.f (T);
					N_seq = N;
					System.out.println
						(FMT_0.format (T) + "\t" +
						 K + "\t" +
						 FMT_0.format (N) + "\t\t");
					}
				else
					{
					N = interp.f (T);
					if (N_seq == 0.0 && K == 1) N_seq = N;
					sizeup = N / N_seq;
					sizeupeff = sizeup / K;
					nvskSeries.add (K, N);
					sizeupvskSeries.add (K, sizeup);
					sizeupeffvskSeries.add (K, sizeupeff);
					System.out.println
						(FMT_0.format (T) + "\t" +
						 K + "\t" +
						 FMT_0.format (N) + "\t" +
						 FMT_3.format (sizeup) + "\t" +
						 FMT_3.format (sizeupeff));
					}
				}

			// Add data series to plots.
			N_plot
				.xySeries (nvskSeries)
				.label
					("<I>T</I> = " + FMT_0.format (T/1000.0) + " sec",
					 nvskSeries.x (nvskSeries.length()-1),
					 nvskSeries.y (nvskSeries.length()-1));
			Sizeup_plot
				.xySeries (sizeupvskSeries)
				.label
					("<I>T</I> = " + FMT_0.format (T/1000.0) + " sec",
					 sizeupvskSeries.x (sizeupvskSeries.length()-1),
					 sizeupvskSeries.y (sizeupvskSeries.length()-1));
			SizeupEff_plot
				.xySeries (sizeupeffvskSeries)
				.label
					("<I>T</I> = " + FMT_0.format (T/1000.0) + " sec",
					 sizeupeffvskSeries.x (sizeupeffvskSeries.length()-1),
					 sizeupeffvskSeries.y (sizeupeffvskSeries.length()-1));
			}

		// Display plots.
		NvsT_plot.getFrame().setVisible (true);
		N_plot.getFrame().setVisible (true);
		Sizeup_plot.getFrame().setVisible (true);
		SizeupEff_plot.getFrame().setVisible (true);
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
			while (scanner.hasNextLong())
				{
				double T = (double) scanner.nextLong();
				if (T <= 0.0) error ("T invalid", linenum);
				T_min = Math.min (T_min, T);
				}
			if (T_min == Double.POSITIVE_INFINITY)
				{
				error ("T values missing", linenum);
				}

			// Record data.
			ListXYSeries nvst = nvstMap.get (K);
			if (nvst == null)
				{
				nvst = new ListXYSeries();
				nvstMap.put (K, nvst);
				}
			nvst.add (T_min, n);
			}
		else
			{
			// A problem size specification line, running time specification
			// line, or plot specification line.
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

				// Record contents.
				ntoNMap.put (n, N);
				}
			else if (keyword.equals ("T"))
				{
				// A running time specification line. Parse contents.
				if (! scanner.hasNextLong())
					error ("Missing T value", linenum);
				long T = scanner.nextLong();

				// Record contents.
				tList.add ((double) T);
				}
			else if (keyword.equals ("nvst"))
				{
				parsePlotSpecification (NvsT_plot, scanner, linenum);
				}
			else if (keyword.equals ("size"))
				{
				parsePlotSpecification (N_plot, scanner, linenum);
				}
			else if (keyword.equals ("sizeup"))
				{
				parsePlotSpecification (Sizeup_plot, scanner, linenum);
				}
			else if (keyword.equals ("sizeupeff"))
				{
				parsePlotSpecification (SizeupEff_plot, scanner, linenum);
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
		System.err.println ("Usage: java Sizeup <inputfile>");
		System.exit (1);
		}

	}
