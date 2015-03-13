/**************************************************************************
*                                                                         *
*         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
*                                                                         *
*                            produced by                                  *
*                                                                         *
*                  Java Grande Benchmarking Project                       *
*                                                                         *
*                                at                                       *
*                                                                         *
*                Edinburgh Parallel Computing Centre                      *
*                                                                         *
*                email: epcc-javagrande@epcc.ed.ac.uk                     *
*                                                                         *
*                 Original version of this code by                        *
*            Florian Doyon (Florian.Doyon@sophia.inria.fr)                *
*              and  Wilfried Klauser (wklauser@acm.org)                   *
*                                                                         *
*      This version copyright (c) The University of Edinburgh, 2001.      *
*                         All rights reserved.                            *
*                                                                         *
**************************************************************************/

package benchmarks.raytracer;

public class Interval implements java.io.Serializable
{
/*
public int number;
  public int width;
  public int height;
  public int yfrom;
  public int yto;
  public int total;
*/  
    public final int number;
    public final int width;
    public final int height;
    public final int yfrom;
    public final int yto;
    public final int total;
    public final int threadid;

    public Interval(int number, int width, int height, int yfrom, int yto, int total, int threadid)
    {
        this.number = number;
        this.width = width;
        this.height = height;
        this.yfrom = yfrom;
        this.yto = yto;
        this.total = total;
        this.threadid = threadid;
    }
}
